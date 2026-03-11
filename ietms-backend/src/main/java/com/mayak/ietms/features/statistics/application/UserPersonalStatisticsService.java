package com.mayak.ietms.features.statistics.application;

import com.mayak.ietms.shared.exception.business.UserNotFoundException;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.shared.statistics.MetricType;
import com.mayak.ietms.shared.statistics.PeriodType;
import com.mayak.ietms.statistics.UserPersonalStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPersonalStatisticsService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public UserPersonalStats getCurrentMonthStats(Long userId) {
        if (!userRepository.existsById(userId)) throw new UserNotFoundException(userId);
        return getStats(userId, PeriodType.CURRENT_MONTH);
    }

    private UserPersonalStats getStats(Long userId, PeriodType period) {
        Instant from = period.getStart();
        Instant to   = period.getEnd();

        return new UserPersonalStats(
                MetricType.PLACED.count(requestRepository, userId, from, to),
                MetricType.JOINED.count(requestRepository, userId, from, to),
                MetricType.BIDED.count(requestRepository, userId, from, to),
                MetricType.ACCEPTED_SPOT.count(requestRepository, userId, from, to),
                MetricType.ACCEPTED_CONTRACT.count(requestRepository, userId, from, to),
                MetricType.DISPATCHED.count(requestRepository, userId, from, to)
        );
    }
}