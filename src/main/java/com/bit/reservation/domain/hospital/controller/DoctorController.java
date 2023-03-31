package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.DoctorDto;
import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.mapper.DoctorMapper;
import com.bit.reservation.domain.hospital.service.DoctorService;
import com.bit.reservation.global.dto.MultiResponseDto;
import com.bit.reservation.global.dto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper mapper;

    @PostMapping("/hospital/{hospital-id}")
    public ResponseEntity postDoctor(@PathVariable("hospital-id") @Positive Long hospitalId,
                                     @RequestBody @Valid DoctorDto.PostDto postDto) {
        Doctor doctor = doctorService.createDoctor(mapper.postDtoToDoctor(postDto), hospitalId);
        return new ResponseEntity<>(mapper.doctorToSimpleResponseDto(doctor), HttpStatus.CREATED);
    }

    @PatchMapping("/{doctor-id}")
    public ResponseEntity patchDoctor(@PathVariable("doctor-id") @Positive Long doctorId,
                                      @RequestBody @Valid DoctorDto.PatchDto patchDto) {
        patchDto.setDoctorId(doctorId);
        Doctor doctor = doctorService.updateDoctor(mapper.patchDtoToDoctor(patchDto));
        return new ResponseEntity<>(mapper.doctorToSimpleResponseDto(doctor), HttpStatus.OK);
    }

    @DeleteMapping("/{doctor-id}")
    public ResponseEntity deleteDoctor(@PathVariable("doctor-id") @Positive Long doctorId) {
        doctorService.deleteDoctor(doctorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{doctor-id}")
    public ResponseEntity getDoctor(@PathVariable("doctor-id") @Positive Long doctorId) {
        Doctor doctor = doctorService.getDoctor(doctorId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.doctorToResponseDto(doctor)), HttpStatus.OK);
    }

    /* 서비스 관리자용 - OK */
    @GetMapping
    public ResponseEntity getDoctors(@RequestParam @Positive int page,
                                     @RequestParam @Positive int size) {
        Page<Doctor> pages = doctorService.getDoctors(page-1, size);
        List<Doctor> doctors = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.doctorsToResponseDto(doctors), pages), HttpStatus.OK);
    }

    @GetMapping("/hospital/{hospital-id}")
    public ResponseEntity getHospitalDoctors(@PathVariable("hospital-id") @Positive Long hospitalId,
                                             @PageableDefault(page = 1, size = 10) Pageable pageable) {
        Page<Doctor> pages = doctorService.findHospitalDoctors(pageable, hospitalId);
        List<Doctor> doctors = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.doctorsToResponseDto(doctors), pages), HttpStatus.OK);
    }
}
