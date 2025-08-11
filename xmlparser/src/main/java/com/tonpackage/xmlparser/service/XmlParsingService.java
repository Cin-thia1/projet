package com.tonpackage.xmlparser.service;

import com.tonpackage.xmlparser.dto.RuleDTO;
import org.w3c.dom.*;
import org.springframework.stereotype.Service;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XmlParsingService {

    public List<RuleDTO> parseAllXmlFiles(String folderPath) throws Exception {
        List<RuleDTO> rules = new ArrayList<>();
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Dossier invalide: " + folderPath);
        }

        File[] xmlFiles = folder.listFiles((dir, name) -> name.endsWith(".xml"));

        if (xmlFiles != null) {
            int ruleNumber = 1;
            for (File file : xmlFiles) {
                try {
                    RuleDTO rule = parseSingleXml(file, ruleNumber);
                    rules.add(rule);
                    ruleNumber++;
                } catch (Exception e) {
                    System.err.println("Erreur avec " + file.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return rules;
    }

    public RuleDTO parseSingleXml(File xmlFile, int ruleNumber) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                           .newDocumentBuilder()
                           .parse(xmlFile);
        doc.getDocumentElement().normalize();

        // Extraction des inputs globaux avec leurs types
        List<Map<String, String>> globalInputs = extractInputsWithTypes(doc, "contexte");
        
        // Extraction des inputs de fonction
        List<String> functionInputs = extractItems(doc, "use/inputParameter");
        
        // Extraction des outputs de fonction
        List<String> functionOutputs = extractItems(doc, "use/outputParameter");
        
        // Extraction des outputs finaux
        List<String> finalOutputs = extractItems(doc, "provide");

        // Nom de la fonction principale
        String functionName = extractFunctionName(doc);

        return new RuleDTO(
            "R" + ruleNumber,
            xmlFile.getName().replace(".xml", ""),
            globalInputs,
            functionName,
            functionInputs,
            functionOutputs,
            finalOutputs
        );
    }

    private List<Map<String, String>> extractInputsWithTypes(Document doc, String parentTag) {
        List<Map<String, String>> inputs = new ArrayList<>();
        NodeList parentNodes = doc.getElementsByTagName(parentTag);
        
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            NodeList itemNodes = parentElement.getElementsByTagName("item");
            
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element item = (Element) itemNodes.item(i);
                Map<String, String> input = new HashMap<>();
                
                // Récupération obligatoire du nom
                String name = item.getAttribute("name");
                if (name == null || name.isEmpty()) {
                    continue; // On ignore les items sans nom
                }
                input.put("name", name);
                
                // Récupération du type avec valeur par défaut "String" si absent
                String type = item.getAttribute("type");
                input.put("type", type != null && !type.isEmpty() ? type : "String");
                
                inputs.add(input);
            }
        }
        return inputs;
    }

    private List<String> extractItems(Document doc, String path) {
        List<String> items = new ArrayList<>();
        String[] tags = path.split("/");
        
        NodeList nodes = doc.getElementsByTagName(tags[0]);
        if (nodes.getLength() == 0) return items;
        
        Element parentElement = (Element) nodes.item(0);
        
        // Gestion des sous-éléments si le chemin est nested (ex: "use/inputParameter")
        for (int i = 1; i < tags.length; i++) {
            NodeList childNodes = parentElement.getElementsByTagName(tags[i]);
            if (childNodes.getLength() == 0) return items;
            parentElement = (Element) childNodes.item(0);
        }
        
        // Extraction des noms des items
        NodeList itemNodes = parentElement.getElementsByTagName("item");
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Element item = (Element) itemNodes.item(i);
            String name = item.getAttribute("name");
            if (name != null && !name.isEmpty()) {
                items.add(name);
            }
        }
        
        return items;
    }

    private String extractFunctionName(Document doc) {
        NodeList useNodes = doc.getElementsByTagName("use");
        if (useNodes.getLength() > 0) {
            Element useElement = (Element) useNodes.item(0);
            return useElement.getAttribute("serviceName");
        }
        return "";
    }
    public RuleDTO parseXmlFile(String file) throws Exception {
    File xmlFile = new File(file);
    return parseSingleXml(xmlFile, extractRuleNumberFromFileName(xmlFile.getName()));
}

private int extractRuleNumberFromFileName(String fileName) {
    try {
        // Exemple: "R1_maRegle.xml" -> 1
        return Integer.parseInt(fileName.substring(1, fileName.indexOf("_")));
    } catch (Exception e) {
        return 1; // Valeur par défaut
    }
}
}