package com.mayak.ietms.features.department.application;

import com.mayak.ietms.department.dto.DepartmentCreateDto;
import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.features.department.domain.model.Department;
import com.mayak.ietms.shared.exception.business.DepartmentInUseException;
import com.mayak.ietms.shared.exception.business.DepartmentNotFoundException;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import com.mayak.ietms.features.department.infra.mapping.DepartmentMapper;
import com.mayak.ietms.features.department.infra.mapping.DepartmentUpdateMapper;
import com.mayak.ietms.features.department.infra.persistence.DepartmentRepository;
import com.mayak.ietms.features.user.infra.persistence.ProfileRepository;
import com.mayak.ietms.features.department.application.validation.DepartmentCreateBackendValidator;
import com.mayak.ietms.features.department.application.validation.DepartmentUpdateBackendValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentCreateBackendValidator departmentCreateBackendValidator;
    private final DepartmentUpdateBackendValidator departmentUpdateBackendValidator;
    private final DepartmentRepository departmentRepository;
    private final ProfileRepository profileRepository;
    private final DepartmentMapper departmentMapper;
    private final DepartmentUpdateMapper departmentUpdateMapper;

    // --- CREATE ---
    @Transactional
    public Department create(DepartmentCreateDto dto) {
        validateCreate(dto);

        Department department = departmentMapper.toEntity(dto);
        Department saved = departmentRepository.save(department);

        log.info("Department placed with ID: {}", saved.getId());
        return saved;
    }

    // --- READ ---
    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    // --- UPDATE ---
    @Transactional
    public void update(Long id,DepartmentDto dto) {
        validateUpdate(id, dto);

        Department department = getDepartmentOrThrow(id);
        departmentUpdateMapper.updateEntityFromDto(dto, department);

        departmentRepository.save(department);
        log.info("Department updated with ID: {}", department.getId());
    }

    // --- DELETE ---
    @Transactional
    public void delete(Long id) {
        Department department = getDepartmentOrThrow(id);

        if (profileRepository.existsByDepartmentId(id)) {
            throw new DepartmentInUseException(id);
        }

        try {
            departmentRepository.delete(department);
            departmentRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new DepartmentInUseException(id);
        }
        log.info("Department {} deleted", id);
    }

    // --- HELPERS ---
    private void validateCreate(DepartmentCreateDto dto) {
        var result = departmentCreateBackendValidator.isValid(dto);
        if (!result.isValid()) throw new ValidationException(result);
    }

    private void validateUpdate(Long id, DepartmentDto dto) {
        var result = departmentUpdateBackendValidator.isValid(id, dto);
        if (!result.isValid()) throw new ValidationException(result);
    }

    private Department getDepartmentOrThrow(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }
}