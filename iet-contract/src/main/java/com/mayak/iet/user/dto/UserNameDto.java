package com.mayak.iet.user.dto;

public record UserNameDto(
        String firstName,
        String lastName) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}