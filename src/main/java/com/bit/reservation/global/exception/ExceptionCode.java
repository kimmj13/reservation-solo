package com.bit.reservation.global.exception;

import lombok.Getter;

public enum ExceptionCode {

    //common
    UNAUTHORIZED(401, "Unauthorized"),
    ACCESS_FORBIDDEN(403, "Access forbidden"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTATION(501, "Not Implementation"),
    INVALID_VALUES(400, "Invalid Values"),
    INVALID_DATE(400, "Invalid Date"),

    //auth
    INVALID_EMAIL_AUTH_NUMBER(400, "Invalid email authNumber"),
    INVALID_EMAIL_AUTH(400, "Invalid email auth"),
    INVALID_REFRESH_TOKEN(400, "Invalid refresh token"),
    EXPIRED_JWT_TOKEN(421, "Expired jwt token"),
    EMAIL_AUTH_REQUIRED(403, "Email auth required"),

    //member
    INVALID_MEMBER_STATUS(400, "Invalid member status"),
    MAX_FILE_SIZE_2MB(400, "Max file size 2MB"),
    MEMBER_EXISTS(409, "Member exists"),
    MEMBER_NOT_FOUND(404, "Member not found"),
    INVALID_PASSWORD (400, "Invalid Password"),
    MEMBER_NOT_LOGIN(400, "Member not login"),
    ID_NOT_EXIST(404, "Id not exist"),

    HOSPITAL_NOT_PATCHED(403, "Hospital not patched"),
    HOSPITAL_NOT_FOUND(404, "Hospital Not Found"),
    HOSPITAL_CHECK_EXISTS(409, "Hospital Check exists"),
    HOSPITAL_EXISTS(409, "Hospital exists"),
    HOSPITAL_REQUEST_EXISTS(409, "Hospital Request exists"),
    HOSPITAL_NOT_RECRUITING(403, "Hospital status not recruiting"),
    INVALID_HOSPITAL_STATUS(403, "Invalid Hospital Status"),
    HOSPITAL_MEMBER_EXISTS(409, "Hospital Member exists"),

    DOCTOR_NOT_PATCHED(403, "Doctor not patched"),
    DOCTOR_NOT_FOUND(404, "Doctor Not Found"),
    DOCTOR_CHECK_EXISTS(409, "Doctor Check exists"),
    DOCTOR_EXISTS(409, "Doctor exists"),

    NOTICE_NOT_PATCHED(403, "Notice not patched"),
    NOTICE_NOT_FOUND(404, "Notice Not Found"),
    NOTICE_CHECK_EXISTS(409, "Notice Check exists"),
    NOTICE_EXISTS(409, "Notice exists"),

    ESTIMATE_NOT_PATCHED(403, "Estimate not patched"),
    ESTIMATE_EXISTS(403, "Estimate exists"),
    ESTIMATE_NOT_FOUND(404, "Estimate Not Found"),
    ESTIMATE_STATUS_EXISTS(409, "Estimate status exists"),
    ESTIMATE_CHECK_EXISTS(409, "Estimate Check exists"),

    RATE_NOT_PATCHED(403, "Rate not patched"),
    RATE_NOT_FOUND(404, "Rate Not Found"),
    RATE_EXISTS(409, "Rate exists"),
    RATE_CHECK_EXISTS(409, "Rate Check exists"),

    RESERVATION_NOT_PATCHED(403, "Reservation not patched"),
    RESERVATION_NOT_DONE(403, "Reservation not done"),
    RESERVATION_NOT_FOUND(404, "Reservation Not Found"),
    RESERVATION_STATUS_EXISTS(409, "Reservation status exists"),
    RESERVATION_CHECK_EXISTS(409, "Reservation Check exists");


    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
