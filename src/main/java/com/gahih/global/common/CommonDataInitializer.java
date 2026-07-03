package com.gahih.global.common;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.community.entity.CountryCommunity;
import com.gahih.domain.community.enumtype.Continent;
import com.gahih.domain.community.repository.CountryCommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
@RequiredArgsConstructor
public class CommonDataInitializer implements CommandLineRunner {

    private final CountryCommunityRepository countryCommunityRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public void run(String... args) {
        initializeCountryCommunity();
        initializeCategory();
    }

    private void initializeCountryCommunity() {
        if (!countryCommunityRepository.existsByCode("DE")) {
            countryCommunityRepository.save(CountryCommunity.create(
                    "DE",
                    "독일",
                    Continent.EUROPE,
                    true,
                    1
            ));
        }

        if (!countryCommunityRepository.existsByCode("JP")) {
            countryCommunityRepository.save(CountryCommunity.create(
                    "JP",
                    "일본",
                    Continent.ASIA,
                    true,
                    2
            ));
        }
    }

    private void initializeCategory() {
        CountryCommunity germany = countryCommunityRepository.findByCode("DE")
                .orElseThrow(() -> new IllegalStateException("독일 커뮤니티를 찾을 수 없습니다."));

        CountryCommunity japan = countryCommunityRepository.findByCode("JP")
                .orElseThrow(() -> new IllegalStateException("일본 커뮤니티를 찾을 수 없습니다."));

        initializeCategoryForCommunity(germany);
        initializeCategoryForCommunity(japan);
    }

    private void initializeCategoryForCommunity(CountryCommunity community) {
        for (CategoryCode categoryCode : CategoryCode.values()) {
            if (categoryRepository.existsByCountryCommunityAndCode(community, categoryCode)) {
                continue;
            }

            categoryRepository.save(Category.create(community, categoryCode));
        }
    }
}