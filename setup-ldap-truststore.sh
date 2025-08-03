#!/bin/bash

# LDAP Truststore Setup Script
# This script helps create and manage truststores for LDAP SSL connections

set -e

# Default values
TRUSTSTORE_FILE="src/main/resources/truststore.jks"
TRUSTSTORE_PASSWORD="changeit"
TRUSTSTORE_TYPE="JKS"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_usage() {
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands:"
    echo "  download-cert HOST PORT    Download certificate from LDAP server"
    echo "  create-truststore         Create new truststore"
    echo "  import-cert FILE          Import certificate file to truststore"
    echo "  import-from-server HOST PORT  Download and import certificate from server"
    echo "  list-certs                List certificates in truststore"
    echo "  copy-system-truststore    Copy system truststore as base"
    echo ""
    echo "Options:"
    echo "  -f FILE     Truststore file (default: $TRUSTSTORE_FILE)"
    echo "  -p PASS     Truststore password (default: $TRUSTSTORE_PASSWORD)"
    echo "  -t TYPE     Truststore type (default: $TRUSTSTORE_TYPE)"
    echo ""
    echo "Examples:"
    echo "  $0 download-cert ldap.example.com 636"
    echo "  $0 import-from-server ldap.example.com 636"
    echo "  $0 create-truststore -f custom-truststore.jks -p mypassword"
}

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

download_certificate() {
    local host=$1
    local port=$2
    local cert_file="${host}-${port}.crt"
    
    print_info "Downloading certificate from $host:$port"
    
    echo | openssl s_client -servername "$host" -connect "$host:$port" -showcerts 2>/dev/null | \
        openssl x509 -outform PEM > "$cert_file"
    
    if [ -s "$cert_file" ]; then
        print_info "Certificate saved to $cert_file"
        
        # Show certificate details
        echo ""
        echo "Certificate details:"
        openssl x509 -in "$cert_file" -text -noout | grep -E "(Subject:|Issuer:|Not Before|Not After)"
        echo ""
        
        return 0
    else
        print_error "Failed to download certificate"
        rm -f "$cert_file"
        return 1
    fi
}

create_truststore() {
    print_info "Creating new truststore: $TRUSTSTORE_FILE"
    
    # Create directory if it doesn't exist
    mkdir -p "$(dirname "$TRUSTSTORE_FILE")"
    
    # Remove existing truststore
    if [ -f "$TRUSTSTORE_FILE" ]; then
        print_warning "Truststore already exists, removing old one"
        rm "$TRUSTSTORE_FILE"
    fi
    
    # Create empty truststore
    keytool -genkeypair -alias dummy -keyalg RSA -keystore "$TRUSTSTORE_FILE" \
        -storepass "$TRUSTSTORE_PASSWORD" -keypass "$TRUSTSTORE_PASSWORD" \
        -dname "CN=dummy" -validity 1 2>/dev/null
    
    keytool -delete -alias dummy -keystore "$TRUSTSTORE_FILE" \
        -storepass "$TRUSTSTORE_PASSWORD" 2>/dev/null
    
    print_info "Empty truststore created"
}

import_certificate() {
    local cert_file=$1
    
    if [ ! -f "$cert_file" ]; then
        print_error "Certificate file not found: $cert_file"
        return 1
    fi
    
    # Create truststore if it doesn't exist
    if [ ! -f "$TRUSTSTORE_FILE" ]; then
        create_truststore
    fi
    
    # Generate alias from filename
    local alias=$(basename "$cert_file" .crt)
    
    print_info "Importing certificate $cert_file with alias $alias"
    
    keytool -importcert -alias "$alias" -file "$cert_file" \
        -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD" \
        -noprompt
    
    print_info "Certificate imported successfully"
}

import_from_server() {
    local host=$1
    local port=$2
    
    print_info "Downloading and importing certificate from $host:$port"
    
    if download_certificate "$host" "$port"; then
        local cert_file="${host}-${port}.crt"
        import_certificate "$cert_file"
        
        # Clean up downloaded file
        rm "$cert_file"
        print_info "Temporary certificate file removed"
    else
        return 1
    fi
}

list_certificates() {
    if [ ! -f "$TRUSTSTORE_FILE" ]; then
        print_error "Truststore not found: $TRUSTSTORE_FILE"
        return 1
    fi
    
    print_info "Certificates in truststore:"
    keytool -list -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD"
}

copy_system_truststore() {
    print_info "Copying system truststore as base"
    
    # Find system truststore
    local system_truststore=""
    if [ -f "$JAVA_HOME/lib/security/cacerts" ]; then
        system_truststore="$JAVA_HOME/lib/security/cacerts"
    elif [ -f "/etc/ssl/certs/java/cacerts" ]; then
        system_truststore="/etc/ssl/certs/java/cacerts"
    elif [ -f "/usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts" ]; then
        system_truststore="/usr/lib/jvm/java-11-openjdk-amd64/lib/security/cacerts"
    else
        print_error "System truststore not found"
        return 1
    fi
    
    print_info "Found system truststore: $system_truststore"
    
    # Create directory if needed
    mkdir -p "$(dirname "$TRUSTSTORE_FILE")"
    
    # Copy system truststore
    cp "$system_truststore" "$TRUSTSTORE_FILE"
    
    print_info "System truststore copied to $TRUSTSTORE_FILE"
    print_warning "You may need to change the password from 'changeit' to your desired password"
}

# Parse command line options
while getopts "f:p:t:h" opt; do
    case $opt in
        f) TRUSTSTORE_FILE="$OPTARG" ;;
        p) TRUSTSTORE_PASSWORD="$OPTARG" ;;
        t) TRUSTSTORE_TYPE="$OPTARG" ;;
        h) print_usage; exit 0 ;;
        \?) print_error "Invalid option: -$OPTARG"; print_usage; exit 1 ;;
    esac
done

shift $((OPTIND-1))

# Get command
COMMAND=$1
shift || true

# Execute command
case $COMMAND in
    download-cert)
        if [ $# -ne 2 ]; then
            print_error "download-cert requires HOST and PORT"
            print_usage
            exit 1
        fi
        download_certificate "$1" "$2"
        ;;
    create-truststore)
        create_truststore
        ;;
    import-cert)
        if [ $# -ne 1 ]; then
            print_error "import-cert requires certificate FILE"
            print_usage
            exit 1
        fi
        import_certificate "$1"
        ;;
    import-from-server)
        if [ $# -ne 2 ]; then
            print_error "import-from-server requires HOST and PORT"
            print_usage
            exit 1
        fi
        import_from_server "$1" "$2"
        ;;
    list-certs)
        list_certificates
        ;;
    copy-system-truststore)
        copy_system_truststore
        ;;
    *)
        print_error "Unknown command: $COMMAND"
        print_usage
        exit 1
        ;;
esac

# Print configuration hint
echo ""
print_info "Truststore configuration for application.properties:"
echo "ldap.ssl.trust-all=false"
echo "ldap.ssl.truststore-path=classpath:$(basename "$TRUSTSTORE_FILE")"
echo "ldap.ssl.truststore-password=$TRUSTSTORE_PASSWORD"
echo "ldap.ssl.truststore-type=$TRUSTSTORE_TYPE"
