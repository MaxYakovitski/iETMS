package com.mayak.iet.infrastructure.config;

import com.mayak.iet.company.validator.CompanyCreateContractValidator;
import com.mayak.iet.company.validator.CompanyUpdateContractValidator;
import com.mayak.iet.department.validator.DepartmentCreateContractValidator;
import com.mayak.iet.department.validator.DepartmentUpdateContractValidator;
import com.mayak.iet.lane.validator.LaneContractValidator;
import com.mayak.iet.common.validation.DateRangeContractValidator;
import com.mayak.iet.location.validator.LocationContractValidator;
import com.mayak.iet.request.validator.RequestContractValidator;
import com.mayak.iet.user.validator.UserCreateContractValidator;
import com.mayak.iet.user.validator.UserUpdateContractValidator;
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