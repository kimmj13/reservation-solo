package com.bit.reservation.domain.hospital.repository;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.entity.HospitalNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalNoticeRepository extends JpaRepository<HospitalNotice, Long> {

    Page<HospitalNotice> findAllByHospital(Pageable pageable, Hospital hospital);
}
