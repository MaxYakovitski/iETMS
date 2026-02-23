package com.mayak.ietms.features.statistics.application;

import com.mayak.ietms.features.request.infra.mapping.RefuseReasonMapper;
import com.mayak.ietms.statistics.DepartmentStatsDto;
import com.mayak.ietms.statistics.MonthlyCountDto;
import com.mayak.ietms.statistics.RefuseReasonCountDto;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.request.domain.model.RefuseReason;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentStatisticsService {

    private final RequestRepository requestRepository;
    private final RefuseReasonMapper refuseReasonMapper;

    public DepartmentStatsDto getDepartmentStats(Long departmentId, Instant from, Instant toExclusive) {
        if (departmentId == null) {
            return DepartmentStatsDto.empty();
        }

        int spotTotal = countByRequestType(
                SpotRequest.class, departmentId, from, toExclusive);
        int contractTotal = countByRequestType(
                ContractRequest.class, departmentId, from, toExclusive);

        int spotAccepted = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.ACCEPTED, from, toExclusive);
        int contractAccepted = countByRequestTypeAndStatus(
                ContractRequest.class, departmentId, RequestStatus.ACCEPTED, from, toExclusive);

        int spotRefused = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.REFUSED, from, toExclusive);
        int contractRefused = countByRequestTypeAndStatus(
                ContractRequest.class, departmentId, RequestStatus.REFUSED, from, toExclusive);

        int spotNew = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.NEW, from, toExclusive);
        int spotInProgress = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.IN_PROGRESS, from, toExclusive);

        int spotNotBided = spotNew + spotInProgress;
        int spotBided = spotTotal - spotNotBided;

        var spotReasons = toReasonDtoList(countRefusedByReason(SpotRequest.class, departmentId, from, toExclusive));
        var contractReasons = toReasonDtoList(countRefusedByReason(ContractRequest.class, departmentId, from, toExclusive));

        return new DepartmentStatsDto(
                spotTotal,
                contractTotal,
                spotAccepted,
                contractAccepted,
                spotRefused,
                contractRefused,
                spotBided,
                spotNotBided,
                spotReasons,
                contractReasons,
                buildMonthlyCompression(departmentId, from, toExclusive)
        );
    }

    public int countByRequestType(
            Class<? extends Request> type,
            Long departmentId,
            Instant from,
            Instant toExclusive
    ) {
        return requestRepository.countByTypeAndDepartment(
                type, departmentId, from, toExclusive
        );
    }

    public int countByRequestTypeAndStatus(
            Class<? extends Request> type,
            Long departmentId,
            RequestStatus status,
            Instant from,
            Instant toExclusive
    ) {
        return requestRepository.countByTypeAndStatusAndDepartment(type, status, departmentId, from, toExclusive);
    }


    public Map<RefuseReason, Integer> countRefusedByReason(
            Class<? extends Request> type,
            Long departmentId,
            Instant from,
            Instant toExclusive
    ) {
        return requestRepository.countRefusedByReason(type, departmentId, RequestStatus.REFUSED, from, toExclusive)
                .stream()
                .collect(Collectors.toMap(
                        row -> mapReason(type, (String) row[0]),
                        row -> ((Long) row[1]).intValue()
                ));
    }

    private RefuseReason mapReason(Class<? extends Request> type, String code) {
        return refuseReasonMapper.fromCode(code);
    }

    private List<MonthlyCountDto> buildMonthlyCompression(
            Long departmentId,
            Instant from,
            Instant toExclusive
    ) {
        List<MonthlyCountDto> result = new ArrayList<>();
        ZoneOffset zone = ZoneOffset.UTC;

        ZonedDateTime current = from.atZone(zone).withDayOfMonth(1);
        ZonedDateTime end = toExclusive.atZone(zone).withDayOfMonth(1);

        while (current.isBefore(end)) {
            Instant monthStart = current.toInstant();
            Instant nextMonthStart = current.plusMonths(1).toInstant();

            int spot = countByRequestType(
                    SpotRequest.class, departmentId, monthStart, nextMonthStart);
            int contract = countByRequestType(
                    ContractRequest.class, departmentId, monthStart, nextMonthStart);

            String label = current.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    .toUpperCase()
                    + " '" + (current.getYear() % 100);

            result.add(new MonthlyCountDto(label, spot, contract));
            current = current.plusMonths(1);
        }

        return result;
    }

    private List<RefuseReasonCountDto> toReasonDtoList(Map<RefuseReason, Integer> map) {
        return map.entrySet().stream()
                .map(e -> new RefuseReasonCountDto(e.getKey().getCode(), e.getValue()))
                .toList();
    }

}