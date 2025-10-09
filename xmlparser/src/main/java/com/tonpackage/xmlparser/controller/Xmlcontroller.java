package com.tonpackage.xmlparser.controller;

import com.tonpackage.xmlparser.dto.RuleDTO;
import com.tonpackage.xmlparser.service.XmlParsingService;
import com.tonpackage.xmlparser.service.XmlWatchingService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/rules")
public class Xmlcontroller {

    private final XmlParsingService xmlParsingService;
    private final XmlWatchingService xmlWatchingService;

    @Value("${xml.folder.path}") 
    private String RULES_FOLDER;

    public Xmlcontroller(XmlParsingService xmlParsingService, XmlWatchingService xmlWatchingService) {
        this.xmlParsingService = xmlParsingService;
        this.xmlWatchingService = xmlWatchingService;
    }

    // 1) Récupérer la liste de règles à partir du cache 
    @GetMapping
    public List<RuleDTO> getCachedRules() {
        return xmlWatchingService.getCachedRules();
    }

    // 2) Endpoint pour forcer une relecture des fichiers XML (relecture complète)
    @GetMapping("/reload")
    public List<RuleDTO> reloadRulesFromFiles() throws Exception {
        return xmlParsingService.parseAllXmlFiles(RULES_FOLDER);
    }

    // 3) Récupérer les inputs d’une règle dans le cache
    @GetMapping("/{ruleId}/inputs")
    public List<Map<String, String>> getRuleInputs(@PathVariable String ruleId) {
        return xmlWatchingService.getCachedRules().stream()
                .filter(rule -> rule.getRuleId().equalsIgnoreCase(ruleId))
                .findFirst()
                .map(RuleDTO::getGlobalInputs)
                .orElseThrow(() -> new RuntimeException("Règle non trouvée: " + ruleId));
    }
}
