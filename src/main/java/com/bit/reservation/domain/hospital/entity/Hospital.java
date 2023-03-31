package com.bit.reservation.domain.hospital.entity;

import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.global.audit.Auditable;
import com.bit.reservation.global.status.HospitalStatus;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Hospital extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hospitalId;

    @Column(nullable = false, updatable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String telNum;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> medicalSubject;

    @Column
    private String openingTime;

    @Column
    private String closingTime;

    @Column
    private String breakStartTime;

    @Column
    private String breakEndTime;

    @Column
    private String intro;

    @Column
    private String hospitalPicture;

    @Enumerated(EnumType.STRING)
    private HospitalStatus hospitalStatus;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    private Integer views;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL)
    private List<Doctor> doctors = new ArrayList<>();

    @OneToMany(mappedBy = "hospital")
    private List<Reservation> reservations = new ArrayList<>();

    public void addDoctor(Doctor doctor) {
        doctor.setHospital(this);
        doctors.add(doctor);
    }

}
