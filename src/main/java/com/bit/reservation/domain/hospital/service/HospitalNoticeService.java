package com.bit.reservation.domain.hospital.service;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.entity.HospitalNotice;
import com.bit.reservation.domain.hospital.repository.HospitalNoticeRepository;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class HospitalNoticeService {

    private final HospitalNoticeRepository hospitalNoticeRepository;
    private final HospitalService hospitalService;

    public HospitalNotice createNotice(HospitalNotice hospitalNotice) {
        hospitalNotice.setHospital(hospitalService.checkHospital());
        return hospitalNoticeRepository.save(hospitalNotice);
    }

    public HospitalNotice updateNotice(HospitalNotice hospitalNotice) {
        HospitalNotice findNotice = existsNotice(hospitalNotice.getHospitalNoticeId());
        hospitalService.checkJwtAndHospital(findNotice.getHospital().getHospitalId());
        findNotice.setTitle(hospitalNotice.getTitle());
        findNotice.setContent(hospitalNotice.getContent());
        return hospitalNoticeRepository.save(findNotice);
    }

    public HospitalNotice findNotice(Long hospitalNoticeId) {
        return existsNotice(hospitalNoticeId);
    }

    public Page<HospitalNotice> findNotices(Pageable pageable, Long hospitalId) {
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by("hospitalNoticeId").descending());
        Hospital hospital = hospitalService.getHospital(hospitalId);
        return hospitalNoticeRepository.findAllByHospital(pageable, hospital);
    }

    public void deleteNotice(Long noticeId) {
        HospitalNotice findNotice = existsNotice(noticeId);
        hospitalService.checkJwtAndHospital(findNotice.getHospital().getHospitalId());
        hospitalNoticeRepository.delete(findNotice);
    }

    private HospitalNotice existsNotice(Long hospitalNoticeId) {
        return hospitalNoticeRepository.findById(hospitalNoticeId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOTICE_NOT_FOUND));
    }
}
