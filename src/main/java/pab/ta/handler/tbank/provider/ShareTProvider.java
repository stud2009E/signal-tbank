package pab.ta.handler.tbank.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.asset.BaseAssetInfo;
import pab.ta.handler.base.lib.asset.provider.AssetInfoProvider;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static pab.ta.handler.base.lib.asset.AssetType.SHARE;

@Component
@Cacheable("share")
@RequiredArgsConstructor
public class ShareTProvider implements AssetInfoProvider {

    private final InvestApi investApi;

    @Override
    public List<AssetInfo> info() {
        var future = investApi.getInstrumentsService().getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE);

        try {
            return future
                    .get()
                    .stream()
                    .filter(asset ->
                            asset.getRealExchange().equals(RealExchange.REAL_EXCHANGE_MOEX)
                                    && asset.getClassCode().equals("TQBR"))
                    .map(asset ->
                            new BaseAssetInfo(asset.getUid(), asset.getTicker(), SHARE, asset.getName()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
