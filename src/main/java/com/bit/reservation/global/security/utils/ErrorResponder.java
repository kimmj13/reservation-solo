package com.bit.reservation.global.security.utils;

import com.bit.reservation.global.exception.BusinessLogicException;
import com.bit.reservation.global.exception.ErrorResponse;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ErrorResponder {
    public static void sendErrorResponse(HttpServletResponse response, HttpStatus status) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = ErrorResponse.of(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }

    public static void sendCustomErrorResponse(HttpServletResponse response, BusinessLogicException businessLogicException) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = ErrorResponse.of(businessLogicException.getExceptionCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(businessLogicException.getExceptionCode().getStatus());
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
