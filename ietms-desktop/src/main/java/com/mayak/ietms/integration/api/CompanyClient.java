package com.mayak.ietms.integration.api;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.dto.CompanyDto;

import java.util.List;
import java.util.Optional;

public interface CompanyClient {

    List<CompanyDto> findAll();
    Optional<CompanyDto> findByName(String name);

    CompanyDto create(CompanyCreateDto dto);
    void update(Long id, CompanyDto dto);

    void delete(Long id);
}