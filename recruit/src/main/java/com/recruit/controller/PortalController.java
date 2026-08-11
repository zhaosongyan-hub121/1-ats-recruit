package com.recruit.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Hidden
@Controller
public class PortalController {

    @GetMapping("/")
    public String root() {
        return "redirect:/portal";
    }

    @GetMapping({"/portal", "/portal/index"})
    public String index() {
        return "portal/index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/portal/position/{id}")
    public String positionDetail(@PathVariable Long id) {
        return "portal/position";
    }

    @GetMapping("/portal/profile")
    public String profilePage() {
        return "portal/profile";
    }
}
