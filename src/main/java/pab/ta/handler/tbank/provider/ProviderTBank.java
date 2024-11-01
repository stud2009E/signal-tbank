package pab.ta.handler.tbank.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import pab.ta.handler.base.asset.AssetInfo;
import pab.ta.handler.base.asset.CandleInterval;
import pab.ta.handler.base.asset.SeriesIdentity;
import pab.ta.handler.base.asset.TimeFrame;
import pab.ta.handler.base.provider.DataProvider;
import pab.ta.handler.tbank.provider.util.Utils;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InvestApi;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProviderTBank implements DataProvider {

    private final InvestApi investApi;

    @Override
    public BarSeries getSeries(SeriesIdentity identity) {
        AssetInfo info = identity.info();
        TimeFrame tf = identity.tf();

        BarSeries series = new BaseBarSeries();

        try {
            series = switch (info.type()) {
                case SHARE -> getShareSeries(info, tf.from(), tf.to(), tf.interval());
                case INDEX -> getIndexSeries(info, tf.from(), tf.to(), tf.interval());
                case FUTURE -> getFutureSeries(info, tf.from(), tf.to(), tf.interval());
                case CURRENCY -> getCurrencySeries(info, tf.from(), tf.to(), tf.interval());
            };
        } catch (RuntimeException ex) {
            System.err.println(ex.getMessage());
            try {
                Thread.sleep(60_000);
                series = getSeries(identity);
            } catch (InterruptedException e) {
                System.err.println(ex.getMessage());
            }
        }

        return series;
    }

    protected BarSeries getShareSeries(AssetInfo data, LocalDateTime from, LocalDateTime to, CandleInterval candleInterval) {
        return getBarSeries(data, from, to, candleInterval);
    }

    protected BarSeries getFutureSeries(AssetInfo data, LocalDateTime from, LocalDateTime to, CandleInterval candleInterval) {
        return getBarSeries(data, from, to, candleInterval);
    }

    protected BarSeries getCurrencySeries(AssetInfo data, LocalDateTime from, LocalDateTime to, CandleInterval candleInterval) {
        return getBarSeries(data, from, to, candleInterval);
    }

    protected BarSeries getIndexSeries(AssetInfo data, LocalDateTime from, LocalDateTime to, CandleInterval candleInterval) {
        return getBarSeries(data, from, to, candleInterval);
    }


    /**
     * quotation to num
     */
    Num quotationToNum(Quotation quotation) {
        BigDecimal bigDecimal = quotation.getUnits() == 0 && quotation.getNano() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(quotation.getUnits()).add(BigDecimal.valueOf(quotation.getNano(), 9));

        return DecimalNum.valueOf(bigDecimal.doubleValue());
    }


    private BarSeries getBarSeries(AssetInfo data, LocalDateTime from, LocalDateTime to, CandleInterval candleInterval) {
        Instant instantFrom = from.atZone(ZoneId.systemDefault()).toInstant();
        Instant instantTo = to.atZone(ZoneId.systemDefault()).toInstant();


        List<HistoricCandle> candles = investApi.getMarketDataService()
                .getCandlesSync(data.id(), instantFrom, instantTo, Utils.toTBankInterval(candleInterval));

        BarSeries series = new BaseBarSeriesBuilder()
                .withName(data.ticker() + " " + candleInterval.name())
                .withNumTypeOf(DecimalNum.class)
                .build();

        candles.stream().map(candle -> {

            ZonedDateTime zdt = Instant.ofEpochSecond(candle.getTime().getSeconds(), candle.getTime().getNanos())
                    .atZone(ZoneId.systemDefault());

            return BaseBar.builder(DecimalNum::valueOf, Number.class)
                    .timePeriod(Utils.duration(candleInterval))
                    .endTime(Utils.endTime(zdt, candleInterval))
                    .openPrice(quotationToNum(candle.getOpen()))
                    .closePrice(quotationToNum(candle.getClose()))
                    .lowPrice(quotationToNum(candle.getLow()))
                    .highPrice(quotationToNum(candle.getHigh()))
                    .volume(DecimalNum.valueOf(candle.getVolume()))
                    .build();
        }).forEach(series::addBar);

        return series;
    }
}