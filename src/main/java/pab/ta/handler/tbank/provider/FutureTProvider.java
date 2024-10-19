package pab.ta.handler.tbank.provider;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pab.ta.handler.base.asset.AssetInfo;
import pab.ta.handler.base.asset.AssetType;
import pab.ta.handler.base.asset.BaseAssetInfo;
import pab.ta.handler.base.provider.AssetInfoProvider;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Cacheable("future")
@Component
@RequiredArgsConstructor
public class FutureTProvider implements AssetInfoProvider {

    private final InvestApi investApi;

    @Override
    public List<AssetInfo> info() {
        var future = investApi.getInstrumentsService().getFutures(InstrumentStatus.INSTRUMENT_STATUS_BASE);

        try {
            return future.get()
                    .stream()
                    .filter(future1 -> {
                        Timestamp ltd = future1.getLastTradeDate();
                        LocalDate lastTradeDate = Instant
                                .ofEpochSecond(ltd.getSeconds(), ltd.getNanos())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

                        return LocalDate.now().plusMonths(3).isAfter(lastTradeDate);
                    })
                    .map(asset -> new BaseAssetInfo(asset.getUid(), asset.getTicker(), AssetType.FUTURE, asset.getName()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}