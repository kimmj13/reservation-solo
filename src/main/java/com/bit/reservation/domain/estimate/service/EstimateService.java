package com.bit.reservation.domain.estimate.service;

import com.bit.reservation.domain.estimate.dto.EstimateDto;
import com.bit.reservation.domain.estimate.entity.Estimate;
import com.bit.reservation.domain.estimate.repository.EstimateRepository;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.service.HospitalService;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.reservation.service.ReservationService;
import com.bit.reservation.domain.user.service.UserService;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import com.bit.reservation.global.status.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EstimateService {

    private final EstimateRepository repository;
    private final HospitalService hospitalService;
    private final ReservationService reservationService;
    private final UserService userService;

    public Estimate createEstimateToId(EstimateDto.PostToIdDto postDto, Long hospitalId) {
        Hospital hospital = hospitalService.existsHospital(hospitalId);
        Estimate estimate = new Estimate();
        postDto.getReservationIds()
                .forEach(reservationId -> {
                    Reservation reservation = reservationService.findHospitalReservation(reservationId, hospitalId);
                    if (reservation.getEstimate() != null) {
                        throw new BusinessLogicException(ExceptionCode.ESTIMATE_EXISTS);
                    }
                    if (reservation.getHospital().getHospitalId() != hospitalId) {
                        throw new BusinessLogicException(ExceptionCode.HOSPITAL_NOT_FOUND);
                    }
                    estimate.addReservations(reservation);
                    reservation.setEstimate(estimate);
                });
        estimate.setHospital(hospital);
        int size = postDto.getReservationIds().size();
        estimate.setNumberOfReservations(size);
        estimate.setTotalAmount(hospital.getHospitalLevel().getPricePerCase() * size);
        return repository.save(estimate);
    }

    public Estimate createEstimateToDate(EstimateDto.PostToDateDto postDto, Long hospitalId) {
        String estimateDate = postDto.getEstimateDate();
        Hospital hospital = hospitalService.existsHospital(hospitalId);
        Estimate estimate = new Estimate();

        hospital.getReservations()
                .forEach(reservation -> {
                    if (reservation.getEstimate() == null && reservation.getDateTime().contains(estimateDate) &&
                            (reservation.getReservationStatus() == ReservationStatus.DONE || reservation.getReservationStatus() == ReservationStatus.APPROVAL)) {
                        estimate.addReservations(reservation);
                        reservation.setEstimate(estimate);
                        estimate.setNumberOfReservations(estimate.getNumberOfReservations() + 1);
                        estimate.setTotalAmount(estimate.getTotalAmount() + hospital.getHospitalLevel().getPricePerCase());
                    }
                });
        estimate.setHospital(hospital);
        estimate.setEstimateDate(estimateDate);
        return repository.save(estimate);
    }

    public Estimate findEstimate(Long estimateId) {
        checkAdmin();
        return existsEstimate(estimateId);
    }

    public Estimate findHospitalEstimate(Long estimateId, Long hospitalId) {
        Hospital hospital = hospitalService.checkJwtAndHospital(hospitalId);
        return repository.findByHospitalAndEstimateId(hospital, estimateId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ESTIMATE_NOT_FOUND));
    }

    public Page<Estimate> findEstimates(Pageable pageable, String estimateDate, Long hospitalId) {
        checkAdmin();
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("estimateId").descending());
        return repository.findAllByEstimateDateAndHospital(estimateDate, hospitalId == null ? null : hospitalService.existsHospital(hospitalId), pageable);
    }

    public Page<Estimate> findHospitalEstimates(Pageable pageable, String estimateDate, Long hospitalId) {
        hospitalService.checkJwtAndHospital(hospitalId);
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("estimateId").descending());
        return repository.findAllByEstimateDateAndHospital(estimateDate, hospitalId == null ? null : hospitalService.existsHospital(hospitalId), pageable);
    }

    public void deleteEstimate(Long estimateId) {
        Estimate estimate = existsEstimate(estimateId);
        checkAdmin();
        repository.delete(estimate);
    }

    private Estimate existsEstimate(Long estimateId) {
        return repository.findById(estimateId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ESTIMATE_NOT_FOUND));
    }

    private void checkAdmin() {
        if (!userService.isServiceAdmin()) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_FORBIDDEN);
        }
    }
}
