package com.example.HM.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllExceptions(Exception ex, Model model, HttpServletRequest request) {
        // Dùng logger thay vì ex.printStackTrace() để tránh output không kiểm soát
        log.error("Lỗi không xử lý được tại {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorType", ex.getClass().getSimpleName());
        
        return "error";
    }
}
