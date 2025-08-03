package com.example.ldapwebtool.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response for failed operations")
public class ErrorResponse {
    
    @Schema(description = "Error type", example = "SEARCH_ERROR")
    private String error;
    
    @Schema(description = "Human-readable error message", 
           example = "Failed to perform LDAP search: Connection refused")
    private String message;
    
    @Schema(description = "HTTP status code", example = "500")
    private int code;
    
    @Schema(description = "Additional error details", example = "LDAPException")
    private String details;
    
    public ErrorResponse() {}
    
    public ErrorResponse(String error, String message, int code) {
        this.error = error;
        this.message = message;
        this.code = code;
    }
    
    public ErrorResponse(String error, String message, int code, String details) {
        this.error = error;
        this.message = message;
        this.code = code;
        this.details = details;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
}
