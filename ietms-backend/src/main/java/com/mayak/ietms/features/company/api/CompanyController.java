package com.mayak.ietms.features.company.api;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.features.company.application.CompanyService;
import com.mayak.ietms.shared.exception.business.CompanyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public List<CompanyDto> findAll() {
        return companyService.findAll();
    }

    @GetMapping("/by-name")
    public CompanyDto findByName(@RequestParam("name") String name) {
        return companyService.findByName(name)
                .orElseThrow(() -> new CompanyNotFoundException(name));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CRM')")
    public CompanyDto create(@RequestBody CompanyCreateDto dto) {
        return companyService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CRM')")
    public void update(
            @PathVariable("id") Long id,
            @RequestBody CompanyDto dto) {
        companyService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CRM')")
    public void delete(@PathVariable("id") Long id) {
        companyService.delete(id);
    }
}
