package com.example.HM.controller;

import com.example.HM.security.SecurityUtils;
import com.example.HM.service.HeroSliderService;
import com.example.HM.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HeroSliderService heroSliderService;
    private final FeedbackService feedbackService;

    @GetMapping("/")
    public String index(Model model) {
        // Get top 5 active sliders
        model.addAttribute("sliders", heroSliderService.getActiveSliders(PageRequest.of(0, 5, Sort.by("displayOrder").ascending())).getContent());
        return "index";
    }

    @GetMapping("/feedback")
    public String publicFeedback(Model model, @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedbacks(pageable));
        return "feedback";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/services")
    public String services() {
        return "service/list";
    }

    @GetMapping({"/login", "/Login"})
    public String login() {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping({"/register", "/Register"})
    public String register() {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/reset-password";
    }
}
