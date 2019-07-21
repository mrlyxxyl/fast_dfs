package net.ys.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PageController {

    @GetMapping("/toUpload")
    public ModelAndView toUpload() {
        ModelAndView modelAndView = new ModelAndView("upload");
        return modelAndView;
    }
}
