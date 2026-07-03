package com.gahih.domain.community.service;

import com.gahih.domain.community.dto.CountryCommunityResponse;
import com.gahih.domain.community.entity.CountryCommunity;
import com.gahih.domain.community.repository.CountryCommunityRepository;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryCommunityService {

    private final CountryCommunityRepository countryCommunityRepository;

    public List<CountryCommunityResponse> findEnabledCommunities() {
        return countryCommunityRepository.findByEnabledTrueOrderByDisplayOrderAsc()
                .stream()
                .map(CountryCommunityResponse::from)
                .toList();
    }

    public CountryCommunity findByCode(String code) {
        return countryCommunityRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 국가 커뮤니티입니다."));
    }

    public CountryCommunityResponse findResponseByCode(String code) {
        return CountryCommunityResponse.from(findByCode(code));
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.toUpperCase();
    }
}