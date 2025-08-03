# LDAP REST API

## Endpoints Overview

The LDAP Web Tool provides both POST and GET endpoints for search operations:

- **POST endpoints**: Accept JSON request body with search parameters
- **GET endpoints**: Accept search parameters as query parameters (useful for browser testing and simple integrations)

## Data Models

### SearchRequest
```json
{
  "uri": "string (required) - The LDAP URI to send the request to (supports ldap:// and ldaps:// schemes)",
  "base": "string (optional) - The LDAP search base. Defaults to empty string",
  "filter": "string (optional) - The LDAP search filter. Defaults to '(objectClass=*)'",
  "scope": "string (optional) - Search scope: 'base'|'one'|'sub'. Defaults to 'sub'"
}
```

### SearchResponse (JSON)
```json
{
  "entries": [
    {
      "dn": "string - Distinguished Name of the entry",
      "attributes": {
        "attributeName": ["string or array - Attribute values"],
        "objectClass": ["array of strings - Object classes"],
        "cn": "string - Common name",
        "mail": "string - Email address"
      }
    }
  ],
  "count": "number - Total number of entries returned"
}
```

### SearchResponse (LDIF)
When `Accept: application/ldif` header is used, the response will be in LDIF (LDAP Data Interchange Format) as plain text:

```ldif
dn: cn=John Doe,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: organizationalPerson
objectClass: person
cn: John Doe
sn: Doe
givenName: John
mail: john.doe@example.com
uid: jdoe

dn: cn=Jane Smith,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
objectClass: organizationalPerson
objectClass: person
cn: Jane Smith
sn: Smith
givenName: Jane
mail: jane.smith@example.com
uid: jsmith
```

### ModifyRequest (JSON)
```json
{
  "uri": "string (required) - The LDAP URI to send the request to (supports ldap:// and ldaps:// schemes)",
  "dn": "string (required) - Distinguished Name of the entry to modify",
  "modifications": [
    {
      "operation": "string (required) - 'add'|'delete'|'replace'",
      "attribute": "string (required) - Attribute name to modify",
      "values": ["array of strings (optional) - New values for the attribute"]
    }
  ]
}
```

### ModifyRequest (LDIF)
When `Content-Type: application/ldif` is used, the request body should be in LDIF change format:

```ldif
dn: cn=John Doe,ou=users,dc=example,dc=com
changetype: modify
add: telephoneNumber
telephoneNumber: +1-555-123-4567
-
replace: mail
mail: john.newemail@example.com
-
delete: description

dn: cn=Jane Smith,ou=users,dc=example,dc=com
changetype: modify
add: title
title: Senior Developer
-
replace: department
department: Engineering
```

**LDIF Change Format Rules:**
- Each change record starts with `dn:` followed by the target DN
- `changetype: modify` indicates this is a modification operation
- Operations: `add:`, `delete:`, `replace:`
- Changes are separated by `-` on its own line
- Multiple change records are separated by blank lines

### ModifyResponse
```json
{
  "success": "boolean - Whether the modification was successful",
  "message": "string - Success or error message",
  "dn": "string - Distinguished Name that was modified"
}
```

### ErrorResponse
```json
{
  "error": "string - Error type",
  "message": "string - Human-readable error message",
  "code": "number - HTTP status code",
  "details": "string (optional) - Additional error details"
}
```

## API Endpoints

### Overview

The API provides separate endpoints for JSON and LDIF formats to ensure clear content type handling and better Swagger UI compatibility. This design eliminates content negotiation issues and provides predictable behavior for each format.

### Search Operations

