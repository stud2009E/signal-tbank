package pab.ta.handler.tbank.rest.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pab.ta.handler.base.asset.AssetType;

@Getter
@AllArgsConstructor
public class AssetInfoDto {

    private String ticker;

    private AssetType type;

    private String description;
}
