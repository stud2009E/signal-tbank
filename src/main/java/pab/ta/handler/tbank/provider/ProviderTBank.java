package pab.ta.handler.tbank.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.asset.CandleInterval;
import pab.ta.handler.base.lib.asset.TimeFrame;
import pab.ta.handler.base.lib.provider.DataProvider;
import pab.ta.handler.tbank.provider.util.Utils;
import ru.ttech.piapi.strategy.candle.backtest.BarData;
import ru.ttech.piapi.strategy.candle.backtest.BarsLoader;
import ru.ttech.piapi.strategy.candle.backtest.TimeHelper;

import java.time.ZonedDateTime;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProviderTBank implements DataProvider {

    private final BarsLoader barsLoader;

    @Override
    public BarSeries getSeries(AssetInfo assetInfo, TimeFrame timeFrame) {

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(assetInfo.getTicker())
                .build();

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

        Iterable<BarData> bars = barsLoader
                .loadBars(data.getId(), Utils.toTBankInterval(candleInterval), from.toLocalDate(), to.toLocalDate());

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(data.getTicker() + " " + candleInterval.name())
                .build();

        StreamSupport.stream(bars.spliterator(), false)
                .map(bar -> {

                    var tInterval = Utils.toTBankInterval(candleInterval);
                    var startTime = TimeHelper.roundFloorStartTime(ZonedDateTime.parse(bar.getStartTime()), tInterval);
                    var endTime = TimeHelper.getEndTime(startTime, tInterval);

                    return series.barBuilder()
                            .timePeriod(Utils.duration(candleInterval))
                            .endTime(endTime.toInstant())
                            .openPrice(bar.getOpen())
                            .closePrice(bar.getClose())
                            .lowPrice(bar.getLow())
                            .highPrice(bar.getHigh())
                            .volume(bar.getVolume())
                            .build();
                }).forEach(series::addBar);

        return series;
    }
}