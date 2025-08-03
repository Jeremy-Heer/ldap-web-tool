package com.example.ldapwebtool.service;

import com.example.ldapwebtool.config.LdapSslConfig;
import com.example.ldapwebtool.model.ModifyRequest;
import com.example.ldapwebtool.model.ModifyResponse;
import com.example.ldapwebtool.model.SearchRequest;
import com.example.ldapwebtool.model.SearchResponse;
import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import com.unboundid.util.ssl.HostNameSSLSocketVerifier;
import com.unboundid.ldif.LDIFReader;
import com.unboundid.ldif.LDIFChangeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.*;

@Service
public class LdapService {
    
    private final LdapSslConfig sslConfig;
    
    @Autowired
    public LdapService(LdapSslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }
    
    /**
     * Creates an LDAP connection with SSL support if the URI scheme is ldaps://
     * Uses configuration settings to determine SSL trust behavior.
     */
    private LDAPConnection createConnection(String uri) throws Exception {
        URI ldapUri = new URI(uri);
        String scheme = ldapUri.getScheme().toLowerCase();
        String host = ldapUri.getHost();
        int port = ldapUri.getPort();
        
        if ("ldaps".equals(scheme)) {
            // Use SSL for ldaps:// URIs
            if (port <= 0) {
                port = 636; // Default LDAPS port
            }
            
            SSLSocketFactory sslSocketFactory = createSSLSocketFactory();
            
            if (sslConfig.isHostnameVerification()) {
                // Use default hostname verification
                return new LDAPConnection(sslSocketFactory, host, port);
            } else {
                // Create connection options with disabled hostname verification
                LDAPConnectionOptions options = new LDAPConnectionOptions();
                options.setSSLSocketVerifier(new HostNameSSLSocketVerifier(false));
                
                // Create connection with options
                LDAPConnection connection = new LDAPConnection(sslSocketFactory, host, port);
                connection.setConnectionOptions(options);
                return connection;
            }
        } else {
            // Use plain connection for ldap:// URIs
            if (port <= 0) {
                port = 389; // Default LDAP port
            }
            return new LDAPConnection(host, port);
        }
    }
    
    /**
     * Creates an SSL socket factory based on the configured trust settings.
     */
    private SSLSocketFactory createSSLSocketFactory() throws Exception {
        SSLUtil sslUtil;
        
        if (sslConfig.isTrustAll()) {
            // Use trust-all for development/testing
            sslUtil = new SSLUtil(new TrustAllTrustManager());
        } else {
            // Use proper certificate validation with truststore
            TrustManager trustManager = createTrustManager();
            sslUtil = new SSLUtil(trustManager);
        }
        
        return sslUtil.createSSLSocketFactory();
    }
    
    /**
     * Creates a trust manager based on the configured truststore settings.
     */
    private TrustManager createTrustManager() throws Exception {
        if (StringUtils.hasText(sslConfig.getTruststorePath())) {
            // Use custom truststore
            File truststoreFile;
            String path = sslConfig.getTruststorePath();
            
            if (path.startsWith("classpath:")) {
                // Handle classpath resources
                String resourcePath = path.substring("classpath:".length());
                String classpathFile = getClass().getClassLoader().getResource(resourcePath).getFile();
                truststoreFile = new File(classpathFile);
            } else if (path.startsWith("file:")) {
                // Handle file:// URLs
                truststoreFile = new File(path.substring("file:".length()));
            } else {
                // Handle direct file paths
                truststoreFile = new File(path);
            }
            
            return new TrustStoreTrustManager(
                truststoreFile,
                sslConfig.getTruststorePassword() != null ? sslConfig.getTruststorePassword().toCharArray() : null,
                sslConfig.getTruststoreType(),
                true // examine validity dates
            );
        } else {
            // Use JVM default truststore
            return new TrustStoreTrustManager(
                System.getProperty("javax.net.ssl.trustStore"),
                System.getProperty("javax.net.ssl.trustStorePassword") != null ? 
                    System.getProperty("javax.net.ssl.trustStorePassword").toCharArray() : null,
                System.getProperty("javax.net.ssl.trustStoreType", "JKS"),
                true // examine validity dates
            );
        }
    }
    
