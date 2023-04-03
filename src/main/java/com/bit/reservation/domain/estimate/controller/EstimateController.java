package com.bit.reservation.domain.estimate.controller;

import com.bit.reservation.domain.estimate.dto.EstimateDto;
import com.bit.reservation.domain.estimate.entity.Estimate;
import com.bit.reservation.domain.estimate.mapper.EstimateMapper;
import com.bit.reservation.domain.estimate.service.EstimateService;
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
@RequestMapping("/api/estimate")
@RequiredArgsConstructor
@Validated
public class EstimateController {

    private final EstimateMapper mapper;
    private final EstimateService service;

    @PostMapping("/v1/hospital/{hospital-id}")
    public ResponseEntity postEstimate(@PathVariable("hospital-id") @Positive Long hospitalId,
                                       @RequestBody @Valid EstimateDto.PostToIdDto postDto) {
        Estimate estimate = service.createEstimateToId(postDto, hospitalId);
        return new ResponseEntity<>(mapper.estimateToSimpleResponseDto(estimate), HttpStatus.CREATED);
    }

    @PostMapping("/v2/hospital/{hospital-id}")
    public ResponseEntity postEstimate(@PathVariable("hospital-id") @Positive Long hospitalId,
                                       @RequestBody @Valid EstimateDto.PostToDateDto postDto) {
        Estimate estimate = service.createEstimateToDate(postDto, hospitalId);
        return new ResponseEntity<>(mapper.estimateToSimpleResponseDto(estimate), HttpStatus.CREATED);
    }

    @GetMapping("/{estimate-id}")
    public ResponseEntity getEstimate(@PathVariable("estimate-id") @Positive Long estimateId) {
        Estimate estimate = service.findEstimate(estimateId);
        return new ResponseEntity<>(mapper.estimateToResponseDto(estimate), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getEstimates(@PageableDefault(page = 1, size = 10) Pageable pageable,
                                       @RequestParam(required = false) String estimateDate,
                                       @RequestParam(required = false) @Positive Long hospitalId) {
        Page<Estimate> pages = service.findEstimates(pageable, estimateDate, hospitalId);
        List<Estimate> estimates = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.estimateToListResponseDto(estimates), pages), HttpStatus.OK);
    }

    @DeleteMapping("/{estimate-id}")
    public ResponseEntity deleteEstimate(@PathVariable("estimate-id") @Positive Long estimateId) {
        service.deleteEstimate(estimateId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //병원이 자신의 견적 목록 조회
    @GetMapping("/hospital/{hospital-id}")
    public ResponseEntity getHospitalEstimates(@PathVariable("hospital-id") @Positive Long hospitalId,
                                               @PageableDefault(page = 1, size = 10) Pageable pageable,
                                               @RequestParam(required = false) String estimateDate) {
        Page<Estimate> pages = service.findHospitalEstimates(pageable, estimateDate, hospitalId);
        List<Estimate> estimates = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.estimateToListResponseDto(estimates), pages), HttpStatus.OK);
    }

    //병원이 자신의 견적 개별 조회
    @GetMapping("/{estimate-id}/hospital/{hospital-id}")
    public ResponseEntity getHospitalEstimate(@PathVariable("estimate-id") @Positive Long estimateId,
                                      @PathVariable("hospital-id") @Positive Long hospitalId) {
        Estimate estimate = service.findHospitalEstimate(estimateId, hospitalId);
        return new ResponseEntity<>(mapper.estimateToResponseDto(estimate), HttpStatus.OK);
    }
}
