package com.bit.reservation.domain.estimate.repository;

import com.bit.reservation.domain.estimate.entity.Estimate;
import com.bit.reservation.domain.hospital.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {

    @Query("SELECT e FROM Estimate e " +
            "WHERE (:estimateDate is null or e.estimateDate = :estimateDate)" +
            "and (:hospital is null or e.hospital = :hospital)")
    Page<Estimate> findAllByEstimateDateAndHospital(String estimateDate, Hospital hospital, Pageable pageable);

    Optional<Estimate> findByHospitalAndEstimateId(Hospital hospital, Long estimateId);
}
