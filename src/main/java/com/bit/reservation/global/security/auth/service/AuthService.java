package com.bit.reservation.global.security.auth.service;

import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ExceptionCode;
import com.bit.reservation.global.security.auth.jwt.JwtTokenizer;
import com.bit.reservation.global.security.utils.CustomAuthorityUtils;
import com.bit.reservation.global.security.utils.RedisUtils;
import com.bit.reservation.domain.user.entity.User;
import com.bit.reservation.domain.user.repository.UserRepository;
import com.bit.reservation.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtTokenizer jwtTokenizer;
    private final RedisUtils redisUtils;
    private final CustomAuthorityUtils authorityUtils;

    public void logout(String accessToken) {
        String email = userService.checkUser().getEmail();
        String key = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        String token = accessToken.replace("Bearer ", "");
        Long atTime = jwtTokenizer.getExpiration(token, key);

        redisUtils.setBlackList(token, email + "expired_access", atTime);
        redisUtils.delete("refresh_" + email);
    }

    public String reissuedToken(String refreshToken) {
        String encodeBase64SecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        try {
            jwtTokenizer.verifySignature(refreshToken, encodeBase64SecretKey);
        } catch (SignatureException | MalformedJwtException | DecodingException e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_VALUES);
        } catch (ExpiredJwtException e) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
        }

        Jws<Claims> claims = jwtTokenizer.getClaims(refreshToken, encodeBase64SecretKey);
        String subject = claims.getBody().getSubject();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        if (!redisUtils.hasKey("refresh_" + subject) || !redisUtils.get("refresh_" + subject).equals(refreshToken)) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED);
        }

        Map<String, Object> map = new HashMap<>();
        boolean isHospital = userService.findByEmail(subject).orElse(null) == null ? true : false;
        List<String> roles = authorityUtils.createRoles(subject, isHospital);
        map.put("username", subject);
        map.put("roles", roles);

        return jwtTokenizer.generateAccessToken(map, subject, expiration, encodeBase64SecretKey);
    }
}
