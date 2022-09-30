package com.example.directory.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class IndexController {

    @Hidden
    @RequestMapping(path = {"/"})
    public RedirectView index() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
