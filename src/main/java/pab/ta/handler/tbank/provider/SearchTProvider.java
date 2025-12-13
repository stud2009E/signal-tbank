package pab.ta.handler.tbank.provider;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pab.ta.handler.base.lib.asset.AssetInfo;
import pab.ta.handler.base.lib.provider.AssetInfoSearchProvider;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchTProvider implements AssetInfoSearchProvider {

    private final AssetInfoTProvider infoTProvider;

    @Override
    public List<AssetInfo> search(@Nonnull String query) {

        return infoTProvider.info().stream().filter(info -> {

            var description = info.getDescription().strip().toLowerCase();
            var ticker = info.getTicker().strip().toLowerCase();

            var hasDescription = description.contains(query.toLowerCase());
            var hasTicker = ticker.contains(query.toLowerCase());

            return hasDescription || hasTicker;
        }).toList();
    }
}
