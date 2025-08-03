# Standalone JAR Configuration Guide

## Overview

When building the LDAP Web Tool as a standalone JAR, all application properties files are embedded in the JAR. However, Spring Boot provides multiple ways to override or supplement the embedded configuration for different deployment scenarios.

## What's Included in the JAR

The standalone JAR (`ldap-web-tool-0.0.1-SNAPSHOT.jar`) includes:

```
BOOT-INF/classes/
├── application.properties                 # Default configuration
├── application-https.properties           # HTTPS profile
├── application-dual.properties            # Dual HTTP/HTTPS profile  
├── application-prod.properties            # Production profile
├── application-prod-test.properties       # Production test profile
├── keystore.p12                          # SSL certificate for web server
└── truststore.jks                        # LDAP SSL truststore
```

## Configuration Methods (Priority Order)

Spring Boot loads configuration in the following priority order (highest to lowest):

1. **Command line arguments**
2. **Environment variables**
3. **External configuration files**
4. **Profile-specific properties inside JAR**
5. **Default properties inside JAR**

## Method 1: Default Configuration (No Changes Needed)

The JAR works out-of-the-box with embedded configuration:

```bash
# Build the JAR
mvn clean package

# Run with default settings (HTTP on port 8090)
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

**Access:** http://localhost:8090/swagger-ui.html

## Method 2: Profile Selection

Use embedded profiles for different deployment scenarios:

```bash
# HTTPS only (port 8443)
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --spring.profiles.active=https

# Dual HTTP/HTTPS (ports 8090 + 8443)
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --spring.profiles.active=dual

# Production profile (HTTPS with certificate validation)
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Method 3: External Configuration Files

Create external configuration files to override embedded settings:

### Example: `external-config.properties`
```properties
# Override server port
server.port=9090

# Override LDAP SSL settings
ldap.ssl.trust-all=false
ldap.ssl.truststore-path=file:/etc/ssl/ldap-truststore.jks
ldap.ssl.truststore-password=${LDAP_TRUSTSTORE_PASSWORD}

# Override logging levels
logging.level.com.example.ldapwebtool=INFO
logging.level.root=WARN
```

### Usage:
```bash
# Use external configuration file
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --spring.config.location=classpath:/application.properties,./external-config.properties

# Or specify additional config
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --spring.config.additional-location=./external-config.properties
```

## Method 4: Environment Variables

Use environment variables for sensitive configuration:

```bash
# Set environment variables
export SERVER_PORT=9090
export LDAP_SSL_TRUST_ALL=false
export LDAP_SSL_TRUSTSTORE_PASSWORD=secretpassword
export SSL_KEYSTORE_PASSWORD=webkeypassword

# Run JAR with environment variables
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

**Note:** Spring Boot converts property names to environment variable format:
- `server.port` → `SERVER_PORT`
- `ldap.ssl.trust-all` → `LDAP_SSL_TRUST_ALL`
- `ldap.ssl.truststore-password` → `LDAP_SSL_TRUSTSTORE_PASSWORD`

## Method 5: Command Line Arguments

Override specific properties directly:

```bash
# Override properties via command line
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --server.port=9090 \
  --ldap.ssl.trust-all=false \
  --logging.level.com.example.ldapwebtool=INFO
```

## Method 6: System Properties

Use JVM system properties:

```bash
# Set properties as JVM arguments
java -Dserver.port=9090 \
     -Dldap.ssl.trust-all=false \
     -Dspring.profiles.active=https \
     -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

## Production Deployment Examples

### Example 1: Simple Production Deployment

```bash
# Create production configuration
cat > production.properties << EOF
server.port=443
ldap.ssl.trust-all=false
ldap.ssl.truststore-path=file:/etc/ssl/certs/ldap-truststore.jks
logging.level.com.example.ldapwebtool=INFO
logging.level.root=WARN
EOF

# Set sensitive values as environment variables
export LDAP_SSL_TRUSTSTORE_PASSWORD=prod-truststore-password
export SSL_KEYSTORE_PASSWORD=prod-web-keystore-password

# Run with production configuration
java -jar ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.config.additional-location=./production.properties
```

### Example 2: Docker Container Deployment

