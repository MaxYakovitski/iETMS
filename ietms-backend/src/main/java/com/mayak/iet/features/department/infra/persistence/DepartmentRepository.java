package com.mayak.iet.features.department.infra.persistence;

import com.mayak.iet.features.department.domain.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    boolean existsByName(String name);
    boolean existsByCode(String code);

    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<Department> findByName(String name);
}