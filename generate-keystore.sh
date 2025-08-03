#!/bin/bash

# Generate a self-signed certificate for development/testing
# This script creates a keystore with a self-signed certificate

KEYSTORE_FILE="src/main/resources/keystore.p12"
KEYSTORE_PASSWORD="changeit"
KEY_ALIAS="ldapwebtool"

echo "Generating self-signed certificate for LDAP Web Tool..."

# Create keystore directory if it doesn't exist
mkdir -p src/main/resources

# Generate keystore with self-signed certificate
keytool -genkeypair \
  -alias $KEY_ALIAS \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore $KEYSTORE_FILE \
  -storepass $KEYSTORE_PASSWORD \
  -validity 365 \
  -dname "CN=localhost,OU=Development,O=LDAP Web Tool,L=Local,ST=Local,C=US" \
  -ext SAN=dns:localhost,ip:127.0.0.1

echo "Keystore generated: $KEYSTORE_FILE"
echo "Keystore password: $KEYSTORE_PASSWORD"
echo "Key alias: $KEY_ALIAS"
echo ""
echo "Add the following to your application.properties:"
echo "server.ssl.key-store=classpath:keystore.p12"
echo "server.ssl.key-store-password=$KEYSTORE_PASSWORD"
echo "server.ssl.key-store-type=PKCS12"
echo "server.ssl.key-alias=$KEY_ALIAS"
echo "server.port=8443"
