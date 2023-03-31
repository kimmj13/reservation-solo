package com.bit.reservation.domain.hospital.dto;

import com.bit.reservation.global.status.HospitalStatus;
import lombok.*;

import javax.validation.constraints.*;
import java.util.List;

public class HospitalDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDto {

        @Email
        @NotBlank(message = "이메일은 공백이 아니어야 합니다.")
        private String email;

        @NotBlank(message = "비밀번호는 공백이 아니어야 합니다.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&]).{8,16}", message = "비밀번호는 영문, 특수문자, 숫자 포함 8-16자 이내여야 합니다.")
        private String password;

        @NotBlank(message = "병원 이름은 공백이 아니어야 합니다.")
        private String name;

        @NotBlank(message = "주소는 공백이 아니어야 합니다.")
        private String address;

        @NotNull
        @PositiveOrZero
        private Integer age;

        @NotNull(message = "진료 과목은 공백이 아니어야 합니다.")
        private List<String> medicalSubject;

        @NotBlank(message = "전화번호는 공백이 아니어야 합니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "잘못된 전화번호 형식입니다.")
        private String telNum;

        //TODO: 패턴 추가
        private String openingTime;
        private String closingTime;
        private String breakStartTime;
        private String breakEndTime;
        private String intro;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchDto {
        private Long hospitalId;

        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&]).{8,16}", message = "비밀번호는 영문, 특수문자, 숫자 포함 8-16자 이내여야 합니다.")
        private String password;

        @Size(min = 1, max = 100)
        private String name;

        @Size(min = 1, max = 100)
        private String address;

        @PositiveOrZero
        private Integer age;

        private List<String> medicalSubject;

        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "잘못된 전화번호 형식입니다.")
        private String telNum;

        private String openingTime;
        private String closingTime;
        private String breakStartTime;
        private String breakEndTime;
        private String intro;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseDto {
        private Long hospitalId;
        private String name;
        private String address;
        private Integer age;
        private List<String> medicalSubject;
        private String openingTime;
        private String closingTime;
        private String breakStartTime;
        private String breakEndTime;
        private String intro;
        private String hospitalPicture;
        private String telNum;
        private HospitalStatus hospitalStatus;

        public String getHospitalStatus() {
            return hospitalStatus.getStatus();
        }
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SimpleResponseDto {
        private Long hospitalId;
    }
}
