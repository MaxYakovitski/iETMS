package com.mayak.ietms.features.statistics.application;

import com.mayak.ietms.features.user.application.UserProfileQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.statistics.UserStatsDto;
import com.mayak.ietms.user.dto.UserNameDto;
import com.mayak.ietms.features.statistics.infra.persistence.UserStatisticsRepository;
import com.mayak.ietms.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatisticsService {

    private final UserStatisticsRepository repo;
    private final UserProfileQueryService userProfileQueryService;
    private final UserRepository userRepository;

    public List<UserStatsDto> getUserStats(LocalDate start, LocalDate end, Long requesterUserId) {
        List<Long> userIds = userProfileQueryService
                .findColleagues(requesterUserId)
                .stream()
                .map(UserResponseDto::id)
                .toList();

        if (userIds.isEmpty()) {
            userIds = userRepository.findAll().stream().map(User::getId).toList();
        }

        return getUserStats(start, end, userIds);
    }

    public List<UserStatsDto> getUserStats(LocalDate start, LocalDate end, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to   = end.atTime(LocalTime.MAX);

        var rows = repo.userStats(
                from,
                to,
                userIds.toArray(Long[]::new)
        );

        return rows.stream()
                .map(r -> new UserStatsDto(
                        r.getUserId(),
                        new UserNameDto(r.getFirstName(), r.getLastName()),
                        nz(r.getCreated()),
                        nz(r.getJoined()),
                        nz(r.getBided()),
                        nz(r.getDispatched()),
                        nzMoney(r.getAvgResponseMinutes())
                ))
                .toList();
    }

    private int nz(Integer v) { return v == null ? 0 : v; }

    private BigDecimal nzMoney(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }
}