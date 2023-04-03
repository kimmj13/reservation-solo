package com.bit.reservation.domain.reservation.mapper;

import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.reservation.dto.ReservationDto;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "doctor", source = "doctor")
    Reservation postDtoToReservation(ReservationDto.PostDto postDto, Doctor doctor);

    Reservation patchDtoToReservation(ReservationDto.PatchDto patchDto);

    @Mapping(target = "doctorName", expression = "java(reservation.getHospital() != null && reservation.getDoctor() != null ? reservation.getDoctor().getName() : null)")
    @Mapping(target = "hospitalInfo", expression = "java(settingHospitalInfo(reservation))")
    ReservationDto.ClientResponseDto reservationToClientResponseDto(Reservation reservation);

    @Mapping(target = "doctorId", expression = "java(reservation.getDoctor() != null ? reservation.getDoctor().getDoctorId() : null)")
    @Mapping(target = "doctorName", expression = "java(reservation.getDoctor() != null ? reservation.getDoctor().getName() : null)")
    @Mapping(target = "clientInfo", expression = "java(settingClientInfo(reservation))")
    ReservationDto.HospitalResponseDto reservationToHospitalResponseDto(Reservation reservation);

    default List<ReservationDto.ListClientResponseDto> reservationsToListClientResponseDto(List<Reservation> reservations) {
        if ( reservations == null ) {
            return null;
        }

        List<ReservationDto.ListClientResponseDto> list = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                return null;
            }

            list.add(ReservationDto.ListClientResponseDto.builder()
                    .reservationId(reservation.getReservationId())
                    .dateTime(reservation.getDateTime())
                    .reservationStatus(reservation.getReservationStatus())
                    .hospitalInfo(settingHospitalInfo(reservation))
                    .build());

        }
        return list;
    }

    default List<ReservationDto.ListHospitalResponseDto> reservationsToListHospitalResponseDto(List<Reservation> reservations) {
        if ( reservations == null ) {
            return null;
        }

        List<ReservationDto.ListHospitalResponseDto> list = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                return null;
            }
            Doctor doctor = reservation.getDoctor();

            list.add(ReservationDto.ListHospitalResponseDto.builder()
                    .doctorId(doctor != null ? doctor.getDoctorId() : null)
                    .doctorName(doctor != null ? doctor.getName() : null)
                    .reservationId(reservation.getReservationId())
                    .dateTime(reservation.getDateTime())
                    .medicalSubject(reservation.getSubject())
                    .reservationStatus(reservation.getReservationStatus())
                    .clientInfo(settingClientInfo(reservation))
                    .build());
        }
        return list;
    }

    default List<ReservationDto.AdminListHospitalResponseDto> reservationsToAdminListHospitalResponseDto(List<Reservation> reservations) {
        if ( reservations == null ) {
            return null;
        }

        List<ReservationDto.AdminListHospitalResponseDto> list = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                return null;
            }
            Doctor doctor = reservation.getDoctor();

            list.add(ReservationDto.AdminListHospitalResponseDto.builder()
                    .doctorId(doctor != null ? doctor.getDoctorId() : null)
                    .doctorName(doctor != null ? doctor.getName() : null)
                    .reservationId(reservation.getReservationId())
                    .dateTime(reservation.getDateTime())
                    .medicalSubject(reservation.getSubject())
                    .reservationStatus(reservation.getReservationStatus())
                    .clientInfo(settingClientInfo(reservation))
                    .hospitalInfo(settingHospitalInfo(reservation))
                    .quotation(reservation.isQuotation())
                    .build());
        }
        return list;
    }

    ReservationDto.SimpleResponseDto reservationToSimpleResponseDto(Reservation reservation);

    Reservation patchDtoForHospitalToReservation(ReservationDto.PatchDtoForHospital patchDto);

    default ReservationDto.ClientInfo settingClientInfo(Reservation reservation) {
        User user = reservation.getUser();
        return user != null ? new ReservationDto.ClientInfo(
                user.getUserId(),
                user.getUserName(),
                user.getAge(),
                user.getProfileImage().getImage()) :
                new ReservationDto.ClientInfo(
                        null, reservation.getQuitClientInfo().get(0),
                        Integer.valueOf(reservation.getQuitClientInfo().get(1)),
                        reservation.getQuitClientInfo().get(2)
                );
    }

    default ReservationDto.HospitalInfo settingHospitalInfo(Reservation reservation) {
        Hospital hospital = reservation.getHospital();
        return hospital != null ? new ReservationDto.HospitalInfo(
                hospital.getHospitalId(),
                hospital.getName(),
                hospital.getTelNum()) :
                new ReservationDto.HospitalInfo(
                        null,
                        reservation.getQuitHospitalInfo().get(0),
                        reservation.getQuitHospitalInfo().get(1)
                );
    }
}
