package com.bit.reservation.global.security.auth.controller;

import com.bit.reservation.global.security.auth.dto.AuthDto;
import com.bit.reservation.global.security.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity logout(@RequestHeader("Authorization") @NotBlank String accessToken) {
        authService.logout(accessToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //토큰 재발급
    @PostMapping("/token")
    public ResponseEntity reissueAccessToken(@RequestHeader("Refresh") @NotBlank String refreshToken,
                                             HttpServletResponse response) {

        String newAccessToken = authService.reissuedToken(refreshToken);
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

