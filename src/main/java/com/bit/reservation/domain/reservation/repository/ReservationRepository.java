package com.bit.reservation.domain.reservation.repository;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.global.status.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
            "WHERE (:dateTime is null or r.dateTime = :dateTime) " +
            "and (:subject is null or r.subject = :subject)" +
            "and (:reservationStatus is null or r.reservationStatus = :reservationStatus)" +
            "and (:hospital is null or r.hospital = :hospital)" +
            "and (:user is null or r.user = :user)")
    Page<Reservation> findByDateTimeAndSubjectAndReservationStatusAndHospitalAndUser(String dateTime, String subject, ReservationStatus reservationStatus, Hospital hospital, User user, Pageable pageable);

    List<Reservation> findAllByReservationStatus(ReservationStatus reservationStatus);

}
