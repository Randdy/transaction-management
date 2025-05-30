package com.hsbc.wpb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SwaggerController {
    
    @GetMapping("/")
    public RedirectView redirectToSwagger() {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/api/swagger-ui.html");
        return redirectView;
    }
} 