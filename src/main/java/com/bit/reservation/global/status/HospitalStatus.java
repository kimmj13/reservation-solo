package com.bit.reservation.global.status;

import lombok.Getter;

import java.util.Arrays;

public enum HospitalStatus {

    WAITING("준비중"),
    ACTIVE("영업중"),
    BREAK("휴식중"),
    CLOSED("영업종료"),
    QUIT("폐쇄");

    @Getter
    private final String status;

    HospitalStatus(String status) {
        this.status = status;
    }
}
