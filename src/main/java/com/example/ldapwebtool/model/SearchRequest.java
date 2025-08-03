package com.example.ldapwebtool.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "LDAP search request parameters")
public class SearchRequest {
    
    @Schema(description = "The LDAP URI to send the request to", 
           example = "ldap://ldap.example.com:389", 
           required = true)
    @NotBlank(message = "URI is required")
    private String uri;
    
    @Schema(description = "The LDAP search base. Defaults to empty string", 
           example = "ou=users,dc=example,dc=com", 
           defaultValue = "")
    private String base = "";
    
    @Schema(description = "The LDAP search filter. Defaults to '(objectClass=*)'", 
           example = "(objectClass=person)", 
           defaultValue = "(objectClass=*)")
    private String filter = "(objectClass=*)";
    
    @Schema(description = "Search scope: 'base', 'one', or 'sub'. Defaults to 'sub'", 
           example = "sub", 
           allowableValues = {"base", "one", "sub"}, 
           defaultValue = "sub")
    private String scope = "sub";
    
    public SearchRequest() {}
    
    public SearchRequest(String uri, String base, String filter, String scope) {
        this.uri = uri;
        this.base = base != null ? base : "";
        this.filter = filter != null ? filter : "(objectClass=*)";
        this.scope = scope != null ? scope : "sub";
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getBase() {
        return base;
    }
    
    public void setBase(String base) {
        this.base = base != null ? base : "";
    }
    
    public String getFilter() {
        return filter;
    }
    
    public void setFilter(String filter) {
        this.filter = filter != null ? filter : "(objectClass=*)";
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope != null ? scope : "sub";
    }
}
