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

import java.time.LocalDateTime;
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

    public DepartmentStatsDto getDepartmentStats(
            Long departmentId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (departmentId == null) {
            // admin without selected department
            return DepartmentStatsDto.empty();
        }

        int spotTotal = countByRequestType(
                SpotRequest.class, departmentId, from, to);
        int contractTotal = countByRequestType(
                ContractRequest.class, departmentId, from, to);

        int spotAccepted = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.ACCEPTED, from, to);
        int contractAccepted = countByRequestTypeAndStatus(
                ContractRequest.class, departmentId, RequestStatus.ACCEPTED, from, to);

        int spotRefused = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.REFUSED, from, to);
        int contractRefused = countByRequestTypeAndStatus(
                ContractRequest.class, departmentId, RequestStatus.REFUSED, from, to);

        int spotNew = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.NEW, from, to);
        int spotInProgress = countByRequestTypeAndStatus(
                SpotRequest.class, departmentId, RequestStatus.IN_PROGRESS, from, to);

        int spotNotBided = spotNew + spotInProgress;
        int spotBided = spotTotal - spotNotBided;

        var spotReasons = toReasonDtoList(countRefusedByReason(SpotRequest.class, departmentId, from, to));
        var contractReasons = toReasonDtoList(countRefusedByReason(ContractRequest.class, departmentId, from, to));

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
                buildMonthlyCompression(departmentId, from, to)
        );
    }

    public int countByRequestType(
            Class<? extends Request> type,
            Long departmentId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return requestRepository.countByTypeAndDepartment(
                type, departmentId, from, to
        );
    }

    public int countByRequestTypeAndStatus(
            Class<? extends Request> type,
            Long departmentId,
            RequestStatus status,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return requestRepository.countByTypeAndStatusAndDepartment(
                type, status, departmentId, from, to
        );
    }


    public Map<RefuseReason, Integer> countRefusedByReason(
            Class<? extends Request> type,
            Long departmentId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return requestRepository.countRefusedByReason(type, departmentId, RequestStatus.REFUSED, from, to)
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
            LocalDateTime from,
            LocalDateTime to
    ) {
        List<MonthlyCountDto> result = new ArrayList<>();

        var current = from.withDayOfMonth(1);
        var end = to.withDayOfMonth(1);

        while (!current.isAfter(end)) {
            var monthStart = current;
            var monthEnd = current.plusMonths(1).minusSeconds(1);

            int spot = countByRequestType(
                    SpotRequest.class, departmentId, monthStart, monthEnd);
            int contract = countByRequestType(
                    ContractRequest.class, departmentId, monthStart, monthEnd);

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