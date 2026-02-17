package com.mayak.ietms.infrastructure.config;

import com.mayak.ietms.company.validator.CompanyCreateContractValidator;
import com.mayak.ietms.company.validator.CompanyUpdateContractValidator;
import com.mayak.ietms.department.validator.DepartmentCreateContractValidator;
import com.mayak.ietms.department.validator.DepartmentUpdateContractValidator;
import com.mayak.ietms.lane.validator.LaneContractValidator;
import com.mayak.ietms.common.validation.DateRangeContractValidator;
import com.mayak.ietms.location.validator.LocationContractValidator;
import com.mayak.ietms.request.validator.RequestContractValidator;
import com.mayak.ietms.user.validator.UserCreateContractValidator;
import com.mayak.ietms.user.validator.UserUpdateContractValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Bean
    public LaneContractValidator laneValidator() {
        return new LaneContractValidator();
    }

    @Bean
    public CompanyCreateContractValidator companyContractValidator() {
        return new CompanyCreateContractValidator();
    }

    @Bean
    public CompanyUpdateContractValidator companyUpdateContractValidator() {return new CompanyUpdateContractValidator();}

    @Bean
    public DepartmentCreateContractValidator departmentCreateContractValidator() {return new DepartmentCreateContractValidator();
    }

    @Bean
    public DepartmentUpdateContractValidator departmentUpdateContractValidator() {return new DepartmentUpdateContractValidator();
    }

    @Bean
    public DateRangeContractValidator dateRangeContractValidator() {
        return new DateRangeContractValidator();
    }

    @Bean
    public LocationContractValidator locationContractValidator() {
        return new LocationContractValidator();
    }

    @Bean
    public RequestContractValidator requestContractValidator() {
        return new RequestContractValidator();
    }

    @Bean
    public UserCreateContractValidator userCreateContractValidator() {
        return new UserCreateContractValidator();
    }

    @Bean
    public UserUpdateContractValidator userUpdateContractValidator() {
        return new UserUpdateContractValidator();
    }
}