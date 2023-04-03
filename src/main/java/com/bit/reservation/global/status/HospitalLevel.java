package com.bit.reservation.global.status;

import lombok.Getter;

import java.util.Arrays;

public enum HospitalLevel {

    FAMILY(0, 2000),
    SILVER(50, 1800),
    GOLD(100, 1600),
    VIP(150, 1400);

    @Getter
    private final int numOfReservation;

    @Getter
    private final int pricePerCase;

    HospitalLevel(int numOfReservation, int pricePerCase) {
        this.numOfReservation = numOfReservation;
        this.pricePerCase = pricePerCase;
    }
}
