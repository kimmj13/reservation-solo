package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.HospitalNoticeDto;
import com.bit.reservation.domain.hospital.entity.HospitalNotice;
import com.bit.reservation.domain.hospital.mapper.HospitalNoticeMapper;
import com.bit.reservation.domain.hospital.service.HospitalNoticeService;
import com.bit.reservation.global.dto.MultiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/hospital-notice")
@RequiredArgsConstructor
public class HospitalNoticeController {

    private final HospitalNoticeService noticeService;
    private final HospitalNoticeMapper mapper;

    @PostMapping
    public ResponseEntity postNotice(@RequestBody @Valid HospitalNoticeDto.BasicDto basicDto) {
        HospitalNotice notice = noticeService.createNotice(mapper.basicDtoToNotice(basicDto));
        return new ResponseEntity<>(mapper.noticeToSimpleResponseDto(notice), HttpStatus.CREATED);
    }

    @PatchMapping("/{notice-id}")
    public ResponseEntity patchNotice(@PathVariable("notice-id") @Positive Long noticeId,
                                      @RequestBody @Valid HospitalNoticeDto.BasicDto basicDto) {
        basicDto.setHospitalNoticeId(noticeId);
        HospitalNotice notice = noticeService.updateNotice(mapper.basicDtoToNotice(basicDto));
        return new ResponseEntity<>(mapper.noticeToSimpleResponseDto(notice), HttpStatus.OK);
    }

    @GetMapping("/{notice-id}")
    public ResponseEntity getNotice(@PathVariable("notice-id") @Positive Long noticeId) {
        HospitalNotice notice = noticeService.findNotice(noticeId);
        return new ResponseEntity<>(mapper.noticeToBasicDto(notice), HttpStatus.OK);
    }

    @GetMapping("/hospital/{hospital-id}")
    public ResponseEntity getNotices(@PageableDefault(page = 1, size = 10) Pageable pageable,
                                     @PathVariable("hospital-id") @Positive Long hospitalId) {
        Page<HospitalNotice> pages = noticeService.findNotices(pageable, hospitalId);
        List<HospitalNotice> notices = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.noticesToSimpleDto(notices), pages), HttpStatus.OK);
    }

    @DeleteMapping("/{notice-id}")
    public ResponseEntity deleteNotice(@PathVariable("notice-id") @Positive Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