    public SearchResponse search(SearchRequest request, String username, String password) throws Exception {
        // Create connection with SSL support if needed
        LDAPConnection connection = createConnection(request.getUri());
        
        try {
            // Authenticate
            connection.bind(username, password);
            
            // Perform search
            SearchScope scope = parseScope(request.getScope());
            SearchResult searchResult = connection.search(
                request.getBase(),
                scope,
                request.getFilter()
            );
            
            // Convert to response
            List<SearchResponse.LdapEntry> entries = new ArrayList<>();
            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                Map<String, Object> attributes = new HashMap<>();
                
                for (Attribute attr : entry.getAttributes()) {
                    String[] values = attr.getValues();
                    if (values.length == 1) {
                        attributes.put(attr.getName(), values[0]);
                    } else {
                        attributes.put(attr.getName(), Arrays.asList(values));
                    }
                }
                
                entries.add(new SearchResponse.LdapEntry(entry.getDN(), attributes));
            }
            
            return new SearchResponse(entries);
            
        } finally {
            connection.close();
        }
    }
    
    public String searchToLdif(SearchRequest request, String username, String password) throws Exception {
        // Create connection with SSL support if needed
        LDAPConnection connection = createConnection(request.getUri());
        
        try {
            connection.bind(username, password);
            
            SearchScope scope = parseScope(request.getScope());
            SearchResult searchResult = connection.search(
                request.getBase(),
                scope,
                request.getFilter()
            );
            
            StringBuilder ldifBuilder = new StringBuilder();
            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                ldifBuilder.append("dn: ").append(entry.getDN()).append("\n");
                
                for (Attribute attr : entry.getAttributes()) {
                    for (String value : attr.getValues()) {
                        ldifBuilder.append(attr.getName()).append(": ").append(value).append("\n");
                    }
                }
                ldifBuilder.append("\n");
            }
            
            return ldifBuilder.toString();
            
        } finally {
            connection.close();
        }
    }
    
    public ModifyResponse modify(ModifyRequest request, String username, String password) throws Exception {
        // Create connection with SSL support if needed
        LDAPConnection connection = createConnection(request.getUri());
        
        try {
            connection.bind(username, password);
            
            List<Modification> modifications = new ArrayList<>();
            
            for (ModifyRequest.Modification mod : request.getModifications()) {
                ModificationType modType = parseModificationType(mod.getOperation());
                
                if (mod.getValues() != null && !mod.getValues().isEmpty()) {
                    modifications.add(new Modification(modType, mod.getAttribute(), 
                        mod.getValues().toArray(new String[0])));
                } else {
                    modifications.add(new Modification(modType, mod.getAttribute()));
                }
            }
            
            LDAPResult result = connection.modify(request.getDn(), modifications);
            
            if (result.getResultCode() == ResultCode.SUCCESS) {
                return new ModifyResponse(true, "Modification successful", request.getDn());
            } else {
                return new ModifyResponse(false, result.getDiagnosticMessage(), request.getDn());
            }
            
        } finally {
            connection.close();
        }
    }
    
    public ModifyResponse modifyFromLdif(String ldifContent, String uri, String username, String password) throws Exception {
        // Create connection with SSL support if needed
        LDAPConnection connection = createConnection(uri);
        
        try {
            connection.bind(username, password);
            
            try (LDIFReader ldifReader = new LDIFReader(new ByteArrayInputStream(ldifContent.getBytes()))) {
                String lastDn = null;
                boolean allSuccessful = true;
                StringBuilder messages = new StringBuilder();
                
                LDIFChangeRecord changeRecord;
                while ((changeRecord = ldifReader.readChangeRecord()) != null) {
                    lastDn = changeRecord.getDN();
                    
                    try {
                        LDAPResult result = changeRecord.processChange(connection);
                        if (result.getResultCode() != ResultCode.SUCCESS) {
                            allSuccessful = false;
                            messages.append("Failed to modify ").append(lastDn)
                                    .append(": ").append(result.getDiagnosticMessage()).append("; ");
                        }
                    } catch (Exception e) {
                        allSuccessful = false;
                        messages.append("Error modifying ").append(lastDn)
                                .append(": ").append(e.getMessage()).append("; ");
                    }
                }
                
                String message = allSuccessful ? "All modifications successful" : messages.toString();
                return new ModifyResponse(allSuccessful, message, lastDn);
            }
            
        } finally {
            connection.close();
        }
    }
    
    private SearchScope parseScope(String scope) {
        switch (scope.toLowerCase()) {
            case "base":
                return SearchScope.BASE;
            case "one":
                return SearchScope.ONE;
            case "sub":
            default:
                return SearchScope.SUB;
        }
    }
    
    private ModificationType parseModificationType(String operation) {
        switch (operation.toLowerCase()) {
            case "add":
                return ModificationType.ADD;
            case "delete":
                return ModificationType.DELETE;
            case "replace":
                return ModificationType.REPLACE;
            default:
                throw new IllegalArgumentException("Invalid modification operation: " + operation);
        }
    }
}
