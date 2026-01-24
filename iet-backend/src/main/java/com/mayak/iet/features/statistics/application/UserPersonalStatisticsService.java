package com.mayak.iet.features.statistics.application;

import com.mayak.iet.shared.exception.business.UserNotFoundException;
import com.mayak.iet.features.request.infra.persistence.RequestRepository;
import com.mayak.iet.features.user.infra.persistence.UserRepository;
import com.mayak.iet.shared.statistics.MetricType;
import com.mayak.iet.shared.statistics.PeriodType;
import com.mayak.iet.statistics.UserPersonalStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        LocalDateTime from = period.getStart();
        LocalDateTime to   = period.getEnd();

        return new UserPersonalStats(
                MetricType.PLACED.count(requestRepository, userId, from, to),
                MetricType.JOINED.count(requestRepository, userId, from, to),
                MetricType.BIDED.count(requestRepository, userId, from, to),
                MetricType.DISPATCHED.count(requestRepository, userId, from, to)
        );
    }
}