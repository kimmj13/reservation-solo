package com.bit.reservation.domain.hospital.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class DoctorDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDto {

        @Size(min = 2, max = 20)
        @NotBlank
        private String name;

        @Size(min = 4, max = 100)
        private String school;

        @Size(min = 1, max = 300)
        private String career;

        @Size(min = 1, max = 300)
        @NotNull
        private List<String> medicalSubject;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchDto {
        private Long doctorId;

        @Size(min = 2, max = 20)
        private String name;

        @Size(min = 4, max = 100)
        private String school;

        @Size(min = 1, max = 300)
        private String career;

        @Size(min = 1, max = 300)
        private List<String> medicalSubject;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDto {
        private Long doctorId;
        private String name;
        private String school;
        private String career;
        private List<String> medicalSubject;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleResponseDto {
        private Long doctorId;
    }
}
