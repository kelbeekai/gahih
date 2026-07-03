package com.gahih.domain.category.entity;

import com.gahih.domain.category.enumtype.CategoryCode;
import com.gahih.domain.community.entity.CountryCommunity;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_community_code",
                        columnNames = {"country_community_id", "code"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_community_id", nullable = false)
    private CountryCommunity countryCommunity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CategoryCode code;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private boolean visibleInHeader;

    @Column(nullable = false)
    private boolean adminWriteOnly;

    @Column(nullable = false)
    private boolean commentAllowed;

    @Column(nullable = false)
    private boolean reactionAllowed;

    @Column(nullable = false)
    private boolean secretPostAllowed;

    private Category(
            CountryCommunity countryCommunity,
            CategoryCode code,
            String name,
            String description,
            Integer displayOrder,
            boolean visibleInHeader,
            boolean adminWriteOnly,
            boolean commentAllowed,
            boolean reactionAllowed,
            boolean secretPostAllowed
    ) {
        validateCountryCommunity(countryCommunity);
        validateCode(code);
        validateName(name);
        validateDescription(description);
        validateDisplayOrder(displayOrder);

        this.countryCommunity = countryCommunity;
        this.code = code;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.visibleInHeader = visibleInHeader;
        this.adminWriteOnly = adminWriteOnly;
        this.commentAllowed = commentAllowed;
        this.reactionAllowed = reactionAllowed;
        this.secretPostAllowed = secretPostAllowed;
    }

    public static Category create(
            CountryCommunity countryCommunity,
            CategoryCode code,
            String name,
            String description,
            Integer displayOrder,
            boolean visibleInHeader,
            boolean adminWriteOnly,
            boolean commentAllowed,
            boolean reactionAllowed,
            boolean secretPostAllowed
    ) {
        return new Category(
                countryCommunity,
                code,
                name,
                description,
                displayOrder,
                visibleInHeader,
                adminWriteOnly,
                commentAllowed,
                reactionAllowed,
                secretPostAllowed
        );
    }

    public static Category create(
            CountryCommunity countryCommunity,
            CategoryCode code
    ) {
        return new Category(
                countryCommunity,
                code,
                code.getDisplayName(),
                code.getDefaultDescription(),
                code.getDisplayOrder(),
                code.isVisibleInHeader(),
                code.isAdminWriteOnly(),
                code.isCommentAllowed(),
                code.isReactionAllowed(),
                code.isSecretPostAllowed()
        );
    }

    public void changeName(String name) {
        validateName(name);
        this.name = name;
    }

    public void changeDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    public void changeDisplayOrder(Integer displayOrder) {
        validateDisplayOrder(displayOrder);
        this.displayOrder = displayOrder;
    }

    public void changeVisibleInHeader(boolean visibleInHeader) {
        this.visibleInHeader = visibleInHeader;
    }

    public void changeAdminWriteOnly(boolean adminWriteOnly) {
        this.adminWriteOnly = adminWriteOnly;
    }

    public void changeCommentAllowed(boolean commentAllowed) {
        this.commentAllowed = commentAllowed;
    }

    public void changeReactionAllowed(boolean reactionAllowed) {
        this.reactionAllowed = reactionAllowed;
    }

    public void changeSecretPostAllowed(boolean secretPostAllowed) {
        this.secretPostAllowed = secretPostAllowed;
    }

    public boolean isCode(CategoryCode code) {
        return this.code == code;
    }

    private void validateCountryCommunity(CountryCommunity countryCommunity) {
        if (countryCommunity == null) {
            throw new DomainValidationException("국가 커뮤니티는 비어 있을 수 없습니다.");
        }
    }

    private void validateCode(CategoryCode code) {
        if (code == null) {
            throw new DomainValidationException("카테고리 코드는 비어 있을 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("카테고리 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new DomainValidationException("카테고리 이름은 100자를 초과할 수 없습니다.");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new DomainValidationException("카테고리 설명은 255자를 초과할 수 없습니다.");
        }
    }

    private void validateDisplayOrder(Integer displayOrder) {
        if (displayOrder == null) {
            throw new DomainValidationException("카테고리 표시 순서는 비어 있을 수 없습니다.");
        }
        if (displayOrder < 1) {
            throw new DomainValidationException("카테고리 표시 순서는 1 이상이어야 합니다.");
        }
    }
}