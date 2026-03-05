package com.mayak.ietms.shared.statistics;

import com.mayak.ietms.features.request.infra.persistence.RequestRepository;

import java.time.Instant;

public enum MetricType {

    PLACED {
        @Override
        public int count(RequestRepository repo, Long userId,
                         Instant from, Instant to) {
            return repo.countByAuthorId(userId, from, to);
        }
    },

    JOINED {
        @Override
        public int count(RequestRepository repo, Long userId,
                         Instant from, Instant to) {
            return repo.countByCompetitorsContains(userId, from, to);
        }
    },

    BIDED {
        @Override
        public int count(RequestRepository repo, Long userId,
                         Instant from, Instant to) {
            return repo.countRequestsWithBidByUser(userId, from, to);
        }
    },

    DISPATCHED {
        @Override
        public int count(RequestRepository repo, Long userId,
                         Instant from, Instant to) {
            return repo.countByDispatchedUser(userId, from, to);
        }
    };

    public abstract int count(
            RequestRepository repo,
            Long userId,
            Instant from,
            Instant to
    );
}