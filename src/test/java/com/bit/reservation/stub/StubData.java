package com.bit.reservation.stub;

import com.bit.reservation.domain.estimate.entity.Estimate;
import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.entity.HospitalNotice;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.global.status.*;

import java.time.LocalDateTime;
import java.util.List;

public class StubData {

    public static User getUser() {
        User userKim = new User();
        userKim.setUserId(1L);
        userKim.setEmail("kim@test.com");
        userKim.setPassword("abcd12!@");
        userKim.setUserName("kim");
        userKim.setAddress("부산광역시 동래구");
        userKim.setAge(100);
        userKim.setPhoneNumber("010-1111-1111");
        userKim.setProfileImage(UserProfileImage.CAT);
        userKim.setUserLevel(UserLevel.FAMILY);
        userKim.setCreatedAt(LocalDateTime.now());
        userKim.setModifiedAt(LocalDateTime.now());
        userKim.setUserStatus(UserStatus.ACTIVE);
        return userKim;
    }

    public static Doctor getDoctor() {

        return Doctor.builder()
                .doctorId(1L)
                .name("의사1")
                .school("학교이름")
                .career("경력")
                .medicalSubject(List.of("진료과목"))
                .hospital(getHospital())
                .build();
    }

    public static HospitalNotice getNotice() {
        return HospitalNotice.builder()
                .hospitalNoticeId(1L)
                .title("제목")
                .content("내용")
                .hospital(getHospital())
                .build();
    }

    public static Reservation getReservation() {
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .dateTime("2023-03-26 12:00")
                .hospitalMemo("메모")
                .subject("진료과목")
                .clientRequest("요청")
                .reservationStatus(ReservationStatus.WAITING)
                .user(getUser())
                .hospital(getHospital())
                .doctor(getDoctor())
                .build();

        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setModifiedAt(LocalDateTime.now());
        return reservation;
    }

    public static Hospital getHospital() {
        Hospital hospital = Hospital.builder()
                .hospitalId(1L)
                .email("hospital@test.com")
                .password("abcd12!@")
                .name("병원이름")
                .address("병원주소")
                .age(100)
                .telNum("051-111-1111")
                .medicalSubject(List.of("진료과목"))
                .openingTime("08:00:00")
                .closingTime("18:00:00")
                .breakStartTime("12:00:00")
                .breakEndTime("13:00:00")
                .intro("병원소개")
                .hospitalStatus(HospitalStatus.WAITING)
                .hospitalLevel(HospitalLevel.FAMILY)
                .build();

        hospital.setCreatedAt(LocalDateTime.now());
        hospital.setModifiedAt(LocalDateTime.now());
        return hospital;
    }

    public static Estimate getEstimate() {
        return Estimate.builder()
                .estimateId(1L)
                .estimateDate("2023-03")
                .numberOfReservations(1)
                .totalAmount(2000)
                .reservations(List.of(StubData.getReservation()))
                .hospital(StubData.getHospital())
                .build();
    }
}
