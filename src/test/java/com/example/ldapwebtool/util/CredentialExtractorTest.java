package com.example.ldapwebtool.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CredentialExtractorTest {

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private Authentication authentication;

    @Test
    public void testExtractFromAuthorizationHeader() {
        // Arrange
        String basicAuth = "Basic " + java.util.Base64.getEncoder().encodeToString("testuser:testpass".getBytes());
        when(httpRequest.getHeader("Authorization")).thenReturn(basicAuth);

        // Act
        CredentialExtractor.ExtractionResult result = CredentialExtractor.extractCredentials(httpRequest, authentication);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("testuser", result.getCredentials().getUsername());
        assertEquals("testpass", result.getCredentials().getPassword());
    }

    @Test
    public void testFallbackToAuthentication() {
        // Arrange
        when(httpRequest.getHeader("Authorization")).thenReturn(null);
        when(authentication.getName()).thenReturn("ldapuser");
        when(authentication.getCredentials()).thenReturn("ldappass");

        // Act
        CredentialExtractor.ExtractionResult result = CredentialExtractor.extractCredentials(httpRequest, authentication);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("ldapuser", result.getCredentials().getUsername());
        assertEquals("ldappass", result.getCredentials().getPassword());
    }

    @Test
    public void testAuthenticationWithEmptyPassword() {
        // Arrange
        when(httpRequest.getHeader("Authorization")).thenReturn(null);
        when(authentication.getName()).thenReturn("user");
        when(authentication.getCredentials()).thenReturn("");

        // Act
        CredentialExtractor.ExtractionResult result = CredentialExtractor.extractCredentials(httpRequest, authentication);

        // Assert
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorResponse());
        assertEquals(401, result.getErrorResponse().getStatusCode().value());
    }

    @Test
    public void testInvalidAuthorizationHeader() {
        // Arrange
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(authentication.getName()).thenReturn("user");
        when(authentication.getCredentials()).thenReturn("password");

        // Act
        CredentialExtractor.ExtractionResult result = CredentialExtractor.extractCredentials(httpRequest, authentication);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("user", result.getCredentials().getUsername());
        assertEquals("password", result.getCredentials().getPassword());
    }
}
