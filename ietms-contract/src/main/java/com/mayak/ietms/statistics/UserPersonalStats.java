package com.mayak.ietms.statistics;

public record UserPersonalStats (
        int placed,
        int joined,
        int bided,
        int acceptedSpot,
        int acceptedContract,
        int dispatched) {
}