# LDAP Web Tool - Quick Start Guide

## Quick Commands

### Build JAR
```bash
mvn clean package
```

### Run JAR
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar
```

### Run on Different Port
```bash
java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar --server.port=8080
```

### Run in Background
```bash
nohup java -jar target/ldap-web-tool-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

## Access Points
- **Application**: http://localhost:8090
- **Swagger UI**: http://localhost:8090/swagger-ui.html
- **API Docs**: http://localhost:8090/v3/api-docs

## Test Commands

### Health Check
```bash
curl http://localhost:8090/actuator/health
```

### Search (JSON)
```bash
curl -X POST http://localhost:8090/api/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'your-ldap-dn:password' | base64)" \
  -d '{"base": "dc=example,dc=com", "filter": "(objectClass=*)", "scope": "SUB"}'
```

### Search (LDIF)
```bash
curl -X POST http://localhost:8090/api/search/ldif \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n 'your-ldap-dn:password' | base64)" \
  -d '{"base": "dc=example,dc=com", "filter": "(objectClass=*)", "scope": "SUB"}'
```

## Common Issues

| Issue | Solution |
|-------|----------|
| Port in use | Add `--server.port=8091` |
| Java version error | Use Java 17+ |
| Out of memory | Add `-Xmx1g` before `-jar` |
| Build fails | Run `mvn clean package -DskipTests` |

For detailed instructions, see `STANDALONE_JAR_INSTRUCTIONS.md`
