package pab.ta.handler.tbank.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pab.ta.handler.base.asset.AssetInfo;
import pab.ta.handler.base.asset.AssetType;
import pab.ta.handler.base.asset.BaseAssetInfo;
import pab.ta.handler.base.provider.AssetInfoProvider;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Cacheable("currency")
@Component
@RequiredArgsConstructor
public class CurrencyTProvider implements AssetInfoProvider {

    private final InvestApi investApi;

    @Override
    public List<AssetInfo> info() {
        var future = investApi.getInstrumentsService().getCurrencies(InstrumentStatus.INSTRUMENT_STATUS_BASE);

        try {
            return future.get()
                    .stream()
                    .map(asset -> new BaseAssetInfo(asset.getUid(), asset.getTicker(), AssetType.CURRENCY, asset.getName()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
