package com.example.ldapwebtool.controller;

import com.example.ldapwebtool.model.SearchRequest;
import com.example.ldapwebtool.model.SearchResponse;
import com.example.ldapwebtool.service.LdapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
public class LdapControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private LdapService ldapService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Test
    public void testContextLoads() {
        // Simple test to verify the application context loads correctly
        assert(context != null);
    }

    @Test
    public void testSearchEndpointWithAuth() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Arrange
        SearchRequest request = new SearchRequest("ldap://localhost:389", "ou=users,dc=test,dc=com", "(objectClass=person)", "sub");
        SearchResponse response = new SearchResponse();
        response.setCount(1);
        
        when(ldapService.search(any(SearchRequest.class), anyString(), anyString()))
            .thenReturn(response);

        String basicAuth = Base64.getEncoder().encodeToString("testuser:testpass".getBytes());

        // Act & Assert
        mockMvc.perform(post("/api/search")
                .header("Authorization", "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthorizedRequestReturns401() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        SearchRequest request = new SearchRequest("ldap://localhost:389", "", "(objectClass=*)", "sub");

        mockMvc.perform(post("/api/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}