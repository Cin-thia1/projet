package com.tonpackage.xmlparser.controller;

import com.tonpackage.xmlparser.dto.ScenarioDTO;
import com.tonpackage.xmlparser.service.ScenarioParsingService;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${parent.folder.path}") 
    private String parentFolderPath;


    @GetMapping
    public List<ScenarioDTO> getAllScenarios() throws Exception {
        return scenarioParsingService.parseAllScenarios(parentFolderPath);
    }
}