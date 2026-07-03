package com.gahih.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServicePageController {

    @GetMapping("/terms")
    public String terms() {
        return "service/terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "service/privacy";
    }

    @GetMapping("/policy")
    public String policy() {
        return "service/policy";
    }

    @GetMapping("/disclaimer")
    public String disclaimer() {
        return "service/disclaimer";
    }

    @GetMapping("/contact")
    public String contact() {
        return "service/contact";
    }
}