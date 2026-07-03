package com.gahih.global.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LegacyUrlRedirectController {

    @RequestMapping(
            value = {
                    "/posts",
                    "/posts/**"
            },
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST
            }
    )
    public String oldPostUrls() {
        return "redirect:/";
    }
}