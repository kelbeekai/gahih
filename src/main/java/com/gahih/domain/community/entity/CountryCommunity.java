package com.gahih.domain.community.entity;

import com.gahih.domain.community.enumtype.Continent;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "country_community",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_country_community_code", columnNames = "code")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CountryCommunity {

    private static final int CODE_MAX_LENGTH = 10;
    private static final int NAME_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = CODE_MAX_LENGTH)
    private String code;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Continent continent;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Integer displayOrder;

    private CountryCommunity(
            String code,
            String name,
            Continent continent,
            boolean enabled,
            Integer displayOrder
    ) {
        validateCode(code);
        validateName(name);
        validateContinent(continent);
        validateDisplayOrder(displayOrder);

        this.code = code.toUpperCase();
        this.name = name;
        this.continent = continent;
        this.enabled = enabled;
        this.displayOrder = displayOrder;
    }

    public static CountryCommunity create(
            String code,
            String name,
            Continent continent,
            boolean enabled,
            Integer displayOrder
    ) {
        return new CountryCommunity(code, name, continent, enabled, displayOrder);
    }

    public void changeName(String name) {
        validateName(name);
        this.name = name;
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void changeDisplayOrder(Integer displayOrder) {
        validateDisplayOrder(displayOrder);
        this.displayOrder = displayOrder;
    }

    public boolean isCode(String code) {
        return this.code.equalsIgnoreCase(code);
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainValidationException("국가 커뮤니티 코드는 비어 있을 수 없습니다.");
        }
        if (code.length() > CODE_MAX_LENGTH) {
            throw new DomainValidationException("국가 커뮤니티 코드는 10자를 초과할 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("국가 커뮤니티 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new DomainValidationException("국가 커뮤니티 이름은 100자를 초과할 수 없습니다.");
        }
    }

    private void validateContinent(Continent continent) {
        if (continent == null) {
            throw new DomainValidationException("대륙은 비어 있을 수 없습니다.");
        }
    }

    private void validateDisplayOrder(Integer displayOrder) {
        if (displayOrder == null) {
            throw new DomainValidationException("국가 커뮤니티 표시 순서는 비어 있을 수 없습니다.");
        }
        if (displayOrder < 1) {
            throw new DomainValidationException("국가 커뮤니티 표시 순서는 1 이상이어야 합니다.");
        }
    }
}