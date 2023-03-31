package com.bit.reservation.domain.hospital.repository;

import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Page<Doctor> findAllByHospital(Pageable pageable, Hospital hospital);

    Optional<Doctor> findByName(String name);

    Optional<Doctor> findByNameAndHospital(String name, Hospital hospital);
}
