package pab.ta.handler.tbank.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.asset.CandleInterval;
import pab.ta.handler.base.lib.asset.TimeFrame;
import pab.ta.handler.base.lib.asset.provider.DataProvider;
import pab.ta.handler.tbank.provider.util.Utils;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProviderTBank implements DataProvider {

    private final InvestApi investApi;

    @Override
    public BarSeries getSeries(AssetInfo assetInfo, TimeFrame timeFrame) {

        BarSeries series = new BaseBarSeries();

        try {
            series = getBarSeries(assetInfo, timeFrame.getFrom(), timeFrame.getTo(), timeFrame.getInterval());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            try {
                Thread.sleep(60_000);
                series = getBarSeries(assetInfo, timeFrame.getFrom(), timeFrame.getTo(), timeFrame.getInterval());
            } catch (Exception ex1) {
                log.error(ex1.getMessage());
            }
        }

        return series;
    }


    private BarSeries getBarSeries(AssetInfo data, ZonedDateTime from, ZonedDateTime to, CandleInterval candleInterval) {
        Instant instantFrom = from.toInstant();
        Instant instantTo = to.toInstant();

        List<HistoricCandle> candles = investApi.getMarketDataService()
                .getCandlesSync(data.getId(), instantFrom, instantTo, Utils.toTBankInterval(candleInterval));

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(data.getTicker() + " " + candleInterval.name())
                .withNumTypeOf(DecimalNum.class)
                .build();

        candles.stream().map(candle -> {

            ZonedDateTime zdt = Instant.ofEpochSecond(candle.getTime().getSeconds(), candle.getTime().getNanos())
                    .atZone(ZoneId.systemDefault());

            return BaseBar.builder(DecimalNum::valueOf, Number.class)
                    .timePeriod(Utils.duration(candleInterval))
                    .endTime(Utils.endTime(zdt, candleInterval))
                    .openPrice(Utils.quotationToNum(candle.getOpen()))
                    .closePrice(Utils.quotationToNum(candle.getClose()))
                    .lowPrice(Utils.quotationToNum(candle.getLow()))
                    .highPrice(Utils.quotationToNum(candle.getHigh()))
                    .volume(DecimalNum.valueOf(candle.getVolume()))
                    .build();
        }).forEach(series::addBar);

        return series;
    }
}