package pab.ta.handler.tbank.rest.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.asset.CandleInterval;
import pab.ta.handler.base.lib.signal.SignalProducer;
import pab.ta.handler.tbank.provider.SearchTProvider;
import pab.ta.handler.tbank.rest.search.dto.AssetInfoDto;
import pab.ta.handler.tbank.rest.search.dto.IndicatorDto;
import pab.ta.handler.tbank.rest.search.dto.TimeframeDto;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static pab.ta.handler.base.lib.asset.AssetType.*;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
public class SearchController {

    private final SearchTProvider provider;
    private final List<SignalProducer> producers;


    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AssetInfoDto>> search(@RequestParam(name = "query") String query) {

        if (query.isBlank() || query.length() < 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<AssetInfo> assetInfoList = provider.search(query);

        List<AssetInfoDto> assets = assetInfoList.stream()
                .filter(info -> info.getType() == FUTURE
                        || info.getType() == SHARE
                        || info.getType() == CURRENCY)
                .map(info -> new AssetInfoDto(info.getTicker(), info.getType(), info.getDescription()))
                .sorted(Comparator.comparingInt(asset -> asset.getType().ordinal()))
                .toList();

        return ResponseEntity.of(Optional.of(assets));
    }

    @GetMapping(path = "/timeframe", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TimeframeDto>> timeframe() {
        return ResponseEntity.ok(Arrays.stream(CandleInterval.values())
                .map(candleInterval -> new TimeframeDto(candleInterval.name()))
                .toList());
    }

    @GetMapping(path = "/indicator", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<IndicatorDto>> indicator() {
        return ResponseEntity.ok(producers.stream()
                .map(producer -> new IndicatorDto(producer.getIndicatorId(), producer.getDirection().name()))
                .toList());
    }
}
