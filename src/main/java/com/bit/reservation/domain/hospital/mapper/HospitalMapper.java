package com.bit.reservation.domain.hospital.mapper;

import com.bit.reservation.domain.hospital.dto.HospitalDto;
import com.bit.reservation.domain.hospital.entity.Hospital;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface HospitalMapper {

    Hospital postDtoToHospital(HospitalDto.PostDto postDto);

    Hospital patchDtoToHospital(HospitalDto.PatchDto patchDto);

    HospitalDto.ResponseDto hospitalToResponseDto(Hospital hospital);

    List<HospitalDto.ResponseDto> hospitalsToResponseDto(List<Hospital> hospitals);

    HospitalDto.SimpleResponseDto hospitalToSimpleResponseDto(Hospital hospital);
}
