package com.example.ldapwebtool.util;

import com.example.ldapwebtool.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Base64;

/**
 * Utility class for extracting credentials from HTTP requests and Authentication objects.
 * This centralizes the credential extraction logic to avoid code duplication.
 */
public class CredentialExtractor {
    
    /**
     * Represents extracted credentials with username and password.
     */
    public static class Credentials {
        private final String username;
        private final String password;
        
        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    /**
     * Result of credential extraction, containing either credentials or an error response.
     */
    public static class ExtractionResult {
        private final Credentials credentials;
        private final ResponseEntity<?> errorResponse;
        
        private ExtractionResult(Credentials credentials, ResponseEntity<?> errorResponse) {
            this.credentials = credentials;
            this.errorResponse = errorResponse;
        }
        
        public static ExtractionResult success(Credentials credentials) {
            return new ExtractionResult(credentials, null);
        }
        
        public static ExtractionResult error(ResponseEntity<?> errorResponse) {
            return new ExtractionResult(null, errorResponse);
        }
        
        public boolean isSuccess() { return credentials != null; }
        public Credentials getCredentials() { return credentials; }
        public ResponseEntity<?> getErrorResponse() { return errorResponse; }
    }
    
    /**
     * Extracts credentials from HTTP request and Authentication object.
     * Tries Authorization header first, then falls back to Authentication object.
     * 
     * @param httpRequest HTTP request containing Authorization header
     * @param authentication Spring Security Authentication object
     * @return ExtractionResult containing either credentials or error response
     */
    public static ExtractionResult extractCredentials(HttpServletRequest httpRequest, Authentication authentication) {
        // Try to get credentials from the Authorization header first
        String[] headerCredentials = extractFromAuthorizationHeader(httpRequest);
        
        if (headerCredentials != null) {
            return ExtractionResult.success(new Credentials(headerCredentials[0], headerCredentials[1]));
        }
        
        // Fallback to authentication object
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        
        // If password is null/empty, return an error
        if (password == null || password.trim().isEmpty()) {
            ErrorResponse error = new ErrorResponse(
                "AUTHENTICATION_ERROR",
                "Unable to extract password from authentication. Please ensure Basic Auth is properly configured.",
                HttpStatus.UNAUTHORIZED.value()
            );
            return ExtractionResult.error(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error));
        }
        
        return ExtractionResult.success(new Credentials(username, password));
    }
    
    /**
     * Extract credentials from the Authorization header.
     * 
     * @param request HTTP request
     * @return String array with [username, password] or null if not found/invalid
     */
    private static String[] extractFromAuthorizationHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String encodedCredentials = authHeader.substring(6);
            String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
            int colonIndex = decodedCredentials.indexOf(':');
            if (colonIndex > 0) {
                String username = decodedCredentials.substring(0, colonIndex);
                String password = decodedCredentials.substring(colonIndex + 1);
                return new String[]{username, password};
            }
        }
        return null;
    }
}
