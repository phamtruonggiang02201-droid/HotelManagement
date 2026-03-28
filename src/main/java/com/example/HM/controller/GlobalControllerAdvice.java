package com.example.HM.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class GlobalControllerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmer = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmer);
    }

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        if (request.getRequestURI().contains("/api/")) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        // For non-API requests, we could re-throw or return a generic error page
        // But to avoid the SyntaxError in JS, we return JSON if it's an /api/ call
        return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
    }
}
