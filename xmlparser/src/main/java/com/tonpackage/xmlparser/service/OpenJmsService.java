package com.tonpackage.xmlparser.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class OpenJmsService {

    private static final String MUTEX_FILE_PATH = "C:\\Users\\Lenovo\\Desktop\\mutex\\";
    
    public String startOpenJms() {
        try {
            System.out.println("Démarrage d'OpenJMS...");
            
            // Vérifier l'état des fichiers avant
            System.out.println("État des fichiers avant opération:");
            logFileStatus("backendRun.txt");
            logFileStatus("toto.txt");
            logFileStatus("work.txt");
            logFileStatus("besoin.txt");
            logFileStatus("instance.txt");
            
            // Créer ou écraser les fichiers de mutex
            createOrOverwriteMutexFiles();
            
            // Vérifier l'état des fichiers après
            System.out.println("État des fichiers après opération:");
            logFileStatus("backendRun.txt");
            logFileStatus("toto.txt");
            logFileStatus("work.txt");
            logFileStatus("besoin.txt");
            logFileStatus("instance.txt");
            
            // Vérifier que backendRun.txt contient bien "yes"
            String backendRunContent = readBackendRunFile();
            if (!"yes".equals(backendRunContent)) {
                return "ERREUR: backendRun.txt ne contient pas 'yes' après écriture. Contenu: '" + backendRunContent + "'";
            }
            
            // Démarrer OpenJMS
            System.out.println("Lancement du script OpenJMS...");
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "start", "C:\\Users\\Lenovo\\Desktop\\start-openjms-java8.bat"
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            
            // Attendre 10 secondes
            System.out.println("Attente de 10 secondes...");
            Thread.sleep(10000);
            
            return "SUCCÈS: OpenJMS démarré. backendRun.txt contient: '" + backendRunContent + "'";
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "ERREUR lors du démarrage d'OpenJMS : " + e.getMessage();
        }
    }

    public String stopOpenJMS() {
        try {
            System.out.println("Arrêt d'OpenJMS...");
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "start", "C:\\Users\\Lenovo\\Desktop\\stop-openjms-java8.bat"
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            
            return "SUCCÈS: Commande d'arrêt d'OpenJMS exécutée";
            
        } catch (IOException e) {
            e.printStackTrace();
            return "ERREUR lors de l'arrêt d'OpenJMS : " + e.getMessage();
        }
    }

    private void createOrOverwriteMutexFiles() throws IOException {
        // S'assurer que le répertoire existe
        Path mutexDir = Paths.get(MUTEX_FILE_PATH);
        if (!Files.exists(mutexDir)) {
            Files.createDirectories(mutexDir);
            System.out.println("Création du répertoire mutex: " + MUTEX_FILE_PATH);
        } else {
            System.out.println("Répertoire mutex existe déjà: " + MUTEX_FILE_PATH);
        }
        
        // Liste des fichiers à créer/écraser avec leur contenu
        String[][] filesToCreate = {
            {"backendRun.txt", "yes"},
            {"toto.txt", ""},
            {"work.txt", ""},
            {"besoin.txt", ""},
            {"instance.txt", ""}
        };
        
        for (String[] fileInfo : filesToCreate) {
            String fileName = fileInfo[0];
            String fileContent = fileInfo[1];
            
            Path filePath = mutexDir.resolve(fileName);
            
            // Vérifier si le fichier existe déjà
            boolean fileExisted = Files.exists(filePath);
            
            if (fileExisted) {
                // Écraser le fichier existant
                Files.write(filePath, 
                           fileContent.getBytes(), 
                           StandardOpenOption.TRUNCATE_EXISTING,
                           StandardOpenOption.WRITE);
                System.out.println("Fichier ÉCRASÉ: " + fileName + " avec contenu: '" + fileContent + "'");
            } else {
                // Créer le nouveau fichier
                Files.write(filePath, 
                           fileContent.getBytes(), 
                           StandardOpenOption.CREATE,
                           StandardOpenOption.WRITE);
                System.out.println("Fichier CRÉÉ: " + fileName + " avec contenu: '" + fileContent + "'");
            }
        }
    }
    
    private String readBackendRunFile() {
        try {
            Path backendRunFile = Paths.get(MUTEX_FILE_PATH + "backendRun.txt");
            if (Files.exists(backendRunFile)) {
                String content = Files.readString(backendRunFile).trim();
                System.out.println("Contenu lu de backendRun.txt: '" + content + "'");
                return content;
            } else {
                System.out.println("Fichier backendRun.txt n'existe pas");
            }
        } catch (IOException e) {
            System.out.println("Erreur lecture backendRun.txt: " + e.getMessage());
        }
        return "";
    }
    
    private void logFileStatus(String fileName) {
        try {
            Path filePath = Paths.get(MUTEX_FILE_PATH + fileName);
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath).trim();
                System.out.println(" - " + fileName + ": EXISTE, contenu: '" + content + "'");
            } else {
                System.out.println(" - " + fileName + ": N'EXISTE PAS");
            }
        } catch (IOException e) {
            System.out.println(" - " + fileName + ": ERREUR LECTURE: " + e.getMessage());
        }
    }


    public String stopOpenJms() {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "start", "C:\\Users\\Lenovo\\Desktop\\stop-openjms-java8.bat"
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            
            return "Commande d'arrêt d'OpenJMS exécutée";
            
        } catch (IOException e) {
            e.printStackTrace();
            return "Erreur lors de l'arrêt d'OpenJMS : " + e.getMessage();
        }
    }
}

   