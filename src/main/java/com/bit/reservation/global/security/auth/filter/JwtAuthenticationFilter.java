package com.bit.reservation.global.security.auth.filter;

import com.bit.reservation.domain.hospital.entity.Hospital;
import com.bit.reservation.domain.hospital.repository.HospitalRepository;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import com.bit.reservation.global.security.auth.jwt.JwtTokenizer;
import com.bit.reservation.global.security.dto.LoginDto;
import com.bit.reservation.global.security.utils.CustomAuthorityUtils;
import com.bit.reservation.global.security.utils.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;
    private final RedisUtils redisUtils;
    private final CustomAuthorityUtils authorityUtils;

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //로그인 http method : POST
        if (!request.getMethod().equals("POST")) {
            throw new BusinessLogicException(ExceptionCode.METHOD_NOT_ALLOWED);
        }
        LoginDto loginDto = new LoginDto();
        UsernamePasswordAuthenticationToken authenticationToken;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            authenticationToken
                    = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());
        }

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        User user;
        String id = "userId";

        try {
            user = (User) authResult.getPrincipal();
            user.setRoles(authorityUtils.createRoles(user.getEmail(), false));
        } catch (ClassCastException e) {
            Hospital hospital = (Hospital) authResult.getPrincipal();
            user = new User();
            user.setEmail(hospital.getEmail());
            user.setUserId(hospital.getHospitalId());
            //TODO : roles lazy로 할 수 있는 방법 찾기
            user.setRoles(authorityUtils.getHospitalRoles(hospital.getEmail()));
            id = "hospitalId";
        }
        log.info("roles : {}", user.getRoles());

        String accessToken = delegateAccessToken(user);
        String refreshToken = delegateRefreshToken(user);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"" + id + "\":" + user.getUserId() + "}"
        );

        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }

    private String delegateAccessToken(User user) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("username", user.getEmail());
        claims.put("roles", user.getRoles());

        String subject = user.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        String encodeBase64SecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        return jwtTokenizer.generateAccessToken(claims, subject, expiration, encodeBase64SecretKey);
    }

    private String delegateRefreshToken(User user) {
        String subject = user.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String encodeBase64SecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, encodeBase64SecretKey);

        //refresh 발급시 redis에 저장
        redisUtils.set("refresh_" + user.getEmail(), refreshToken, jwtTokenizer.getRefreshTokenExpirationMinutes());

        return refreshToken;
    }
}
