package com.tonpackage.xmlparser.controller;

import com.tonpackage.xmlparser.dto.ScenarioDTO;
import com.tonpackage.xmlparser.service.ScenarioParsingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioParsingService scenarioParsingService;

    public ScenarioController(ScenarioParsingService scenarioParsingService) {
        this.scenarioParsingService = scenarioParsingService;
    }

    @GetMapping
    public List<ScenarioDTO> getAllScenarios() throws Exception {
        String parentFolderPath = "C:\\Users\\Lenovo\\Desktop\\instance";
        return scenarioParsingService.parseAllScenarios(parentFolderPath);
    }
}