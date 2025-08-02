package pab.ta.handler.tbank.provider;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.asset.AssetType;
import pab.ta.handler.base.lib.provider.AssetInfoProvider;
import pab.ta.handler.tbank.exception.BrokerApiException;
import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static pab.ta.handler.base.lib.asset.AssetType.SHARE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetInfoTProvider implements AssetInfoProvider {

    private final InvestApi investApi;

    @Cacheable(value = "assets")
    @Override
    public List<AssetInfo> info() {
        var assetInfo = shareInfo();

//        assetInfo.addAll(futureInfo());
//        assetInfo.addAll(currencyInfo());

        return assetInfo;
    }

    /**
     * Gets currency info.
     *
     * @return Assets data.
     */
    private List<AssetInfo> currencyInfo() {
        var future = investApi.getInstrumentsService().getCurrencies(InstrumentStatus.INSTRUMENT_STATUS_BASE);

        try {
            log.info("Gets assets");

            return future.get()
                    .stream()
                    .map(asset ->
                            new AssetInfo(asset.getUid(), asset.getTicker(), AssetType.CURRENCY, asset.getName()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            throw new BrokerApiException(ex.getMessage());
        }
    }

    /**
     * Gets future info.
     *
     * @return Assets data.
     */
    private List<AssetInfo> futureInfo() {
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
                    .map(asset ->
                            new AssetInfo(asset.getUid(), asset.getTicker(), AssetType.FUTURE, asset.getName()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            throw new BrokerApiException(ex.getMessage());
        }
    }

    /**
     * Gets share info.
     *
     * @return Assets data.
     */
    private List<AssetInfo> shareInfo() {
        var future = investApi.getInstrumentsService().getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE);

        try {
            return future
                    .get()
                    .stream()
                    .filter(asset ->
                            asset.getRealExchange().equals(RealExchange.REAL_EXCHANGE_MOEX)
                                    && asset.getClassCode().equals("TQBR"))
                    .map(asset ->
                            new AssetInfo(asset.getUid(), asset.getTicker(), SHARE, asset.getName()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException ex) {
            throw new BrokerApiException(ex.getMessage());
        }
    }
}
