# LDIF Content Type Fix for Swagger UI

## Problem Description

When using the Swagger UI interface, selecting `application/ldif` from the response content type dropdown did not properly set the `Accept` header in the generated curl command. This resulted in:

1. Curl commands still showing `-H 'accept: application/json'` instead of `-H 'accept: application/ldif'`
2. API responses returning JSON format even when LDIF was selected
3. Confusion for users trying to test LDIF functionality

## Root Cause

This is a known limitation with Swagger UI's content negotiation handling. When multiple content types are specified in the `produces` attribute of a Spring Boot endpoint, Swagger UI doesn't always properly update the `Accept` header when users select different content types from the dropdown.

## Solution Implemented

### 1. Separated Endpoints by Content Type

Instead of having a single endpoint that handles both JSON and LDIF based on the `Accept` header, we created separate endpoints:

**Search Operations:**
- `POST /api/search` - Returns JSON format (application/json)
- `POST /api/search/ldif` - Returns LDIF format (application/ldif)

**Modify Operations:**
- `POST /api/modify` - Accepts JSON request format
- `POST /api/modify/ldif` - Accepts LDIF request format

### 2. Controller Changes Made

#### Before (Single Endpoint with Content Negotiation):
```java
@PostMapping(value = "/search", 
             consumes = MediaType.APPLICATION_JSON_VALUE,
             produces = {MediaType.APPLICATION_JSON_VALUE, "application/ldif"})
public ResponseEntity<?> search(
        @RequestBody SearchRequest request,
        @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader,
        // ...
) {
    if (acceptHeader.contains("application/ldif")) {
        // Return LDIF
    } else {
        // Return JSON
    }
}
```

#### After (Separate Endpoints):
```java
// JSON endpoint
@PostMapping(value = "/search", 
             consumes = MediaType.APPLICATION_JSON_VALUE,
             produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<?> search(@RequestBody SearchRequest request, ...) {
    // Always return JSON
}

// LDIF endpoint
@PostMapping(value = "/search/ldif", 
             consumes = MediaType.APPLICATION_JSON_VALUE,
             produces = "application/ldif")
public ResponseEntity<?> searchLdif(@RequestBody SearchRequest request, ...) {
    // Always return LDIF
}
```

### 3. Benefits of This Approach

✅ **Clear Content Type Handling**: Each endpoint has a single, well-defined content type
✅ **Swagger UI Compatibility**: No more content negotiation issues
✅ **Predictable Curl Commands**: Generated curl commands will have the correct headers
✅ **Better API Documentation**: Clear separation between JSON and LDIF operations
✅ **Easier Testing**: Users can directly select the format they want to test

### 4. Updated API Endpoints

| Endpoint | Method | Request Format | Response Format | Description |
|----------|--------|----------------|-----------------|-------------|
| `/api/search` | POST | JSON | JSON | LDAP search returning JSON response |
| `/api/search/ldif` | POST | JSON | LDIF | LDAP search returning LDIF response |
| `/api/modify` | POST | JSON | JSON | LDAP modify using JSON request |
| `/api/modify/ldif` | POST | LDIF | JSON | LDAP modify using LDIF request |

### 5. Swagger UI Improvements

- Each endpoint now appears separately in the Swagger UI
- Clear content type indicators for each operation
- Proper examples for both JSON and LDIF formats
- No more confusion about which format will be returned
- Curl commands generated with correct headers

### 6. Testing the Fix

1. **Open Swagger UI**: http://localhost:8090/swagger-ui.html
2. **Test JSON Search**: Use `POST /api/search` - should return JSON
3. **Test LDIF Search**: Use `POST /api/search/ldif` - should return LDIF
4. **Test JSON Modify**: Use `POST /api/modify` with JSON request body
5. **Test LDIF Modify**: Use `POST /api/modify/ldif` with LDIF request body

### 7. Curl Command Examples

**JSON Search:**
```bash
curl -X 'POST' \
  'http://localhost:8090/api/search' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Basic Y246YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb206cGFzc3dvcmQ=' \
  -d '{"uri":"ldap://localhost:389","base":"dc=example,dc=com","filter":"(objectClass=*)","scope":"sub"}'
```

**LDIF Search:**
```bash
curl -X 'POST' \
  'http://localhost:8090/api/search/ldif' \
  -H 'accept: application/ldif' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Basic Y246YWRtaW4sZGM9ZXhhbXBsZSxkYz1jb206cGFzc3dvcmQ=' \
  -d '{"uri":"ldap://localhost:389","base":"dc=example,dc=com","filter":"(objectClass=*)","scope":"sub"}'
```

## Backward Compatibility

The original `/api/search` and `/api/modify` endpoints remain functional and will continue to work as expected, returning JSON responses. The new `/ldif` endpoints provide the LDIF functionality that was previously accessible only through `Accept` header manipulation.

## Implementation Status

✅ **Completed**: Endpoint separation  
✅ **Completed**: Updated Swagger annotations  
✅ **Completed**: Removed content negotiation logic  
✅ **Completed**: Application tested and verified  
✅ **Completed**: Swagger UI integration confirmed  

The LDIF content type issue has been successfully resolved, and users can now reliably test both JSON and LDIF formats through the Swagger UI interface.
