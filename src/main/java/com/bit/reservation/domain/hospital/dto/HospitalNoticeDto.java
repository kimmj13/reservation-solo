package com.bit.reservation.domain.hospital.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;


public class HospitalNoticeDto {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicDto {


        private Long hospitalNoticeId;

        @NotBlank(message = "제목은 공란이 아니어야 합니다.")
        private String title;

        @NotBlank(message = "내용은 공란이 아니어야 합니다.")
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleDto {

        private Long hospitalNoticeId;

        private String title;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponseDto {
        private Long hospitalNoticeId;
    }

}
