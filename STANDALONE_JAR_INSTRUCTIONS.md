# LDAP Web Tool - Standalone JAR Instructions

This document provides comprehensive instructions for building and running the LDAP Web Tool as a standalone JAR file.

**📋 For detailed configuration options, see:**
- **[STANDALONE_JAR_CONFIGURATION.md](STANDALONE_JAR_CONFIGURATION.md)** - Complete configuration guide
- **[HTTPS_IMPLEMENTATION.md](HTTPS_IMPLEMENTATION.md)** - HTTPS setup for JAR deployment
- **[LDAP_SSL_CONFIGURATION.md](LDAP_SSL_CONFIGURATION.md)** - LDAP SSL/TLS configuration

## Prerequisites

- **Java 17 or later** - Required for Spring Boot 3.2.0
- **Maven 3.6 or later** - For building the application
- **Access to an LDAP server** - For testing the application

### Verify Java Version
```bash
java -version
```
Ensure you have Java 17 or later installed.

### Verify Maven Version
```bash
mvn -version
```
Ensure you have Maven 3.6 or later installed.

## Building the Standalone JAR

### 1. Navigate to Project Directory
```bash
cd /path/to/ldapWebTool
```

### 2. Clean and Build the JAR
```bash
mvn clean package
```

This command will:
- Clean any previous build artifacts
- Compile the source code
- Run tests
- Package the application into an executable JAR file

The JAR file will be created in the `target/` directory with the name:
```
ldap-web-tool-0.0.1-SNAPSHOT.jar
```

### 3. Build without Running Tests (Optional)
If you want to skip tests during the build process:
```bash
mvn clean package -DskipTests
```

## Running the Standalone JAR

### Basic Execution
Once the JAR is built, you can run it directly with Java:

```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

The application will start and be available at:
- **Main Application**: http://localhost:8090
- **Swagger UI**: http://localhost:8090/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8090/v3/api-docs

### Running with Custom Configuration

#### 1. Custom Port
To run on a different port:
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --server.port=8080
```

#### 2. Custom Properties File
To use a custom application.properties file:
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:/custom-application.properties
```

#### 3. External Properties File
To use an external properties file:
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --spring.config.location=file:./config/application.properties
```

#### 4. Environment-Specific Profiles
To run with a specific Spring profile:
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

#### 5. JVM Options
To set JVM options (memory, garbage collection, etc.):
```bash
java -Xmx512m -Xms256m -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

### Running in Background

#### Linux/macOS - Using nohup
```bash
nohup java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

#### Linux/macOS - Using screen
```bash
screen -S ldap-web-tool
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
# Press Ctrl+A, then D to detach
```

#### Windows - Using start
```cmd
start java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

## Configuration Options

### Default Configuration
The application uses the following default settings:
- **Port**: 8090
- **Context Path**: / (root)
- **Log Level**: DEBUG for application, INFO for LDAP SDK

### Environment Variables
You can override configuration using environment variables:

```bash
# Set port via environment variable
export SERVER_PORT=8080
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar

# Set multiple environment variables
export SERVER_PORT=8080
export LOGGING_LEVEL_COM_EXAMPLE_LDAPWEBTOOL=INFO
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

### Command Line Arguments
Override specific properties via command line:

```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --server.port=8080 \
  --logging.level.com.example.ldapwebtool=INFO \
  --spring.application.name=my-ldap-tool
```

## Deployment Options

### 1. Simple File Copy Deployment
Copy the JAR file to your target server:
```bash
scp target/ldap-web-tool-0.0.1-SNAPSHOT.jar user@server:/opt/ldap-web-tool/
```

### 2. Docker Deployment
Create a simple Dockerfile:
```dockerfile
FROM openjdk:17-jre-slim
COPY target/ldap-web-tool-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t ldap-web-tool .
docker run -p 8090:8090 ldap-web-tool
```

### 3. Systemd Service (Linux)
Create a systemd service file `/etc/systemd/system/ldap-web-tool.service`:

```ini
[Unit]
Description=LDAP Web Tool
After=network.target

[Service]
Type=simple
User=ldap-web-tool
WorkingDirectory=/opt/ldap-web-tool
ExecStart=/usr/bin/java -jar /opt/ldap-web-tool/ldap-web-tool-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable ldap-web-tool
sudo systemctl start ldap-web-tool
sudo systemctl status ldap-web-tool
```

## Testing the Application

### 1. Health Check
```bash
curl http://localhost:8090/actuator/health
```

### 2. Access Swagger UI
Open your browser and navigate to:
```
http://localhost:8090/swagger-ui.html
```

### 3. Test LDAP Search (JSON)
```bash
curl -X POST http://localhost:8090/api/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'cn=admin,dc=example,dc=com:password' | base64)" \
  -d '{
    "base": "dc=example,dc=com",
    "filter": "(objectClass=*)",
    "scope": "SUB"
  }'
```

### 4. Test LDAP Search (LDIF)
```bash
curl -X POST http://localhost:8090/api/search/ldif \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'cn=admin,dc=example,dc=com:password' | base64)" \
  -d '{
    "base": "dc=example,dc=com",
    "filter": "(objectClass=*)",
    "scope": "SUB"
  }'
```

## Troubleshooting

### Common Issues

#### 1. Port Already in Use
```
Error: Port 8090 is already in use
```
**Solution**: Use a different port:
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --server.port=8091
```

#### 2. Java Version Mismatch
```
Error: Unsupported major.minor version
```
**Solution**: Ensure you're using Java 17 or later:
```bash
java -version
```

#### 3. Out of Memory
```
Error: Java heap space
```
**Solution**: Increase heap size:
```bash
java -Xmx1g -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

#### 4. Application Won't Start
Check the application logs for detailed error messages. The application logs will show on the console or in the specified log file.

### Enabling Additional Logging
To enable more detailed logging:
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar \
  --logging.level.com.example.ldapwebtool=DEBUG \
  --logging.level.org.springframework.security=DEBUG
```

## Security Considerations

### 1. Network Security
- Run the application behind a reverse proxy (nginx, Apache)
- Use HTTPS in production environments
- Restrict access to trusted networks

### 2. LDAP Security
- Use secure LDAP connections (LDAPS) when possible
- Validate LDAP server certificates
- Use service accounts with minimal required permissions

### 3. Application Security
- Keep Java and dependencies updated
- Monitor application logs for security events
- Consider implementing rate limiting

## Maintenance

### Updating the Application
1. Stop the running application
2. Build the new JAR file
3. Replace the old JAR with the new one
4. Restart the application

### Log Management
- Logs are written to the console by default
- Consider redirecting logs to files for production use
- Implement log rotation to manage disk space

### Monitoring
- Monitor application health via `/actuator/health`
- Set up alerts for application downtime
- Monitor resource usage (CPU, memory, disk)

## Additional Resources

- **Project Documentation**: See `README.md` for API usage examples
- **Swagger UI**: Interactive API documentation at `/swagger-ui.html`
- **OpenAPI Specification**: Machine-readable API docs at `/v3/api-docs`
- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **UnboundID LDAP SDK**: https://ldap.com/unboundid-ldap-sdk-for-java/
