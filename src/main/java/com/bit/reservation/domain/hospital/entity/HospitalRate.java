package com.bit.reservation.domain.hospital.entity;

import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.user.entity.User;
import lombok.*;

import javax.persistence.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class HospitalRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hospitalRateId;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private String content;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "HOSPITAL_ID")
    private Hospital hospital;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "RESERVATION_ID")
    private Reservation reservation;
}
