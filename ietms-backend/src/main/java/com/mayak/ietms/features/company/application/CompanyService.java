package com.mayak.ietms.features.company.application;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.features.company.application.notify.CompanyNotificationService;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
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
    private final ShipmentRepository shipmentRepository;
    private final LaneRepository laneRepository;
    private final CompanyMapper companyMapper;
    private final CompanyCreateBackendValidator companyCreateBackendValidator;
    private final CompanyUpdateBackendValidator companyUpdateBackendValidator;
    private final CompanyNotificationService companyNotificationService;

    /**
     * Creates a new company and publishes a creation event.
     *
     * @param dto company creation data
     * @return the created company
     * @throws ValidationException if the input data is invalid
     */
    @Transactional
    public CompanyDto create(CompanyCreateDto dto) {
        validateCreate(dto);

        Company company = companyMapper.toEntity(dto);
        Company saved = companyRepository.save(company);

        log.info("Company placed with ID: {}", saved.getId());
        CompanyDto result = companyMapper.toDto(saved);
        companyNotificationService.publishCreated(result);
        return companyMapper.toDto(saved);
    }

    /**
     * Finds a company by exact name, case-insensitive.
     *
     * @param name the company name to search for
     * @return an {@link Optional} containing the company if found, or empty
     */
    @Transactional(readOnly = true)
    public Optional<CompanyDto> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return companyRepository
                .findByNameIgnoreCase(name)
                .map(companyMapper::toDto);
    }

    /**
     * Returns all companies.
     *
     * @return list of all companies
     */
    @Transactional(readOnly = true)
    public List<CompanyDto> findAll() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toDto)
                .toList();
    }

    /**
     * Updates an existing company and publishes an update event.
     *
     * @param id  the company ID
     * @param dto updated company data
     * @throws CompanyNotFoundException if no company with the given ID exists
     * @throws ValidationException      if the input data is invalid
     */
    @Transactional
    public void update(Long id, CompanyDto dto) {
        validateUpdate(id, dto);

        Company company = getOrThrow(id);
        companyMapper.updateEntityFromDto(dto, company);
        companyRepository.save(company);
        companyNotificationService.publishUpdated(companyMapper.toDto(company));
    }

    /**
     * Deletes a company if it is not referenced by any request, lane, or shipment.
     *
     * @param id the company ID
     * @throws CompanyNotFoundException if no company with the given ID exists
     * @throws CompanyInUseException    if the company is in use
     */
    @Transactional
    public void delete(Long id) {
        Company company = getOrThrow(id);

        assertNotInUse(id);
        companyRepository.delete(company);
        log.info("Company {} deleted", id);
        companyNotificationService.publishDeleted(id);
    }

    /**
     * Finds a company by name or creates it automatically if it does not exist.
     * Used for carrier assignment during shipment processing.
     *
     * @param name the company name
     * @return the existing or newly created {@link Company} entity,
     *         or {@code null} if the name is blank
     */
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
                    companyNotificationService.publishCreated(companyMapper.toDto(saved));

                    return saved;
                });
    }

    /**
     * Returns a company entity by ID or throws if not found.
     *
     * @param id the company ID
     * @return the {@link Company} entity
     * @throws CompanyNotFoundException if no company with the given ID exists
     */
    public Company getOrThrow(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
    }

    private void assertNotInUse(Long companyId) {
        if (requestRepository.existsByCustomer_Id(companyId))
            throw new CompanyInUseException("This company cannot be deleted because it is used in existing requests.");
        if (laneRepository.existsByCustomer_Id(companyId))
            throw new CompanyInUseException("This company cannot be deleted because it is used in contract lanes.");
        if (shipmentRepository.existsByCarrier_Id(companyId))
            throw new CompanyInUseException("This company cannot be deleted because it is assigned as a carrier in shipments.");
    }

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
}