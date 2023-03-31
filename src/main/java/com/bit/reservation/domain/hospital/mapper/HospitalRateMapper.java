package com.bit.reservation.domain.hospital.mapper;

import com.bit.reservation.domain.hospital.dto.HospitalRateDto;
import com.bit.reservation.domain.hospital.entity.HospitalRate;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface HospitalRateMapper {

    HospitalRate postDtoToRate(HospitalRateDto.PostDto postDto);

    HospitalRate patchDtoToRate(HospitalRateDto.BasicDto patchDto);

    HospitalRateDto.SimpleResponseDto rateToSimpleResponseDto(HospitalRate hospitalRate);

    List<HospitalRateDto.BasicDto> ratesToBasicDto(List<HospitalRate> hospitalRate);

    default List<HospitalRateDto.ResponseDto> ratesToResponseDto(List<HospitalRate> hospitalRate) {
        if (hospitalRate == null) {
            return null;
        }

        List<HospitalRateDto.ResponseDto> list = new ArrayList<HospitalRateDto.ResponseDto>(hospitalRate.size());
        for (HospitalRate hospitalRate1 : hospitalRate) {
            list.add(hospitalRateToResponseDto(hospitalRate1));
        }
        return list;
    }

    private HospitalRateDto.ResponseDto hospitalRateToResponseDto(HospitalRate hospitalRate) {
        if ( hospitalRate == null ) {
            return null;
        }

        HospitalRateDto.ResponseDto responseDto = new HospitalRateDto.ResponseDto();
        User user = hospitalRate.getUser();

        responseDto.setHospitalRateId(hospitalRate.getHospitalRateId());
        responseDto.setRating(hospitalRate.getRating());
        responseDto.setContent(hospitalRate.getContent());
        if (user != null) {
            responseDto.setUserId(user.getUserId());
            responseDto.setUserName(user.getUserName());
            responseDto.setUserProfileImage(user.getProfileImage().getImage());
        } else {
            List<String> quitClientInfo = hospitalRate.getReservation().getQuitClientInfo();
            responseDto.setUserId(null);
            responseDto.setUserName(quitClientInfo.get(0));
            responseDto.setUserProfileImage(quitClientInfo.get(2));
        }

        return responseDto;
    }

}
