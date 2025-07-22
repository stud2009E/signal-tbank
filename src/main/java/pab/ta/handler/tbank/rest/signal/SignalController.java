package pab.ta.handler.tbank.rest.signal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pab.ta.handler.base.lib.task.SignalStore;
import pab.ta.handler.tbank.rest.signal.dto.SignalDto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
public class SignalController {

    private final SignalStore store;

    /**
     * Get signals no later than now - {secondsBefore} moment.
     *
     * @param secondsBefore seconds
     * @return signals
     */
    @GetMapping(path = "/signals", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SignalDto>> signals(@RequestParam(name = "secondBefore") Long secondsBefore) {
        ZonedDateTime moment = ZonedDateTime.now().minusSeconds(secondsBefore);

        List<SignalDto> signals = store.getAll().stream()
                .filter(signal -> signal.getCreatedAt().isAfter(moment))
                .map(signal ->
                        new SignalDto()
                                .setTicker(signal.getTicker())
                                .setType(signal.getType().name())
                                .setInterval(signal.getInterval().name())
                                .setName(signal.getName())
                                .setCreatedAt(signal.getCreatedAt())
                                .setDirection(signal.getDirection().name())
                )
                .toList();

        return ResponseEntity.of(Optional.of(signals));
    }

}
