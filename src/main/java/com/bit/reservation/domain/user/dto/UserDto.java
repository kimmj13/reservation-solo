package com.bit.reservation.domain.user.dto;

import com.bit.reservation.global.status.UserLevel;
import com.bit.reservation.global.status.UserProfileImage;
import com.bit.reservation.global.status.UserStatus;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.time.LocalDate;

public class UserDto {

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter @Setter
    public static class PostDto {

        @Size(min = 2, max = 20)
        @NotBlank(message = "이름은 공백이 아니어야 합니다.")
        private String userName;

        @Email
        @NotBlank(message = "이메일은 공백이 아니어야 합니다.")
        private String email;

        @NotBlank(message = "비밀번호는 공백이 아니어야 합니다.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&]).{8,16}", message = "비밀번호는 영문, 특수문자, 숫자 포함 8-16자 이내여야 합니다.")
        private String password;

        @NotBlank(message = "주소는 공백이 아니어야 합니다.")
        private String address;

        @Positive
        @NotNull(message = "나이는 공백이 아니어야 합니다.")
        private Integer age;

        @NotBlank(message = "휴대폰 번호는 공백이 아니어야 합니다.")
        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "잘못된 휴대폰 번호 형식입니다.")
        private String phoneNumber;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchDto {

        private Long userId;

        @Length(min = 2, max = 16)
        private String userName;

        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&]).{8,16}", message = "비밀번호는 영문, 특수문자, 숫자 포함 8-16자 이내여야 합니다.")
        private String password;

        @Length(min = 1)
        private String address;

        @Positive
        private Integer age;

        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "잘못된 휴대폰 번호 형식입니다.")
        private String phoneNumber;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter @Setter
    public static class SimpleResponseDto {
        private Long userId;
    }

    //TODO : 항목 좀더 생각해보기
    //특정 회원 조회
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter @Setter
    public static class ResponseDto {
        private Long userId;
        private String userName;
        private UserProfileImage profileImage;
        private UserLevel userLevel;

        public String getProfileImage() {
            return profileImage.getImage();
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter @Setter
    @Builder
    public static class ListResponseDto {
        private Long userId;
        private String userName;
        private String email;
        private String address;
        private Integer age;
        private String phoneNumber;
        private UserProfileImage profileImage;
        private UserLevel userLevel;
        private UserStatus userStatus;
        private LocalDate createdAt;
        private LocalDate modifiedAt;

        public String getProfileImage() {
            return profileImage.getImage();
        }
    }
}
