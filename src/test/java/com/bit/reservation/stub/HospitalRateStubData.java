package com.bit.reservation.stub;

import com.bit.reservation.domain.hospital.dto.HospitalRateDto;
import com.bit.reservation.domain.hospital.entity.HospitalRate;
import com.bit.reservation.domain.user.entity.User;

public class HospitalRateStubData {

    public static HospitalRate getRate () {
        return HospitalRate.builder()
                .hospitalRateId(1L)
                .rating(2.5)
                .content("후기입니다.")
                .hospital(StubData.getHospital())
                .user(StubData.getUser())
                .reservation(StubData.getReservation())
                .build();
    }

    public static HospitalRateDto.PostDto getPostDto() {
        return new HospitalRateDto.PostDto(2.5, "후기입니다.");
    }

    public static HospitalRateDto.BasicDto getBasicDto() {
        return new HospitalRateDto.BasicDto(1L, 2.5, "후기입니다.");
    }

    public static HospitalRateDto.ResponseDto getResponseDto() {
        User user = StubData.getUser();
        return new HospitalRateDto.ResponseDto(1L, 2.5, "후기입니다.", 1L, user.getUserName(), user.getProfileImage().getImage());
    }
}
