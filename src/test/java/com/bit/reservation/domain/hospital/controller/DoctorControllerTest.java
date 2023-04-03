package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.DoctorDto;
import com.bit.reservation.domain.hospital.entity.Doctor;
import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.mapper.DoctorMapper;
import com.bit.reservation.domain.hospital.service.DoctorService;
import com.bit.reservation.global.config.SecurityConfiguration;
import com.bit.reservation.global.status.HospitalStatus;
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

import java.util.List;

import static com.bit.reservation.global.utils.ApiDocumentUtils.getRequestPreProcessor;
import static com.bit.reservation.global.utils.ApiDocumentUtils.getResponsePreProcessor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DoctorController.class, excludeAutoConfiguration = SecurityConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private DoctorService service;

    @MockBean
    private DoctorMapper mapper;

    private Doctor doctor;
    Long id = 1L;

    @BeforeEach
    void setUp() {
        doctor = StubData.getDoctor();
    }

    @DisplayName("의사 등록 테스트")
    @Test
    void postDoctor() throws Exception {
        //given
        DoctorDto.PostDto postDto = new DoctorDto.PostDto("의사1", "학교이름", "경력", List.of("진료과목"));
        String requestBody = gson.toJson(postDto);
        given(mapper.postDtoToDoctor(Mockito.any(DoctorDto.PostDto.class))).willReturn(doctor);
        given(service.createDoctor(Mockito.any(Doctor.class), Mockito.anyLong())).willReturn(doctor);
        given(mapper.doctorToSimpleResponseDto(Mockito.any(Doctor.class))).willReturn(new DoctorDto.SimpleResponseDto(1L));

        //when
        ResultActions actions = mockMvc.perform(post("/api/doctors/hospital/{hospitalId}", id)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(id))
                .andDo(document("post-doctor",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        requestFields(List.of(
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("school").description("학교").optional(),
                                fieldWithPath("career").description("경력").optional(),
                                fieldWithPath("medicalSubject").description("진료 과목")
                        )),
                        responseFields(fieldWithPath("doctorId").description("의사 식별자"))
                ));
    }

    @DisplayName("의사 수정 테스트")
    @Test
    void patchDoctor() throws Exception {
        //given
        DoctorDto.PatchDto patchDto = new DoctorDto.PatchDto(1L, "의사1", "학교이름", "경력", List.of("진료과목"));
        String requestBody = gson.toJson(patchDto);
        given(mapper.patchDtoToDoctor(Mockito.any(DoctorDto.PatchDto.class))).willReturn(doctor);
        given(service.updateDoctor(Mockito.any(Doctor.class))).willReturn(doctor);
        given(mapper.doctorToSimpleResponseDto(Mockito.any(Doctor.class))).willReturn(new DoctorDto.SimpleResponseDto(1L));

        //when
        ResultActions actions = mockMvc.perform(patch("/api/doctors/{doctorId}", id)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(id))
                .andDo(document("patch-doctor",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("doctorId").description("의사 식별자")),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        requestFields(List.of(
                                fieldWithPath("doctorId").ignored(),
                                fieldWithPath("name").description("이름"),
                                fieldWithPath("school").description("학교").optional(),
                                fieldWithPath("career").description("경력").optional(),
                                fieldWithPath("medicalSubject").description("진료 과목")
                        )),
                        responseFields(fieldWithPath("doctorId").description("의사 식별자"))
                ));
    }

    @DisplayName("의사 삭제 테스트")
    @Test
    void deleteDoctor() throws Exception {
        doNothing().when(service).deleteDoctor(Mockito.anyLong());

        ResultActions actions =
                mockMvc.perform(delete("/api/doctors/{doctorId}", id)
                .header("Authorization", "Bearer token"));

        actions.andExpect(status().isNoContent())
                .andDo(document("delete-doctor",
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("doctorId").description("의사 식별자"))
                ));
    }

    @DisplayName("의사 조회")
    @Test
    void getDoctor() throws Exception {
        //given
        DoctorDto.ResponseDto res = new DoctorDto.ResponseDto(1L, doctor.getName(), doctor.getSchool(), doctor.getCareer(), doctor.getMedicalSubject());
        given(service.getDoctor(Mockito.anyLong())).willReturn(doctor);
        given(mapper.doctorToResponseDto(Mockito.any(Doctor.class))).willReturn(res);

        //when
        ResultActions actions = mockMvc.perform(get("/api/doctors/{doctorId}", id));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.doctorId").value(1L))
                .andExpect(jsonPath("$.data.name").value(doctor.getName()))
                .andExpect(jsonPath("$.data.school").value(doctor.getSchool()))
                .andExpect(jsonPath("$.data.career").value(doctor.getCareer()))
                .andExpect(jsonPath("$.data.medicalSubject[0]").value(doctor.getMedicalSubject().get(0)))
                .andDo(document("get-doctor",
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("doctorId").description("의사 식별자")),
                        responseFields(List.of(
                                fieldWithPath("data.doctorId").description("의사 식별자"),
                                fieldWithPath("data.name").description("이름"),
                                fieldWithPath("data.school").description("학교"),
                                fieldWithPath("data.career").description("경력"),
                                fieldWithPath("data.medicalSubject").description("진료 과목")
                        ))
                ));
    }

    @DisplayName("전체 의사 목록 조회 테스트 [관리자용]")
    @Test
    void getDoctors() throws Exception {

        List<Doctor> doctors = List.of(doctor);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Doctor> pages = new PageImpl(doctors, pageable, doctors.size());
        DoctorDto.ResponseDto res = new DoctorDto.ResponseDto(1L, doctor.getName(), doctor.getSchool(), doctor.getCareer(), doctor.getMedicalSubject());

        given(service.getDoctors(Mockito.anyInt(), Mockito.anyInt())).willReturn(pages);
        given(mapper.doctorsToResponseDto(Mockito.anyList())).willReturn(List.of(res));

        ResultActions actions = mockMvc.perform(get("/api/doctors?page=1&size=10")
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
        );

        actions.andDo(print());

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].doctorId").value(doctor.getDoctorId()))
                .andExpect(jsonPath("$.data[0].name").value(doctor.getName()))
                .andExpect(jsonPath("$.data[0].school").value(doctor.getSchool()))
                .andExpect(jsonPath("$.data[0].career").value(doctor.getCareer()))
                .andExpect(jsonPath("$.data[0].medicalSubject[0]").value(doctor.getMedicalSubject().get(0)))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-doctors (admin)",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지"),
                                parameterWithName("size").description("한 페이지당 요소 개수")
                        ),
                        responseFields(List.of(
                                fieldWithPath("data[].doctorId").description("의사 식별자"),
                                fieldWithPath("data[].name").description("이름"),
                                fieldWithPath("data[].school").description("학교"),
                                fieldWithPath("data[].career").description("경력"),
                                fieldWithPath("data[].medicalSubject").description("진료 과목"),
                                fieldWithPath("pageInfo.page").description("현재 페이지"),
                                fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                        ))
                ));
    }

    @DisplayName("병원 의사 목록 조회 테스트")
    @Test
    void getHospitalDoctors() throws Exception {
        List<Doctor> doctors = List.of(doctor);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Doctor> pages = new PageImpl(doctors, pageable, doctors.size());
        DoctorDto.ResponseDto res = new DoctorDto.ResponseDto(1L, doctor.getName(), doctor.getSchool(), doctor.getCareer(), doctor.getMedicalSubject());

        given(service.findHospitalDoctors(Mockito.any(Pageable.class), Mockito.anyLong())).willReturn(pages);
        given(mapper.doctorsToResponseDto(Mockito.anyList())).willReturn(List.of(res));

        ResultActions actions =
                mockMvc.perform(get("/api/doctors/hospital/{hospitalId}", id)
                .accept(MediaType.APPLICATION_JSON));

        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].doctorId").value(doctor.getDoctorId()))
                .andExpect(jsonPath("$.data[0].name").value(doctor.getName()))
                .andExpect(jsonPath("$.data[0].school").value(doctor.getSchool()))
                .andExpect(jsonPath("$.data[0].career").value(doctor.getCareer()))
                .andExpect(jsonPath("$.data[0].medicalSubject[0]").value(doctor.getMedicalSubject().get(0)))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-doctors",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지").optional(),
                                parameterWithName("size").description("한 페이지당 요소 개수").optional()
                        ),
                        responseFields(List.of(
                                fieldWithPath("data[].doctorId").description("의사 식별자"),
                                fieldWithPath("data[].name").description("이름"),
                                fieldWithPath("data[].school").description("학교"),
                                fieldWithPath("data[].career").description("경력"),
                                fieldWithPath("data[].medicalSubject").description("진료 과목"),
                                fieldWithPath("pageInfo.page").description("현재 페이지"),
                                fieldWithPath("pageInfo.size").description("한 페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").description("총 페이지 수")
                        ))
                ));
    }
}