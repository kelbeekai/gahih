package com.gahih.domain.post.entity;

import com.gahih.domain.post.enumtype.TradeStatus;
import com.gahih.domain.post.enumtype.TradeType;
import com.gahih.global.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "post_trade_info",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_trade_info_post",
                        columnNames = "post_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTradeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TradeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TradeStatus status;

    private PostTradeInfo(Post post, TradeType type) {
        validatePost(post);
        validateType(type);

        this.post = post;
        this.type = type;
        this.status = TradeStatus.OPEN;
    }

    public static PostTradeInfo create(Post post, TradeType type) {
        return new PostTradeInfo(post, type);
    }

    public void changeType(TradeType type) {
        validateType(type);
        this.type = type;
    }

    public void changeTypeForUpdate(TradeType type) {
        validateType(type);

        if (this.type == type) {
            return;
        }

        this.type = type;

        if (isClosed()) {
            reopen();
        }
    }

    public void close() {
        this.status = TradeStatus.CLOSED;
    }

    public void reopen() {
        this.status = TradeStatus.OPEN;
    }

    public void toggleStatus() {
        this.status = this.status == TradeStatus.OPEN
                ? TradeStatus.CLOSED
                : TradeStatus.OPEN;
    }

    public boolean isOpen() {
        return status == TradeStatus.OPEN;
    }

    public boolean isClosed() {
        return status == TradeStatus.CLOSED;
    }

    public String getStatusLabel() {
        return type.getStatusLabel(status);
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new DomainValidationException("거래 정보의 게시글은 비어 있을 수 없습니다.");
        }
    }

    private void validateType(TradeType type) {
        if (type == null) {
            throw new DomainValidationException("거래 유형은 비어 있을 수 없습니다.");
        }
    }
}