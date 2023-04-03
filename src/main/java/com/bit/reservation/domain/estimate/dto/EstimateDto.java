package com.bit.reservation.domain.estimate.dto;

import com.bit.reservation.global.status.HospitalStatus;
import com.bit.reservation.global.status.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.Pattern;
import java.util.List;

public class EstimateDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostToIdDto {
        private List<Long> reservationIds;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostToDateDto {
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "잘못된 날짜 형식입니다.")
        private String estimateDate;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchDto {
        private Long estimateId;
        private Long reservationId;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponseDto {
        private Long estimateId;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseDto {
        private Long estimateId;
        private String estimateDate;
        private Integer numberOfReservations;
        private Integer totalAmount;
        private HospitalInfo hospitalInfo;
        private List<ReservationInfo> reservationInfo;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ListResponseDto {
        private Long estimateId;
        private String estimateDate;
        private Integer totalAmount;
        private HospitalInfo hospitalInfo;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HospitalInfo {
        private Long hospitalId;
        private String hospitalName;
        private HospitalStatus hospitalStatus;

        public String getHospitalStatus() {
            return hospitalStatus.getStatus();
        }
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationInfo {
        private Long reservationId;
        private String dateTime;
        private String subject;
        private ReservationStatus reservationStatus;

        public String getReservationStatus() {
            return reservationStatus.getStatus();
        }
    }

}
