package pab.ta.handler.tbank.rest.signal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
public class SignalDto {
    String ticker;

    String interval;

    String indicatorId;

    String direction;

    String type;

    ZonedDateTime createdAt;
}
