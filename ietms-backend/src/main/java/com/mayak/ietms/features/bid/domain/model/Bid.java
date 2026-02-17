package com.mayak.ietms.features.bid.domain.model;

import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString (exclude = {"user", "request"})
@NoArgsConstructor
@Table(name = "bids")
public class Bid {

    @Version
    private Long version = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant time;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(nullable = false)
    private boolean deleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bid bid)) return false;
        return id != null && id.equals(bid.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}