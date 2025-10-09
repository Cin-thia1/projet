package com.tonpackage.xmlparser.controller;

import com.tonpackage.xmlparser.service.OpenJmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/openjms")
public class OpenJmsController {

    private final OpenJmsService openJmsService;

    public OpenJmsController(OpenJmsService openJmsService) {
        this.openJmsService = openJmsService;
    }

    /** Démarrage classique */
    @PostMapping("/start")
    public ResponseEntity<String> start() {
        String res = openJmsService.startOpenJms();
        return ResponseEntity.ok(res);
    }

    /** Démarrage en mode SEND (toto=1) */
    @PostMapping("/start/send")
    public ResponseEntity<String> startSend() {
        String res = openJmsService.startOpenJmsSend();
        return ResponseEntity.ok(res);
    }

    /** Démarrage en mode RECEIVER (toto=2) */
    @PostMapping("/start/receiver")
    public ResponseEntity<String> startReceiver() {
        String res = openJmsService.startOpenJmsReceiver();
        return ResponseEntity.ok(res);
    }

    /** Arrêt (vide backendRun, toto, etc.) */
    @PostMapping("/stop")
    public ResponseEntity<String> stop() {
        String res = openJmsService.stopOpenJms();
        return ResponseEntity.ok(res);
    }

    @PostMapping("/rule/select")
public ResponseEntity<String> selectRule(@RequestParam("number") String ruleNumber) {
    String res = openJmsService.selectRule(ruleNumber);
    return ResponseEntity.ok(res);
}

// Définition d’un paramètre : POST /openjms/param/define?value=AGE_CLIENT
@PostMapping("/param/define")
public ResponseEntity<String> defineParam(@RequestParam("value") String paramValue) {
    String res = openJmsService.defineParameter(paramValue);
    return ResponseEntity.ok(res);
}

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        String res = openJmsService.status();
        return ResponseEntity.ok(res);
    }
}
