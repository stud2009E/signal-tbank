package pab.ta.handler.tbank.provider;

import lombok.RequiredArgsConstructor;
import pab.ta.handler.base.asset.AssetInfo;
import pab.ta.handler.base.asset.BaseAssetInfo;
import pab.ta.handler.base.provider.AssetInfoSearchProvider;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SearchTProvider implements AssetInfoSearchProvider {

    private final InvestApi investApi;

    @Override
    public List<AssetInfo> search(String query) {
        var future = investApi.getInstrumentsService().findInstrument(query);

        try {
            return future.get()
                    .stream()
                    .map(instrumentShort -> new BaseAssetInfo(instrumentShort.getUid(), instrumentShort.getTicker(), null, instrumentShort.getName()))
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
