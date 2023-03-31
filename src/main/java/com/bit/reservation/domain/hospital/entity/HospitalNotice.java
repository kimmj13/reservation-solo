package com.bit.reservation.domain.hospital.entity;

import lombok.*;

import javax.persistence.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class HospitalNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hospitalNoticeId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "HOSPITAL_ID")
    private Hospital hospital;

}
