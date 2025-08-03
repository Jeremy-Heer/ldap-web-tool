package com.example.ldapwebtool.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "LDAP modify request with operations to perform")
public class ModifyRequest {
    
    @Schema(description = "The LDAP URI to send the request to", 
           example = "ldap://ldap.example.com:389", 
           required = true)
    @NotBlank(message = "URI is required")
    private String uri;
    
    @Schema(description = "Distinguished Name of the entry to modify", 
           example = "cn=John Doe,ou=users,dc=example,dc=com", 
           required = true)
    @NotBlank(message = "DN is required")
    private String dn;
    
    @Schema(description = "List of modifications to perform", required = true)
    @NotEmpty(message = "Modifications cannot be empty")
    private List<Modification> modifications;
    
    public ModifyRequest() {}
    
    public ModifyRequest(String uri, String dn, List<Modification> modifications) {
        this.uri = uri;
        this.dn = dn;
        this.modifications = modifications;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getDn() {
        return dn;
    }
    
    public void setDn(String dn) {
        this.dn = dn;
    }
    
    public List<Modification> getModifications() {
        return modifications;
    }
    
    public void setModifications(List<Modification> modifications) {
        this.modifications = modifications;
    }
    
    @Schema(description = "Individual modification operation")
    public static class Modification {
        
        @Schema(description = "Modification operation type", 
               example = "replace", 
               allowableValues = {"add", "delete", "replace"}, 
               required = true)
        @NotBlank(message = "Operation is required")
        private String operation;
        
        @Schema(description = "Name of the attribute to modify", 
               example = "mail", 
               required = true)
        @NotBlank(message = "Attribute is required")
        private String attribute;
        
        @Schema(description = "Values for the attribute (optional for delete operations)", 
               example = "[\"john.doe@example.com\"]")
        private List<String> values;
        
        public Modification() {}
        
        public Modification(String operation, String attribute, List<String> values) {
            this.operation = operation;
            this.attribute = attribute;
            this.values = values;
        }
        
        public String getOperation() {
            return operation;
        }
        
        public void setOperation(String operation) {
            this.operation = operation;
        }
        
        public String getAttribute() {
            return attribute;
        }
        
        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }
        
        public List<String> getValues() {
            return values;
        }
        
        public void setValues(List<String> values) {
            this.values = values;
        }
    }
}
