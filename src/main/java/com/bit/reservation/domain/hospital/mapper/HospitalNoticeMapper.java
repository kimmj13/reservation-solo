package com.bit.reservation.domain.hospital.mapper;

import com.bit.reservation.domain.hospital.dto.HospitalNoticeDto;
import com.bit.reservation.domain.hospital.entity.HospitalNotice;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HospitalNoticeMapper {

    HospitalNotice basicDtoToNotice(HospitalNoticeDto.BasicDto basicDto);

    HospitalNoticeDto.BasicDto noticeToBasicDto(HospitalNotice hospitalNotice);

    HospitalNoticeDto.SimpleResponseDto noticeToSimpleResponseDto(HospitalNotice hospitalNotice);

    List<HospitalNoticeDto.SimpleDto> noticesToSimpleDto(List<HospitalNotice> hospitalNotice);


}
