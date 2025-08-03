package com.example.ldapwebtool.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "LDAP search response containing entries and count")
public class SearchResponse {
    
    @Schema(description = "List of LDAP entries found in the search")
    private List<LdapEntry> entries;
    
    @Schema(description = "Total number of entries returned", example = "5")
    private int count;
    
    public SearchResponse() {}
    
    public SearchResponse(List<LdapEntry> entries) {
        this.entries = entries;
        this.count = entries != null ? entries.size() : 0;
    }
    
    public List<LdapEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(List<LdapEntry> entries) {
        this.entries = entries;
        this.count = entries != null ? entries.size() : 0;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    @Schema(description = "Individual LDAP entry with DN and attributes")
    public static class LdapEntry {
        
        @Schema(description = "Distinguished Name of the entry", 
               example = "cn=John Doe,ou=users,dc=example,dc=com")
        private String dn;
        
        @Schema(description = "Map of attribute names to values (can be single value or array)", 
               example = "{\"cn\":\"John Doe\",\"mail\":\"john@example.com\",\"objectClass\":[\"person\",\"inetOrgPerson\"]}")
        private Map<String, Object> attributes;
        
        public LdapEntry() {}
        
        public LdapEntry(String dn, Map<String, Object> attributes) {
            this.dn = dn;
            this.attributes = attributes;
        }
        
        public String getDn() {
            return dn;
        }
        
        public void setDn(String dn) {
            this.dn = dn;
        }
        
        public Map<String, Object> getAttributes() {
            return attributes;
        }
        
        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }
    }
}
