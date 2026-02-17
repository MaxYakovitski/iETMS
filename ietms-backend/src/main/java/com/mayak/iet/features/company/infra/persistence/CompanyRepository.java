package com.mayak.iet.features.company.infra.persistence;

import com.mayak.iet.features.company.domain.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Company> findByNameIgnoreCase(String name);
}