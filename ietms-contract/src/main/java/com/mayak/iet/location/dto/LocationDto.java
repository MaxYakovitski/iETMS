package com.mayak.iet.location.dto;

public record LocationDto(
        Long id,
        String countryCode,
        String zipCode,
        String placeName) {
}