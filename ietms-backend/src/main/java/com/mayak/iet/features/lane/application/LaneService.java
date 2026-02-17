package com.mayak.iet.features.lane.application;

import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.lane.dto.LaneViewDto;
import com.mayak.iet.features.company.domain.model.Company;
import com.mayak.iet.features.lane.domain.model.Lane;
import com.mayak.iet.features.location.domain.model.Location;
import com.mayak.iet.shared.exception.business.LaneInUseException;
import com.mayak.iet.shared.exception.business.LaneNotFoundException;
import com.mayak.iet.features.lane.infra.mapping.LaneMapper;
import com.mayak.iet.features.location.infra.mapping.LocationMapper;
import com.mayak.iet.features.lane.infra.persistence.LaneRepository;
import com.mayak.iet.features.request.infra.persistence.ContractRequestRepository;
import com.mayak.iet.features.company.application.CompanyService;
import com.mayak.iet.features.location.application.LocationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LaneService {

    private final LaneRepository laneRepository;
    private final ContractRequestRepository contractRequestRepository;
    private final LocationCommandService locationCommandService;
    private final CompanyService companyService;
    private final LaneMapper laneMapper;
    private final LocationMapper locationMapper;


    @Transactional
    public LaneViewDto create(Long companyId, LaneCreateDto dto) {
        Company company = companyService.getOrThrow(companyId);
        Lane lane = laneMapper.toEntity(dto);
        lane.setCustomer(company);

        Location from = locationMapper.toEntity(dto.fromLocation());
        Location to   = locationMapper.toEntity(dto.toLocation());

        lane.setFromLocation(locationCommandService.resolve(from));
        lane.setToLocation(locationCommandService.resolve(to));

        Lane saved = laneRepository.save(lane);
        return laneMapper.toViewDto(saved);
    }

    @Transactional(readOnly = true)
    public List<LaneViewDto> findByCompany(Long companyId) {
        return laneRepository.findByCompanyIdWithFetch(companyId)
                .stream()
                .map(laneMapper::toViewDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Lane getOrThrow(Long id) {
        return laneRepository.findById(id)
                .orElseThrow(() -> new LaneNotFoundException(id));
    }

    @Transactional
    public LaneViewDto update(Long id, LaneCreateDto dto) {
        Lane lane = getOrThrow(id);

        laneMapper.updateEntity(lane, dto);

        Location from = locationMapper.toEntity(dto.fromLocation());
        Location to   = locationMapper.toEntity(dto.toLocation());

        lane.setFromLocation(locationCommandService.resolve(from));
        lane.setToLocation(locationCommandService.resolve(to));

        Lane saved = laneRepository.save(lane);

        log.info("Lane updated: id={}", saved.getId());
        return laneMapper.toViewDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Lane lane = getOrThrow(id);
        boolean usedInRequests = contractRequestRepository.existsByLane_Id(id);
        boolean usedAsLinked   = laneRepository.existsByLinkedLane_Id(id);

        if (usedInRequests || usedAsLinked) {
            throw new LaneInUseException(id);
        }

        laneRepository.delete(lane);
        log.info("Lane deleted: id={}", id);
    }
}