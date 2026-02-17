package com.mayak.ietms.location.dto;

public record LocationCreateDto(
        String countryCode,
        String zipCode,
        String placeName) {
}