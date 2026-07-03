package com.gahih.domain.community.repository;

import com.gahih.domain.community.entity.CountryCommunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CountryCommunityRepository extends JpaRepository<CountryCommunity, Long> {

    Optional<CountryCommunity> findByCode(String code);

    boolean existsByCode(String code);

    List<CountryCommunity> findByEnabledTrueOrderByDisplayOrderAsc();
}