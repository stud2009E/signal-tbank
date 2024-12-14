package pab.ta.handler.tbank.rest.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pab.ta.handler.base.asset.AssetInfo;
import pab.ta.handler.base.asset.AssetType;
import pab.ta.handler.tbank.provider.SearchTProvider;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/${application.api.version}/data")
@RequiredArgsConstructor
public class SearchController {

    private final SearchTProvider provider;

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AssetInfoDto>> search(@RequestParam(name = "query") String query) {

        if (query.isBlank() || query.length() < 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<AssetInfo> assetInfoList = provider.search(query);

        List<AssetInfoDto> assets = assetInfoList.stream()
                .filter(info -> info.type() == AssetType.FUTURE
                        || info.type() == AssetType.SHARE
                        || info.type() == AssetType.CURRENCY)
                .map(info -> new AssetInfoDto(info.ticker(), info.type(), info.description()))
                .toList();

        return ResponseEntity.of(Optional.of(assets));
    }
}
