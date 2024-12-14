package pab.ta.handler.tbank.rest.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pab.ta.handler.base.asset.AssetType;

@Setter
@Getter
@AllArgsConstructor
public class AssetInfoDto {

    private String ticker;

    private AssetType type;

    private String description;
}
