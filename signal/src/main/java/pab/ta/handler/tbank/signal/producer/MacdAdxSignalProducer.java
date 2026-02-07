package pab.ta.handler.tbank.signal.producer;

import lombok.RequiredArgsConstructor;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import pab.ta.handler.base.lib.asset.AssetData;
import pab.ta.handler.base.lib.asset.CandleInterval;
import pab.ta.handler.base.lib.signal.Signal;
import pab.ta.handler.base.lib.signal.SignalProcessor;
import pab.ta.handler.base.lib.task.AssetDataProcessor;

import java.util.ArrayList;
import java.util.List;

import static pab.ta.handler.base.lib.asset.Direction.BUY;
import static pab.ta.handler.base.lib.asset.Direction.SELL;

@RequiredArgsConstructor
public class MacdAdxSignalProducer implements AssetDataProcessor {

    private final SignalProcessor signalProcessor;

    @Override
    public void process(List<AssetData> assetDataList) {

        List<Signal> signalList = new ArrayList<>();

        assetDataList.forEach(assetData -> {
            var series = assetData.getBarSeries();
            var index = series.getEndIndex();

            var closePrice = new ClosePriceIndicator(series);
            var adxMa6 = new SMAIndicator(new ADXIndicator(series, 14), 6);
            var hist = new MACDIndicator(closePrice).getHistogram(9);

            signals(assetData.getTicker(), assetData.getInterval(), hist, adxMa6)
                    .stream()
                    .filter(signal -> signal.getRule().isSatisfied(index))
                    .forEach(signalList::add);
        });

        if (!signalList.isEmpty()) {
            signalProcessor.process(assetDataList.getFirst().getInfo(), signalList);
        }
    }


    protected List<Signal> signals(String ticker, CandleInterval interval,
                                   NumericIndicator macdHist, SMAIndicator adx6) {

        var macdHistCrossUp0 = new CrossedUpIndicatorRule(macdHist, 0);
        var macdHistDownUp0 = new CrossedDownIndicatorRule(macdHist, 0);
        var adx6Over30Under40 = new OverIndicatorRule(adx6, 30).and(new UnderIndicatorRule(adx6, 40));
        var adx6Over40 = new OverIndicatorRule(adx6, 40);

        return List.of(
                Signal.builder()
                        .name("MACD hist <> 0 | 30 < ADX6 < 40")
                        .ticker(ticker)
                        .interval(interval)
                        .direction(BUY)
                        .rule(macdHistCrossUp0.and(adx6Over30Under40))
                        .build(),

                Signal.builder()
                        .name("MACD hist <> 0 | ADX6 > 40")
                        .ticker(ticker)
                        .interval(interval)
                        .direction(BUY)
                        .rule(macdHistCrossUp0.and(adx6Over40))
                        .build(),

                Signal.builder()
                        .name("MACD hist >< 0 | 30 < ADX6 < 40")
                        .ticker(ticker)
                        .interval(interval)
                        .direction(SELL)
                        .rule(macdHistDownUp0.and(adx6Over30Under40))
                        .build(),

                Signal.builder()
                        .name("MACD hist >< 0 | ADX6 > 40")
                        .ticker(ticker)
                        .interval(interval)
                        .direction(SELL)
                        .rule(macdHistDownUp0.and(adx6Over40))
                        .build()
        );
    }
}
