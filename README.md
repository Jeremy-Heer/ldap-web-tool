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

## Recent Updates

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

### Build the application
```bash
mvn clean compile
```

### Run the application
```bash
mvn spring-boot:run
```

The application will start on port 8090 by default.

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
- Server port
- Logging levels
- JSON serialization settings

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
- Use HTTPS in production to protect credentials in transit
- Consider implementing additional security measures like rate limiting
- The application does not store or cache credentials

## License

This project is provided as-is for educational and development purposes.
