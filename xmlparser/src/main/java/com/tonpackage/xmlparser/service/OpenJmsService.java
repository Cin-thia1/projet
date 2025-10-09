package com.tonpackage.xmlparser.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.*;

@Service
public class OpenJmsService {

    // === CONFIG ===
    @Value("${file.folder.path}")
    private  String MUTEX_DIR ;

    @Value("${openjms.start}")
    private String START_BAT;

    @Value("${openjms.stop}")
    private String STOP_BAT ;

    // Fichiers
    private static final String F_BACKEND = "backendRun.txt";
    private static final String F_TOTO    = "toto.txt";      // 1 = send, 2 = receiver, "" = classique
    private static final String F_WORK    = "work.txt";
    private static final String F_BESOIN  = "besoin.txt";
    private static final String F_INSTANCE= "instance.txt";

    // Codes de mode
    private static final String MODE_SEND_CODE     = "1";
    private static final String MODE_RECEIVER_CODE = "2";

    // === API PUBLIQUE (à appeler depuis ton contrôleur) ===

    /** Démarrage classique, comme avant */
    public synchronized String startOpenJms() {
        try {
            ensureDir();
            // fichiers mutex (classique)
            writeString(F_BACKEND, "yes");
            writeString(F_TOTO, "");             // pas de mode
            clearOthers();

            // lance le script
            launchBat(START_BAT);

            // petite attente )
            Thread.sleep(10_000);

            return "SUCCÈS: OpenJMS démarré en mode classique. backendRun=yes";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERREUR startOpenJms: " + e.getMessage();
        }
    }

    /** Démarre (si besoin) puis passe en mode SEND (toto=1) */
    public synchronized String startOpenJmsSend() {
        return startWithModeInternal(MODE_SEND_CODE, "SEND");
    }

    /** Démarre (si besoin) puis passe en mode RECEIVER (toto=2) */
    public synchronized String startOpenJmsReceiver() {
        return startWithModeInternal(MODE_RECEIVER_CODE, "RECEIVER");
    }

    /** Arrêt : lance le .bat puis vide backendRun.txt */
    public synchronized String stopOpenJms() {
        try {
            ensureDir();
            launchBat(STOP_BAT);

            // marquer comme arrêté
            writeString(F_BACKEND, "");  // <-- enlève "yes"
            
            writeString(F_TOTO, "");
            clearOthers();

            return "SUCCÈS: Stop demandé. backendRun vidé.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERREUR stopOpenJms: " + e.getMessage();
        }
    }

    // === Implémentation interne ===

    private String startWithModeInternal(String modeCode, String humanLabel) {
    try {
        ensureDir();

        // A. S'assurer qu'OpenJMS est démarré (backendRun=yes)
        if (!isRunning()) {
            writeString(F_BACKEND, "yes"); // marque démarré
            writeString(F_TOTO, "");       // pas de mode pendant le boot
            clearOthers();                  // work/besoin/instance = ""

            launchBat(START_BAT);          // lance OpenJMS
            Thread.sleep(10_000);          // optionnel: laisser le process partir
        }

        // B. Poser le mode puis patienter 4s
        writeString(F_TOTO, modeCode);     // 1 = send, 2 = receiver
        clearOthers();                     // comme demandé: les autres restent vides

        Thread.sleep(4_000);               // <-- attente de 4 secondes

        return "SUCCÈS: OpenJMS en mode " + humanLabel +
               " (backendRun=" + readTrim(F_BACKEND) +
               ", toto=" + readTrim(F_TOTO) + ")";
    } catch (Exception e) {
        e.printStackTrace();
        return "ERREUR startWithMode(" + humanLabel + "): " + e.getMessage();
    }
}


    private boolean isRunning() {
        String v = readTrim(F_BACKEND);
        return "yes".equalsIgnoreCase(v);
    }

    private void clearOthers() throws IOException {
        writeString(F_WORK, "");
        writeString(F_BESOIN, "");
        writeString(F_INSTANCE, "");
    }

    private void launchBat(String batPath) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "start", batPath);
        builder.redirectErrorStream(true);
        builder.start();
    }

    private void ensureDir() throws IOException {
        Path dir = Paths.get(MUTEX_DIR);
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private void writeString(String fileName, String content) throws IOException {
        Path p = Paths.get(MUTEX_DIR).resolve(fileName);
        byte[] bytes = Objects.toString(content, "").getBytes(StandardCharsets.UTF_8);
        if (Files.exists(p)) {
            Files.write(p, bytes, TRUNCATE_EXISTING, WRITE);
        } else {
            Files.write(p, bytes, CREATE, WRITE);
        }
    }

    private String readTrim(String fileName) {
        try {
            Path p = Paths.get(MUTEX_DIR).resolve(fileName);
            if (Files.exists(p)) {
                return Files.readString(p, StandardCharsets.UTF_8).trim();
            }
        } catch (IOException ignored) {}
        return "";
    }

//  sélectionner une règle ---
public synchronized String selectRule(String ruleNumber) {
    try {
        ensureDir();

        // toto = numéro de règle, besoin = vide
        writeString(F_TOTO, ruleNumber == null ? "" : ruleNumber.trim());
        writeString(F_BESOIN, "");
        // (optionnel) vider aussi work/instance 
        writeString(F_WORK, "");
        writeString(F_INSTANCE, "");

        Thread.sleep(4_000); // attendre 4s

        return "OK: rule selected -> toto='" + readTrim(F_TOTO) + "', besoin='"
                + readTrim(F_BESOIN) + "'";
    } catch (Exception e) {
        e.printStackTrace();
        return "ERR selectRule: " + e.getMessage();
    }
}

//  définir un paramètre ---
public synchronized String defineParameter(String paramValue) {
    try {
        ensureDir();

        // toto = valeur du paramètre, besoin = vide
        writeString(F_TOTO, paramValue == null ? "" : paramValue.trim());
        writeString(F_BESOIN, "");
        // idem : garder les autres vides
        writeString(F_WORK, "");
        writeString(F_INSTANCE, "");

        Thread.sleep(4_000); // attendre 4s

        return "OK: parameter defined -> toto='" + readTrim(F_TOTO) + "', besoin='"
                + readTrim(F_BESOIN) + "'";
    } catch (Exception e) {
        e.printStackTrace();
        return "ERR defineParameter: " + e.getMessage();
    }
}



public String status() {
    return String.format("backendRun='%s'; toto='%s'; work='%s'; besoin='%s'; instance='%s'",
            readTrim(F_BACKEND), readTrim(F_TOTO), readTrim(F_WORK), readTrim(F_BESOIN), readTrim(F_INSTANCE));
}

}
