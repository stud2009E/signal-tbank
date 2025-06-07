package pab.ta.handler.tbank.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.asset.AssetType;
import pab.ta.handler.base.lib.asset.BaseAssetInfo;
import pab.ta.handler.base.lib.asset.provider.AssetInfoSearchProvider;
import pab.ta.handler.tbank.exception.BrokerApiException;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SearchTProvider implements AssetInfoSearchProvider {

    private final InvestApi investApi;

    @Override
    public List<AssetInfo> search(String query) {
        var future = investApi.getInstrumentsService().findInstrument(query);

        try {
            return future.get()
                    .stream()
                    .filter(instrumentShort -> switch (instrumentShort.getInstrumentKind()) {
                        case InstrumentType.INSTRUMENT_TYPE_FUTURES,
                            InstrumentType.INSTRUMENT_TYPE_CURRENCY,
                            InstrumentType.INSTRUMENT_TYPE_SHARE -> true;
                        default -> false;
                    })
                    .map(instrumentShort -> {
                        AssetType type = switch (instrumentShort.getInstrumentKind()) {
                            case InstrumentType.INSTRUMENT_TYPE_FUTURES -> AssetType.FUTURE;
                            case InstrumentType.INSTRUMENT_TYPE_CURRENCY -> AssetType.CURRENCY;
                            case InstrumentType.INSTRUMENT_TYPE_SHARE -> AssetType.SHARE;
                            default -> throw new IllegalArgumentException("No handler for type " +
                                    instrumentShort.getInstrumentKind());
                        };

                        return new BaseAssetInfo(
                                instrumentShort.getUid(),
                                instrumentShort.getTicker(),
                                type,
                                instrumentShort.getName());
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            throw new BrokerApiException(ex);
        }
    }
}
