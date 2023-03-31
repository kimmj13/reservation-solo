package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.HospitalRateDto;
import com.bit.reservation.domain.hospital.entity.HospitalRate;
import com.bit.reservation.domain.hospital.mapper.HospitalRateMapper;
import com.bit.reservation.domain.hospital.service.HospitalRateService;
import com.bit.reservation.global.dto.MultiResponseDto;
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
@RequestMapping("/api/hospital-rate")
@RequiredArgsConstructor
@Validated
public class HospitalRateController {

    private final HospitalRateService service;
    private final HospitalRateMapper mapper;

    @PostMapping("/reservation/{reservation-id}")
    public ResponseEntity postRate(@PathVariable("reservation-id") @Positive Long reservationId,
                                   @RequestBody @Valid HospitalRateDto.PostDto postDto) {
        HospitalRate hospitalRate = mapper.postDtoToRate(postDto);
        HospitalRate createdRate = service.createRate(hospitalRate, reservationId);
        return new ResponseEntity<>(mapper.rateToSimpleResponseDto(createdRate), HttpStatus.CREATED);
    }

    @PatchMapping("/{hospital-rate-id}")
    public ResponseEntity patchRate(@RequestBody @Valid HospitalRateDto.BasicDto patchDto,
                                    @PathVariable("hospital-rate-id") @Positive Long rateId) {
        patchDto.setHospitalRateId(rateId);
        HospitalRate hospitalRate = mapper.patchDtoToRate(patchDto);
        HospitalRate updatedRate = service.updateRate(hospitalRate);
        return new ResponseEntity<>(mapper.rateToSimpleResponseDto(updatedRate), HttpStatus.OK);
    }

    // 유저의 모든 후기 조회 (본인만 가능)
    @GetMapping("/user/{user-id}")
    public ResponseEntity getUserRate(@PathVariable("user-id") @Positive Long userId,
                                      @PageableDefault(page = 1, size = 10) Pageable pageable) {
        Page<HospitalRate> pages = service.findUserRates(userId, pageable);
        List<HospitalRate> rates = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.ratesToBasicDto(rates), pages), HttpStatus.OK);
    }

    @GetMapping("/hospital/{hospital-id}")
    public ResponseEntity getHospitalRate(@PathVariable("hospital-id") @Positive Long hospitalId,
                                          @PageableDefault(page = 1, size = 10) Pageable pageable) {
        Page<HospitalRate> pages = service.findHospitalRate(hospitalId, pageable);
        List<HospitalRate> rates = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.ratesToResponseDto(rates), pages), HttpStatus.OK);
    }

    @DeleteMapping("/{hospital-rate-id}")
    public ResponseEntity deleteRate(@PathVariable("hospital-rate-id") @Positive Long rateId) {
        service.deleteRate(rateId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /* 서비스 관리자용 */
    @GetMapping
    public ResponseEntity getAllRate(@PageableDefault(page = 1, size = 10) Pageable pageable,
                                     @RequestParam(required = false) Long hospitalId,
                                     @RequestParam(required = false) Long userId) {
        Page<HospitalRate> pages = service.findAllRates(pageable, hospitalId, userId);
        List<HospitalRate> rates = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.ratesToResponseDto(rates), pages), HttpStatus.OK);
    }
}
