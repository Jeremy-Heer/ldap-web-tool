package com.example.ldapwebtool.controller;

import com.example.ldapwebtool.model.*;
import com.example.ldapwebtool.service.LdapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api")
@Tag(name = "LDAP Operations", description = "REST API for LDAP search and modify operations")
@SecurityRequirement(name = "basicAuth")
public class LdapController {
    
    @Autowired
    private LdapService ldapService;
    
    /**
     * Extract credentials from the Authorization header
     */
    private String[] extractCredentials(HttpServletRequest request) {
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
    
    @Operation(
        summary = "Perform LDAP search (JSON response)",
        description = "Search for LDAP entries with configurable base, filter, and scope. Returns JSON format response.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search successful",
                content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = SearchResponse.class),
                            examples = @ExampleObject(value = "{\"entries\":[{\"dn\":\"cn=John Doe,ou=users,dc=example,dc=com\",\"attributes\":{\"cn\":\"John Doe\",\"mail\":\"john@example.com\"}}],\"count\":1}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @PostMapping(value = "/search", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> search(
            @Parameter(description = "LDAP search request parameters", required = true)
            @Valid @RequestBody SearchRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Try to get credentials from the Authorization header first
            String[] credentials = extractCredentials(httpRequest);
            String username, password;
            
            if (credentials != null) {
                username = credentials[0];
                password = credentials[1];
            } else {
                // Fallback to authentication object
                username = authentication.getName();
                password = (String) authentication.getCredentials();
                
                // If password is still null/empty, return an error
                if (password == null || password.trim().isEmpty()) {
                    ErrorResponse error = new ErrorResponse(
                        "AUTHENTICATION_ERROR",
                        "Unable to extract password from authentication. Please ensure Basic Auth is properly configured.",
                        HttpStatus.UNAUTHORIZED.value()
                    );
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            }
            
            SearchResponse response = ldapService.search(request, username, password);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "SEARCH_ERROR",
                "Failed to perform LDAP search: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
        summary = "Perform LDAP search via GET (JSON response)",
        description = "Search for LDAP entries using query parameters. Returns JSON format response.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search successful",
                content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = SearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchGet(
            @Parameter(description = "LDAP URI", example = "ldap://localhost:389", required = true)
            @RequestParam(value = "uri") String uri,
            @Parameter(description = "Search base DN", example = "ou=users,dc=example,dc=com", required = true)
            @RequestParam(value = "base") String base,
            @Parameter(description = "Search filter", example = "(objectClass=person)", required = true)
            @RequestParam(value = "filter") String filter,
            @Parameter(description = "Search scope", example = "sub")
            @RequestParam(value = "scope", defaultValue = "sub") String scope,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Create SearchRequest from query parameters
            SearchRequest request = new SearchRequest();
            request.setUri(uri);
            request.setBase(base);
            request.setFilter(filter);
            request.setScope(scope);

            // Try to get credentials from the Authorization header first
            String[] credentials = extractCredentials(httpRequest);
            String username, password;
            
            if (credentials != null) {
                username = credentials[0];
                password = credentials[1];
            } else {
                // Fallback to authentication object
                username = authentication.getName();
                password = (String) authentication.getCredentials();
                
                // If password is still null/empty, return an error
                if (password == null || password.trim().isEmpty()) {
                    ErrorResponse error = new ErrorResponse(
                        "AUTHENTICATION_ERROR",
                        "Unable to extract password from authentication. Please ensure Basic Auth is properly configured.",
                        HttpStatus.UNAUTHORIZED.value()
                    );
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            }
            
            SearchResponse response = ldapService.search(request, username, password);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "SEARCH_ERROR",
                "Failed to perform LDAP search: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
        summary = "Perform LDAP search (LDIF response)",
        description = "Search for LDAP entries with configurable base, filter, and scope. Returns LDIF format response.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search successful",
                content = @Content(mediaType = "application/ldif", 
                          examples = @ExampleObject(value = "dn: cn=John Doe,ou=users,dc=example,dc=com\\ncn: John Doe\\nmail: john@example.com\\n\\n"))),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @PostMapping(value = "/search/ldif", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = "application/ldif")
    public ResponseEntity<?> searchLdif(
            @Parameter(description = "LDAP search request parameters", required = true)
            @Valid @RequestBody SearchRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Try to get credentials from the Authorization header first
            String[] credentials = extractCredentials(httpRequest);
            String username, password;
            
            if (credentials != null) {
                username = credentials[0];
                password = credentials[1];
            } else {
                // Fallback to authentication object
                username = authentication.getName();
                password = (String) authentication.getCredentials();
                
                // If password is still null/empty, return an error
                if (password == null || password.trim().isEmpty()) {
                    ErrorResponse error = new ErrorResponse(
                        "AUTHENTICATION_ERROR",
                        "Unable to extract password from authentication. Please ensure Basic Auth is properly configured.",
                        HttpStatus.UNAUTHORIZED.value()
                    );
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            }
            
            String ldifResult = ldapService.searchToLdif(request, username, password);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/ldif"))
                .body(ldifResult);
            
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "SEARCH_ERROR",
                "Failed to perform LDAP search: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
        summary = "Perform LDAP search via GET (LDIF response)",
        description = "Search for LDAP entries using query parameters. Returns LDIF format response.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search successful",
                content = @Content(mediaType = "application/ldif")),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @GetMapping(value = "/search/ldif", produces = "application/ldif")
    public ResponseEntity<?> searchLdifGet(
            @Parameter(description = "LDAP URI", example = "ldap://localhost:389", required = true)
            @RequestParam(value = "uri") String uri,
            @Parameter(description = "Search base DN", example = "ou=users,dc=example,dc=com", required = true)
            @RequestParam(value = "base") String base,
            @Parameter(description = "Search filter", example = "(objectClass=person)", required = true)
            @RequestParam(value = "filter") String filter,
            @Parameter(description = "Search scope", example = "sub")
            @RequestParam(value = "scope", defaultValue = "sub") String scope,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Create SearchRequest from query parameters
            SearchRequest request = new SearchRequest();
            request.setUri(uri);
            request.setBase(base);
            request.setFilter(filter);
            request.setScope(scope);

            // Try to get credentials from the Authorization header first
            String[] credentials = extractCredentials(httpRequest);
            String username, password;
            
            if (credentials != null) {
                username = credentials[0];
                password = credentials[1];
            } else {
                // Fallback to authentication object
                username = authentication.getName();
                password = (String) authentication.getCredentials();
                
                // If password is still null/empty, return an error
                if (password == null || password.trim().isEmpty()) {
                    ErrorResponse error = new ErrorResponse(
                        "AUTHENTICATION_ERROR",
                        "Unable to extract password from authentication. Please ensure Basic Auth is properly configured.",
                        HttpStatus.UNAUTHORIZED.value()
                    );
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
                }
            }
            
            String ldifResult = ldapService.searchToLdif(request, username, password);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/ldif"))
                .body(ldifResult);
            
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "SEARCH_ERROR",
                "Failed to perform LDAP search: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
        }
    }

    @Operation(
        summary = "Modify LDAP entries (JSON format)",
        description = "Perform LDAP modify operations (add, delete, replace attributes) using JSON request format.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Modification successful",
                content = @Content(schema = @Schema(implementation = ModifyResponse.class),
                          examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Modification successful\",\"dn\":\"cn=John Doe,ou=users,dc=example,dc=com\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @PostMapping(value = "/modify",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modify(
            @Parameter(description = "JSON modify request", required = true,
                      examples = @ExampleObject(value = "{\"uri\":\"ldap://localhost:389\",\"dn\":\"cn=John,ou=users,dc=example,dc=com\",\"modifications\":[{\"operation\":\"replace\",\"attribute\":\"mail\",\"values\":[\"new@email.com\"]}]}"))
            @Valid @RequestBody ModifyRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Try to get credentials from the Authorization header first
            String[] credentials = extractCredentials(httpRequest);
            String username, password;
            
            if (credentials != null) {
                username = credentials[0];
                password = credentials[1];
            } else {
                // Fallback to authentication object
                username = authentication.getName();
                password = (String) authentication.getCredentials();
                
                // If password is still null/empty, return an error
                if (password == null || password.trim().isEmpty()) {
                    ErrorResponse error = new ErrorResponse(
                        "AUTHENTICATION_ERROR",
                        "Unable to extract password from authentication. Please ensure Basic Auth is properly configured.",
                        HttpStatus.UNAUTHORIZED.value()
                    );
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            }
            
            ModifyResponse response = ldapService.modify(request, username, password);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "MODIFY_ERROR",
                "Failed to perform LDAP modification: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
        summary = "Modify LDAP entries (LDIF format)",
        description = "Perform LDAP modify operations using LDIF request format.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Modification successful",
                content = @Content(schema = @Schema(implementation = ModifyResponse.class),
                          examples = @ExampleObject(value = "{\"success\":true,\"message\":\"Modification successful\",\"dn\":\"cn=John Doe,ou=users,dc=example,dc=com\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        }
    )
    @PostMapping(value = "/modify/ldif",
                 consumes = "application/ldif",
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyLdif(
            @Parameter(description = "LDIF modify request", required = true,
                      examples = @ExampleObject(value = "dn: cn=John,ou=users,dc=example,dc=com\\nchangetype: modify\\nreplace: mail\\nmail: new@email.com\\n-"))
            @RequestBody String ldifContent,
            @Parameter(description = "LDAP URI", example = "ldap://localhost:389", required = true)
            @RequestParam(value = "uri") String uri,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Try to get credentials from the Authorization header first
            String[] credentials = extractCredentials(httpRequest);
            String username, password;
            
            if (credentials != null) {
                username = credentials[0];
                password = credentials[1];
            } else {
                // Fallback to authentication object
                username = authentication.getName();
                password = (String) authentication.getCredentials();
                
                // If password is still null/empty, return an error
                if (password == null || password.trim().isEmpty()) {
                    ErrorResponse error = new ErrorResponse(
                        "AUTHENTICATION_ERROR",
                        "Unable to extract password from authentication. Please ensure Basic Auth is properly configured.",
                        HttpStatus.UNAUTHORIZED.value()
                    );
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            }
            
            ModifyResponse response = ldapService.modifyFromLdif(ldifContent, uri, username, password);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "MODIFY_ERROR",
                "Failed to perform LDAP modification: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        StringBuilder message = new StringBuilder("Validation failed: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            message.append(error.getField()).append(" ").append(error.getDefaultMessage()).append("; ")
        );
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            message.toString(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred: " + ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getClass().getSimpleName()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