```bash
# Use environment variables for container deployment
docker run -d \
  -p 8443:8443 \
  -e SPRING_PROFILES_ACTIVE=https \
  -e SERVER_PORT=8443 \
  -e LDAP_SSL_TRUST_ALL=false \
  -e LDAP_SSL_TRUSTSTORE_PASSWORD=secretpassword \
  -v /host/path/to/truststore.jks:/app/truststore.jks \
  -v /host/path/to/keystore.p12:/app/keystore.p12 \
  my-ldap-web-tool:latest
```

### Example 3: Systemd Service Deployment

```ini
# /etc/systemd/system/ldap-web-tool.service
[Unit]
Description=LDAP Web Tool
After=network.target

[Service]
Type=simple
User=ldap-web-tool
WorkingDirectory=/opt/ldap-web-tool
ExecStart=/usr/bin/java -jar ldap-web-tool.jar --spring.profiles.active=prod
Environment=LDAP_SSL_TRUSTSTORE_PASSWORD=secretpassword
Environment=SSL_KEYSTORE_PASSWORD=webkeypassword
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

## External Certificate Management

### Option 1: External Keystore for Web Server SSL
```bash
# Use external keystore for web server
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=https \
  --server.ssl.key-store=file:/etc/ssl/certs/web-keystore.p12 \
  --server.ssl.key-store-password=${WEB_KEYSTORE_PASSWORD}
```

### Option 2: External Truststore for LDAP SSL
```bash
# Use external truststore for LDAP connections
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --ldap.ssl.trust-all=false \
  --ldap.ssl.truststore-path=file:/etc/ssl/certs/ldap-truststore.jks \
  --ldap.ssl.truststore-password=${LDAP_TRUSTSTORE_PASSWORD}
```

## Configuration File Locations

Spring Boot searches for external configuration files in these locations:

1. Current directory: `./application.properties`
2. Current directory config subdirectory: `./config/application.properties`
3. Classpath root: `classpath:/application.properties`
4. Classpath config package: `classpath:/config/application.properties`

### Example Directory Structure:
```
/opt/ldap-web-tool/
├── ldap-web-tool-0.0.1-SNAPSHOT.jar
├── application.properties              # Overrides embedded config
├── config/
│   ├── application-prod.properties     # Production overrides
│   └── logback-spring.xml              # Custom logging config
├── ssl/
│   ├── web-keystore.p12               # External web server certificate
│   └── ldap-truststore.jks            # External LDAP truststore
└── logs/
    └── application.log
```

## Testing Configuration

### Verify Active Configuration:
```bash
# Add actuator endpoint to see configuration
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --management.endpoints.web.exposure.include=env,configprops

# Check active configuration (if actuator is enabled)
curl http://localhost:8090/actuator/env
curl http://localhost:8090/actuator/configprops
```

### Debug Configuration Loading:
```bash
# Enable configuration debug logging
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --debug \
  --logging.level.org.springframework.boot.context.config=DEBUG
```

## Security Best Practices

### 1. Never Embed Secrets in JAR
❌ **Don't do this:**
```properties
# application.properties inside JAR
ldap.ssl.truststore-password=secretpassword
```

✅ **Do this instead:**
```properties
# application.properties inside JAR
ldap.ssl.truststore-password=${LDAP_TRUSTSTORE_PASSWORD}
```

### 2. Use External Configuration for Production
```bash
# Production deployment with external config
export LDAP_SSL_TRUSTSTORE_PASSWORD=secure-password
java -jar app.jar --spring.config.location=file:./config/
```

### 3. Secure Configuration Files
```bash
# Set appropriate file permissions
chmod 600 /etc/ldap-web-tool/application.properties
chown ldap-web-tool:ldap-web-tool /etc/ldap-web-tool/application.properties
```

## Summary

The standalone JAR provides maximum flexibility:

- **✅ Works out-of-the-box** with embedded configuration
- **✅ Multiple profiles** for different environments
- **✅ External configuration** for production customization
- **✅ Environment variables** for sensitive data
- **✅ Command line overrides** for quick changes
- **✅ External certificates** for enhanced security

Choose the method that best fits your deployment requirements and security policies.
