package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.HospitalRateDto;
import com.bit.reservation.domain.hospital.entity.HospitalRate;
import com.bit.reservation.domain.hospital.mapper.HospitalRateMapper;
import com.bit.reservation.domain.hospital.service.HospitalRateService;
import com.bit.reservation.domain.reservation.entity.Reservation;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.global.config.SecurityConfiguration;
import com.bit.reservation.stub.HospitalRateStubData;
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
import org.springframework.restdocs.payload.JsonFieldType;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HospitalRateController.class, excludeAutoConfiguration = SecurityConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class HospitalRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private HospitalRateService service;

    @MockBean
    private HospitalRateMapper mapper;

    private HospitalRateDto.BasicDto basicDto;
    private HospitalRate hospitalRate;
    private Page<HospitalRate> pages;

    @BeforeEach
    void setUp() {
        hospitalRate = HospitalRateStubData.getRate();
        basicDto = HospitalRateStubData.getBasicDto();
        pages = new PageImpl<>(List.of(hospitalRate), PageRequest.of(0, 10), 1);
    }

    @DisplayName("병원 후기 등록")
    @Test
    void postRate() throws Exception {
        //given
        given(mapper.postDtoToRate(Mockito.any(HospitalRateDto.PostDto.class))).willReturn(hospitalRate);
        given(service.createRate(Mockito.any(HospitalRate.class), Mockito.anyLong())).willReturn(hospitalRate);
        given(mapper.rateToSimpleResponseDto(Mockito.any(HospitalRate.class))).willReturn(new HospitalRateDto.SimpleResponseDto(1L));
        String requestBody = gson.toJson(HospitalRateStubData.getPostDto());

        //when
        ResultActions actions = mockMvc.perform(post("/api/hospital-rate/reservation/{reservationId}", 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody));

        actions.andDo(print());
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.hospitalRateId").value(1L))
                .andDo(document("post-rate",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("reservationId").description("예약 식별자")),
                        requestHeaders(headerWithName("Authorization").description("회원 액세스 토큰")),
                        requestFields(List.of(
                                        fieldWithPath("rating").description("평점"),
                                        fieldWithPath("content").description("후기")
                                )
                        ),
                        responseFields(List.of(
                                fieldWithPath("hospitalRateId").description("병원 후기 식별자")
                        ))
                ));
    }

    @DisplayName("병원 후기 수정")
    @Test
    void patchRate() throws Exception {
        //given
        given(mapper.patchDtoToRate(Mockito.any(HospitalRateDto.BasicDto.class))).willReturn(hospitalRate);
        given(service.updateRate(Mockito.any(HospitalRate.class))).willReturn(hospitalRate);
        given(mapper.rateToSimpleResponseDto(Mockito.any(HospitalRate.class))).willReturn(new HospitalRateDto.SimpleResponseDto(1L));
        String requestBody = gson.toJson(basicDto);

        //when
        ResultActions actions = mockMvc.perform(patch("/api/hospital-rate/{hospitalRateId}", 1L)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody));

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalRateId").value(1L))
                .andDo(document("patch-rate",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("회원 액세스 토큰")),
                        pathParameters(parameterWithName("hospitalRateId").description("후기 식별자")),
                        requestFields(List.of(
                                        fieldWithPath("hospitalRateId").ignored(),
                                        fieldWithPath("rating").description("평점"),
                                        fieldWithPath("content").description("후기")
                                )
                        ),
                        responseFields(List.of(
                                fieldWithPath("hospitalRateId").description("병원 후기 식별자")
                        ))
                ));
    }

    @DisplayName("한 회원의 모든 후기 조회")
    @Test
    void getUserRate() throws Exception {
        //given
        given(service.findUserRates(Mockito.anyLong(), Mockito.any(Pageable.class))).willReturn(pages);
        given(mapper.ratesToBasicDto(Mockito.anyList())).willReturn(List.of(basicDto));

        //when
        ResultActions actions = mockMvc.perform(get("/api/hospital-rate/user/{userId}", 1L)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hospitalRateId").value(1L))
                .andExpect(jsonPath("$.data[0].rating").value(hospitalRate.getRating()))
                .andExpect(jsonPath("$.data[0].content").value(hospitalRate.getContent()))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-rates(user)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("회원 액세스 토큰")),
                        pathParameters(parameterWithName("userId").description("회원 식별자")),
                        responseFields(List.of(
                                fieldWithPath("data[].hospitalRateId").description("병원 후기 식별자"),
                                fieldWithPath("data[].rating").description("평점"),
                                fieldWithPath("data[].content").description("후기 내용"),
                                fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수")
                        ))
                ));
    }

    @DisplayName("한 병원의 모든 후기 조회")
    @Test
    void getHospitalRate() throws Exception {
        //given
        given(service.findHospitalRate(Mockito.anyLong(), Mockito.any(Pageable.class))).willReturn(pages);
        given(mapper.ratesToResponseDto(Mockito.anyList())).willReturn(List.of(HospitalRateStubData.getResponseDto()));

        //when
        ResultActions actions = mockMvc.perform(get("/api/hospital-rate/hospital/{hospitalId}", 1L)
                .accept(MediaType.APPLICATION_JSON));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hospitalRateId").value(1L))
                .andExpect(jsonPath("$.data[0].rating").value(hospitalRate.getRating()))
                .andExpect(jsonPath("$.data[0].content").value(hospitalRate.getContent()))
                .andExpect(jsonPath("$.data[0].userId").value(hospitalRate.getUser().getUserId()))
                .andExpect(jsonPath("$.data[0].userName").value(hospitalRate.getUser().getUserName()))
                .andExpect(jsonPath("$.data[0].userProfileImage").value(hospitalRate.getUser().getProfileImage().getImage()))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-rates(hospital)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        responseFields(List.of(
                                fieldWithPath("data[].hospitalRateId").description("병원 후기 식별자"),
                                fieldWithPath("data[].rating").description("평점"),
                                fieldWithPath("data[].content").description("후기 내용"),
                                fieldWithPath("data[].userId").description("회원 식별자"),
                                fieldWithPath("data[].userName").description("회원 이름"),
                                fieldWithPath("data[].userProfileImage").description("회원 프로필 이미지"),
                                fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수")
                        ))
                ));
    }

    @DisplayName("병원 후기 삭제")
    @Test
    void deleteRate() throws Exception {
        doNothing().when(service).deleteRate(Mockito.anyLong());

        ResultActions actions = mockMvc.perform(delete("/api/hospital-rate/{hospitalRateId}", 1L)
                .header("Authorization", "Bearer token"));

        actions.andExpect(status().isNoContent())
                .andDo(document("delete-rate",
                        requestHeaders(headerWithName("Authorization").description("회원 액세스 토크")),
                        pathParameters(parameterWithName("hospitalRateId").description("병원 후기 식별자"))
                ));
    }

    @DisplayName("모든 후기 조회[관리자용]")
    @Test
    void getAllRate() throws Exception {
        //given
        given(service.findAllRates(Mockito.any(Pageable.class), Mockito.anyLong(), Mockito.anyLong())).willReturn(pages);
        given(mapper.ratesToResponseDto(Mockito.anyList())).willReturn(List.of(HospitalRateStubData.getResponseDto()));

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("page", "1");
        queryParams.add("size", "10");
        queryParams.add("hospitalId", "1");
        queryParams.add("userId", "1");

        //when
        ResultActions actions = mockMvc.perform(get("/api/hospital-rate")
                .header("Authorization", "Bearer token")
                .queryParams(queryParams)
                .accept(MediaType.APPLICATION_JSON));

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hospitalRateId").value(1L))
                .andExpect(jsonPath("$.data[0].rating").value(hospitalRate.getRating()))
                .andExpect(jsonPath("$.data[0].content").value(hospitalRate.getContent()))
                .andExpect(jsonPath("$.data[0].userId").value(hospitalRate.getUser().getUserId()))
                .andExpect(jsonPath("$.data[0].userName").value(hospitalRate.getUser().getUserName()))
                .andExpect(jsonPath("$.data[0].userProfileImage").value(hospitalRate.getUser().getProfileImage().getImage()))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-rates(admin)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지").optional(),
                                parameterWithName("size").description("페이지당 요수 개수").optional(),
                                parameterWithName("hospitalId").description("병원 식별자").optional(),
                                parameterWithName("userId").description("회원 식별자").optional()
                        ),
                        responseFields(List.of(
                                fieldWithPath("data[].hospitalRateId").description("병원 후기 식별자"),
                                fieldWithPath("data[].rating").description("평점"),
                                fieldWithPath("data[].content").description("후기 내용"),
                                fieldWithPath("data[].userId").description("회원 식별자"),
                                fieldWithPath("data[].userName").description("회원 이름"),
                                fieldWithPath("data[].userProfileImage").description("회원 프로필 이미지"),
                                fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수")
                        ))
                ));
    }
}