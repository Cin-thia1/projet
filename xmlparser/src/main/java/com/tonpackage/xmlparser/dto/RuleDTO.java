package com.tonpackage.xmlparser.dto;

import java.util.List;
import java.util.Map;

public class RuleDTO {
    private String ruleId;          // Format: R1, R2, etc.
    private String fileName;        // Nom du fichier XML source
    private List<Map<String, String>> globalInputs;  // [{name: "...", type: "..."}, ...]
    private String functionName;    // Nom de la fonction principale
    private List<String> functionInputs;   // Entrées de la fonction
    private List<String> functionOutputs;  // Sorties de la fonction
    private List<String> finalOutputs;     // Sorties finales

    // Constructeurs
    public RuleDTO() {}

    public RuleDTO(String ruleId, String fileName, 
                 List<Map<String, String>> globalInputs,
                 String functionName, 
                 List<String> functionInputs,
                 List<String> functionOutputs, 
                 List<String> finalOutputs) {
        this.ruleId = ruleId;
        this.fileName = fileName;
        this.globalInputs = globalInputs;
        this.functionName = functionName;
        this.functionInputs = functionInputs;
        this.functionOutputs = functionOutputs;
        this.finalOutputs = finalOutputs;
    }

    // Getters & Setters
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<Map<String, String>> getGlobalInputs() { return globalInputs; }
    public void setGlobalInputs(List<Map<String, String>> globalInputs) { this.globalInputs = globalInputs; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public List<String> getFunctionInputs() { return functionInputs; }
    public void setFunctionInputs(List<String> functionInputs) { this.functionInputs = functionInputs; }

    public List<String> getFunctionOutputs() { return functionOutputs; }
    public void setFunctionOutputs(List<String> functionOutputs) { this.functionOutputs = functionOutputs; }

    public List<String> getFinalOutputs() { return finalOutputs; }
    public void setFinalOutputs(List<String> finalOutputs) { this.finalOutputs = finalOutputs; }
}