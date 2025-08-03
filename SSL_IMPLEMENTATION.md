# SSL Implementation for LDAP Web Tool

## Overview

This document describes the SSL/TLS support implementation that enables the LDAP Web Tool to connect to LDAP servers using secure connections when `ldaps://` URIs are provided.

## Changes Made

### 1. Updated LdapService.java

**File**: `src/main/java/com/example/ldapwebtool/service/LdapService.java`

**Changes**:
- Added SSL-related imports:
  - `com.unboundid.util.ssl.SSLUtil`
  - `com.unboundid.util.ssl.TrustAllTrustManager`
  - `javax.net.ssl.SSLSocketFactory`

- Added `createConnection(String uri)` helper method that:
  - Parses the URI scheme to detect `ldaps://` vs `ldap://`
  - For `ldaps://` URIs:
    - Uses default port 636 if no port is specified
    - Creates an SSL socket factory with a trust-all trust manager
    - Returns an `LDAPConnection` with SSL support
  - For `ldap://` URIs:
    - Uses default port 389 if no port is specified
    - Returns a plain `LDAPConnection`

- Updated all LDAP connection creation points to use the new helper method:
  - `search()` method
  - `searchToLdif()` method
  - `modify()` method
  - `modifyFromLdif()` method

### 2. Updated API Documentation

**File**: `docs/API.md`

**Changes**:
- Updated URI field descriptions to mention support for both `ldap://` and `ldaps://` schemes
- Added new "SSL Support" section explaining:
  - Supported URI schemes (`ldap://` and `ldaps://`)
  - Default ports (389 for plain LDAP, 636 for LDAPS)
  - Trust-all certificate manager usage for development
  - Example URIs for both plain and SSL connections
- Added SSL usage example with `ldaps://` URI

## Usage Examples

### Plain LDAP Connection
```json
{
  "uri": "ldap://localhost:389",
  "base": "ou=users,dc=example,dc=com",
  "filter": "(objectClass=person)"
}
```

### SSL LDAP Connection
```json
{
  "uri": "ldaps://secure-ldap.example.com:636",
  "base": "ou=users,dc=example,dc=com", 
  "filter": "(objectClass=person)"
}
```

## Security Considerations

### Development vs Production

The current implementation uses `TrustAllTrustManager` which accepts all SSL certificates without validation. This is suitable for development and testing environments but **should not be used in production**.

### Production Recommendations

For production environments, consider:

1. **Proper Certificate Validation**: Replace `TrustAllTrustManager` with proper certificate validation
2. **Certificate Store Configuration**: Configure a proper trust store with valid CA certificates
3. **SSL Context Configuration**: Use a properly configured SSL context with appropriate cipher suites
4. **Client Certificate Authentication**: If required by your LDAP server

### Example Production SSL Configuration

```java
// Production example (not implemented in current version)
SSLUtil sslUtil = new SSLUtil(new JVMDefaultTrustManager());
SSLContext sslContext = sslUtil.createSSLContext("TLS");
SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
```

## Testing

The implementation has been tested with:
- ✅ Maven compilation (`mvn clean compile`)
- ✅ Unit tests (`mvn test`)
- ✅ Spring Boot application context loading

## Backward Compatibility

The changes are fully backward compatible:
- Existing `ldap://` URIs continue to work exactly as before
- All existing API endpoints and request/response formats remain unchanged
- No breaking changes to the public API

## Future Enhancements

Potential improvements for future versions:

1. **Configurable SSL Settings**: Allow SSL configuration through application properties
2. **Custom Trust Store**: Support for custom trust store configuration
3. **StartTLS Support**: Add support for StartTLS extension for upgrading plain connections to SSL
4. **SSL Certificate Validation**: Replace trust-all approach with proper certificate validation
5. **SSL Debugging**: Add SSL handshake debugging capabilities for troubleshooting
