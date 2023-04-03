package com.bit.reservation.domain.estimate.mapper;

import com.bit.reservation.domain.estimate.dto.EstimateDto;
import com.bit.reservation.domain.estimate.entity.Estimate;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.reservation.entity.Reservation;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EstimateMapper {

    EstimateDto.SimpleResponseDto estimateToSimpleResponseDto(Estimate estimateDto);

    default EstimateDto.ResponseDto estimateToResponseDto(Estimate estimate) {
        if ( estimate == null ) {
            return null;
        }

        EstimateDto.ResponseDto.ResponseDtoBuilder responseDto = EstimateDto.ResponseDto.builder();
        List<EstimateDto.ReservationInfo> reservationInfos = new ArrayList<>();

        responseDto.estimateId( estimate.getEstimateId() );
        responseDto.estimateDate( estimate.getEstimateDate() );
        responseDto.numberOfReservations( estimate.getNumberOfReservations() );
        responseDto.totalAmount( estimate.getTotalAmount() );

        for (Reservation reservation : estimate.getReservations()) {
            EstimateDto.ReservationInfo reservationInfo = EstimateDto.ReservationInfo.builder()
                    .reservationId(reservation.getReservationId())
                    .dateTime(reservation.getDateTime())
                    .subject(reservation.getSubject())
                    .reservationStatus(reservation.getReservationStatus())
                    .build();

            reservationInfos.add(reservationInfo);
        }
        Hospital hospital = estimate.getHospital();
        EstimateDto.HospitalInfo hospitalInfo = EstimateDto.HospitalInfo.builder()
                .hospitalId(hospital.getHospitalId())
                .hospitalName(hospital.getName())
                .hospitalStatus(hospital.getHospitalStatus())
                .build();
        responseDto.reservationInfo(reservationInfos);
        responseDto.hospitalInfo(hospitalInfo);

        return responseDto.build();
    }

    default List<EstimateDto.ListResponseDto> estimateToListResponseDto(List<Estimate> estimates) {
        if ( estimates == null ) {
            return null;
        }

        List<EstimateDto.ListResponseDto> list = new ArrayList<>();

        for (Estimate estimate : estimates) {
            if ( estimate == null ) {
                return null;
            }

            EstimateDto.ListResponseDto.ListResponseDtoBuilder listResponseDto = EstimateDto.ListResponseDto.builder();
            Hospital hospital = estimate.getHospital();
            listResponseDto.estimateId( estimate.getEstimateId() );
            listResponseDto.estimateDate( estimate.getEstimateDate() );
            listResponseDto.totalAmount( estimate.getTotalAmount() );
            EstimateDto.HospitalInfo hospitalInfo = EstimateDto.HospitalInfo.builder()
                    .hospitalId(hospital.getHospitalId())
                    .hospitalName(hospital.getName())
                    .hospitalStatus(hospital.getHospitalStatus())
                    .build();
            listResponseDto.hospitalInfo(hospitalInfo)
            .build();

            list.add(listResponseDto.build());
        }
        return list;
    }

}
