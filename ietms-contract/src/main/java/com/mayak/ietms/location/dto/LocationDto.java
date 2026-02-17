package com.mayak.ietms.location.dto;

public record LocationDto(
        Long id,
        String countryCode,
        String zipCode,
        String placeName) {
}