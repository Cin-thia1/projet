package com.tonpackage.xmlparser.service;

import com.tonpackage.xmlparser.dto.RuleDTO;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.nio.file.Files;

@Service
public class XmlWatchingService {

    private final XmlParsingService xmlParsingService;

    // Cache des règles extraites (List de RuleDTO) pour le contrôleur actuel
    private volatile List<RuleDTO> cachedRules = Collections.synchronizedList(new ArrayList<>());

    // Cache du contenu XML brut, clé = nom fichier, valeur = contenu complet
    private final Map<String, String> cachedXmlContents = new ConcurrentHashMap<>();

    // Dossier à surveiller (à adapter si besoin)
    private final String folderPath = "C:\\Users\\Lenovo\\Desktop\\schemaExecution";

    public XmlWatchingService(XmlParsingService xmlParsingService) {
        this.xmlParsingService = xmlParsingService;
    }

    @PostConstruct
    public void init() {
        // Charger au démarrage
        loadAll();

        // Démarrer la surveillance dans un thread séparé
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                watchXmlFolder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Charge à la fois :
     * - le cache des règles (List<RuleDTO>)
     * - le cache des fichiers XML bruts (Map<String, String>)
     */
    public synchronized void loadAll() {
        loadRules();
        loadXmlContents();
    }

    /**
     * Charge le cache List<RuleDTO> via ton service de parsing existant
     */
    public synchronized void loadRules() {
        try {
            List<RuleDTO> rules = xmlParsingService.parseAllXmlFiles(folderPath);
            cachedRules = Collections.synchronizedList(rules);
            System.out.println("Cache rules rechargé: " + rules.size() + " règles.");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des règles : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charge le cache du contenu XML brut dans la Map<String,String>
     */
    public synchronized void loadXmlContents() {
        File folder = new File(folderPath);
        File[] xmlFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        if (xmlFiles != null) {
            for (File file : xmlFiles) {
                try {
                    String content = Files.readString(file.toPath());
                    cachedXmlContents.put(file.getName(), content);
                } catch (Exception e) {
                    System.err.println("Erreur lecture fichier " + file.getName() + ": " + e.getMessage());
                }
            }
            System.out.println("Cache XML contents rechargé. Fichiers en mémoire: " + cachedXmlContents.size());
        }
    }

    /**
     * Surveille le dossier pour toute modification/création/suppression de fichiers XML,
     * puis recharge automatiquement les caches.
     */
    private void watchXmlFolder() throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(folderPath);

        path.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);

        while (true) {
            WatchKey key = watchService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path fileName = (Path) event.context();

                if (fileName.toString().toLowerCase().endsWith(".xml")) {
                    System.out.println("Fichier XML changé : " + fileName + " (" + kind + ")");
                    // Recharge à chaque événement pertinent
                    loadAll();
                }
            }
            key.reset();
        }
    }

    // Getters pour le contrôleur

    /**
     * Retourne la liste des règles extraites (pour ton contrôleur existant)
     */
    public List<RuleDTO> getCachedRules() {
        return cachedRules;
    }

    /**
     * Retourne la Map nomFichier -> contenu XML complet,
     * si jamais tu souhaites exposer cette info via une autre API REST
     */
    public Map<String, String> getCachedXmlContents() {
        return Collections.unmodifiableMap(cachedXmlContents);
    }
}
