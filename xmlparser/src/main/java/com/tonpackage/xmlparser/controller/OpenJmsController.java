package com.tonpackage.xmlparser.controller;

import com.tonpackage.xmlparser.service.OpenJmsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/openjms")
public class OpenJmsController {

    private final OpenJmsService openJmsService;

    public OpenJmsController(OpenJmsService openJmsService) {
        this.openJmsService = openJmsService;
    }

    @PostMapping("/start")
    public String start() {
        return openJmsService.startOpenJms();
    }

    @PostMapping("/stop")
    public String stop() {
        return openJmsService.stopOpenJms();
    }
}
