package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.HospitalDto;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.mapper.HospitalMapper;
import com.bit.reservation.domain.hospital.service.HospitalService;
import com.bit.reservation.global.dto.MultiResponseDto;
import com.bit.reservation.global.dto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
@Validated
public class HospitalController {

    private final HospitalService hospitalService;
    private final HospitalMapper mapper;

    @PostMapping
    public ResponseEntity postHospital(@RequestBody @Valid HospitalDto.PostDto postDto) {
        Hospital hospital = hospitalService.createHospital(mapper.postDtoToHospital(postDto));
        return new ResponseEntity<>(mapper.hospitalToSimpleResponseDto(hospital), HttpStatus.CREATED);
    }

    @PatchMapping("/{hospital-id}")
    public ResponseEntity patchHospital(@PathVariable("hospital-id") @Positive Long hospitalId,
                                      @RequestBody @Valid HospitalDto.PatchDto patchDto) {
        patchDto.setHospitalId(hospitalId);
        Hospital hospital = hospitalService.updateHospital(mapper.patchDtoToHospital(patchDto));
        return new ResponseEntity<>(mapper.hospitalToSimpleResponseDto(hospital), HttpStatus.OK);
    }

    @DeleteMapping("/{hospital-id}")
    public ResponseEntity deleteHospital(@PathVariable("hospital-id") @Positive Long hospitalId) {
        hospitalService.deleteHospital(hospitalId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{hospital-id}")
    public ResponseEntity getHospital(@PathVariable("hospital-id") @Positive Long hospitalId) {
        Hospital hospital = hospitalService.getHospital(hospitalId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.hospitalToResponseDto(hospital)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getHospitals(@PageableDefault(page = 1, size = 10, sort = "hospitalId", direction = Sort.Direction.DESC) Pageable pageable,
                                       @RequestParam(required = false) String status) {
        Page<Hospital> pages = hospitalService.getHospitals(pageable, status);
        List<Hospital> hospitals = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.hospitalsToResponseDto(hospitals), pages), HttpStatus.OK);
    }

    /* 서비스 관리자용 - OK */
    @PutMapping("/{hospital-id}/{status}")
    public ResponseEntity putHospitalRoles(@PathVariable("hospital-id") @Positive Long hospitalId,
                                           @PathVariable("status") String status) {
        hospitalService.updateHospitalRoles(hospitalId, status);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
