package com.bit.reservation.domain.hospital.repository;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.global.status.HospitalStatus;
import com.bit.reservation.global.status.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    @Query("SELECT h FROM Hospital h WHERE (:hospitalStatus is null or h.hospitalStatus = :hospitalStatus)" +
            "and h.hospitalStatus <> 'WAITING' and h.hospitalStatus <> 'QUIT'")
    Page<Hospital> findAllByHospitalStatus(Pageable pageable, HospitalStatus hospitalStatus);

    // 서비스 관리자용
    @Query("SELECT h FROM Hospital h WHERE (:hospitalStatus is null or h.hospitalStatus = :hospitalStatus)")
    Page<Hospital> findByHospitalStatus(Pageable pageable, HospitalStatus hospitalStatus);
    Optional<Hospital> findByEmail(String email);

}
