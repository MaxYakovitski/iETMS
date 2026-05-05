package com.mayak.ietms.user.dto;

public record UserLookupDto(Long id, UserNameDto name) {
    public String fullName() {
        return name != null ? name.fullName() : "";
    }
}