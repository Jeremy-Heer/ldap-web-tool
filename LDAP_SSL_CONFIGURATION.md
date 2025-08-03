# LDAP SSL/TLS Configuration Guide

## Overview

This guide explains how to configure SSL/TLS trust settings for LDAP connections in the LDAP Web Tool. The application supports both development-friendly "trust-all" mode and production-ready certificate validation using truststores.

## Configuration Options

### Development Mode (Trust All)

**Default Setting** - Accepts all SSL certificates without validation:

```properties
# Trust all SSL certificates (development/testing)
ldap.ssl.trust-all=true
```

**Pros:**
- Easy setup for development and testing
- Works with self-signed certificates
- No additional certificate management required

**Cons:**
- Not secure for production use
- Vulnerable to man-in-the-middle attacks
- Does not validate certificate authenticity

### Production Mode (Certificate Validation)

**Secure Setting** - Validates certificates using configured truststore:

```properties
# Production SSL configuration
ldap.ssl.trust-all=false
ldap.ssl.truststore-path=file:/path/to/truststore.jks
ldap.ssl.truststore-password=your-truststore-password
ldap.ssl.truststore-type=JKS
ldap.ssl.hostname-verification=true
```

## Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `ldap.ssl.trust-all` | `true` | Whether to trust all SSL certificates |
| `ldap.ssl.truststore-path` | - | Path to truststore file |
| `ldap.ssl.truststore-password` | - | Password for truststore |
| `ldap.ssl.truststore-type` | `JKS` | Truststore format (JKS, PKCS12) |
| `ldap.ssl.hostname-verification` | `true` | Enable hostname verification |

## Truststore Path Options

### 1. Classpath Resource
```properties
ldap.ssl.truststore-path=classpath:truststore.jks
```
Place `truststore.jks` in `src/main/resources/`

### 2. Absolute File Path
```properties
ldap.ssl.truststore-path=file:/etc/ssl/certs/truststore.jks
```

### 3. Relative File Path
```properties
ldap.ssl.truststore-path=/path/to/truststore.jks
```

### 4. System Default Truststore
```properties
# Use JVM default truststore (leave truststore-path empty)
ldap.ssl.trust-all=false
```

## Setting Up Production SSL

### Step 1: Create a Truststore

#### Option A: Add LDAP Server Certificate to Truststore

```bash
# Download the LDAP server certificate
openssl s_client -servername ldap.example.com -connect ldap.example.com:636 \
  -showcerts < /dev/null | openssl x509 -outform PEM > ldap-server.crt

# Create truststore and import certificate
keytool -importcert \
  -alias ldap-server \
  -file ldap-server.crt \
  -keystore truststore.jks \
  -storepass changeit \
  -noprompt
```

#### Option B: Import CA Certificate

```bash
# If you have the CA certificate that signed the LDAP server certificate
keytool -importcert \
  -alias ca-cert \
  -file ca-certificate.crt \
  -keystore truststore.jks \
  -storepass changeit \
  -noprompt
```

#### Option C: Copy from System Truststore

```bash
# Copy system default truststore
cp $JAVA_HOME/lib/security/cacerts custom-truststore.jks

# Or on many Linux systems:
cp /etc/ssl/certs/java/cacerts custom-truststore.jks
```

### Step 2: Configure Application

#### For Custom Truststore:

```properties
# Application configuration
ldap.ssl.trust-all=false
ldap.ssl.truststore-path=classpath:truststore.jks
ldap.ssl.truststore-password=changeit
ldap.ssl.truststore-type=JKS
ldap.ssl.hostname-verification=true
```

#### For System Truststore:

```properties
# Use JVM default truststore
ldap.ssl.trust-all=false
ldap.ssl.hostname-verification=true
```

### Step 3: Set Environment Variables (Production)

```bash
# For custom truststore
export LDAP_TRUSTSTORE_PASSWORD=your-secure-password

# For system truststore (if needed)
export JAVAX_NET_SSL_TRUSTSTORE=/path/to/truststore.jks
export JAVAX_NET_SSL_TRUSTSTOREPASSWORD=password
```

## Configuration Examples

### Example 1: Development with Self-Signed Certificates

```properties
# application-dev.properties
ldap.ssl.trust-all=true
ldap.ssl.hostname-verification=false
```

