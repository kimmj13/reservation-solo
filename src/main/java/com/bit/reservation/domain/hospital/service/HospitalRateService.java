package com.bit.reservation.domain.hospital.service;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.entity.HospitalRate;
import com.bit.reservation.domain.hospital.repository.HospitalRateRepository;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.reservation.service.ReservationService;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.domain.user.service.UserService;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import com.bit.reservation.global.status.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class HospitalRateService {

    private final UserService userService;
    private final HospitalService hospitalService;
    private final ReservationService reservationService;
    private final HospitalRateRepository repository;

    public HospitalRate createRate(HospitalRate rate, Long reservationId) {
        User user = userService.getLoginUser();
        Reservation reservation = reservationService.findClientReservation(reservationId, user.getUserId());

        if (reservation.getHospitalRate() != null) {
            throw new BusinessLogicException(ExceptionCode.RATE_EXISTS);
        }
        //TODO reservation 상태가 done 일때 후기 등록 가능
//        else if (reservation.getReservationStatus() != ReservationStatus.DONE) {
//            throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
//        }
        rate.setUser(user);
        rate.setReservation(reservation);
        rate.setHospital(reservation.getHospital());

        return repository.save(rate);
    }

    public HospitalRate updateRate(HospitalRate rate) {
        HospitalRate hospitalRate = verifiedUserRate(rate.getHospitalRateId());
        Optional.ofNullable(rate.getRating())
                .ifPresent(hospitalRate::setRating);
        Optional.ofNullable(rate.getContent())
                .ifPresent(hospitalRate::setContent);
        return repository.save(hospitalRate);
    }

    public void deleteRate(Long hospitalRateId) {
        repository.delete(verifiedUserRate(hospitalRateId));
    }

    public Page<HospitalRate> findUserRates(Long userId, Pageable pageable) {
        User user = userService.checkJwtAndUser(userId);
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("hospitalRateId").descending());
        return repository.findByUser(user, pageable);
    }

    public Page<HospitalRate> findHospitalRate(Long hospitalId, Pageable pageable) {
        Hospital hospital = hospitalService.getHospital(hospitalId);
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("hospitalRateId").descending());
        return  repository.findByHospital(hospital, pageable);
    }

    public Page<HospitalRate> findAllRates(Pageable pageable, Long hospitalId, Long userId) {
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("hospitalRateId").descending());
        Hospital hospital = hospitalId != null ? hospitalService.getHospital(hospitalId) : null;
        User user = userId != null ? userService.getUser(userId) : null;
        return repository.findByHospitalAndUser(hospital, user, pageable);
    }


    private HospitalRate existsRate(Long hospitalRateId) {
        return repository.findById(hospitalRateId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.RATE_NOT_FOUND));
    }

    private HospitalRate verifiedUserRate(Long hospitalRateId) {
        User user = userService.getLoginUser();
        HospitalRate hospitalRate = existsRate(hospitalRateId);
        if (hospitalRate.getUser() != user) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
        }
        return hospitalRate;
    }

}
