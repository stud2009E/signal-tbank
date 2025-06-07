package pab.ta.handler.tbank.provider.util;


import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import pab.ta.handler.base.lib.asset.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

import static ru.tinkoff.piapi.contract.v1.CandleInterval.*;


/**
 * Convert {@link CandleInterval} to different representation
 */
public class Utils {

    public static Num quotationToNum(Quotation quotation) {
        BigDecimal bigDecimal = quotation.getUnits() == 0 && quotation.getNano() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(quotation.getUnits()).add(BigDecimal.valueOf(quotation.getNano(), 9));

        return DecimalNum.valueOf(bigDecimal.doubleValue());
    }

    public static ru.tinkoff.piapi.contract.v1.CandleInterval toTBankInterval(CandleInterval interval) {
        return switch (interval) {
            case HOUR_1 -> CANDLE_INTERVAL_HOUR;
            case HOUR_2 -> CANDLE_INTERVAL_2_HOUR;
            case HOUR_4 -> CANDLE_INTERVAL_4_HOUR;
            case DAY -> CANDLE_INTERVAL_DAY;
            case WEEK -> CANDLE_INTERVAL_WEEK;
            case MONTH -> null;
        };
    }

    public static Duration duration(CandleInterval interval) {
        return switch (interval) {
            case HOUR_1 -> Duration.ofHours(1);
            case HOUR_2 -> Duration.ofHours(2);
            case HOUR_4 -> Duration.ofHours(4);
            case DAY -> Duration.ofDays(1);
            case WEEK -> Duration.ofDays(7);
            case MONTH -> null;
        };
    }

    public static ZonedDateTime endTime(ZonedDateTime zdt, CandleInterval interval) {
        return switch (interval) {
            case HOUR_1 -> zdt.plusHours(1);
            case HOUR_2 -> zdt.plusHours(2);
            case HOUR_4 -> zdt.plusHours(4);
            case DAY -> zdt.plusDays(1);
            case WEEK -> zdt.plusWeeks(1);
            case MONTH -> null;
        };
    }
}
