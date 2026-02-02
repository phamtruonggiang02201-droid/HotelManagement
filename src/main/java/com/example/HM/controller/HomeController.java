package com.example.HM.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        // Returns the view name "index" (src/main/resources/templates/index.html) - NOW LANDING PAGE
        return "layout/landing_layout";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // Returns the dashboard view (src/main/resources/templates/dashboard.html)
        return "layout/main_layout";
    }

    @GetMapping({"/login", "/Login"})
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping({"/register", "/Register"})
    public String register() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "auth/reset-password";
    }
}
