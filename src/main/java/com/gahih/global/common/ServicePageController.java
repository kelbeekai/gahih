package com.gahih.global.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServicePageController {

    @GetMapping("/terms")
    public String terms() {
        return "service/service-terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "service/service-privacy";
    }

    @GetMapping("/policy")
    public String policy() {
        return "service/service-policy";
    }

    @GetMapping("/disclaimer")
    public String disclaimer() {
        return "service/service-disclaimer";
    }

    @GetMapping("/contact")
    public String contact() {
        return "service/service-contact";
    }
}