package pab.ta.handler.tbank.provider;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.asset.AssetType;
import pab.ta.handler.base.lib.provider.AssetInfoProvider;
import ru.tinkoff.piapi.contract.v1.InstrumentsRequest;
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc.InstrumentsServiceBlockingStub;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.ttech.piapi.core.connector.SyncStubWrapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static pab.ta.handler.base.lib.asset.AssetType.SHARE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetInfoTProvider implements AssetInfoProvider {

    private final SyncStubWrapper<InstrumentsServiceBlockingStub> instrumentService;

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
        var currencies = instrumentService.callSyncMethod(stub ->
                stub.currencies(InstrumentsRequest.getDefaultInstance()));

        log.info("Gets currencies");

        return currencies.getInstrumentsList().stream()
                .map(asset -> new AssetInfo(
                        asset.getUid(),
                        asset.getTicker(), AssetType.CURRENCY, asset.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets future info.
     *
     * @return Assets data.
     */
    private List<AssetInfo> futureInfo() {
        var futures = instrumentService.callSyncMethod(stub ->
                stub.futures(InstrumentsRequest.getDefaultInstance()));
        return futures.getInstrumentsList().stream()
                .filter(future -> {
                    Timestamp ltd = future.getLastTradeDate();
                    LocalDate lastTradeDate = Instant.ofEpochSecond(ltd.getSeconds(), ltd.getNanos())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    return LocalDate.now().plusMonths(3).isAfter(lastTradeDate);
                }).map(asset -> new AssetInfo(asset.getUid(), asset.getTicker(), AssetType.FUTURE, asset.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets share info.
     *
     * @return Assets data.
     */
    private List<AssetInfo> shareInfo() {
        var shares = instrumentService.callSyncMethod(stub ->
                stub.shares(InstrumentsRequest.getDefaultInstance()));

        return shares.getInstrumentsList().stream()
                .filter(asset -> asset.getRealExchange().equals(RealExchange.REAL_EXCHANGE_MOEX)
                        && asset.getClassCode().equals("TQBR"))
                .map(asset ->
                        new AssetInfo(asset.getUid(), asset.getTicker(), SHARE, asset.getName()))
                .collect(Collectors.toList());
    }
}
