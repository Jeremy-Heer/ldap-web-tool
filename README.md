# LDAP Web Tool

A Spring Boot REST API. The application will start on port 8090 by default.

## API Documentation

### Swagger UI
Once the application is running, you can access the interactive Swagger UI documentation at:
**http://localhost:8090/swagger-ui.html**

The Swagger UI provides:
- Interactive API documentation
- Ability to test endpoints directly from the browser
- Request/response examples
- Schema definitions
- Authentication setup for testing

### OpenAPI Specification
The OpenAPI 3.0 specification is available at:
**http://localhost:8090/v3/api-docs**

## API Usage API for performing LDAP operations via HTTP requests. This tool acts as a proxy between HTTP clients and LDAP servers, supporting both JSON and LDIF formats.

## Features

- **LDAP Search**: Perform LDAP searches with customizable base, filter, and scope
- **LDAP Modify**: Modify LDAP entries using add, delete, and replace operations
- **Multiple Formats**: Support for both JSON and LDIF (LDAP Data Interchange Format)
- **Separate Endpoints**: Dedicated endpoints for JSON and LDIF formats for better Swagger UI compatibility
- **Basic Authentication**: Uses HTTP Basic Authentication with LDAP credentials
- **RESTful API**: Clean REST endpoints for easy integration
- **Swagger UI**: Interactive API documentation and testing interface
- **UnboundID LDAP SDK**: Robust LDAP client implementation
- **SSL/TLS Support**: Configurable SSL trust settings for secure LDAP connections
- **HTTPS Support**: Optional HTTPS for web server with self-signed or CA certificates

## Recent Updates

**v1.2.0 - Configurable SSL Trust Settings**
- Added configurable SSL trust settings for LDAP connections
- Support for both trust-all (development) and truststore-based (production) SSL validation
- New configuration properties for LDAP SSL behavior
- Truststore setup script and comprehensive SSL documentation
- Backward compatible with existing trust-all behavior

**v1.1.0 - LDIF Content Type Fix**
- Separated endpoints by content type for better Swagger UI compatibility
- Fixed issue where Swagger UI curl commands showed incorrect Accept headers for LDIF
- Added dedicated `/ldif` endpoints for clear content type handling
- Improved API documentation with usage examples
- Enhanced Swagger UI experience with predictable content negotiation

## Prerequisites

- Java 17 or later
- Maven 3.6 or later
- Access to an LDAP server for testing

## Building and Running

### Development Mode
```bash
# Build the application
mvn clean compile

# Run in development mode (HTTP)
mvn spring-boot:run

# Run with HTTPS enabled
./generate-keystore.sh  # Generate self-signed certificate
mvn spring-boot:run -Dspring.profiles.active=https

# Run with both HTTP and HTTPS (dual mode)
mvn spring-boot:run -Dspring.profiles.active=dual
```

### Standalone JAR
```bash
# Build standalone JAR
mvn clean package

# Run standalone JAR
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

The application will start on port 8090 by default. For HTTPS, it will start on port 8443.

**📋 For HTTPS configuration, see:**
- **[HTTPS_IMPLEMENTATION.md](HTTPS_IMPLEMENTATION.md)** - Complete HTTPS setup guide

**📋 For deployment and configuration guides, see:**
- **[HTTPS_IMPLEMENTATION.md](HTTPS_IMPLEMENTATION.md)** - Complete HTTPS setup guide
- **[LDAP_SSL_CONFIGURATION.md](LDAP_SSL_CONFIGURATION.md)** - LDAP SSL/TLS configuration
- **[STANDALONE_JAR_INSTRUCTIONS.md](STANDALONE_JAR_INSTRUCTIONS.md)** - Comprehensive deployment guide
- **[QUICK_START.md](QUICK_START.md)** - Quick reference commands

## API Documentation

### Swagger UI
Once the application is running, you can access the interactive Swagger UI documentation at:
**http://localhost:8090/swagger-ui.html**

The Swagger UI provides:
- Interactive API documentation
- Ability to test endpoints directly from the browser
- Request/response examples for both JSON and LDIF formats
- Schema definitions
- Authentication setup for testing

### OpenAPI Specification
The OpenAPI 3.0 specification is available at:
**http://localhost:8090/v3/api-docs**

## API Usage

### Authentication

All API endpoints require HTTP Basic Authentication. Use your LDAP DN as the username and your LDAP password as the password.

Example:
- Username: `cn=admin,dc=example,dc=com`
- Password: `your-ldap-password`

### Search Endpoints

The API provides separate endpoints for JSON and LDIF response formats to ensure clear content type handling and better Swagger UI compatibility.

#### JSON Search Response

**POST /api/search**

```bash
curl -X POST http://localhost:8090/api/search \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -u "cn=admin,dc=example,dc=com:password" \
  -d '{
    "uri": "ldap://ldap.example.com:389",
    "base": "ou=users,dc=example,dc=com",
    "filter": "(objectClass=person)",
    "scope": "sub"
  }'