### Example 2: Production with Custom Truststore

```properties
# application-prod.properties
ldap.ssl.trust-all=false
ldap.ssl.truststore-path=file:/etc/ssl/ldap/truststore.jks
ldap.ssl.truststore-password=${LDAP_TRUSTSTORE_PASSWORD}
ldap.ssl.truststore-type=JKS
ldap.ssl.hostname-verification=true
```

### Example 3: Production with System Truststore

```properties
# application-prod.properties
ldap.ssl.trust-all=false
ldap.ssl.hostname-verification=true
# No truststore path = use JVM default
```

## Testing SSL Configuration

### Test LDAP Connection

```bash
# Test with secure configuration
curl -k -X POST https://localhost:8443/api/search \
  -H "Content-Type: application/json" \
  -u "cn=admin,dc=example,dc=com:password" \
  -d '{
    "uri": "ldaps://secure-ldap.example.com:636",
    "base": "dc=example,dc=com",
    "filter": "(objectClass=*)",
    "scope": "base"
  }'
```

### Verify Certificate Chain

```bash
# Check LDAP server certificate
openssl s_client -servername ldap.example.com -connect ldap.example.com:636

# List certificates in truststore
keytool -list -keystore truststore.jks -storepass changeit
```

## Running with Different Profiles

### Development Mode
```bash
# Uses trust-all by default
mvn spring-boot:run

# Explicit development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Mode
```bash
# Production profile with certificate validation
export LDAP_TRUSTSTORE_PASSWORD=your-password
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### HTTPS + Production SSL
```bash
# Both HTTPS for web server and SSL validation for LDAP
export SSL_KEYSTORE_PASSWORD=web-server-keystore-password
export LDAP_TRUSTSTORE_PASSWORD=ldap-truststore-password
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Troubleshooting

### Common SSL Issues

#### 1. Certificate Not Trusted
```
Error: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```

**Solution:** Add the LDAP server certificate or its CA to your truststore.

#### 2. Hostname Verification Failed
```
Error: java.security.cert.CertificateException: No subject alternative names matching IP address found
```

**Solution:** Set `ldap.ssl.hostname-verification=false` or ensure certificate has correct SAN.

#### 3. Truststore Not Found
```
Error: java.io.FileNotFoundException: truststore.jks
```

**Solution:** Verify the truststore path and ensure the file exists.

#### 4. Wrong Truststore Password
```
Error: java.io.IOException: Keystore was tampered with, or password was incorrect
```

**Solution:** Verify the truststore password is correct.

### Debug SSL Issues

Add JVM arguments for SSL debugging:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod \
  -Djavax.net.debug=ssl:handshake:verbose \
  -Dcom.unboundid.ldap.sdk.debug.enabled=true
```

## Security Best Practices

### Production Deployment

1. **Never use trust-all in production**
2. **Use strong truststore passwords**
3. **Store passwords as environment variables**
4. **Regularly update certificates**
5. **Enable hostname verification**
6. **Use proper certificate authorities**
7. **Monitor certificate expiration**

### Certificate Management

1. **Use certificate automation tools** (Let's Encrypt, cert-manager)
2. **Implement certificate rotation procedures**
3. **Monitor certificate validity dates**
4. **Use proper certificate storage** (not in application JAR)
5. **Document certificate renewal procedures**

## Migration Guide

### From Trust-All to Certificate Validation

1. **Identify your LDAP servers and their certificates**
2. **Create truststore with appropriate certificates**
3. **Test in development environment first**
4. **Update configuration gradually**
5. **Monitor for SSL-related errors**

### Example Migration Steps:

```bash
# Step 1: Backup current configuration
cp application.properties application.properties.backup

# Step 2: Create truststore with LDAP certificates
./create-truststore.sh

# Step 3: Update configuration
echo "ldap.ssl.trust-all=false" >> application-prod.properties
echo "ldap.ssl.truststore-path=classpath:truststore.jks" >> application-prod.properties

# Step 4: Test
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Step 5: Verify LDAP connections work
curl -k https://localhost:8443/v3/api-docs
```

This configuration provides a secure and flexible SSL/TLS setup for LDAP connections while maintaining backward compatibility for development environments.
