package pab.ta.handler.tbank.common.provider;

import pab.ta.handler.base.lib.asset.CandleInterval;

import java.time.Duration;

import static ru.tinkoff.piapi.contract.v1.CandleInterval.*;


/**
 * Convert {@link CandleInterval} to different representation
 */
public class Utils {


    public static ru.tinkoff.piapi.contract.v1.CandleInterval toTBankInterval(CandleInterval interval) {
        return switch (interval) {
            case H1 -> CANDLE_INTERVAL_HOUR;
            case H2 -> CANDLE_INTERVAL_2_HOUR;
            case H4 -> CANDLE_INTERVAL_4_HOUR;
            case DAY -> CANDLE_INTERVAL_DAY;
            case WEEK -> CANDLE_INTERVAL_WEEK;
        };
    }

    public static Duration duration(CandleInterval interval) {
        return switch (interval) {
            case H1 -> Duration.ofHours(1);
            case H2 -> Duration.ofHours(2);
            case H4 -> Duration.ofHours(4);
            case DAY -> Duration.ofDays(1);
            case WEEK -> Duration.ofDays(7);
        };
    }
}
