package com.bit.reservation.domain.reservation.controller;

import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.service.DoctorService;
import com.bit.reservation.domain.reservation.dto.ReservationDto;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.reservation.mapper.ReservationMapper;
import com.bit.reservation.domain.reservation.service.ReservationService;
import com.bit.reservation.global.dto.MultiResponseDto;
import com.bit.reservation.global.dto.SingleResponseDto;
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
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Validated
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper mapper;
    private final DoctorService doctorService;

    @PostMapping("/hospital/{hospital-id}/user/{user-id}")
    public ResponseEntity postReservation(@PathVariable("hospital-id") @Positive Long hospitalId,
                                          @PathVariable("user-id") @Positive Long userId,
                                          @RequestBody @Valid ReservationDto.PostDto postDto) {
        Doctor doctor = postDto.getDoctorName() != null ? doctorService.findDoctorByNameAndHospital(postDto.getDoctorName(), hospitalId) : null;
        Reservation reservation = mapper.postDtoToReservation(postDto, doctor);
        Reservation createdReservation = reservationService.createReservation(reservation, userId, hospitalId);
        return new ResponseEntity<>(mapper.reservationToSimpleResponseDto(createdReservation), HttpStatus.CREATED);
    }

    @PatchMapping("/{reservation-id}")
    public ResponseEntity patchReservation(@PathVariable("reservation-id") @Positive Long reservationId,
                                           @RequestBody @Valid ReservationDto.PatchDto patchDto) {
        patchDto.setReservationId(reservationId);
        Reservation reservation = mapper.patchDtoToReservation(patchDto);
        Reservation updatedReservation = reservationService.updateReservation(reservation, patchDto.getDoctorName());
        return new ResponseEntity<>(mapper.reservationToSimpleResponseDto(updatedReservation), HttpStatus.OK);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity deleteReservation(@PathVariable("reservation-id") @Positive Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{reservation-id}/user/{user-id}")
    public ResponseEntity getClientReservation(@PathVariable("reservation-id") @Positive Long reservationId,
                                               @PathVariable("user-id") @Positive Long userId) {
        Reservation reservation = reservationService.findClientReservation(reservationId, userId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.reservationToClientResponseDto(reservation)), HttpStatus.OK);
    }

    @GetMapping("/user/{user-id}")
    public ResponseEntity getUserReservations(@PathVariable("user-id") @Positive Long userId,
                                              @PageableDefault(page = 1, size = 15) Pageable pageable,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String hospitalName) {
        Page<Reservation> pages = reservationService.findClientReservations(pageable, userId, null, null, status, hospitalName);
        List<Reservation> reservations = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.reservationsToListClientResponseDto(reservations), pages), HttpStatus.OK);
    }

    @GetMapping("/{reservation-id}/hospital/{hospital-id}")
    public ResponseEntity getHospitalReservation(@PathVariable("reservation-id") @Positive Long reservationId,
                                                 @PathVariable("hospital-id") @Positive Long hospitalId) {
        Reservation reservation = reservationService.findHospitalReservation(reservationId, hospitalId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.reservationToHospitalResponseDto(reservation)), HttpStatus.OK);
    }

    @GetMapping("/hospital/{hospital-id}")
    public ResponseEntity getHospitalReservations(@PathVariable("hospital-id") @Positive Long hospitalId,
                                                  @PageableDefault(page = 1, size = 10) Pageable pageable,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String userName) {
        Page<Reservation> pages = reservationService.findHospitalReservations(pageable, hospitalId, null, null, status, userName);
        List<Reservation> reservations = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.reservationsToListHospitalResponseDto(reservations), pages), HttpStatus.OK);
    }

    /*
     * 병원 - 예약 수정
     * 항목 : 병원 메모, 예약 승인/거료
     * */
    @PatchMapping("/{reservation-id}/hospital/{hospital-id}")
    public ResponseEntity patchReservationByHospital(@PathVariable("reservation-id") @Positive Long reservationId,
                                                     @PathVariable("hospital-id") @Positive Long hospitalId,
                                                     @RequestBody @Valid ReservationDto.PatchDtoForHospital patchDto) {
        patchDto.setReservationId(reservationId);
        Reservation reservation = mapper.patchDtoForHospitalToReservation(patchDto);
        Reservation updatedReservation = reservationService.updateReservationByHospital(hospitalId, reservation, patchDto.getDoctorId());
        return new ResponseEntity<>(mapper.reservationToSimpleResponseDto(updatedReservation), HttpStatus.OK);
    }

    /* 서비스 관리자용 */
    @GetMapping
    public ResponseEntity getAllReservations(@RequestParam @Positive int page,
                                             @RequestParam @Positive int size,
                                             @RequestParam(required = false) String dateTime,
                                             @RequestParam(required = false) String subject,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) Long hospitalId,
                                             @RequestParam(required = false) Long userId) {
        Page<Reservation> pages = reservationService.findAllReservations(page, size, dateTime, subject, status, hospitalId, userId);
        List<Reservation> reservations = pages.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.reservationsToAdminListHospitalResponseDto(reservations), pages), HttpStatus.OK);
    }

}