## POST /api/search
- **Purpose**: Perform an LDAP search (JSON response)
- **Authentication**: Basic using LDAP DN and password
- **Content-Type**: application/json
- **Accept**: application/json
- **Request Body**: [SearchRequest](#searchrequest)
- **Response**: 
  - **200 OK**: [SearchResponse (JSON)](#searchresponse-json)
  - **400 Bad Request**: [ErrorResponse](#errorresponse)
  - **401 Unauthorized**: [ErrorResponse](#errorresponse)
  - **500 Internal Server Error**: [ErrorResponse](#errorresponse)

## POST /api/search/ldif
- **Purpose**: Perform an LDAP search (LDIF response)
- **Authentication**: Basic using LDAP DN and password
- **Content-Type**: application/json
- **Accept**: application/ldif
- **Request Body**: [SearchRequest](#searchrequest)
- **Response**: 
  - **200 OK**: [SearchResponse (LDIF)](#searchresponse-ldif) - Plain text in LDIF format
  - **400 Bad Request**: [ErrorResponse](#errorresponse)
  - **401 Unauthorized**: [ErrorResponse](#errorresponse)
  - **500 Internal Server Error**: [ErrorResponse](#errorresponse)

## GET /api/search
- **Purpose**: Perform an LDAP search (JSON response) using query parameters
- **Authentication**: Basic using LDAP DN and password
- **Accept**: application/json
- **Query Parameters**:
  - `uri` (required): LDAP URI (e.g., "ldap://localhost:389")
  - `base` (required): Search base DN (e.g., "ou=users,dc=example,dc=com")  
  - `filter` (required): LDAP filter (e.g., "(objectClass=person)")
  - `scope` (optional): Search scope ("base", "one", or "sub", defaults to "sub")
- **Response**: 
  - **200 OK**: [SearchResponse (JSON)](#searchresponse-json)
  - **400 Bad Request**: [ErrorResponse](#errorresponse)
  - **401 Unauthorized**: [ErrorResponse](#errorresponse)
  - **500 Internal Server Error**: [ErrorResponse](#errorresponse)

**Example**: `GET /api/search?uri=ldap://localhost:389&base=dc=example,dc=com&filter=(objectClass=*)&scope=sub`

## GET /api/search/ldif
- **Purpose**: Perform an LDAP search (LDIF response) using query parameters
- **Authentication**: Basic using LDAP DN and password
- **Accept**: application/ldif
- **Query Parameters**:
  - `uri` (required): LDAP URI (e.g., "ldap://localhost:389")
  - `base` (required): Search base DN (e.g., "ou=users,dc=example,dc=com")  
  - `filter` (required): LDAP filter (e.g., "(objectClass=person)")
  - `scope` (optional): Search scope ("base", "one", or "sub", defaults to "sub")
- **Response**: 
  - **200 OK**: [SearchResponse (LDIF)](#searchresponse-ldif) - Plain text in LDIF format
  - **400 Bad Request**: [ErrorResponse](#errorresponse)
  - **401 Unauthorized**: [ErrorResponse](#errorresponse)
  - **500 Internal Server Error**: [ErrorResponse](#errorresponse)

**Example**: `GET /api/search/ldif?uri=ldap://localhost:389&base=dc=example,dc=com&filter=(objectClass=*)&scope=sub`

### Modify Operations

## POST /api/modify
- **Purpose**: Perform an LDAP modify operation (JSON request)
- **Authentication**: Basic using LDAP DN and password
- **Content-Type**: application/json
- **Accept**: application/json
- **Request Body**: [ModifyRequest (JSON)](#modifyrequest-json)
- **Response**:
  - **200 OK**: [ModifyResponse](#modifyresponse)
  - **400 Bad Request**: [ErrorResponse](#errorresponse)
  - **401 Unauthorized**: [ErrorResponse](#errorresponse)
  - **500 Internal Server Error**: [ErrorResponse](#errorresponse)

## POST /api/modify/ldif
- **Purpose**: Perform an LDAP modify operation (LDIF request)
- **Authentication**: Basic using LDAP DN and password
- **Content-Type**: application/ldif
- **Accept**: application/json
- **Query Parameters**: 
  - `uri` (required): LDAP server URI (e.g., `ldap://localhost:389`)
- **Request Body**: [ModifyRequest (LDIF)](#modifyrequest-ldif) - Plain text in LDIF change format
- **Response**:
  - **200 OK**: [ModifyResponse](#modifyresponse)
  - **400 Bad Request**: [ErrorResponse](#errorresponse)
  - **401 Unauthorized**: [ErrorResponse](#errorresponse)
  - **500 Internal Server Error**: [ErrorResponse](#errorresponse)

## Usage Examples

### Search Examples

**JSON Search Request:**
```bash
curl -X 'POST' \
  'http://localhost:8090/api/search' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Basic Y246YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb206cGFzc3dvcmQ=' \
  -d '{
    "uri": "ldap://localhost:389",
    "base": "ou=users,dc=example,dc=com",
    "filter": "(objectClass=person)",
    "scope": "sub"
  }'
```

**LDIF Search Request:**
```bash
curl -X 'POST' \
  'http://localhost:8090/api/search/ldif' \
  -H 'accept: application/ldif' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Basic Y246YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb206cGFzc3dvcmQ=' \
  -d '{
    "uri": "ldap://localhost:389",
    "base": "ou=users,dc=example,dc=com",
    "filter": "(objectClass=person)",
    "scope": "sub"
  }'
```

### Modify Examples

**JSON Modify Request:**
```bash
curl -X 'POST' \
  'http://localhost:8090/api/modify' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Basic Y246YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb206cGFzc3dvcmQ=' \
  -d '{
    "uri": "ldap://localhost:389",
    "dn": "cn=John Doe,ou=users,dc=example,dc=com",
    "modifications": [
      {
        "operation": "replace",
        "attribute": "mail",
        "values": ["john.newemail@example.com"]
      }
    ]
  }'
```

**LDIF Modify Request:**
```bash
curl -X 'POST' \
  'http://localhost:8090/api/modify/ldif?uri=ldap://localhost:389' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/ldif' \
  -H 'Authorization: Basic Y246YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb206cGFzc3dvcmQ=' \
  -d 'dn: cn=John Doe,ou=users,dc=example,dc=com
changetype: modify
replace: mail
mail: john.newemail@example.com
-'
```

**SSL Search Request (using ldaps://):**
```bash
curl -X 'POST' \
  'http://localhost:8090/api/search' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Basic Y246YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb206cGFzc3dvcmQ=' \
  -d '{
    "uri": "ldaps://secure-ldap.example.com:636",
    "base": "ou=users,dc=example,dc=com",
    "filter": "(objectClass=person)",
    "scope": "sub"
  }'
```

## Authentication

All endpoints require HTTP Basic Authentication where:
- **Username**: LDAP Distinguished Name (DN) of the user
- **Password**: LDAP password for the user

Example: `cn=admin,dc=example,dc=com` with password `admin123`

## SSL Support

The API supports both plain LDAP and LDAP over SSL/TLS connections:

- **ldap://**: Plain LDAP connection (default port 389)
- **ldaps://**: LDAP over SSL/TLS connection (default port 636)

When using `ldaps://` in the URI, the service automatically establishes an SSL connection. For development purposes, the service uses a trust-all certificate manager, but in production environments, proper certificate validation should be configured.

**Examples:**
- Plain LDAP: `ldap://localhost:389`
- LDAP over SSL: `ldaps://secure-ldap.example.com:636`

## Content Type Summary

| Endpoint | Request Content-Type | Response Content-Type | Description |
|----------|---------------------|----------------------|-------------|
| `POST /api/search` | application/json | application/json | JSON search request and response |
| `GET /api/search` | Query parameters | application/json | Query parameter search request, JSON response |
| `POST /api/search/ldif` | application/json | application/ldif | JSON search request, LDIF response |
| `GET /api/search/ldif` | Query parameters | application/ldif | Query parameter search request, LDIF response |
| `POST /api/modify` | application/json | application/json | JSON modify request and response |
| `POST /api/modify/ldif` | application/ldif | application/json | LDIF modify request, JSON response |