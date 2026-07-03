package com.gahih.domain.community.dto;

import com.gahih.domain.community.entity.CountryCommunity;
import lombok.Getter;

@Getter
public class CountryCommunityResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final String continentName;
    private final boolean enabled;
    private final Integer displayOrder;

    private CountryCommunityResponse(
            Long id,
            String code,
            String name,
            String continentName,
            boolean enabled,
            Integer displayOrder
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.continentName = continentName;
        this.enabled = enabled;
        this.displayOrder = displayOrder;
    }

    public static CountryCommunityResponse from(CountryCommunity community) {
        return new CountryCommunityResponse(
                community.getId(),
                community.getCode(),
                community.getName(),
                community.getContinent().getDisplayName(),
                community.isEnabled(),
                community.getDisplayOrder()
        );
    }
}