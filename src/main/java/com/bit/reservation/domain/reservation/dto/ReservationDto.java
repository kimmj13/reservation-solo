package com.bit.reservation.domain.reservation.dto;

import com.bit.reservation.global.status.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class ReservationDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDto {

        @NotBlank
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}$", message = "잘못된 날짜 형식입니다.")
        private String dateTime;

        @NotBlank
        private String subject;

        private String clientRequest;
        private String doctorName;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchDto {
        private Long reservationId;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}$", message = "잘못된 날짜 형식입니다.")
        private String dateTime;

        private String subject;
        private String clientRequest;
        private String doctorName;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientResponseDto {
        private Long reservationId;
        private String dateTime;
        private String subject;
        private String clientRequest;
        private String doctorName;
        private ReservationStatus reservationStatus;
        private HospitalInfo hospitalInfo;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;

        public String getReservationStatus() {
            return reservationStatus.getStatus();
        }
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListClientResponseDto {
        private Long reservationId;
        private String dateTime;
        private ReservationStatus reservationStatus;
        private HospitalInfo hospitalInfo;

        public String getReservationStatus() {
            return reservationStatus.getStatus();
        }
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HospitalResponseDto {

        private Long doctorId;
        private String doctorName;

        private Long reservationId;
        private String dateTime;
        private String subject;
        private String clientRequest;

        private String hospitalMemo;
        private ReservationStatus reservationStatus;

        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;

        private ClientInfo clientInfo;

        public String getReservationStatus() {
            return reservationStatus.getStatus();
        }
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListHospitalResponseDto {

        private Long doctorId;
        private String doctorName;

        private Long reservationId;
        private String dateTime;
        private String medicalSubject;
        private ReservationStatus reservationStatus;
        private ClientInfo clientInfo;

        public String getReservationStatus() {
            return reservationStatus.getStatus();
        }
    }



    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponseDto {
        private Long reservationId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    public static class ClientInfo {
        private Long userId;
        private String name;
        private Integer age;
        private String profileImage;
    }

    @AllArgsConstructor
    @Getter @Builder
    @NoArgsConstructor
    public static class HospitalInfo {
        private Long hospitalId;
        private String name;
        private String telNum;
    }

    @AllArgsConstructor
    @Getter @Setter
    @NoArgsConstructor
    @Builder
    public static class PatchDtoForHospital {
        private Long reservationId;
        private String hospitalMemo;
        private Long doctorId;
        private ReservationStatus reservationStatus;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminListHospitalResponseDto {

        private Long doctorId;
        private String doctorName;

        private Long reservationId;
        private String dateTime;
        private String medicalSubject;
        private ReservationStatus reservationStatus;
        private ClientInfo clientInfo;
        private HospitalInfo hospitalInfo;
        private boolean quotation;

        public String getReservationStatus() {
            return reservationStatus.getStatus();
        }
    }
}
