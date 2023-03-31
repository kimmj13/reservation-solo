package com.bit.reservation.domain.hospital.mapper;

import com.bit.reservation.domain.hospital.dto.DoctorDto;
import com.bit.reservation.domain.hospital.entity.Doctor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    Doctor postDtoToDoctor(DoctorDto.PostDto postDto);

    Doctor patchDtoToDoctor(DoctorDto.PatchDto patchDto);

    DoctorDto.ResponseDto doctorToResponseDto(Doctor doctor);

    List<DoctorDto.ResponseDto> doctorsToResponseDto(List<Doctor> doctors);

    DoctorDto.SimpleResponseDto doctorToSimpleResponseDto(Doctor doctor);
}
