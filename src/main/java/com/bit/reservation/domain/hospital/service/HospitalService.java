package com.bit.reservation.domain.hospital.service;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.repository.HospitalRepository;
import com.bit.reservation.domain.user.service.UserService;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import com.bit.reservation.global.security.utils.CustomAuthorityUtils;
import com.bit.reservation.global.status.HospitalLevel;
import com.bit.reservation.global.status.HospitalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HospitalService {
    private final HospitalRepository hospitalRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;

    public Hospital createHospital(Hospital hospital) {
        verifiedUser(hospital.getEmail());
        hospital.setPassword(passwordEncoder.encode(hospital.getPassword()));
        hospital.setHospitalStatus(HospitalStatus.WAITING);
        hospital.setHospitalLevel(HospitalLevel.FAMILY);
        List<String> roles = authorityUtils.createRoles(hospital.getEmail(), true);
        hospital.setRoles(roles);
        return hospitalRepository.save(hospital);
    }

    public Hospital updateHospital(Hospital hospital) {
        Hospital findHospital = existsHospital(hospital.getHospitalId());
        checkJwtAndHospital(hospital.getHospitalId());

        Optional.ofNullable(hospital.getPassword())
                .ifPresent(password -> findHospital.setPassword(passwordEncoder.encode(password)));
        Optional.ofNullable(hospital.getName())
                .ifPresent(findHospital::setName);
        Optional.ofNullable(hospital.getAddress())
                .ifPresent(findHospital::setAddress);
        Optional.ofNullable(hospital.getAge())
                .ifPresent(findHospital::setAge);
        Optional.ofNullable(hospital.getMedicalSubject())
                .ifPresent(findHospital::setMedicalSubject);
        Optional.ofNullable(hospital.getOpeningTime())
                .ifPresent(findHospital::setOpeningTime);
        Optional.ofNullable(hospital.getClosingTime())
                .ifPresent(findHospital::setClosingTime);
        Optional.ofNullable(hospital.getBreakStartTime())
                .ifPresent(findHospital::setBreakStartTime);
        Optional.ofNullable(hospital.getBreakEndTime())
                .ifPresent(findHospital::setBreakEndTime);
        Optional.ofNullable(hospital.getIntro())
                .ifPresent(findHospital::setIntro);
        Optional.ofNullable(hospital.getTelNum())
                .ifPresent(findHospital::setTelNum);

        return hospitalRepository.save(findHospital);
    }

    public void deleteHospital(long hospitalId) {
        Hospital hospital = checkJwtAndHospital(hospitalId);
        //병원 탈퇴시 예약은 삭제 X
//        hospital.getReservations().forEach(reservation -> {
//            reservation.setHospital(null);
//            reservation.setDoctor(null);
//            reservation.setQuitHospitalInfo(
//                    List.of(hospital.getName(), hospital.getTelNum()));
//        });
//        hospitalRepository.delete(hospital);
        hospital.setHospitalStatus(HospitalStatus.QUIT);
        hospitalRepository.save(hospital);
    }

    public Hospital getHospital(long hospitalId) {
        Hospital hospital = existsHospital(hospitalId);
        hospital.setViews(hospital.getViews() + 1);
        return hospitalRepository.save(hospital);
    }

    public Page<Hospital> getHospitals(Pageable pageable, String status) {
        Page<Hospital> pages;

        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());
        //서비스 관리자 - 모든 병원 조회 허용 (waiting, quit 포함)
        if (userService.isServiceAdmin()) {
            pages = hospitalRepository.findByHospitalStatus(pageable,
                    status == null ? null : HospitalStatus.valueOf(status.toUpperCase()));
        } else {
            pages = hospitalRepository.findAllByHospitalStatus(pageable,
                    status == null ? null : HospitalStatus.valueOf(status.toUpperCase()));
        }
        return pages;
    }

    public Hospital existsHospital(long hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.HOSPITAL_NOT_FOUND));
    }

    public void verifiedUser(String email) {
        Optional<Hospital> hospital = hospitalRepository.findByEmail(email);
        if (hospital.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }
    }

    private Hospital getLoginHospital() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return hospitalRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.HOSPITAL_NOT_FOUND));
    }

    //서비스 관리자 + 본인만 접근 허용
    public Hospital checkJwtAndHospital(long hospitalId) {
        if (!userService.isServiceAdmin()) {
            Hospital hospital = getLoginHospital();
            if (hospital.getHospitalId() != hospitalId || hospital.getHospitalStatus().equals(HospitalStatus.QUIT)) {
                throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
            }
        }
        return existsHospital(hospitalId);
    }

    public Hospital checkHospital() {
        Hospital hospital = getLoginHospital();
        if (hospital.getHospitalStatus().equals(HospitalStatus.QUIT)) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
        }
        return hospital;
    }

    public void updateHospitalRoles(Long hospitalId, String status) {
        Hospital hospital = existsHospital(hospitalId);
        if (status.equals("normal")) {
            hospital.setRoles(authorityUtils.acceptedHospital());
            if (hospital.getHospitalStatus() == HospitalStatus.WAITING) {
                settingStatusByLocalTime(hospital);
            }
        } else if (status.equals("abnormal")) {
            hospital.setRoles(List.of("HOSPITAL"));
            hospital.setHospitalStatus(HospitalStatus.QUIT);
        } else {
            throw new BusinessLogicException(ExceptionCode.METHOD_NOT_ALLOWED);
        }
    }

    private void settingStatusByLocalTime(Hospital hospital) {
        LocalTime now = LocalTime.now();
        LocalTime breakStart = LocalTime.parse(hospital.getBreakStartTime());
        LocalTime breakEnd = LocalTime.parse(hospital.getBreakEndTime());
        LocalTime open = LocalTime.parse(hospital.getOpeningTime());
        LocalTime close = LocalTime.parse(hospital.getClosingTime());
        if (now.isAfter(breakStart) || now.isBefore(breakEnd)) {
            hospital.setHospitalStatus(HospitalStatus.BREAK);
        } else if (now.isAfter(open) || now.isBefore(close)) {
            hospital.setHospitalStatus(HospitalStatus.ACTIVE);
        }
        hospital.setHospitalStatus(HospitalStatus.CLOSED);
    }

    /*
    * 영업시간, 휴식시간에 따라 병원 상태 업데이트
    * 주기: 매일 8 ~ 20시, 30분 간격
    * */
    @Scheduled(cron = "0 0/30 8-20 * * *", zone = "Asia/Seoul")
    public void updateHospitalStatus() {
        List<Hospital> all = hospitalRepository.findAll();
        String time = LocalTime.now().toString();
        log.info("병원 영업 상태 업데이트 [every 30M]: {}", time);

        all.forEach(hospital -> {
            if (hospital.getRoles().contains("OK")) {
                if (time.contains(hospital.getOpeningTime())) {
                    hospital.setHospitalStatus(HospitalStatus.ACTIVE);
                    hospitalRepository.save(hospital);
                } else if (time.contains(hospital.getBreakStartTime())) {
                    hospital.setHospitalStatus(HospitalStatus.BREAK);
                    hospitalRepository.save(hospital);
                } else if (time.contains(hospital.getBreakEndTime())) {
                    hospital.setHospitalStatus(HospitalStatus.ACTIVE);
                    hospitalRepository.save(hospital);
                } else if (time.contains(hospital.getClosingTime())) {
                    hospital.setHospitalStatus(HospitalStatus.CLOSED);
                    hospitalRepository.save(hospital);
                }
            }
        });
    }
}
