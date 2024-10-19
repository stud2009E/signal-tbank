package pab.ta.handler.tbank.provider.util;

import pab.ta.handler.base.asset.CandleInterval;


import java.time.Duration;
import java.time.ZonedDateTime;


/**
 * Convert {@link CandleInterval} to different representation
 */
public class Utils {

    public static ru.tinkoff.piapi.contract.v1.CandleInterval toTBankInterval(CandleInterval interval){
        return switch (interval) {
            case WEEK -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_WEEK;
            case DAY -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_DAY;
            case HOUR_4 -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_4_HOUR;
            case HOUR_2 -> ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_2_HOUR;
        };
    }

    public static Duration duration(CandleInterval interval) {
        return switch (interval) {
            case WEEK -> Duration.ofDays(7);
            case DAY -> Duration.ofDays(1);
            case HOUR_4 -> Duration.ofHours(4);
            case HOUR_2 -> Duration.ofHours(2);
        };
    }

    public static ZonedDateTime endTime(ZonedDateTime zdt, CandleInterval interval) {
        return switch (interval) {
            case WEEK -> zdt.plusWeeks(1);
            case DAY -> zdt.plusDays(1);
            case HOUR_4 -> zdt.plusHours(4);
            case HOUR_2 -> zdt.plusHours(2);
        };
    }
}
