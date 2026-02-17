package com.mayak.ietms.features.company.application;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.shared.exception.business.CompanyInUseException;
import com.mayak.ietms.shared.exception.business.CompanyNotFoundException;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import com.mayak.ietms.features.company.infra.mapping.CompanyMapper;
import com.mayak.ietms.features.company.infra.persistence.CompanyRepository;
import com.mayak.ietms.features.lane.infra.persistence.LaneRepository;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.features.company.application.validation.CompanyCreateBackendValidator;
import com.mayak.ietms.features.company.application.validation.CompanyUpdateBackendValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final RequestRepository requestRepository;
    private final LaneRepository laneRepository;
    private final CompanyMapper companyMapper;
    private final CompanyCreateBackendValidator companyCreateBackendValidator;
    private final CompanyUpdateBackendValidator companyUpdateBackendValidator;

    // --- CREATE ---
    @Transactional
    public CompanyDto create(CompanyCreateDto dto) {
        validateCreate(dto);

        Company company = companyMapper.toEntity(dto);
        Company saved = companyRepository.save(company);

        log.info("Company placed with ID: {}", saved.getId());
        return companyMapper.toDto(saved);
    }

    // --- READ ---
    @Transactional(readOnly = true)
    public Optional<CompanyDto> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return companyRepository
                .findByNameIgnoreCase(name)
                .map(companyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> findAll() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toDto)
                .toList();
    }

    // --- UPDATE ---
    @Transactional
    public void update (Long id, CompanyDto dto) {
        validateUpdate(id, dto);

        Company company = getOrThrow(id);
        companyMapper.updateEntityFromDto(dto, company);
        companyRepository.save(company);
    }

    // --- DELETE ---
    @Transactional
    public void delete(Long id) {
        Company company = getOrThrow(id);

        if (isCompanyUsed(id)) {
            throw new CompanyInUseException(id);
        }
        companyRepository.delete(company);
        log.info("Company {} deleted", id);
    }

    private boolean isCompanyUsed(Long companyId) {
        return requestRepository.existsByCustomer_Id(companyId)
                || laneRepository.existsByCustomer_Id(companyId);
    }

    @Transactional
    public Company resolveCompany(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        return companyRepository
                .findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Company created = new Company(name);
                    Company saved = companyRepository.save(created);

                    log.info("Company auto-placed: id={}, name='{}'", saved.getId(), saved.getName());

                    return saved;
                });
    }

    // --- HELPERS ---
    private void validateCreate(CompanyCreateDto dto) {
        ValidationResult result = companyCreateBackendValidator.isValid(dto);
        if (!result.isValid()) {
            throw new ValidationException(result);
        }
    }

    private void validateUpdate(Long id, CompanyDto dto) {
        ValidationResult result = companyUpdateBackendValidator.isValid(id, dto);
        if (!result.isValid()) {
            throw new ValidationException(result);
        }
    }

    public Company getOrThrow(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
    }
}