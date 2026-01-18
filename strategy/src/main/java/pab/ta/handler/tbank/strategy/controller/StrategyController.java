package pab.ta.handler.tbank.strategy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import pab.ta.handler.base.lib.asset.CandleInterval;
import pab.ta.handler.base.lib.asset.TimeFrame;
import pab.ta.handler.base.lib.provider.AssetInfoProvider;
import pab.ta.handler.base.lib.provider.SeriesProvider;
import pab.ta.handler.tbank.strategy.controller.dto.TradingDto;

import java.time.ZonedDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/strategy")
public class StrategyController {

    private final AssetInfoProvider assetInfoProvider;
    private final SeriesProvider seriesProvider;

    @GetMapping("/test")
    public TradingDto test() {

        var sber = assetInfoProvider.info()
                .stream()
                .filter(info -> info.getTicker().toLowerCase().contains("sber"))
                .findFirst()
                .orElseThrow();

        var series = seriesProvider.getSeries(sber, new TimeFrame(CandleInterval.DAY, ZonedDateTime.now().minusMonths(6), ZonedDateTime.now()));

        ClosePriceIndicator close = new ClosePriceIndicator(series);
        EMAIndicator fastEma = new EMAIndicator(close, 12);  // 12-period EMA
        EMAIndicator slowEma = new EMAIndicator(close, 26);  // 26-period EMA

        Rule entry = new CrossedUpIndicatorRule(fastEma, slowEma);

        Rule exit = new StopGainRule(close, 3.0)      // take profit at +3%
                .or(new StopLossRule(close, 1.5));    // or cut losses at -1.5%

        Strategy strategy = new BaseStrategy("EMA Crossover", entry, exit);

        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord record = manager.run(strategy);

        System.out.println("Number of trades: " + record.getTrades().size());
        System.out.println("Number of positions: " + record.getPositionCount());

        return new TradingDto();
    }

}
