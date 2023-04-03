package com.bit.reservation.domain.reservation.entity;

import com.bit.reservation.domain.estimate.entity.Estimate;
import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.entity.HospitalRate;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.global.audit.Auditable;
import com.bit.reservation.global.status.ReservationStatus;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reservation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column(nullable = false)
    private String dateTime;

    @Column
    private String hospitalMemo; //병원만 작성

    @Column(nullable = false)
    private String subject;

    @Column
    private String clientRequest; //고객만 작성

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    private boolean quotation;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "HOSPITAL_ID")
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "DOCTOR_ID")
    private Doctor doctor;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    @JoinColumn(name = "HOSPITAL_RATE_ID")
    private HospitalRate hospitalRate;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> quitHospitalInfo = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> quitClientInfo = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ESTIMATE_ID")
    private Estimate estimate;

}
