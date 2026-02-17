package com.mayak.iet.location.dto;

public record LocationCreateDto(
        String countryCode,
        String zipCode,
        String placeName) {
}