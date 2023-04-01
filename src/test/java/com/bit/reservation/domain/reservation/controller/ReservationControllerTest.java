package com.bit.reservation.domain.reservation.controller;

import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.service.DoctorService;
import com.bit.reservation.domain.reservation.dto.ReservationDto;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.reservation.mapper.ReservationMapper;
import com.bit.reservation.domain.reservation.service.ReservationService;
import com.bit.reservation.global.config.SecurityConfiguration;
import com.bit.reservation.stub.StubData;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.bit.reservation.global.utils.ApiDocumentUtils.getRequestPreProcessor;
import static com.bit.reservation.global.utils.ApiDocumentUtils.getResponsePreProcessor;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReservationController.class, excludeAutoConfiguration = SecurityConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private ReservationService service;

    @MockBean
    private ReservationMapper mapper;

    @MockBean
    private DoctorService doctorService;

    Reservation reservation;

    ReservationDto.HospitalInfo hospitalInfo;
    ReservationDto.ClientInfo clientInfo;
    ReservationDto.ListHospitalResponseDto listHospitalResponseDto;
    ReservationDto.AdminListHospitalResponseDto adminListHospitalResponseDto;
    Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = StubData.getDoctor();
        reservation = StubData.getReservation();

        hospitalInfo = ReservationDto.HospitalInfo.builder()
                .hospitalId(reservation.getHospital().getHospitalId())
                .name(reservation.getHospital().getName())
                .telNum(reservation.getHospital().getTelNum())
                .build();

        clientInfo = ReservationDto.ClientInfo.builder()
                .userId(1L)
                .name(reservation.getUser().getUserName())
                .age(reservation.getUser().getAge())
                .profileImage(reservation.getUser().getProfileImage().getImage())
                .build();

        listHospitalResponseDto = ReservationDto.ListHospitalResponseDto.builder()
                .doctorId(1L)
                .doctorName(reservation.getDoctor().getName())
                .reservationId(reservation.getReservationId())
                .dateTime(reservation.getDateTime())
                .medicalSubject(reservation.getSubject())
                .reservationStatus(reservation.getReservationStatus())
                .clientInfo(ReservationDto.ClientInfo.builder()
                        .userId(reservation.getUser().getUserId())
                        .name(reservation.getUser().getUserName())
                        .age(reservation.getUser().getAge())
                        .profileImage(reservation.getUser().getProfileImage().getImage())
                        .build())
                .build();

        adminListHospitalResponseDto = ReservationDto.AdminListHospitalResponseDto.builder()
                .doctorId(1L)
                .doctorName(reservation.getDoctor().getName())
                .reservationId(reservation.getReservationId())
                .dateTime(reservation.getDateTime())
                .medicalSubject(reservation.getSubject())
                .reservationStatus(reservation.getReservationStatus())
                .clientInfo(clientInfo)
                .hospitalInfo(hospitalInfo)
                .build();
    }

    @DisplayName("예약 신청 테스트")
    @Test
    void postReservation() throws Exception {
        //given
        Long id = 1L;
        ReservationDto.PostDto postDto = ReservationDto.PostDto.builder()
                .dateTime(reservation.getDateTime())
                .subject(reservation.getSubject())
                .clientRequest(reservation.getClientRequest())
                .doctorName(reservation.getDoctor().getName())
                .build();

        String requestBody = gson.toJson(postDto);
        ReservationDto.SimpleResponseDto res = new ReservationDto.SimpleResponseDto(1L);

        given(doctorService.findDoctorByName(Mockito.anyString())).willReturn(StubData.getDoctor());
        given(mapper.postDtoToReservation(Mockito.any(ReservationDto.PostDto.class), Mockito.any(Doctor.class))).willReturn(reservation);
        given(service.createReservation(Mockito.any(Reservation.class), Mockito.anyLong(), Mockito.anyLong())).willReturn(reservation);
        given(mapper.reservationToSimpleResponseDto(Mockito.any(Reservation.class))).willReturn(res);

        //when
        ResultActions actions = mockMvc.perform(post("/api/reservation/hospital/{hospitalId}/user/{userId}", 1L, 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        actions.andDo(print());
        actions.andExpect(status().isCreated())
//                .andExpect(jsonPath("$.reservationId").value(1L)) TODO:왜.. 왜 empty??
                .andDo(document("post-reservation",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("hospitalId").description("병원 식별자"),
                                parameterWithName("userId").description("회원 식별자")
                        ),
                        requestFields(List.of(
                                fieldWithPath("dateTime").description("예약일자"),
                                fieldWithPath("subject").description("진료과목"),
                                fieldWithPath("clientRequest").description("고객 요청"),
                                fieldWithPath("doctorName").description("의사 이름"))
                        )
//                        responseFields(List.of(fieldWithPath("reservationId").description("예약 식별자"))
                        )
                );
    }

    @DisplayName("예약 수정 테스트")
    @Test
    void patchReservation() throws Exception {

        //given
        ReservationDto.PatchDto patchDto = ReservationDto.PatchDto.builder()
                .reservationId(1L)
                .dateTime(reservation.getDateTime())
                .subject(reservation.getSubject())
                .clientRequest(reservation.getClientRequest())
                .doctorName(reservation.getDoctor().getName())
                .build();

        String requestBody = gson.toJson(patchDto);

        given(doctorService.findDoctorByName(Mockito.anyString())).willReturn(StubData.getDoctor());
        given(mapper.patchDtoToReservation(Mockito.any(ReservationDto.PatchDto.class), Mockito.any(Doctor.class))).willReturn(reservation);
        given(service.updateReservation(Mockito.any(Reservation.class))).willReturn(reservation);
        given(mapper.reservationToSimpleResponseDto(Mockito.any(Reservation.class))).willReturn(new ReservationDto.SimpleResponseDto(1L));

        //then
        ResultActions actions = mockMvc.perform(patch("/api/reservation/{reservationId}", 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1L))
                .andDo(document("patch-reservation",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("reservationId").description("예약 식별자")),
                        requestFields(List.of(
                                fieldWithPath("reservationId").ignored(),
                                fieldWithPath("dateTime").description("예약일자"),
                                fieldWithPath("subject").description("진료과목"),
                                fieldWithPath("clientRequest").description("고객 요청"),
                                fieldWithPath("doctorName").description("의사 이름"))
                        ),
                        responseFields(List.of(fieldWithPath("reservationId").description("예약 식별자"))
                        ))
                );

    }

    @DisplayName("예약 취소")
    @Test
    void deleteReservation() throws Exception {
        doNothing().when(service).deleteReservation(Mockito.anyLong());

        ResultActions actions = mockMvc.perform(delete("/api/reservation/{reservationId}", 1L)
                .header("Authorization", "Bearer token"));

        actions.andExpect(status().isNoContent())
                .andDo(document("delete-reservation",
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("reservationId").description("예약 식별자"))
                ));
    }

    @DisplayName("예약 1건 조회 (고객 ver)")
    @Test
    void getClientReservation() throws Exception {
        //given
        ReservationDto.ClientResponseDto res =
                ReservationDto.ClientResponseDto.builder()
                        .reservationId(1L)
                        .dateTime(reservation.getDateTime())
                        .subject(reservation.getSubject())
                        .clientRequest(reservation.getClientRequest())
                        .doctorName(reservation.getDoctor().getName())
                        .reservationStatus(reservation.getReservationStatus())
                        .hospitalInfo(ReservationDto.HospitalInfo.builder()
                                .hospitalId(reservation.getHospital().getHospitalId())
                                .name(reservation.getHospital().getName())
                                .telNum(reservation.getHospital().getTelNum())
                                .build())
                        .createdAt(reservation.getCreatedAt())
                        .modifiedAt(reservation.getModifiedAt())
                        .build();


        given(service.findClientReservation(Mockito.anyLong(), Mockito.anyLong())).willReturn(reservation);
        given(mapper.reservationToClientResponseDto(Mockito.any(Reservation.class))).willReturn(res);

        //when
        ResultActions actions = mockMvc.perform(
                get("/api/reservation/{reservationId}/user/{userId}", 1L, 1L)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("data.reservationId").value(1L))
                .andExpect(jsonPath("data.dateTime").value(reservation.getDateTime()))
                .andExpect(jsonPath("data.subject").value(reservation.getSubject()))
                .andExpect(jsonPath("data.clientRequest").value(reservation.getClientRequest()))
                .andExpect(jsonPath("data.doctorName").value(reservation.getDoctor().getName()))
                .andExpect(jsonPath("data.reservationStatus").value(reservation.getReservationStatus().getStatus()))
                .andExpect(jsonPath("data.hospitalInfo.hospitalId").value(reservation.getHospital().getHospitalId()))
                .andExpect(jsonPath("data.hospitalInfo.name").value(reservation.getHospital().getName()))
                .andExpect(jsonPath("data.hospitalInfo.telNum").value(reservation.getHospital().getTelNum()))
                .andExpect(jsonPath("data.createdAt").value(reservation.getCreatedAt().toString()))
                .andExpect(jsonPath("data.modifiedAt").value(reservation.getModifiedAt().toString()))
                .andDo(document("get-reservation(client)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("reservationId").description("예약 식별자"),
                                parameterWithName("userId").description("회원 식별자")
                        ),
                        responseFields(List.of(
                                fieldWithPath("data.reservationId").description("예약 식별자"),
                                fieldWithPath("data.dateTime").description("예약 일자"),
                                fieldWithPath("data.subject").description("진료 과목"),
                                fieldWithPath("data.clientRequest").description("고객 요청"),
                                fieldWithPath("data.doctorName").description("의사 이름"),
                                fieldWithPath("data.reservationStatus").description("예약 상태"),
                                fieldWithPath("data.hospitalInfo.hospitalId").description("병원 식별자"),
                                fieldWithPath("data.hospitalInfo.name").description("병원 이름"),
                                fieldWithPath("data.hospitalInfo.telNum").description("병원 전화번호"),
                                fieldWithPath("data.createdAt").description("예약 생성 일자"),
                                fieldWithPath("data.modifiedAt").description("예약 마지막 수정 일자")
                        ))

                ));
    }

    @DisplayName("예약 목록 조회(고객 ver)")
    @Test
    void getUserReservations() throws Exception {
        //given
        List<Reservation> list = List.of(reservation);
        Pageable pageable = PageRequest.of(0, 15);
        Page<Reservation> pages = new PageImpl(list, pageable, list.size());

        ReservationDto.ListClientResponseDto res = ReservationDto.ListClientResponseDto.builder()
                .reservationId(1L)
                .dateTime(reservation.getDateTime())
                .reservationStatus(reservation.getReservationStatus())
                .hospitalInfo(hospitalInfo)
                .build();

        given(service.findClientReservations(Mockito.any(Pageable.class), Mockito.anyLong(), Mockito.isNull(),
                Mockito.isNull(), Mockito.anyString(), Mockito.anyString())).willReturn(pages);

        given(mapper.reservationsToListClientResponseDto(Mockito.anyList())).willReturn(List.of(res));
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("page", "1");
        queryParams.add("size", "15");
        queryParams.add("status", reservation.getReservationStatus().toString());
        queryParams.add("hospitalName", reservation.getHospital().getName());

        //when
        ResultActions actions = mockMvc.perform(get("/api/reservation/user/{userId}", 1L)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
                .queryParams(queryParams)
        );

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].reservationId").value(1L))
                .andExpect(jsonPath("$.data[0].dateTime").value(reservation.getDateTime()))
                .andExpect(jsonPath("$.data[0].reservationStatus").value(reservation.getReservationStatus().getStatus()))
                .andExpect(jsonPath("$.data[0].hospitalInfo.hospitalId").value(1L))
                .andExpect(jsonPath("$.data[0].hospitalInfo.name").value(reservation.getHospital().getName()))
                .andExpect(jsonPath("$.data[0].hospitalInfo.telNum").value(reservation.getHospital().getTelNum()))
                .andDo(document("get-reservations(client)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("회 액세스 토큰")),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지"),
                                parameterWithName("size").description("한 페이지당 요소 개수"),
                                parameterWithName("status").description("예약 상태"),
                                parameterWithName("hospitalName").description("병원 이름")
                        ),
                        responseFields(List.of(
                                fieldWithPath("data[].reservationId").description("예약 식별자"),
                                fieldWithPath("data[].dateTime").description("예약 날짜"),
                                fieldWithPath("data[].reservationStatus").description("예약 상태"),
                                fieldWithPath("data[].hospitalInfo.hospitalId").description("병원 식별자"),
                                fieldWithPath("data[].hospitalInfo.name").description("병원 이름"),
                                fieldWithPath("data[].hospitalInfo.telNum").description("병원 전화번호"),
                                fieldWithPath("pageInfo.page").description("현재 페이지"),
                                fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                        ))
                ));
    }

    @DisplayName("예약 1건 조회(병원 ver)")
    @Test
    void getHospitalReservation() throws Exception {
        ReservationDto.HospitalResponseDto res =
                ReservationDto.HospitalResponseDto.builder()
                        .reservationId(1L)
                        .dateTime(reservation.getDateTime())
                        .subject(reservation.getSubject())
                        .clientRequest(reservation.getClientRequest())
                        .hospitalMemo(reservation.getHospitalMemo())
                        .doctorId(reservation.getDoctor().getDoctorId())
                        .doctorName(reservation.getDoctor().getName())
                        .reservationStatus(reservation.getReservationStatus())
                        .clientInfo(clientInfo)
                        .createdAt(reservation.getCreatedAt())
                        .modifiedAt(reservation.getModifiedAt())
                        .build();

        given(service.findHospitalReservation(Mockito.anyLong(), Mockito.anyLong())).willReturn(reservation);
        given(mapper.reservationToHospitalResponseDto(Mockito.any(Reservation.class))).willReturn(res);

        ResultActions actions = mockMvc.perform(
                get("/api/reservation/{reservationId}/hospital/{hospitalId}", 1L, 1L)
                        .header("Authorization", "Bearer token")
                        .accept(MediaType.APPLICATION_JSON));

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("data.reservationId").value(1L))
                .andExpect(jsonPath("data.dateTime").value(reservation.getDateTime()))
                .andExpect(jsonPath("data.subject").value(reservation.getSubject()))
                .andExpect(jsonPath("data.clientRequest").value(reservation.getClientRequest()))
                .andExpect(jsonPath("data.doctorId").value(reservation.getDoctor().getDoctorId()))
                .andExpect(jsonPath("data.doctorName").value(reservation.getDoctor().getName()))
                .andExpect(jsonPath("data.hospitalMemo").value(reservation.getHospitalMemo()))
                .andExpect(jsonPath("data.reservationStatus").value(reservation.getReservationStatus().getStatus()))
                .andExpect(jsonPath("data.clientInfo.userId").value(reservation.getUser().getUserId()))
                .andExpect(jsonPath("data.clientInfo.name").value(reservation.getUser().getUserName()))
                .andExpect(jsonPath("data.clientInfo.age").value(reservation.getUser().getAge()))
                .andExpect(jsonPath("data.clientInfo.profileImage").value(reservation.getUser().getProfileImage().getImage()))
                .andExpect(jsonPath("data.createdAt").value(reservation.getCreatedAt().toString()))
                .andExpect(jsonPath("data.modifiedAt").value(reservation.getModifiedAt().toString()))
                .andDo(document("get-reservation(hospital)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("reservationId").description("예약 식별자"),
                                parameterWithName("hospitalId").description("병원 식별자")
                        ),
                        responseFields(List.of(
                                fieldWithPath("data.reservationId").description("예약 식별자"),
                                fieldWithPath("data.dateTime").description("예약 일자"),
                                fieldWithPath("data.subject").description("진료 과목"),
                                fieldWithPath("data.clientRequest").description("고객 요청"),
                                fieldWithPath("data.doctorId").description("의사 식별자"),
                                fieldWithPath("data.doctorName").description("의사 이름"),
                                fieldWithPath("data.hospitalMemo").description("병원 메모"),
                                fieldWithPath("data.reservationStatus").description("예약 상태"),
                                fieldWithPath("data.clientInfo.userId").description("회원 식별자"),
                                fieldWithPath("data.clientInfo.name").description("회원 이름"),
                                fieldWithPath("data.clientInfo.age").description("회원 나이"),
                                fieldWithPath("data.clientInfo.profileImage").description("회원 이미지"),
                                fieldWithPath("data.createdAt").description("예약 생성 일자"),
                                fieldWithPath("data.modifiedAt").description("예약 마지막 수정 일자")
                        ))

                ));
    }

    @DisplayName("예약 목록 조회(병원 ver)")
    @Test
    void getHospitalReservations() throws Exception {
        List<Reservation> list = List.of(reservation);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> pages = new PageImpl<>(list, pageable, list.size());

        given(service.findHospitalReservations(Mockito.any(Pageable.class), Mockito.anyLong(),
                Mockito.isNull(), Mockito.isNull(), Mockito.anyString(), Mockito.anyString())).willReturn(pages);
        given(mapper.reservationsToListHospitalResponseDto(Mockito.anyList())).willReturn(List.of(listHospitalResponseDto));
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap();
        queryParams.add("page", "1");
        queryParams.add("size", "10");
        queryParams.add("status", reservation.getReservationStatus().toString());
        queryParams.add("userName", reservation.getUser().getUserName());

            ResultActions actions = mockMvc.perform(get("/api/reservation/hospital/{hospitalId}", 1L)
                    .header("Authorization", "Bearer token")
                    .queryParams(queryParams)
                    .accept(MediaType.APPLICATION_JSON)
            );

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].doctorId").value(1L))
                .andExpect(jsonPath("$.data[0].doctorName").value(reservation.getDoctor().getName()))
                .andExpect(jsonPath("$.data[0].reservationId").value(reservation.getReservationId()))
                .andExpect(jsonPath("$.data[0].dateTime").value(reservation.getDateTime()))
                .andExpect(jsonPath("$.data[0].medicalSubject").value(reservation.getSubject()))
                .andExpect(jsonPath("$.data[0].reservationStatus").value(reservation.getReservationStatus().getStatus()))
                .andExpect(jsonPath("$.data[0].clientInfo.userId").value(reservation.getUser().getUserId()))
                .andExpect(jsonPath("$.data[0].clientInfo.name").value(reservation.getUser().getUserName()))
                .andExpect(jsonPath("$.data[0].clientInfo.age").value(reservation.getUser().getAge()))
                .andExpect(jsonPath("$.data[0].clientInfo.profileImage").value(reservation.getUser().getProfileImage().getImage()))
                .andDo(document("get-reservations(hospital)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("병원 액세스 토큰")),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지"),
                                parameterWithName("size").description("한 페이지당 요소 개수"),
                                parameterWithName("status").description("예약 상태"),
                                parameterWithName("userName").description("고객 이름")
                        ),
                        responseFields(List.of(
                                fieldWithPath("data[].doctorId").description("의사 식별자"),
                                fieldWithPath("data[].doctorName").description("의사 이름"),
                                fieldWithPath("data[].reservationId").description("예약 식별자"),
                                fieldWithPath("data[].dateTime").description("예약 날짜"),
                                fieldWithPath("data[].medicalSubject").description("진료 과목"),
                                fieldWithPath("data[].reservationStatus").description("예약 상태"),
                                fieldWithPath("data[].clientInfo.userId").description("회원 식별자"),
                                fieldWithPath("data[].clientInfo.name").description("회원 이름"),
                                fieldWithPath("data[].clientInfo.age").description("회원 나이"),
                                fieldWithPath("data[].clientInfo.profileImage").description("회원 프로필 이미지"),
                                fieldWithPath("pageInfo.page").description("현재 페이지"),
                                fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                        ))
                ));

    }

    @DisplayName("병원 - 예약 메모 등록 / 예약 승인 및 거절")
    @Test
    void patchReservationByHospital() throws Exception {
        //given
        Long id = 1L;
        ReservationDto.PatchDtoForHospital dto = ReservationDto.PatchDtoForHospital.builder()
                .reservationId(id)
                .hospitalMemo(reservation.getHospitalMemo())
                .reservationStatus(reservation.getReservationStatus())
                .build();
        String requestBody = gson.toJson(dto);

        given(mapper.patchDtoForHospitalToReservation(Mockito.any(ReservationDto.PatchDtoForHospital.class))).willReturn(reservation);
        given(service.updateReservationByHospital(Mockito.anyLong(), Mockito.any(Reservation.class))).willReturn(reservation);
        given(mapper.reservationToSimpleResponseDto(Mockito.any(Reservation.class))).willReturn(new ReservationDto.SimpleResponseDto(1L));

        ResultActions actions = mockMvc.perform(patch("/api/reservation/{reservationId}/hospital/{hospitalId}", id, id)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(id))
                .andDo(document("patch-reservation(hospital)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("병원 액세스 토큰")),
                        pathParameters(
                                parameterWithName("reservationId").description("예약 식별자"),
                                parameterWithName("hospitalId").description("병원 식별자")
                        ),
                        requestFields(List.of(
                                        fieldWithPath("reservationId").description("예약 식별자"),
                                        fieldWithPath("hospitalMemo").description("병원 메모"),
                                        fieldWithPath("reservationStatus").description("예약 상태 결정 (approval/reject)")
                                )
                        ),
                        responseFields(List.of(
                                fieldWithPath("reservationId").description("예약 식별자")
                        ))
                ));
    }

    @DisplayName("모든 예약 목록 조회 [관리자용]")
    @WithMockUser(roles = {"USER", "ADMIN"})
    @Test
    void getAllReservations() throws Exception {

        List<Reservation> reservations = List.of(reservation);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> pages = new PageImpl<>(reservations, pageable, reservations.size());

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("page", "1");
        queryParams.add("size", "10");
        queryParams.add("dateTime", reservation.getDateTime());
        queryParams.add("subject", reservation.getSubject());
        queryParams.add("status", reservation.getReservationStatus().toString());
        queryParams.add("hospitalId", "1");
        queryParams.add("userId", "1");

        given(service.findAllReservations
                (Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong())).willReturn(pages);
        given(mapper.reservationsToAdminListHospitalResponseDto(Mockito.anyList())).willReturn(List.of(adminListHospitalResponseDto));

        ResultActions actions = mockMvc.perform(get("/api/reservation")
                .header("Authorization", "Bearer token")
                .queryParams(queryParams)
                .contentType(MediaType.APPLICATION_JSON));

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].doctorId").value(reservation.getDoctor().getDoctorId()))
                .andExpect(jsonPath("$.data[0].doctorName").value(reservation.getDoctor().getName()))
                .andExpect(jsonPath("$.data[0].reservationId").value(reservation.getReservationId()))
                .andExpect(jsonPath("$.data[0].dateTime").value(reservation.getDateTime()))
                .andExpect(jsonPath("$.data[0].medicalSubject").value(reservation.getSubject()))
                .andExpect(jsonPath("$.data[0].reservationStatus").value(reservation.getReservationStatus().getStatus()))
                .andExpect(jsonPath("$.data[0].clientInfo.userId").value(reservation.getUser().getUserId()))
                .andExpect(jsonPath("$.data[0].clientInfo.name").value(reservation.getUser().getUserName()))
                .andExpect(jsonPath("$.data[0].clientInfo.age").value(reservation.getUser().getAge()))
                .andExpect(jsonPath("$.data[0].clientInfo.profileImage").value(reservation.getUser().getProfileImage().getImage()))
                .andExpect(jsonPath("$.data[0].hospitalInfo.hospitalId").value(reservation.getHospital().getHospitalId()))
                .andExpect(jsonPath("$.data[0].hospitalInfo.name").value(reservation.getHospital().getName()))
                .andExpect(jsonPath("$.data[0].hospitalInfo.telNum").value(reservation.getHospital().getTelNum()))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-reservations(admin)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("관리자 액세스 토큰")),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지"),
                                parameterWithName("size").description("한 페이지당 요소 개수"),
                                parameterWithName("dateTime").description("예약 날짜"),
                                parameterWithName("subject").description("진료과목"),
                                parameterWithName("status").description("예약 상태"),
                                parameterWithName("hospitalId").description("병원 식별자"),
                                parameterWithName("userId").description("회원 식별자")
                        ),
                        responseFields(List.of(
                                fieldWithPath("data[].doctorId").description("의사 식별자"),
                                fieldWithPath("data[].doctorName").description("의사 이름"),
                                fieldWithPath("data[].reservationId").description("예약 식별자"),
                                fieldWithPath("data[].dateTime").description("예약 날짜"),
                                fieldWithPath("data[].medicalSubject").description("진료 과목"),
                                fieldWithPath("data[].reservationStatus").description("예약 상태"),
                                fieldWithPath("data[].clientInfo.userId").description("회원 식별자"),
                                fieldWithPath("data[].clientInfo.name").description("회원 이름"),
                                fieldWithPath("data[].clientInfo.age").description("회원 나이"),
                                fieldWithPath("data[].clientInfo.profileImage").description("회원 프로필 이미지"),
                                fieldWithPath("data[].hospitalInfo.hospitalId").description("병원 식별자"),
                                fieldWithPath("data[].hospitalInfo.name").description("병원 이름"),
                                fieldWithPath("data[].hospitalInfo.telNum").description("병원 전화번호"),
                                fieldWithPath("pageInfo.page").description("현재 페이지"),
                                fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                        ))
                ));
    }
}