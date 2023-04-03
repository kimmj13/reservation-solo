package com.bit.reservation.domain.reservation.service;

import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.repository.DoctorRepository;
import com.bit.reservation.domain.hospital.service.HospitalService;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.reservation.repository.ReservationRepository;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.domain.user.service.UserService;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import com.bit.reservation.global.status.HospitalStatus;
import com.bit.reservation.global.status.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository repository;
    private final UserService userService;
    private final HospitalService hospitalService;
    private final DoctorRepository doctorRepository;

    public Reservation createReservation(Reservation reservation, Long userId, Long hospitalId) {
        Hospital hospital = hospitalService.getHospital(hospitalId);
        Optional.ofNullable(reservation.getDoctor())
                .ifPresent(doctor -> {
                    if (!hospital.getDoctors().contains(doctor)) {
                        throw new BusinessLogicException(ExceptionCode.DOCTOR_NOT_FOUND);
                    }
                });
        checkForbiddenStatus(hospital, HospitalStatus.WAITING, HospitalStatus.QUIT);
        reservation.setUser(userService.checkJwtAndUser(userId));
        reservation.setReservationStatus(ReservationStatus.WAITING);
        reservation.setHospital(hospital);
        return repository.save(reservation);
    }

    private void checkForbiddenStatus(Hospital hospital, HospitalStatus ... status) {
        HospitalStatus hospitalStatus = hospital.getHospitalStatus();
        if (Arrays.stream(status).anyMatch(hospitalStatus::equals)) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
        }
    }

    public Reservation updateReservation(Reservation reservation, String doctorName) {
        Reservation findReservation = existsReservation(reservation.getReservationId());

        Optional.ofNullable(reservation.getDateTime())
                .ifPresent(findReservation::setDateTime);
        Optional.ofNullable(reservation.getSubject())
                .ifPresent(findReservation::setSubject);
        Optional.ofNullable(reservation.getClientRequest())
                .ifPresent(findReservation::setClientRequest);
        Optional.ofNullable(doctorName)
                .ifPresent(name -> {
                    Doctor findDoctor = findReservation.getHospital().getDoctors()
                            .stream()
                            .filter(doctor -> doctor.getName().equals(name))
                            .findFirst()
                            .orElseThrow(() -> new BusinessLogicException(ExceptionCode.DOCTOR_NOT_FOUND));
                    reservation.setDoctor(findDoctor);
                });

        return findReservation;
    }

    public void deleteReservation(Long reservationId) {
        repository.delete(existsReservation(reservationId));
    }

    public Reservation findClientReservation(Long reservationId, Long userId) {
        userService.checkJwtAndUser(userId);
        return existsReservation(reservationId);
    }

    public Page<Reservation> findClientReservations(Pageable pageable, Long userId, String dateTime, String subject, String status, String hospitalName) {
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("reservationId").descending());
        User user = userService.checkJwtAndUser(userId);
        ReservationStatus reservationStatus = status != null ? ReservationStatus.valueOf(status.toUpperCase()) : null;
        Page<Reservation> pages = repository.findByDateTimeAndSubjectAndReservationStatusAndHospitalAndUser(dateTime, subject, reservationStatus, null, user, pageable);

        if (hospitalName != null) {
            List<Reservation> reservations = pages.getContent().stream()
                    .filter(reservation -> reservation.getHospital() != null && reservation.getHospital().getName().contains(hospitalName))
                    .collect(Collectors.toList());
            pages = new PageImpl<>(reservations, pageable, reservations.size());
        }
        return pages;
    }

    public Reservation findHospitalReservation(Long reservationId, Long hospitalId) {
        hospitalService.checkJwtAndHospital(hospitalId);
        return existsReservation(reservationId);
    }

    public Page<Reservation> findHospitalReservations(Pageable pageable, Long hospitalId, String dateTime, String subject, String status, String userName) {
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("reservationId").descending());
        Hospital hospital = hospitalService.checkJwtAndHospital(hospitalId);
        ReservationStatus reservationStatus = status != null ? ReservationStatus.valueOf(status.toUpperCase()) : null;
        Page<Reservation> pages = repository.findByDateTimeAndSubjectAndReservationStatusAndHospitalAndUser(dateTime, subject, reservationStatus, hospital, null, pageable);

        if (userName != null) {
            List<Reservation> reservations = pages.getContent().stream()
                    .filter(reservation -> reservation.getUser() != null && reservation.getUser().getUserName().contains(userName))
                    .collect(Collectors.toList());
            pages = new PageImpl<>(reservations, pageable, reservations.size());
        }
        return pages;
    }

    public Page<Reservation> findAllReservations(int page, int size, String dateTime, String subject, String status, Long hospitalId, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("reservationId").descending());
        ReservationStatus reservationStatus = status != null ? ReservationStatus.valueOf(status.toUpperCase()) : null;
        Hospital hospital = hospitalId != null ? hospitalService.getHospital(hospitalId) : null;
        User user = userId != null ? userService.getUser(userId) : null;
        return repository.findByDateTimeAndSubjectAndReservationStatusAndHospitalAndUser(dateTime, subject, reservationStatus, hospital, user, pageable);
    }

    public Reservation updateReservationByHospital(Long hospitalId, Reservation reservation, Long doctorId) {
        Reservation findReservation = existsReservation(reservation.getReservationId());
        Hospital hospital = hospitalService.getHospital(hospitalId);

        if (findReservation.getHospital() != hospital) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
        }

        Optional.ofNullable(reservation.getHospitalMemo())
                .ifPresent(findReservation::setHospitalMemo);
        Optional.ofNullable(doctorId)
                        .ifPresent(id -> {
                            Doctor doctor = doctorRepository.findByHospital(hospital)
                                    .orElseThrow(() -> new BusinessLogicException(ExceptionCode.DOCTOR_NOT_FOUND));
                            findReservation.setDoctor(doctor);
                        });
        Optional.ofNullable(reservation.getReservationStatus())
                .ifPresent(status -> {
                    if (status == ReservationStatus.WAITING || status == ReservationStatus.DONE) {
                        throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
                    } else if (findReservation.getReservationStatus() != ReservationStatus.WAITING) {
                        throw new BusinessLogicException(ExceptionCode.RESERVATION_STATUS_EXISTS);
                    }
                    findReservation.setReservationStatus(status);
                });

        return repository.save(findReservation);
    }

    public Reservation existsReservation(Long reservationId) {
        return repository.findById(reservationId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.RESERVATION_NOT_FOUND));
    }

    //매일 00시에 예약 상태 업데이트 (approval -> done)
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void updateDoneStatus() {
        LocalDate localDate = LocalDate.now().minusDays(1);
        log.info("예약 완료건에 대한 상태 업데이트 [00시]: {}", localDate);

        List<Reservation> all = repository.findAllByReservationStatus(ReservationStatus.APPROVAL);

        all.forEach(reservation -> {
            String findDate = reservation.getDateTime().split(" ")[0];
            if (localDate.toString().equals(findDate)) {
                reservation.setReservationStatus(ReservationStatus.DONE);
                repository.save(reservation);
            }
        });
    }
}
