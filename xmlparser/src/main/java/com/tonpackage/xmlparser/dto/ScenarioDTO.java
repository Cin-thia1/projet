package com.tonpackage.xmlparser.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ScenarioDTO {
    private String scenarioId;
    private String rootServiceName;
    private String rootServiceType;
    private Map<String, String> rootInputs = new HashMap<>();
    private Map<String, String> rootOutputs = new HashMap<>();
    private List<ServiceNodeDTO> childServices = new ArrayList<>();

    // Constructeurs
    public ScenarioDTO() {}

    public ScenarioDTO(String scenarioId, String rootServiceName, String rootServiceType) {
        this.scenarioId = scenarioId;
        this.rootServiceName = rootServiceName;
        this.rootServiceType = rootServiceType;
    }

    // Getters & Setters
    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getRootServiceName() {
        return rootServiceName;
    }

    public void setRootServiceName(String rootServiceName) {
        this.rootServiceName = rootServiceName;
    }

    public String getRootServiceType() {
        return rootServiceType;
    }

    public void setRootServiceType(String rootServiceType) {
        this.rootServiceType = rootServiceType;
    }

    public Map<String, String> getRootInputs() {
        return rootInputs;
    }

    public void setRootInputs(Map<String, String> rootInputs) {
        this.rootInputs = rootInputs;
    }

    public Map<String, String> getRootOutputs() {
        return rootOutputs;
    }

    public void setRootOutputs(Map<String, String> rootOutputs) {
        this.rootOutputs = rootOutputs;
    }

    public List<ServiceNodeDTO> getChildServices() {
        return childServices;
    }

    public void setChildServices(List<ServiceNodeDTO> childServices) {
        this.childServices = childServices;
    }

    // Sous-classe pour les nœuds de service
    public static class ServiceNodeDTO {
        private String name;
        private String type;
        private Map<String, String> inputs = new HashMap<>();
        private Map<String, String> outputs = new HashMap<>();
        private List<ServiceNodeDTO> children = new ArrayList<>();

        // Constructeurs
        public ServiceNodeDTO() {}

        public ServiceNodeDTO(String name, String type) {
            this.name = name;
            this.type = type;
        }

        // Getters & Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, String> getInputs() {
            return inputs;
        }

        public void setInputs(Map<String, String> inputs) {
            this.inputs = inputs;
        }

        public Map<String, String> getOutputs() {
            return outputs;
        }

        public void setOutputs(Map<String, String> outputs) {
            this.outputs = outputs;
        }

        public List<ServiceNodeDTO> getChildren() {
            return children;
        }

        public void setChildren(List<ServiceNodeDTO> children) {
            this.children = children;
        }
    }
}