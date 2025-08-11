package com.tonpackage.xmlparser.service;

import com.tonpackage.xmlparser.dto.ScenarioDTO;
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
public class ScenarioParsingService {

    public List<ScenarioDTO> parseAllScenarios(String parentFolderPath) throws Exception {
        List<ScenarioDTO> scenarios = new ArrayList<>();
        File parentFolder = new File(parentFolderPath);

        if (!parentFolder.exists() || !parentFolder.isDirectory()) {
            throw new IllegalArgumentException("Dossier parent introuvable: " + parentFolderPath);
        }

        // Parcours des dossiers scenario1 à scenario20
        for (int i = 0; i <= 20; i++) {
            File scenarioFolder = new File(parentFolder, "scenario" + i);
            File artefactFile = new File(scenarioFolder, "Artefact.xml");

            if (artefactFile.exists()) {
                try {
                    ScenarioDTO scenario = parseScenarioXml(artefactFile, i);
                    scenarios.add(scenario);
                } catch (Exception e) {
                    System.err.println("Erreur dans scenario" + i + ": " + e.getMessage());
                }
            }
        }
        return scenarios;
    }

    private ScenarioDTO parseScenarioXml(File xmlFile, int scenarioId) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        Element rootService = doc.getDocumentElement();
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setScenarioId("scenario" + scenarioId);
        scenario.setRootServiceName(rootService.getAttribute("name"));
        scenario.setRootServiceType(rootService.getAttribute("type"));

        // Extraction des inputs/outputs racine
        scenario.setRootInputs(extractContextItems(rootService));
        scenario.setRootOutputs(extractProvideItems(rootService));

        // Extraction récursive des sous-services
        scenario.setChildServices(extractChildServices(rootService));

        return scenario;
    }

    private Map<String, String> extractContextItems(Element serviceElement) {
        Map<String, String> inputs = new HashMap<>();
        NodeList contexteItems = serviceElement.getElementsByTagName("contexte");
        if (contexteItems.getLength() > 0) {
            NodeList items = ((Element) contexteItems.item(0)).getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                inputs.put(item.getAttribute("name"), item.getTextContent());
            }
        }
        return inputs;
    }

    private Map<String, String> extractProvideItems(Element serviceElement) {
        Map<String, String> outputs = new HashMap<>();
        NodeList provideItems = serviceElement.getElementsByTagName("provide");
        if (provideItems.getLength() > 0) {
            NodeList items = ((Element) provideItems.item(0)).getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                outputs.put(item.getAttribute("name"), item.getTextContent());
            }
        }
        return outputs;
    }

    private List<ScenarioDTO.ServiceNodeDTO> extractChildServices(Element parentService) {
        List<ScenarioDTO.ServiceNodeDTO> children = new ArrayList<>();
        NodeList serviceNodes = parentService.getElementsByTagName("service");
        for (int i = 0; i < serviceNodes.getLength(); i++) {
            Element serviceElement = (Element) serviceNodes.item(i);
            ScenarioDTO.ServiceNodeDTO childNode = new ScenarioDTO.ServiceNodeDTO();
            childNode.setName(serviceElement.getAttribute("name"));
            childNode.setType(serviceElement.getAttribute("type"));
            childNode.setInputs(extractContextItems(serviceElement));
            childNode.setOutputs(extractProvideItems(serviceElement));
            childNode.setChildren(extractChildServices(serviceElement)); // Récursivité
            children.add(childNode);
        }
        return children;
    }
}