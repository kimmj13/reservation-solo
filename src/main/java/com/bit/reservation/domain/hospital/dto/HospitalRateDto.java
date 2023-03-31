package com.bit.reservation.domain.hospital.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class HospitalRateDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDto {

        @NotNull(message = "평점은 공란이 아니어야 합니다.")
        private Double rating;

        @NotBlank(message = "내용은 공란이 아니어야 합니다.")
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicDto {

        private Long hospitalRateId;

        private Double rating;

        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponseDto {
        private Long hospitalRateId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDto {

        private Long hospitalRateId;

        private Double rating;

        private String content;

        private Long userId;
        private String userName;
        private String userProfileImage;
    }


}
