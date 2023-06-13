package com.jwconsulting.invoicemanagement.utils;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwconsulting.invoicemanagement.exception.ApiException;
import com.jwconsulting.invoicemanagement.model.HttpResponse;
import io.jsonwebtoken.InvalidClaimException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.io.OutputStream;

import static java.time.LocalDateTime.now;

@Slf4j
public class ExceptionUtils {

    public static void processError(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        if(exception instanceof ApiException || exception instanceof BadCredentialsException || exception instanceof DisabledException ||
                exception instanceof InvalidClaimException || exception instanceof LockedException) {
            HttpResponse httpResponse = getHttpResponse(request, response, exception.getMessage(), HttpStatus.BAD_REQUEST);
            writeResponse(response, httpResponse);
        } else if(exception instanceof TokenExpiredException) {
            HttpResponse httpResponse = getHttpResponse(request, response, exception.getMessage(), HttpStatus.UNAUTHORIZED);
            writeResponse(response, httpResponse);
        }
        else {
            HttpResponse httpResponse = getHttpResponse(request, response, "An error occurred. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
            writeResponse(response, httpResponse);
        }
        log.error(exception.getMessage());
    }

    private static void writeResponse(HttpServletResponse response, HttpResponse httpResponse) {
        OutputStream out;
        try {
            out = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, httpResponse);
            out.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private static HttpResponse getHttpResponse(HttpServletRequest request, HttpServletResponse response, String message, HttpStatus httpStatus) {
        HttpResponse httpResponse = HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(message)
                .path(request.getRequestURI())
                .status(httpStatus)
                .statusCode(httpStatus.value())
                .build();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        return httpResponse;
    }
}
