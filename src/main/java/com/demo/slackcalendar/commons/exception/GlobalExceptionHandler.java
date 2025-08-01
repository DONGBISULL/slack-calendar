package com.demo.slackcalendar.commons.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.vault.VaultException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(VaultException.class)
    public Object handleVaultException(VaultException e, HttpServletRequest request) {
        log.error("Vault error: {}", e.getMessage());

        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Vault access failed", "message", e.getMessage()));
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("error/vault");
            mav.addObject("message", e.getMessage());
            return mav;
        }
    }

    @ExceptionHandler(GoogleOAuthException.class)
    public Object handleVaultException(GoogleOAuthException e, HttpServletRequest request) {
        log.error("Google Auth error: {}", e.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Google access failed", "message", e.getMessage()));
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("error/vault");
            mav.addObject("message", e.getMessage());
            return mav;
        }
    }

    @ExceptionHandler
    public Object exception(Exception e, HttpServletRequest request) {
        log.error("Exception error: {}", e.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("error/error");
            return mav;
        }

    }

    private boolean isApiRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith) || request.getRequestURI().startsWith("/api/");
    }
}
