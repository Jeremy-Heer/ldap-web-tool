# HTTPS Implementation Guide for LDAP Web Tool

## Overview

This guide shows how to enable HTTPS for the LDAP Web Tool web server. The project already supports SSL connections to LDAP servers (`ldaps://`), but this guide covers securing the web server itself with HTTPS.

## Quick Start

### Option 1: HTTPS Only (Recommended for Production)

1. **Generate or obtain SSL certificate** (see below for options)
2. **Use the HTTPS profile:**
   ```bash
   # With self-signed certificate
   ./generate-keystore.sh
   mvn spring-boot:run -Dspring.profiles.active=https
   ```
3. **Access via HTTPS:** https://localhost:8443/swagger-ui.html

### Option 2: Dual HTTP/HTTPS (Development)

1. **Generate certificate:**
   ```bash
   ./generate-keystore.sh
   ```
2. **Run with dual profile:**
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=dual
   ```
3. **Access via either:**
   - HTTPS: https://localhost:8443/swagger-ui.html
   - HTTP: http://localhost:8090/swagger-ui.html

## Certificate Options

### 1. Self-Signed Certificate (Development/Testing)

**Generate using provided script:**
```bash
./generate-keystore.sh
```

**Manual generation:**
```bash
keytool -genkeypair \
  -alias ldapwebtool \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore.p12 \
  -storepass changeit \
  -validity 365 \
  -dname "CN=localhost,OU=Development,O=LDAP Web Tool,L=Local,ST=Local,C=US" \
  -ext SAN=dns:localhost,ip:127.0.0.1
```

### 2. Let's Encrypt Certificate (Production)

```bash
# Install certbot
sudo apt-get install certbot

# Generate certificate
sudo certbot certonly --standalone -d yourdomain.com

# Convert to PKCS12 format
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/yourdomain.com/privkey.pem \
  -out keystore.p12 \
  -name ldapwebtool \
  -passout pass:your-secure-password
```

### 3. Corporate/CA Certificate

```bash
# If you have separate certificate and key files
openssl pkcs12 -export \
  -in certificate.crt \
  -inkey private.key \
  -out keystore.p12 \
  -name ldapwebtool \
  -passout pass:your-secure-password
```

## Configuration Profiles

### Profile: `https` (HTTPS Only)

**File:** `src/main/resources/application-https.properties`

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=ldapwebtool
```

**Usage:**
```bash
mvn spring-boot:run -Dspring.profiles.active=https
# Access: https://localhost:8443
```

### Profile: `dual` (HTTP + HTTPS)

**File:** `src/main/resources/application-dual.properties`

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=ldapwebtool
ldapwebtool.http.port=8090
```

**Usage:**
```bash
mvn spring-boot:run -Dspring.profiles.active=dual
# Access: https://localhost:8443 OR http://localhost:8090
```

### Default Profile (HTTP Only)

**File:** `src/main/resources/application.properties`

```properties
server.port=8090
# No SSL configuration
```

**Usage:**
```bash
mvn spring-boot:run
# Access: http://localhost:8090
```

## Production Deployment

### 1. Secure Configuration

For production, create `application-prod.properties`:

```properties
# Production HTTPS Configuration
server.port=443
server.ssl.enabled=true
server.ssl.key-store=file:/path/to/production/keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=ldapwebtool

# Security headers
server.ssl.client-auth=none
security.require-ssl=true

# Logging (reduce for production)
logging.level.com.example.ldapwebtool=INFO
logging.level.org.springframework.security=WARN
```

### 2. Environment Variables

```bash
export SSL_KEYSTORE_PASSWORD=your-secure-password
java -jar -Dspring.profiles.active=prod target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

### 3. Reverse Proxy (Alternative)

Instead of application-level SSL, use a reverse proxy:

**Nginx example:**
```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;
    
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    location / {
        proxy_pass http://localhost:8090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## API Usage with HTTPS

### CURL Examples

**Self-signed certificate (ignore certificate warnings):**
```bash
curl -k -X POST https://localhost:8443/api/search \
  -H "Content-Type: application/json" \
  -u "cn=admin,dc=example,dc=com:password" \
  -d '{
    "uri": "ldaps://ldap.example.com:636",
    "base": "ou=users,dc=example,dc=com",
    "filter": "(objectClass=person)"
  }'
```

**Valid certificate:**
```bash
curl -X POST https://yourdomain.com/api/search \
  -H "Content-Type: application/json" \
  -u "cn=admin,dc=example,dc=com:password" \
  -d '{
    "uri": "ldaps://ldap.example.com:636",
    "base": "ou=users,dc=example,dc=com",
    "filter": "(objectClass=person)"
  }'
```

## Swagger UI with HTTPS

Once HTTPS is enabled, access Swagger UI at:
- HTTPS: https://localhost:8443/swagger-ui.html
- Dual mode: https://localhost:8443/swagger-ui.html or http://localhost:8090/swagger-ui.html

**Note:** Browsers will show security warnings for self-signed certificates. Click "Advanced" and "Proceed" to access the site.

## Testing HTTPS Configuration

### 1. Test certificate installation:
```bash
# Check if keystore is valid
keytool -list -keystore src/main/resources/keystore.p12 -storetype PKCS12

# Test application startup
mvn spring-boot:run -Dspring.profiles.active=https
```

### 2. Test HTTPS connectivity:
```bash
# Test HTTPS endpoint
curl -k https://localhost:8443/v3/api-docs

# Test SSL handshake
openssl s_client -connect localhost:8443 -servername localhost
```

## Security Considerations

### Development
- Self-signed certificates are acceptable
- Use `-k` flag with curl to ignore certificate warnings
- Browser will show security warnings

### Production
- Use valid certificates from trusted CA
- Keep certificates updated (monitor expiration)
- Use strong keystore passwords
- Store keystore passwords as environment variables
- Consider using external certificate management
- Enable security headers and HSTS

### LDAP Connections
- The project supports both `ldap://` and `ldaps://` for backend LDAP connections
- Use `ldaps://` URIs for secure LDAP connections
- Current implementation uses trust-all for LDAPS (suitable for development)

## Troubleshooting

### Common Issues

1. **Certificate not found:**
   ```
   Error: java.io.FileNotFoundException: class path resource [keystore.p12] cannot be opened
   ```
   Solution: Ensure keystore.p12 is in `src/main/resources/`

2. **Wrong password:**
   ```
   Error: java.io.IOException: keystore password was incorrect
   ```
   Solution: Check `server.ssl.key-store-password` in properties

3. **Port already in use:**
   ```
   Error: Port 8443 was already in use
   ```
   Solution: Change port or stop other services using that port

4. **Browser certificate warnings:**
   - Expected with self-signed certificates
   - Click "Advanced" → "Proceed to localhost (unsafe)"
   - Or add certificate to browser's trusted certificates

### Debug SSL Issues

Add these JVM arguments for SSL debugging:
```bash
mvn spring-boot:run -Dspring.profiles.active=https \
  -Djavax.net.debug=ssl:handshake:verbose
```

## Summary

This implementation provides flexible HTTPS support:
- **Development:** Self-signed certificates with simple setup
- **Production:** Support for CA certificates and secure configuration
- **Flexibility:** Multiple profiles for different deployment scenarios
- **Backward compatibility:** Original HTTP mode still available

Choose the configuration that best fits your environment and security requirements.
