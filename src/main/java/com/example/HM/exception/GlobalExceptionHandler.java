package com.example.HM.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAllExceptions(Exception ex, Model model) {
        // In a real app, log the exception here
        ex.printStackTrace();
        
        // Pass error information to the view
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorType", ex.getClass().getSimpleName());
        
        // Return the name of the error view (e.g., "error" or "error/500")
        return "error";
    }
    
    // You can add more specific exception handlers here (e.g., ResourceNotFoundException)
}
