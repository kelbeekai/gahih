package com.gahih.domain.category.service;

import com.gahih.domain.category.entity.Category;
import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.category.repository.CategoryRepository;
import com.gahih.domain.community.entity.CountryCommunity;
import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.member.enumtype.MemberRole;
import com.gahih.domain.member.enumtype.MemberStatus;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CountryCommunityService countryCommunityService;

    public List<Category> findAllCategories(String communityCode) {
        return categoryRepository.findAllByCountryCommunity_CodeOrderByDisplayOrderAsc(
                normalizeCommunityCode(communityCode)
        );
    }

    public List<Category> findHeaderCategories(String communityCode) {
        return findAllCategories(communityCode).stream()
                .filter(Category::isVisibleInHeader)
                .toList();
    }

    public List<Category> findWritableCategories(String communityCode, LoginMember loginMember) {
        List<Category> categories = findAllCategories(communityCode);

        if (isAdmin(loginMember)) {
            return categories;
        }

        if (loginMember != null && loginMember.getStatus() == MemberStatus.SUSPENDED) {
            return categories.stream()
                    .filter(category -> category.isCode(CategoryCode.INQUIRY))
                    .toList();
        }

        return categories.stream()
                .filter(category -> !category.isAdminWriteOnly())
                .toList();
    }

    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다."));
    }

    public Category findByIdInCommunity(Long categoryId, String communityCode) {
        Category category = findById(categoryId);

        if (!category.getCountryCommunity().isCode(communityCode)) {
            throw new NotFoundException("현재 커뮤니티에 속하지 않는 카테고리입니다.");
        }

        return category;
    }

    public Category findByIdOrNull(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId).orElse(null);
    }

    public CountryCommunity findCommunity(String communityCode) {
        return countryCommunityService.findByCode(communityCode);
    }

    private boolean isAdmin(LoginMember loginMember) {
        return loginMember != null && loginMember.getRole() == MemberRole.ADMIN;
    }

    private String normalizeCommunityCode(String communityCode) {
        if (communityCode == null) {
            return null;
        }
        return communityCode.toUpperCase();
    }
}