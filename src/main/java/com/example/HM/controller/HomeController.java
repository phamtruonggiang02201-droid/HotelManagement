package com.example.HM.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Returns the view name "index" (src/main/resources/templates/index.html)
        return "index";
    }
}
