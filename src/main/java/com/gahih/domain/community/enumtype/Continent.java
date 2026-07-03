package com.gahih.domain.community.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Continent {

    EUROPE("유럽"),
    ASIA("아시아"),
    NORTH_AMERICA("북미"),
    SOUTH_AMERICA("남미"),
    OCEANIA("오세아니아"),
    AFRICA("아프리카");

    private final String displayName;
}