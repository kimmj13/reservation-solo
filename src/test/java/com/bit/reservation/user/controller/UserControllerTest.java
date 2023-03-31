package com.bit.reservation.user.controller;

import com.bit.reservation.domain.hospital.controller.DoctorController;
import com.bit.reservation.domain.hospital.repository.HospitalRepository;
import com.bit.reservation.domain.user.controller.UserController;
import com.bit.reservation.domain.user.dto.UserDto;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.domain.user.mapper.UserMapper;
import com.bit.reservation.domain.user.repository.UserRepository;
import com.bit.reservation.domain.user.service.UserService;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@Import({SecurityConfiguration.class,
        JwtTokenizer.class,
        CustomAuthorityUtils.class,
        RedisUtils.class,
        RedisConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserMapper mapper;

    @MockBean
    private HospitalRepository hospitalRepository;

    User userKim = new User();

    @BeforeEach
    void initial() {
        userKim = StubData.getUser();
    }

    @Test
    @DisplayName("회원가입 테스트")
    void postUser() throws Exception {
        //given
        UserDto.PostDto postDto = new UserDto.PostDto("kim", "kim@test.com", "abcd12!@",
                "부산광역시 동래구", 100, "010-1111-1111");

        String requestBody = gson.toJson(postDto);

        UserDto.SimpleResponseDto simpleResponseDto = new UserDto.SimpleResponseDto(1L);
        given(mapper.postDtoToUser(Mockito.any(UserDto.PostDto.class))).willReturn(userKim);
        given(userService.join(Mockito.any(User.class))).willReturn(userKim);
        given(mapper.userToSimpleResponseDto(Mockito.any(User.class))).willReturn(simpleResponseDto);

        //when
        ResultActions actions =
                mockMvc.perform(
                        post("/api/users/sign-up")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                );

        //then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L))
                .andDo(document("sign-up",
                                getRequestPreProcessor(),
                                getResponsePreProcessor(),
                                requestFields(
                                        List.of(fieldWithPath("userName").type(JsonFieldType.STRING).description("이름"),
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일(ID)"),
                                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                                fieldWithPath("address").type(JsonFieldType.STRING).description("주소"),
                                                fieldWithPath("age").type(JsonFieldType.NUMBER).description("나이"),
                                                fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("휴대폰 번호")
                                        )
                                ),
                                responseFields(List.of(fieldWithPath("userId").type(JsonFieldType.NUMBER).description("회원 식별자")))
                        )
                );
    }

    @Test
    @DisplayName("회원 정보 수정 테스트")
    @WithMockUser
    void patchUser() throws Exception {
        //given
        UserDto.PatchDto patchDto = new UserDto.PatchDto(1L, "hong", "abcd12@!", "부산광역시 해운대구", 90, "010-0000-0000");

        String requestBody = gson.toJson(patchDto);

        userKim.setUserName("hong");
        userKim.setPassword("abcd12@!");
        userKim.setAddress("부산광역시 해운대구");
        userKim.setAge(90);
        userKim.setPhoneNumber("010-0000-0000");

        UserDto.SimpleResponseDto simpleResponseDto = new UserDto.SimpleResponseDto(1L);
        given(mapper.patchDtoToUser(Mockito.any(UserDto.PatchDto.class))).willReturn(userKim);
        given(userService.updateUser(Mockito.any(User.class))).willReturn(userKim);
        given(mapper.userToSimpleResponseDto(Mockito.any(User.class))).willReturn(simpleResponseDto);

        //when
        ResultActions actions = mockMvc.perform(
                patch("/api/users/{userId}", 1L)
                        .header("Authorization", "Bearer token")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andDo(document("patch-user",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(
                                headerWithName("Authorization").description("액세스 토큰")),
                        pathParameters(parameterWithName("userId").description("회원 식별자")),
                        requestFields(List.of(
                                        fieldWithPath("userId").type(JsonFieldType.NUMBER).description("회원 식별자").ignored(),
                                        fieldWithPath("userName").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                        fieldWithPath("address").type(JsonFieldType.STRING).description("주소"),
                                        fieldWithPath("age").type(JsonFieldType.NUMBER).description("나이"),
                                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("휴대폰 번호")
                                )
                        ),
                        responseFields(fieldWithPath("userId").type(JsonFieldType.NUMBER).description("회원 식별자"))
                ));
    }

    @DisplayName("회원 탈퇴 테스트")
    @Test
    @WithMockUser
    void deleteUser() throws Exception {
        //given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(Mockito.anyLong());

        //when
        ResultActions actions = mockMvc.perform(
                delete("/api/users/{userId}", userId)
                        .header("Authorization", "Bearer token")
        );

        actions.andExpect(status().isNoContent())
                .andDo(document
                        ("delete-user",
                                getRequestPreProcessor(),
                                getResponsePreProcessor(),
                                pathParameters(parameterWithName("userId").description("회원 식별자")),
                                requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰"))
                        )
                );
    }

    @DisplayName("회원 조회 (1인)")
    @Test
    void getUser() throws Exception {
        //given
        Long userId = 1L;
        UserDto.ResponseDto responseDto = new UserDto.ResponseDto(1L, userKim.getUserName(), userKim.getProfileImage(), userKim.getUserLevel());

        given(userService.getUser(Mockito.anyLong())).willReturn(userKim);
        given(mapper.userToResponseDto(Mockito.any(User.class))).willReturn(responseDto);

        //when
        ResultActions actions = mockMvc.perform(
                get("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.userName").value(userKim.getUserName()))
                .andExpect(jsonPath("$.profileImage").value(userKim.getProfileImage().getImage()))
                .andExpect(jsonPath("$.userLevel").value(userKim.getUserLevel().toString()))
                .andDo(document("get-user",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(parameterWithName("userId").description("회원 식별자")),
                        responseFields(List.of(
                                        fieldWithPath("userId").type(JsonFieldType.NUMBER).description("회원 식별자"),
                                        fieldWithPath("userName").type(JsonFieldType.STRING).description("이름"),
                                        fieldWithPath("profileImage").type(JsonFieldType.STRING).description("프로필 이미지"),
                                        fieldWithPath("userLevel").type(JsonFieldType.STRING).description("회원 등급")
                                )
                        )
                ));
    }

    @DisplayName("회원 목록 조회")
    @WithMockUser(roles = {"USER", "ADMIN"})
    @Test
    void getAllUsers() throws Exception {
        //given
        UserDto.ListResponseDto res = UserDto.ListResponseDto.builder()
                .userId(userKim.getUserId())
                .userName(userKim.getUserName())
                .email(userKim.getEmail())
                .address(userKim.getAddress())
                .age(userKim.getAge())
                .phoneNumber(userKim.getPhoneNumber())
                .profileImage(userKim.getProfileImage())
                .userLevel(userKim.getUserLevel())
                .userStatus(userKim.getUserStatus())
                .createdAt(userKim.getCreatedAt().toLocalDate())
                .modifiedAt(userKim.getModifiedAt().toLocalDate())
                .build();

        List<User> users = List.of(userKim);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> pages = new PageImpl<>(users, pageable, 1);

        given(userService.getAllUser(Mockito.any(Pageable.class))).willReturn(pages);
        given(mapper.usersToResponseDto(Mockito.anyList())).willReturn(List.of(res));

        //when
        ResultActions actions = mockMvc.perform(
                get("/api/users")
                        .header("Authorization", "Bearer token")
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(userKim.getUserId()))
                .andExpect(jsonPath("$.data[0].userName").value(userKim.getUserName()))
                .andExpect(jsonPath("$.data[0].email").value(userKim.getEmail()))
                .andExpect(jsonPath("$.data[0].address").value(userKim.getAddress()))
                .andExpect(jsonPath("$.data[0].age").value(userKim.getAge()))
                .andExpect(jsonPath("$.data[0].phoneNumber").value(userKim.getPhoneNumber()))
                .andExpect(jsonPath("$.data[0].profileImage").value(userKim.getProfileImage().getImage()))
                .andExpect(jsonPath("$.data[0].userLevel").value(userKim.getUserLevel().toString()))
                .andExpect(jsonPath("$.data[0].userStatus").value(userKim.getUserStatus().toString()))
                .andExpect(jsonPath("$.data[0].createdAt").value(userKim.getCreatedAt().toLocalDate().toString()))
                .andExpect(jsonPath("$.data[0].modifiedAt").value(userKim.getCreatedAt().toLocalDate().toString()))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(10))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andDo(document("get-users",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestHeaders(headerWithName("Authorization").description("관리자 액세스 토큰")),
                        requestParameters(
                                parameterWithName("page").description("[선택] 조회할 페이지").optional(),
                                parameterWithName("size").description("[선택] 한페이지당 요소 개수 (기본 10개)").optional()
                        ),
                        responseFields(List.of(
                                        fieldWithPath("data[].userId").description("회원 식별자"),
                                        fieldWithPath("data[].userName").description("이름"),
                                        fieldWithPath("data[].email").description("이메일"),
                                        fieldWithPath("data[].address").description("주소"),
                                        fieldWithPath("data[].age").description("나이"),
                                        fieldWithPath("data[].phoneNumber").description("휴대폰 번호"),
                                        fieldWithPath("data[].profileImage").description("프로필 이미지"),
                                        fieldWithPath("data[].userLevel").description("회원 등급"),
                                        fieldWithPath("data[].userStatus").description("회원 상태"),
                                        fieldWithPath("data[].createdAt").description("가입 일자"),
                                        fieldWithPath("data[].modifiedAt").description("마지막 수정 일자"),
                                        fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                        fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("페이지당 요소 개수"),
                                        fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 요소 개수"),
                                        fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수")
                                )
                        )
                ));

    }
}