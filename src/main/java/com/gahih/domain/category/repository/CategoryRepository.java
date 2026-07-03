package com.gahih.domain.category.repository;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.community.entity.CountryCommunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCountryCommunityAndCode(
            CountryCommunity countryCommunity,
            CategoryCode code
    );

    List<Category> findAllByCountryCommunityOrderByDisplayOrderAsc(
            CountryCommunity countryCommunity
    );

    Optional<Category> findByCountryCommunityAndCode(
            CountryCommunity countryCommunity,
            CategoryCode code
    );

    List<Category> findAllByCountryCommunity_CodeOrderByDisplayOrderAsc(String communityCode);

    Optional<Category> findByCountryCommunity_CodeAndCode(
            String communityCode,
            CategoryCode code
    );

    List<Category> findAllByOrderByCountryCommunity_DisplayOrderAscDisplayOrderAsc();
}