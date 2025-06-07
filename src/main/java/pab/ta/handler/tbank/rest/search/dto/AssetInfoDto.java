package pab.ta.handler.tbank.rest.search.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pab.ta.handler.base.lib.asset.AssetType;

@Getter
@AllArgsConstructor
public class AssetInfoDto {

    @EqualsAndHashCode.Include
    private String ticker;

    private AssetType type;

    private String description;
}
