package com.bit.reservation.domain.hospital.controller;

import com.bit.reservation.domain.hospital.dto.HospitalNoticeDto;
import com.bit.reservation.domain.hospital.entity.HospitalNotice;
import com.bit.reservation.domain.hospital.mapper.HospitalNoticeMapper;
import com.bit.reservation.domain.hospital.service.HospitalNoticeService;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HospitalNoticeController.class, excludeAutoConfiguration = SecurityConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class HospitalNoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private HospitalNoticeService service;

    @MockBean
    private HospitalNoticeMapper mapper;

    private HospitalNotice notice;

    HospitalNoticeDto.BasicDto basicDto;

    @BeforeEach
    void setUp() {
        notice = StubData.getNotice();
        basicDto = new HospitalNoticeDto.BasicDto(1L, "제목", "내용");
    }

    @DisplayName("공지사항 등록 테스트")
    @Test
    void postNotice() throws Exception {
        //given
        HospitalNoticeDto.BasicDto basicDto = new HospitalNoticeDto.BasicDto();
        basicDto.setTitle("제목");
        basicDto.setContent("내용");
        String requestBody = gson.toJson(basicDto);

        given(mapper.basicDtoToNotice(Mockito.any(HospitalNoticeDto.BasicDto.class))).willReturn(notice);
        given(service.createNotice(Mockito.any(HospitalNotice.class))).willReturn(notice);
        given(mapper.noticeToSimpleResponseDto(Mockito.any(HospitalNotice.class))).willReturn(new HospitalNoticeDto.SimpleResponseDto(1L));

        //when
        ResultActions actions = mockMvc.perform(post("/api/hospital-notice")
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.hospitalNoticeId").value(1L))
                .andDo(document("post-notice",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        requestFields(
                                List.of(fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                                )
                        ),
                        responseFields(fieldWithPath("hospitalNoticeId").type(JsonFieldType.NUMBER).description("공지사항 식별자"))
                ));
    }

    @DisplayName("공지사항 수정 테스트")
    @Test
    void patchNotice() throws Exception {
        //given
        Long noticeId = 1L;
        String requestBody = gson.toJson(basicDto);

        given(mapper.basicDtoToNotice(Mockito.any(HospitalNoticeDto.BasicDto.class))).willReturn(notice);
        given(service.updateNotice(Mockito.any(HospitalNotice.class))).willReturn(notice);
        given(mapper.noticeToSimpleResponseDto(Mockito.any(HospitalNotice.class))).willReturn(new HospitalNoticeDto.SimpleResponseDto(1L));

        //when
        ResultActions actions = mockMvc.perform(patch("/api/hospital-notice/{hospitalNoticeId}", noticeId)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalNoticeId").value(1L))
                .andDo(document("patch-notice",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("hospitalNoticeId").description("공지사항 식별자")),
                        requestFields(
                                List.of(
                                        fieldWithPath("hospitalNoticeId").type(JsonFieldType.NUMBER).ignored(),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                                )
                        ),
                        responseFields(fieldWithPath("hospitalNoticeId").type(JsonFieldType.NUMBER).description("공지사항 식별자"))
                ));
    }

    @DisplayName("공지사항 1건 조회 테스트")
    @Test
    void getNotice() throws Exception {
        //given
        Long noticeId = 1L;
        given(service.findNotice(Mockito.anyLong())).willReturn(notice);
        given(mapper.noticeToBasicDto(Mockito.any(HospitalNotice.class))).willReturn(basicDto);

        //when
        ResultActions actions =
                mockMvc.perform(get("/api/hospital-notice/{hospitalNoticeId}", noticeId)
                .accept(MediaType.APPLICATION_JSON));

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.hospitalNoticeId").value(1L))
                .andExpect(jsonPath("$.title").value(notice.getTitle()))
                .andExpect(jsonPath("$.content").value(notice.getContent()))
                .andDo(document("get-notice",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("hospitalNoticeId").description("공지사항 식별자")),
                        responseFields(List.of(
                                fieldWithPath("hospitalNoticeId").type(JsonFieldType.NUMBER).description("공지사항 식별자"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")
                        ))
                ));
    }

    @DisplayName("병원 공지사항 목록 조회 테스트")
    @Test
    void getNotices() throws Exception {
        //given
        Long id = 1L;
        List<HospitalNotice> notices = List.of(notice);
        Pageable pageable = PageRequest.of(0, 10);
        Page<HospitalNotice> pages = new PageImpl<>(notices, pageable, 1);

        given(service.findNotices(Mockito.any(Pageable.class), Mockito.anyLong())).willReturn(pages);
        given(mapper.noticesToSimpleDto(Mockito.anyList()))
                .willReturn(List.of(new HospitalNoticeDto.SimpleDto(1L, "제목")));

        //when
        ResultActions actions =
                mockMvc.perform(get("/api/hospital-notice/hospital/{hospitalId}", id)
                        .accept(MediaType.APPLICATION_JSON));

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hospitalNoticeId").value(1L))
                .andExpect(jsonPath("$.data[0].title").value(notice.getTitle()))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-notices",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("hospitalId").description("병원 식별자")),
                        responseFields(List.of(
                                fieldWithPath("data[].hospitalNoticeId").type(JsonFieldType.NUMBER).description("공지사항 식별자"),
                                fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("페이지당 요소 개수"),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 요소 개수"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수")

                        ))
                ));
    }

    @DisplayName("공지사항 삭제 테스트")
    @Test
    void deleteNotice() throws Exception {
        //given
        Long id = 1L;
        doNothing().when(service).deleteNotice(Mockito.anyLong());

        //when
        ResultActions actions = mockMvc.perform(delete("/api/hospital-notice/{hospitalNoticeId}", id)
                .header("Authorization", "Bearer token"));

        //then
        actions.andExpect(status().isNoContent())
                .andDo(document("delete-notice",
                        pathParameters(parameterWithName("hospitalNoticeId").description("공지사항 식별자")),
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰"))
                ));
    }
}