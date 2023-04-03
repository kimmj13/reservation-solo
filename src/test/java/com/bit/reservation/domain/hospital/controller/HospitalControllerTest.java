package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.HospitalDto;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.mapper.HospitalMapper;
import com.bit.reservation.domain.hospital.repository.HospitalRepository;
import com.bit.reservation.domain.hospital.service.HospitalService;
import com.bit.reservation.global.config.RedisConfig;
import com.bit.reservation.global.config.SecurityConfiguration;
import com.bit.reservation.global.security.auth.jwt.JwtTokenizer;
import com.bit.reservation.global.security.utils.CustomAuthorityUtils;
import com.bit.reservation.global.security.utils.RedisUtils;
import com.bit.reservation.stub.StubData;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(value = HospitalController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@Import({SecurityConfiguration.class,
        JwtTokenizer.class,
        CustomAuthorityUtils.class,
        RedisUtils.class,
        RedisConfig.class})
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private HospitalService service;

    @MockBean
    private HospitalMapper mapper;

    @MockBean
    private HospitalRepository repository;

    private Hospital hospital;
    private HospitalDto.ResponseDto responseDto;

    Long id = 1L;

    @BeforeEach
    void setUp() {
        hospital = StubData.getHospital();
        responseDto = HospitalDto.ResponseDto.builder()
                .hospitalId(id)
                .name(hospital.getName())
                .address(hospital.getAddress())
                .age(hospital.getAge())
                .medicalSubject(hospital.getMedicalSubject())
                .openingTime(hospital.getOpeningTime())
                .closingTime(hospital.getClosingTime())
                .breakStartTime(hospital.getBreakStartTime())
                .breakEndTime(hospital.getBreakEndTime())
                .intro(hospital.getIntro())
                .telNum(hospital.getTelNum())
                .hospitalStatus(hospital.getHospitalStatus())
                .build();
    }

    @DisplayName("병원 가입")
    @Test
    void postHospital() throws Exception {
        //given
        HospitalDto.PostDto postDto = HospitalDto.PostDto.builder()
                .email(hospital.getEmail())
                .password(hospital.getPassword())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .age(hospital.getAge())
                .medicalSubject(hospital.getMedicalSubject())
                .telNum(hospital.getTelNum())
                .openingTime(hospital.getOpeningTime())
                .closingTime(hospital.getClosingTime())
                .breakEndTime(hospital.getBreakEndTime())
                .breakStartTime(hospital.getBreakStartTime())
                .intro(hospital.getIntro())
                .build();

        String requestBody = gson.toJson(postDto);

        given(service.createHospital(Mockito.any(Hospital.class))).willReturn(hospital);
        given(mapper.postDtoToHospital(Mockito.any(HospitalDto.PostDto.class))).willReturn(hospital);
        given(mapper.hospitalToSimpleResponseDto(Mockito.any(Hospital.class))).willReturn(new HospitalDto.SimpleResponseDto(1L));

        //when
        ResultActions actions = mockMvc.perform(post("/api/hospital")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody));

        //then
        actions.andDo(print());
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.hospitalId").value(1L))
                .andDo(document("post-hospital",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("name").description("병원이름"),
                                fieldWithPath("address").description("병원주소"),
                                fieldWithPath("age").description("업력"),
                                fieldWithPath("medicalSubject").description("진료과목"),
                                fieldWithPath("telNum").description("전화번호"),
                                fieldWithPath("openingTime").description("영업시작시간"),
                                fieldWithPath("closingTime").description("영업종료시간"),
                                fieldWithPath("breakStartTime").description("휴식시작시간"),
                                fieldWithPath("breakEndTime").description("휴식종료시간"),
                                fieldWithPath("intro").description("병원소개")
                        ),
                        responseFields(
                                fieldWithPath("hospitalId").description("병원 식별자")
                        ))
                );

    }

    @DisplayName("병원 정보 수정")
    @Test
    @WithMockUser(roles = {"HOSPITAL"})
    void patchHospital() throws Exception {
        //given
        given(mapper.patchDtoToHospital(Mockito.any(HospitalDto.PatchDto.class))).willReturn(hospital);
        given(service.updateHospital(Mockito.any(Hospital.class))).willReturn(hospital);
        given(mapper.hospitalToSimpleResponseDto(Mockito.any(Hospital.class))).willReturn(new HospitalDto.SimpleResponseDto(id));

        HospitalDto.PatchDto patchDto = HospitalDto.PatchDto.builder()
                .hospitalId(id)
                .password(hospital.getPassword())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .age(hospital.getAge())
                .medicalSubject(hospital.getMedicalSubject())
                .telNum(hospital.getTelNum())
                .openingTime(hospital.getOpeningTime())
                .closingTime(hospital.getClosingTime())
                .breakStartTime(hospital.getBreakStartTime())
                .breakEndTime(hospital.getBreakEndTime())
                .intro(hospital.getIntro())
                .build();

        String requestBody = gson.toJson(patchDto);

        //when
        ResultActions actions = mockMvc.perform(patch("/api/hospital/{hospitalId}", id)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody));

        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("hospitalId").value(id))
                .andDo(document("patch-hospital",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        requestFields(List.of(
                                fieldWithPath("hospitalId").ignored(),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("name").description("병원이름"),
                                fieldWithPath("address").description("병원주소"),
                                fieldWithPath("age").description("업력"),
                                fieldWithPath("medicalSubject").description("진료과목"),
                                fieldWithPath("telNum").description("전화번호"),
                                fieldWithPath("openingTime").description("영업시작시간"),
                                fieldWithPath("closingTime").description("영업종료시간"),
                                fieldWithPath("breakStartTime").description("휴식시작시간"),
                                fieldWithPath("breakEndTime").description("휴식종료시간"),
                                fieldWithPath("intro").description("병원소개")
                        )),
                        responseFields(fieldWithPath("hospitalId").description("병원 식별자"))
                ));
    }

    @DisplayName("병원 탈퇴")
    @WithMockUser(roles = {"HOSPITAL"})
    @Test
    void deleteHospital() throws Exception {
        doNothing().when(service).deleteHospital(Mockito.anyLong());

        ResultActions actions = mockMvc.perform(delete("/api/hospital/{hospitalId}", id)
                .header("Authorization", "Bearer token"));

        actions.andExpect(status().isNoContent())
                .andDo(document("delete-hospital",
                        requestHeaders(headerWithName("Authorization").description("병원 액세스 토큰")),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자"))
                ));
    }

    @DisplayName("병원 개별 조회")
    @Test
    @WithMockUser(roles = {"HOSPITAL"})
    void getHospital() throws Exception {
        //given
        given(service.getHospital(Mockito.anyLong())).willReturn(hospital);
        given(mapper.hospitalToResponseDto(Mockito.any(Hospital.class))).willReturn(responseDto);

        //when
        ResultActions actions = mockMvc.perform(get("/api/hospital/{hospitalId}", id)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hospitalId").value(id))
                .andExpect(jsonPath("$.data.name").value(hospital.getName()))
                .andExpect(jsonPath("$.data.address").value(hospital.getAddress()))
                .andExpect(jsonPath("$.data.age").value(hospital.getAge()))
                .andExpect(jsonPath("$.data.medicalSubject[0]").value(hospital.getMedicalSubject().get(0)))
                .andExpect(jsonPath("$.data.openingTime").value(hospital.getOpeningTime()))
                .andExpect(jsonPath("$.data.closingTime").value(hospital.getClosingTime()))
                .andExpect(jsonPath("$.data.breakStartTime").value(hospital.getBreakStartTime()))
                .andExpect(jsonPath("$.data.breakEndTime").value(hospital.getBreakEndTime()))
                .andExpect(jsonPath("$.data.intro").value(hospital.getIntro()))
                .andExpect(jsonPath("$.data.telNum").value(hospital.getTelNum()))
                .andExpect(jsonPath("$.data.hospitalStatus").value(hospital.getHospitalStatus().getStatus()))
                .andDo(document("get-hospital",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("병원 액세스 토큰")),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        responseFields(List.of(
                                fieldWithPath("data.hospitalId").description("병원 식별자"),
                                fieldWithPath("data.name").description("병원이름"),
                                fieldWithPath("data.address").description("병원주소"),
                                fieldWithPath("data.age").description("업력"),
                                fieldWithPath("data.medicalSubject").description("진료과목"),
                                fieldWithPath("data.openingTime").description("영업시작시간"),
                                fieldWithPath("data.closingTime").description("영업종료시간"),
                                fieldWithPath("data.breakStartTime").description("휴식시간시간"),
                                fieldWithPath("data.breakEndTime").description("휴식종료시간"),
                                fieldWithPath("data.intro").description("병원소개"),
                                fieldWithPath("data.telNum").description("전화번호"),
                                fieldWithPath("data.hospitalStatus").description("병원상태")
                        ))
                ));
    }

    @DisplayName("병원 목록 조회")
    @Test
    void getHospitals() throws Exception {
        //given
        List<Hospital> list = List.of(hospital);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hospital> pages = new PageImpl<>(list, pageable, list.size());
        given(service.getHospitals(Mockito.any(Pageable.class), Mockito.anyString())).willReturn(pages);
        given(mapper.hospitalsToResponseDto(Mockito.anyList())).willReturn(List.of(responseDto));

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("page", "1");
        queryParams.add("size", "10");
        queryParams.add("status", hospital.getHospitalStatus().toString());
        queryParams.add("sort", "views");

        //when
        ResultActions actions = mockMvc.perform(get("/api/hospital")
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
                .queryParams(queryParams)
        );

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hospitalId").value(id))
                .andExpect(jsonPath("$.data[0].name").value(hospital.getName()))
                .andExpect(jsonPath("$.data[0].address").value(hospital.getAddress()))
                .andExpect(jsonPath("$.data[0].age").value(hospital.getAge()))
                .andExpect(jsonPath("$.data[0].medicalSubject[0]").value(hospital.getMedicalSubject().get(0)))
                .andExpect(jsonPath("$.data[0].openingTime").value(hospital.getOpeningTime()))
                .andExpect(jsonPath("$.data[0].closingTime").value(hospital.getClosingTime()))
                .andExpect(jsonPath("$.data[0].breakStartTime").value(hospital.getBreakStartTime()))
                .andExpect(jsonPath("$.data[0].breakEndTime").value(hospital.getBreakEndTime()))
                .andExpect(jsonPath("$.data[0].intro").value(hospital.getIntro()))
                .andExpect(jsonPath("$.data[0].telNum").value(hospital.getTelNum()))
                .andExpect(jsonPath("$.data[0].hospitalStatus").value(hospital.getHospitalStatus().getStatus()))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-hospitals",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("병원 액세스 토큰")),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지").optional(),
                                parameterWithName("size").description("한 페이지당 요소 개수").optional(),
                                parameterWithName("status").description("병원 상태"),
                                parameterWithName("sort").description("정렬 (views)")
                        ),
                        responseFields(List.of(
                                fieldWithPath("data[].hospitalId").description("병원 식별자"),
                                fieldWithPath("data[].name").description("병원이름"),
                                fieldWithPath("data[].address").description("병원주소"),
                                fieldWithPath("data[].age").description("업력"),
                                fieldWithPath("data[].medicalSubject").description("진료과목"),
                                fieldWithPath("data[].openingTime").description("영업시작시간"),
                                fieldWithPath("data[].closingTime").description("영업종료시간"),
                                fieldWithPath("data[].breakStartTime").description("휴식시작시간"),
                                fieldWithPath("data[].breakEndTime").description("휴식종료시간"),
                                fieldWithPath("data[].intro").description("병원소개"),
                                fieldWithPath("data[].telNum").description("전화번호"),
                                fieldWithPath("data[].hospitalStatus").description("병원상태"),
                                fieldWithPath("pageInfo.page").description("현재 페이지"),
                                fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                        ))
                ));
    }

    @DisplayName("병원 상태 등록 [관리자용]")
    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void putHospitalRoles() throws Exception {
        //given
        doNothing().when(service).updateHospitalRoles(Mockito.anyLong(), Mockito.anyString());

        //when
        ResultActions actions = mockMvc
                .perform(put("/api/hospital/{hospitalId}/{status}", id, "normal")
                .header("Authorization", "Bearer token"));

        //then
        actions.andDo(print());
        actions.andExpect(status().isOk())
                .andDo(document("put-hospitalRoles",
                        pathParameters(
                                parameterWithName("hospitalId").description("병원 식별자"),
                                parameterWithName("status").description("등록할 상태 (normal/abnormal)")
                        )
                ));
    }
}