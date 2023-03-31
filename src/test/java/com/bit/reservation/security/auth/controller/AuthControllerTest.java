package com.bit.reservation.security.auth.controller;

import com.bit.reservation.domain.hospital.repository.HospitalRepository;
import com.bit.reservation.global.config.RedisConfig;
import com.bit.reservation.global.config.SecurityConfiguration;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.global.security.auth.controller.AuthController;
import com.bit.reservation.global.security.auth.jwt.JwtTokenizer;
import com.bit.reservation.global.security.auth.service.AuthService;
import com.bit.reservation.global.security.utils.CustomAuthorityUtils;
import com.bit.reservation.global.security.utils.RedisUtils;
import com.bit.reservation.domain.user.repository.UserRepository;
import com.bit.reservation.global.status.UserLevel;
import com.bit.reservation.global.status.UserProfileImage;
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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@Import({SecurityConfiguration.class,
        JwtTokenizer.class,
        CustomAuthorityUtils.class,
        RedisUtils.class,
        RedisConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private HospitalRepository hospitalRepository;

    @MockBean
    private AuthService authService;

    User userKim = new User();

    @BeforeEach
    void initial() {
        userKim = StubData.getUser();
    }

    @DisplayName("로그아웃 테스트")
    @Test
    void logout() throws Exception {
        //given
        doNothing().when(authService).logout(Mockito.anyString());

        //when
        ResultActions actions = mockMvc.perform(
                post("/api/auth/logout")
                        .header("Authorization", "token")
        );

        //then
        actions.andExpect(status().isOk())
                .andDo(document("logout",
                        requestHeaders(headerWithName("Authorization").description("액세스 토큰"))
                ));
    }

    @DisplayName("액세스토큰 재발급 테스트")
    @Test
    void reissueAccessToken() throws Exception {
        //given
        given(authService.reissuedToken(Mockito.anyString())).willReturn(Mockito.anyString());

        //when
        ResultActions actions = mockMvc.perform(post("/api/auth/token")
                .header("Refresh", "refresh token")
        );

        //then
        actions.andExpect(status().isOk())
                .andDo(document("reissue-token",
                        requestHeaders(headerWithName("Refresh").description("리프레시 토큰")),
                        responseHeaders(headerWithName("Authorization").description("재발급된 액세스 토큰"))
                        )
                );
    }

//    @DisplayName("로그인 테스트")
//    @WithMockUser
//    @Test
//    void login() throws Exception {
//        //given
//        LoginDto loginDto = new LoginDto("kim@test.com", "abcd12!@");
//        String requestBody = gson.toJson(loginDto);
//
//        ResultActions actions = mockMvc.perform(post("/api/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .content(requestBody)
//        );
//
//        actions.andExpect(status().isOk())
//                .andDo(document("login",
//                        getRequestPreProcessor(),
//                        getResponsePreProcessor(),
//                        requestFields(List.of(
//                                fieldWithPath("email").description("이메일(ID)"),
//                                fieldWithPath("password").description("비밀번호")
//                        )),
//                        responseHeaders(List.of(
//                                headerWithName("Authorization").description("액세스 토큰"),
//                                headerWithName("Refresh").description("리프레시 토큰")
//                                )
//                        )
//                ));
//    }
}