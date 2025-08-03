package com.example.ldapwebtool.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response from LDAP modify operation")
public class ModifyResponse {
    
    @Schema(description = "Whether the modification was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Success or error message", example = "Modification successful")
    private String message;
    
    @Schema(description = "Distinguished Name that was modified", 
           example = "cn=John Doe,ou=users,dc=example,dc=com")
    private String dn;
    
    public ModifyResponse() {}
    
    public ModifyResponse(boolean success, String message, String dn) {
        this.success = success;
        this.message = message;
        this.dn = dn;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDn() {
        return dn;
    }
    
    public void setDn(String dn) {
        this.dn = dn;
    }
}
