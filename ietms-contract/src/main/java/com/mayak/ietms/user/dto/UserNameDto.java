package com.mayak.ietms.user.dto;

public record UserNameDto(String firstName, String lastName) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}