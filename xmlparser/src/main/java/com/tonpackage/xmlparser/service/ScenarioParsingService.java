package com.tonpackage.xmlparser.service;

import com.tonpackage.xmlparser.dto.ScenarioDTO;
import org.w3c.dom.*;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

@Service
public class ScenarioParsingService {

    public List<ScenarioDTO> parseAllScenarios(String parentFolderPath) throws Exception {
        List<ScenarioDTO> scenarios = new ArrayList<>();
        File parentFolder = new File(parentFolderPath);

        if (!parentFolder.exists() || !parentFolder.isDirectory()) {
            throw new IllegalArgumentException("Dossier parent introuvable: " + parentFolderPath);
        }

        // Parcours des sous-dossiers "scenarioX"
        File[] scenarioFolders = parentFolder.listFiles(
                file -> file.isDirectory() && file.getName().toLowerCase().startsWith("scenario")
        );

        if (scenarioFolders != null) {
            for (File scenarioFolder : scenarioFolders) {
                File artefactFile = new File(scenarioFolder, "Artefact.xml");

                if (artefactFile.exists()) {
                    try {
                        ScenarioDTO scenario = parseScenarioXml(artefactFile, scenarioFolder.getName());
                        scenarios.add(scenario);
                    } catch (Exception e) {
                        System.err.println("Erreur dans " + scenarioFolder.getName() + ": " + e.getMessage());
                    }
                } else {
                    // 👉 Même si pas d'Artefact.xml, on renvoie quand même un scénario "vide"
                    ScenarioDTO emptyScenario = new ScenarioDTO();
                    emptyScenario.setScenarioId(scenarioFolder.getName());
                    emptyScenario.setRootServiceName("Aucun artefact.xml trouvé");
                    emptyScenario.setRootServiceType("composite");
                    emptyScenario.setRootInputs(Collections.emptyMap());
                    emptyScenario.setRootOutputs(Collections.emptyMap());
                    emptyScenario.setChildServices(Collections.emptyList());
                    scenarios.add(emptyScenario);
                }
            }
        }
        return scenarios;
    }

    private ScenarioDTO parseScenarioXml(File xmlFile, String scenarioId) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        Element rootService = doc.getDocumentElement();
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setScenarioId(scenarioId);
        scenario.setRootServiceName(rootService.getAttribute("name"));
        scenario.setRootServiceType(rootService.getAttribute("type"));

        // Toujours extraire, même si vide
        scenario.setRootInputs(extractContextItems(rootService));
        scenario.setRootOutputs(extractProvideItems(rootService));

        // Extraction récursive
        scenario.setChildServices(extractDirectChildServices(rootService));

        return scenario;
    }

    private Map<String, String> extractContextItems(Element serviceElement) {
        Map<String, String> inputs = new HashMap<>();
        NodeList contexteItems = serviceElement.getElementsByTagName("contexte");
        if (contexteItems.getLength() > 0) {
            Node contexte = contexteItems.item(0);
            if (contexte.getNodeType() == Node.ELEMENT_NODE) {
                NodeList items = ((Element) contexte).getElementsByTagName("item");
                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    inputs.put(item.getAttribute("name"),
                            item.getTextContent() != null ? item.getTextContent() : "");
                }
            }
        }
        return inputs;
    }

    private Map<String, String> extractProvideItems(Element serviceElement) {
        Map<String, String> outputs = new HashMap<>();
        NodeList provideItems = serviceElement.getElementsByTagName("provide");
        if (provideItems.getLength() > 0) {
            Node provide = provideItems.item(0);
            if (provide.getNodeType() == Node.ELEMENT_NODE) {
                NodeList items = ((Element) provide).getElementsByTagName("item");
                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    outputs.put(item.getAttribute("name"),
                            item.getTextContent() != null ? item.getTextContent() : "");
                }
            }
        }
        return outputs;
    }

    private List<ScenarioDTO.ServiceNodeDTO> extractDirectChildServices(Element parentService) {
        List<ScenarioDTO.ServiceNodeDTO> children = new ArrayList<>();

        NodeList allChildren = parentService.getChildNodes();
        for (int i = 0; i < allChildren.getLength(); i++) {
            Node node = allChildren.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                
                if (element.getTagName().equals("service")) {
                    // Traitement des balises <service>
                    ScenarioDTO.ServiceNodeDTO childNode = createServiceNodeFromElement(element);
                    children.add(childNode);
                } else if (element.getTagName().equals("use")) {
                    // Traitement des balises <use>
                    ScenarioDTO.ServiceNodeDTO useNode = createServiceNodeFromUseElement(element);
                    children.add(useNode);
                }
            }
        }
        return children;
    }

    private ScenarioDTO.ServiceNodeDTO createServiceNodeFromElement(Element serviceElement) {
        ScenarioDTO.ServiceNodeDTO childNode = new ScenarioDTO.ServiceNodeDTO();
        childNode.setName(serviceElement.getAttribute("name"));
        childNode.setType(serviceElement.getAttribute("type"));

        childNode.setInputs(extractContextItems(serviceElement));
        childNode.setOutputs(extractProvideItems(serviceElement));

        // Récursivité pour les enfants
        childNode.setChildren(extractDirectChildServices(serviceElement));

        return childNode;
    }

    private ScenarioDTO.ServiceNodeDTO createServiceNodeFromUseElement(Element useElement) {
        ScenarioDTO.ServiceNodeDTO useNode = new ScenarioDTO.ServiceNodeDTO();
        useNode.setName(useElement.getAttribute("serviceName"));
        useNode.setType(useElement.getAttribute("type"));
        
        // Extraire les inputs des balises <inputParameter>
        useNode.setInputs(extractInputParametersFromUse(useElement));
        
        // Extraire les outputs des balises <outputParameter>
        useNode.setOutputs(extractOutputParametersFromUse(useElement));
        
        // Les balises <use> n'ont généralement pas d'enfants
        useNode.setChildren(new ArrayList<>());
        
        return useNode;
    }

    private Map<String, String> extractInputParametersFromUse(Element useElement) {
        Map<String, String> inputs = new HashMap<>();
        NodeList inputParameters = useElement.getElementsByTagName("inputParameter");
        
        if (inputParameters.getLength() > 0) {
            Element inputParam = (Element) inputParameters.item(0);
            NodeList items = inputParam.getElementsByTagName("item");
            
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String name = item.getAttribute("name");
                String value = item.getTextContent();
                // Si value est vide, on garde quand même l'entrée
                inputs.put(name, value != null ? value : "");
            }
        }
        
        return inputs;
    }

    private Map<String, String> extractOutputParametersFromUse(Element useElement) {
        Map<String, String> outputs = new HashMap<>();
        NodeList outputParameters = useElement.getElementsByTagName("outputParameter");
        
        if (outputParameters.getLength() > 0) {
            Element outputParam = (Element) outputParameters.item(0);
            NodeList items = outputParam.getElementsByTagName("item");
            
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                String name = item.getAttribute("name");
                String value = item.getTextContent();
                // Si value est vide, on garde quand même la sortie
                outputs.put(name, value != null ? value : "");
            }
        }
        
        return outputs;
    }
}