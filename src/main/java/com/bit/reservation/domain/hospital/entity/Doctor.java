package com.bit.reservation.domain.hospital.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @Column(nullable = false)
    private String name;

    @Column
    private String school;

    @Column
    private String career;

    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> medicalSubject;

    //TODO: 사진 올리는 로직 구현할지 생각해보기..
    @Column
    private String picture;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "HOSPITAL_ID")
    private Hospital hospital;

}