```

#### LDIF Search Response

**POST /api/search/ldif**

```bash
curl -X POST http://localhost:8090/api/search/ldif \
  -H "Content-Type: application/json" \
  -H "Accept: application/ldif" \
  -u "cn=admin,dc=example,dc=com:password" \
  -d '{
    "uri": "ldap://ldap.example.com:389",
    "base": "ou=users,dc=example,dc=com",
    "filter": "(objectClass=person)",
    "scope": "sub"
  }'
```

### Modify Endpoints

#### JSON Modify Request

**POST /api/modify**

```bash
curl -X POST http://localhost:8090/api/modify \
  -H "Content-Type: application/json" \
  -u "cn=admin,dc=example,dc=com:password" \
  -d '{
    "uri": "ldap://ldap.example.com:389",
    "dn": "cn=John Doe,ou=users,dc=example,dc=com",
    "modifications": [
      {
        "operation": "replace",
        "attribute": "mail",
        "values": ["john.newemail@example.com"]
      },
      {
        "operation": "add",
        "attribute": "telephoneNumber",
        "values": ["+1-555-123-4567"]
      }
    ]
  }'
```

#### LDIF Modify Request

**POST /api/modify/ldif**

```bash
curl -X POST "http://localhost:8090/api/modify/ldif?uri=ldap://ldap.example.com:389" \
  -H "Content-Type: application/ldif" \
  -u "cn=admin,dc=example,dc=com:password" \
  -d 'dn: cn=John Doe,ou=users,dc=example,dc=com
changetype: modify
replace: mail
mail: john.newemail@example.com
-
add: telephoneNumber
telephoneNumber: +1-555-123-4567
-'
```

## API Endpoints Summary

| Endpoint | Request Format | Response Format | Description |
|----------|---------------|----------------|-------------|
| `POST /api/search` | JSON | JSON | LDAP search with JSON response |
| `POST /api/search/ldif` | JSON | LDIF | LDAP search with LDIF response |
| `POST /api/modify` | JSON | JSON | LDAP modify with JSON request |
| `POST /api/modify/ldif` | LDIF | JSON | LDAP modify with LDIF request |

## Request/Response Models

### SearchRequest
- `uri` (required): LDAP server URI
- `base` (optional): Search base DN (defaults to empty string)
- `filter` (optional): LDAP filter (defaults to "(objectClass=*)")
- `scope` (optional): Search scope - "base", "one", or "sub" (defaults to "sub")

### ModifyRequest
- `uri` (required): LDAP server URI
- `dn` (required): DN of the entry to modify
- `modifications` (required): Array of modification objects
  - `operation` (required): "add", "delete", or "replace"
  - `attribute` (required): Attribute name
  - `values` (optional): Array of values

## Error Handling

The API returns appropriate HTTP status codes and error messages:
- `400 Bad Request`: Invalid request format or missing required fields
- `401 Unauthorized`: Invalid LDAP credentials
- `500 Internal Server Error`: LDAP server errors or connection issues

## Configuration

Edit `src/main/resources/application.properties` to customize:
- Server port and SSL settings
- LDAP SSL trust configuration
- Logging levels
- JSON serialization settings

### LDAP SSL Configuration

The application supports configurable SSL trust settings for LDAP connections:

**Development Mode (Default):**
```properties
# Trust all SSL certificates (development/testing)
ldap.ssl.trust-all=true
```

**Production Mode:**
```properties
# Use proper certificate validation
ldap.ssl.trust-all=false
ldap.ssl.truststore-path=classpath:truststore.jks
ldap.ssl.truststore-password=changeit
ldap.ssl.truststore-type=JKS
ldap.ssl.hostname-verification=true
```

**Setup Truststore:**
```bash
# Use helper script to set up truststore
./setup-ldap-truststore.sh import-from-server ldap.example.com 636
```

**📋 For complete SSL configuration guide, see:**
- **[LDAP_SSL_CONFIGURATION.md](LDAP_SSL_CONFIGURATION.md)** - Comprehensive SSL setup guide

## Testing

Run the tests with:
```bash
mvn test
```

## Technology Stack

- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Web** - REST API endpoints
- **UnboundID LDAP SDK 6.0.11** - High-performance LDAP client operations
- **SpringDoc OpenAPI 2.3.0** - Swagger UI and OpenAPI documentation
- **Jackson** - JSON serialization/deserialization
- **Maven** - Build and dependency management
- **JUnit 5** - Testing framework

## Security Notes

- This application passes LDAP credentials through to the target LDAP server
- **Use HTTPS in production** to protect credentials in transit (see [HTTPS_IMPLEMENTATION.md](HTTPS_IMPLEMENTATION.md))
- For LDAP connections, use `ldaps://` URIs for SSL/TLS encrypted connections
- Consider implementing additional security measures like rate limiting
- The application does not store or cache credentials

## License

This project is provided as-is for educational and development purposes.
