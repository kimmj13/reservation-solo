package com.bit.reservation.domain.estimate.controller;

import com.bit.reservation.domain.estimate.dto.EstimateDto;
import com.bit.reservation.domain.estimate.entity.Estimate;
import com.bit.reservation.domain.estimate.mapper.EstimateMapper;
import com.bit.reservation.domain.estimate.service.EstimateService;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.reservation.entity.Reservation;
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

@WebMvcTest(controllers = EstimateController.class, excludeAutoConfiguration = SecurityConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class EstimateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private EstimateService service;

    @MockBean
    private EstimateMapper mapper;

    Estimate estimate;
    Page<Estimate> pages;
    Hospital hospital;
    Reservation reservation;

    @BeforeEach
    void setUp() {
        estimate = StubData.getEstimate();
        List<Estimate> list = List.of(estimate);
        Pageable pageable = PageRequest.of(0, 10);
        pages = new PageImpl(list, pageable, list.size());
        hospital = StubData.getHospital();
        reservation = StubData.getReservation();
    }

    @DisplayName("견적 생성 v1")
    @Test
    void postEstimate() throws Exception {
        //given
        EstimateDto.PostToIdDto postToIdDto = new EstimateDto.PostToIdDto(List.of(1L));
        String requestBody = gson.toJson(postToIdDto);

        given(service.createEstimateToId(Mockito.any(EstimateDto.PostToIdDto.class), Mockito.anyLong())).willReturn(estimate);
        given(mapper.estimateToSimpleResponseDto(Mockito.any(Estimate.class))).willReturn(new EstimateDto.SimpleResponseDto(1L));

        //when
        ResultActions actions = mockMvc.perform(post("/api/estimate/v1/hospital/{hospitalId}", 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        actions.andDo(print());
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.estimateId").value(1L))
                .andDo(document("post-estimate",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        requestFields(List.of(
                                fieldWithPath("reservationIds").description("예약 식별자 리스트")
                        )),
                        responseFields(List.of(fieldWithPath("estimateId").description("견적 식별자"))
                        ))
                );
    }

    @DisplayName("견적 생성 v2")
    @Test
    void testPostEstimate() throws Exception {
        //given
        EstimateDto.PostToDateDto postToDateDto = new EstimateDto.PostToDateDto("2023-03");
        String requestBody = gson.toJson(postToDateDto);

        given(service.createEstimateToDate(Mockito.any(EstimateDto.PostToDateDto.class), Mockito.anyLong())).willReturn(estimate);
        given(mapper.estimateToSimpleResponseDto(Mockito.any(Estimate.class))).willReturn(new EstimateDto.SimpleResponseDto(1L));

        //when
        ResultActions actions = mockMvc.perform(post("/api/estimate/v2/hospital/{hospitalId}", 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        actions.andDo(print());
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.estimateId").value(1L))
                .andDo(document("post-estimate(v2)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        requestFields(List.of(
                                fieldWithPath("estimateDate").description("견적 날짜 (형식: YYYY-MM)")
                        )),
                        responseFields(List.of(fieldWithPath("estimateId").description("견적 식별자"))
                        ))
                );
    }

    @DisplayName("견적 개별 조회")
    @Test
    void getEstimate() throws Exception {
        //given
        EstimateDto.ResponseDto res = new EstimateDto.ResponseDto(1L, "2023-03", 1, 2000, new EstimateDto.HospitalInfo(1L, hospital.getName(), hospital.getHospitalStatus()),
                List.of(new EstimateDto.ReservationInfo(1L, reservation.getDateTime(), reservation.getSubject(), reservation.getReservationStatus())));

        given(service.findEstimate(Mockito.anyLong())).willReturn(estimate);
        given(mapper.estimateToResponseDto(Mockito.any(Estimate.class))).willReturn(res);

        //when
        ResultActions actions = mockMvc.perform(get("/api/estimate/{estimateId}", 1L)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andDo(document("get-estimate",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("estimateId").description("견적 식별자")),
                        responseFields(List.of(
                                fieldWithPath("estimateId").description("견적 식별자"),
                                fieldWithPath("estimateDate").description("견적 날짜"),
                                fieldWithPath("numberOfReservations").description("예약 숫자"),
                                fieldWithPath("totalAmount").description("견적 총금액"),
                                fieldWithPath("hospitalInfo.hospitalId").description("병원 식별자"),
                                fieldWithPath("hospitalInfo.hospitalName").description("병원 이름"),
                                fieldWithPath("hospitalInfo.hospitalStatus").description("병원 상태"),
                                fieldWithPath("reservationInfo[].reservationId").description("예약 식별자"),
                                fieldWithPath("reservationInfo[].dateTime").description("예약 날짜"),
                                fieldWithPath("reservationInfo[].subject").description("예약 진료과목"),
                                fieldWithPath("reservationInfo[].reservationStatus").description("예약 상태")
                                )
                        ))
                );
    }

    @DisplayName("견적 목록 조회")
    @Test
    void getEstimates() throws Exception {
        //given
        EstimateDto.ListResponseDto listResponseDto = new EstimateDto.ListResponseDto(1L, "2023-03", 2000, new EstimateDto.HospitalInfo(1L, hospital.getName(), hospital.getHospitalStatus()));
        given(service.findEstimates(Mockito.any(Pageable.class), Mockito.anyString(), Mockito.anyLong())).willReturn(pages);
        given(mapper.estimateToListResponseDto(Mockito.anyList())).willReturn(List.of(listResponseDto));

        MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
        queryParam.add("page", "1");
        queryParam.add("size", "10");
        queryParam.add("estimateDate", "2023-03");
        queryParam.add("hospitalId", "1");

        //when
        ResultActions actions = mockMvc.perform(get("/api/estimate")
                .header("Authorization", "Bearer token")
                .params(queryParam)
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andDo(document("get-estimates",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        requestParameters(
                                parameterWithName("page").description("[선택] 현재 페이지 (기본값:1)").optional(),
                                parameterWithName("size").description("[선택] 한 페이지당 요소 개수 (기본값:10)").optional(),
                                parameterWithName("estimateDate").description("[선택] 견적 날짜"),
                                parameterWithName("hospitalId").description("[선택] 병원 식별자")
                        ),
                        responseFields(List.of(
                                        fieldWithPath("data[].estimateId").description("견적 식별자"),
                                        fieldWithPath("data[].estimateDate").description("견적 날짜"),
                                        fieldWithPath("data[].totalAmount").description("견적 총금액"),
                                        fieldWithPath("data[].hospitalInfo.hospitalId").description("병원 식별자"),
                                        fieldWithPath("data[].hospitalInfo.hospitalName").description("병원 이름"),
                                        fieldWithPath("data[].hospitalInfo.hospitalStatus").description("병원 상태"),
                                        fieldWithPath("pageInfo.page").description("현재 페이지"),
                                        fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                        fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                        fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                                )
                        )
                ));
    }

    @DisplayName("병원의 견적 조회 [병원용]")
    @Test
    void getHospitalEstimates() throws Exception {
        //given
        EstimateDto.ListResponseDto listResponseDto = new EstimateDto.ListResponseDto(1L, estimate.getEstimateDate(), estimate.getTotalAmount(), new EstimateDto.HospitalInfo(1L, hospital.getName(), hospital.getHospitalStatus()));
        given(service.findHospitalEstimates(Mockito.any(Pageable.class), Mockito.anyString(), Mockito.anyLong())).willReturn(pages);
        given(mapper.estimateToListResponseDto(Mockito.anyList())).willReturn(List.of(listResponseDto));

        MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
        queryParam.add("page", "1");
        queryParam.add("size", "10");
        queryParam.add("estimateDate", "2023-03");

        //when
        ResultActions actions = mockMvc.perform(get("/api/estimate/hospital/{hospitalId}", 1L)
                .header("Authorization", "Bearer token")
                .params(queryParam)
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andDo(document("get-hospital-estimates",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        requestParameters(
                                parameterWithName("page").description("[선택] 현재 페이지 (기본값:1)").optional(),
                                parameterWithName("size").description("[선택] 한 페이지당 요소 개수 (기본값:10)").optional(),
                                parameterWithName("estimateDate").description("[선택] 견적 날짜")
                        ),
                        responseFields(List.of(
                                        fieldWithPath("data[].estimateId").description("견적 식별자"),
                                        fieldWithPath("data[].estimateDate").description("견적 날짜"),
                                        fieldWithPath("data[].totalAmount").description("견적 총금액"),
                                        fieldWithPath("data[].hospitalInfo.hospitalId").description("병원 식별자"),
                                        fieldWithPath("data[].hospitalInfo.hospitalName").description("병원 이름"),
                                        fieldWithPath("data[].hospitalInfo.hospitalStatus").description("병원 상태"),
                                        fieldWithPath("pageInfo.page").description("현재 페이지"),
                                        fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                        fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                        fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                                )
                        )
                ));
    }

    @DisplayName("견적 삭제")
    @Test
    void deleteEstimate() throws Exception {
        //given
        doNothing().when(service).deleteEstimate(Mockito.anyLong());
        ResultActions actions = mockMvc.perform(delete("/api/estimate/{estimateId}", 1L)
                .header("Authorization", "Bearer token"));

        actions.andExpect(status().isNoContent())
                .andDo(document("delete-estimate",
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("estimateId").description("견적 식별자"))
                ));
    }

    @DisplayName("병원 견적 개별 조회[병원용]")
    @Test
    void getHospitalEstimate() throws Exception {
        //given
        EstimateDto.ResponseDto res = new EstimateDto.ResponseDto(1L, "2023-03", 1, 2000, new EstimateDto.HospitalInfo(1L, hospital.getName(), hospital.getHospitalStatus()),
                List.of(new EstimateDto.ReservationInfo(1L, reservation.getDateTime(), reservation.getSubject(), reservation.getReservationStatus())));

        given(service.findHospitalEstimate(Mockito.anyLong(), Mockito.anyLong())).willReturn(estimate);
        given(mapper.estimateToResponseDto(Mockito.any(Estimate.class))).willReturn(res);

        //when
        ResultActions actions = mockMvc.perform(get("/api/estimate/{estimateId}/hospital/{hospitalId}", 1L, 1L)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andDo(document("get-hospital-estimate",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(
                                parameterWithName("estimateId").description("견적 식별자"),
                                parameterWithName("hospitalId").description("병원 식별자")
                                ),
                        responseFields(List.of(
                                        fieldWithPath("estimateId").description("견적 식별자"),
                                        fieldWithPath("estimateDate").description("견적 날짜"),
                                        fieldWithPath("numberOfReservations").description("예약 숫자"),
                                        fieldWithPath("totalAmount").description("견적 총금액"),
                                        fieldWithPath("hospitalInfo.hospitalId").description("병원 식별자"),
                                        fieldWithPath("hospitalInfo.hospitalName").description("병원 이름"),
                                        fieldWithPath("hospitalInfo.hospitalStatus").description("병원 상태"),
                                        fieldWithPath("reservationInfo[].reservationId").description("예약 식별자"),
                                        fieldWithPath("reservationInfo[].dateTime").description("예약 날짜"),
                                        fieldWithPath("reservationInfo[].subject").description("예약 진료과목"),
                                        fieldWithPath("reservationInfo[].reservationStatus").description("예약 상태")
                                )
                        ))
                );
    }
}