package com.bit.reservation.domain.hospital.service;

import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.repository.DoctorRepository;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final HospitalService hospitalService;

    public Doctor createDoctor(Doctor doctor, Long hospitalId) {
        Hospital hospital = hospitalService.checkJwtAndUser(hospitalId);

        // 해당 병원에 동일한 이름 불가
        boolean match = hospital.getDoctors().stream().anyMatch(dc -> dc.getName().equals(doctor.getName()));
        if (match) {
            throw new BusinessLogicException(ExceptionCode.DOCTOR_EXISTS);
        }

        hospital.addDoctor(doctor);
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Doctor doctor) {
        Doctor findDoctor = existsDoctor(doctor.getDoctorId());

        Optional.ofNullable(doctor.getName())
                        .ifPresent(findDoctor::setName);
        Optional.ofNullable(doctor.getSchool())
                        .ifPresent(findDoctor::setSchool);
        Optional.ofNullable(doctor.getCareer())
                        .ifPresent(findDoctor::setCareer);
        Optional.ofNullable(doctor.getMedicalSubject())
                        .ifPresent(findDoctor::setMedicalSubject);

        return doctorRepository.save(findDoctor);
    }

    public void deleteDoctor(long doctorId) {
        doctorRepository.deleteById(doctorId);
    }

    public Doctor getDoctor(long doctorId) {
        return existsDoctor(doctorId);
    }

    public Page<Doctor> getDoctors(int page, int size) {
        return doctorRepository.findAll(PageRequest.of(page, size, Sort.by("doctorId").descending()));
    }

    public Page<Doctor> findHospitalDoctors(Pageable pageable, Long hospitalId) {
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        Hospital hospital = hospitalService.getHospital(hospitalId);
        return doctorRepository.findAllByHospital(pageable, hospital);
    }

    private Doctor existsDoctor(long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.DOCTOR_NOT_FOUND));
    }

    public Doctor findDoctorByName(String name) {
        return doctorRepository.findByName(name)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.DOCTOR_NOT_FOUND));
    }

    public Doctor findDoctorByNameAndHospital(String name, Long hospitalId) {
        return doctorRepository.findByNameAndHospital(name, hospitalService.getHospital(hospitalId))
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.DOCTOR_NOT_FOUND));
    }
}
