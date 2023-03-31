package com.bit.reservation.domain.hospital.repository;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.entity.HospitalRate;
import com.bit.reservation.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HospitalRateRepository extends JpaRepository<HospitalRate, Long> {

    Page<HospitalRate> findByUser(User user, Pageable pageable);
    Page<HospitalRate> findByHospital(Hospital hospital, Pageable pageable);

    @Query("SELECT r FROM HospitalRate r " +
            "WHERE (:hospital is null or r.hospital = :hospital)" +
            "and (:user is null or r.user = :user)")
    Page<HospitalRate> findByHospitalAndUser(Hospital hospital, User user, Pageable pageable);
}
