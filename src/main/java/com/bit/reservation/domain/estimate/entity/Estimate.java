package com.bit.reservation.domain.estimate.entity;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.global.audit.Auditable;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estimate extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long estimateId;

    @Column
    private String estimateDate;

    @Column
    private int numberOfReservations;

    @Column
    private int totalAmount;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Reservation> reservations = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "HOSPITAL_ID")
    private Hospital hospital;

    public void addReservations(Reservation reservation) {
        reservations.add(reservation);
        reservation.setEstimate(this);
    }
}
