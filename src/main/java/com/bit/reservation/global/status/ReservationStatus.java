package com.bit.reservation.global.status;

import lombok.Getter;

public enum ReservationStatus {

    WAITING("대기중"),
    REJECT("거절"),
    APPROVAL("승인"),
    DONE("완료");

    @Getter
    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }
}
