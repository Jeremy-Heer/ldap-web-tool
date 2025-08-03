package com.example.ldapwebtool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for LDAP SSL/TLS settings.
 * These properties control how the application handles SSL connections to LDAP servers.
 */
@Configuration
@ConfigurationProperties(prefix = "ldap.ssl")
public class LdapSslConfig {
    
    /**
     * Whether to trust all SSL certificates when connecting to LDAP servers.
     * 
     * When true (default): Uses TrustAllTrustManager - accepts all certificates without validation.
     * Suitable for development and testing environments.
     * 
     * When false: Uses proper certificate validation with configured truststore.
     * Required for production environments.
     */
    private boolean trustAll = true;
    
    /**
     * Path to the truststore file containing trusted CA certificates.
     * Only used when trustAll=false.
     * 
     * Examples:
     * - classpath:truststore.jks (file in src/main/resources)
     * - file:/path/to/truststore.jks (absolute file path)
     * - /etc/ssl/certs/java/cacerts (system default)
     */
    private String truststorePath;
    
    /**
     * Password for the truststore file.
     * Only used when trustAll=false and truststorePath is specified.
     */
    private String truststorePassword;
    
    /**
     * Type of the truststore (JKS, PKCS12, etc.).
     * Defaults to JKS if not specified.
     */
    private String truststoreType = "JKS";
    
    /**
     * Whether to enable hostname verification for SSL connections.
     * When false, hostname verification is disabled (useful for self-signed certificates).
     * When true, hostname must match certificate CN/SAN.
     */
    private boolean hostnameVerification = true;
    
    // Getters and setters
    
    public boolean isTrustAll() {
        return trustAll;
    }
    
    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }
    
    public String getTruststorePath() {
        return truststorePath;
    }
    
    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }
    
    public String getTruststorePassword() {
        return truststorePassword;
    }
    
    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }
    
    public String getTruststoreType() {
        return truststoreType;
    }
    
    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }
    
    public boolean isHostnameVerification() {
        return hostnameVerification;
    }
    
    public void setHostnameVerification(boolean hostnameVerification) {
        this.hostnameVerification = hostnameVerification;
    }
}
