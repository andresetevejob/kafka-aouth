#!/bin/sh

set -e

PASSWORD=changeit

echo "#### Create server certificate for Keycloak"
keytool -keystore keycloak.server.keystore.p12 -storetype pkcs12 -keyalg RSA -alias keycloak -validity 366 -genkey -storepass $PASSWORD -keypass $PASSWORD -dname CN=keycloak -ext SAN=DNS:keycloak

echo "#### Create CA"
openssl req -new -x509 -keyout ca-key -out ca-cert -days 3650 -subj "/CN=strimzi.io" -passout pass:$PASSWORD

echo "#### Create client truststore"
keytool -keystore keycloak.client.truststore.p12 -storetype pkcs12 -alias KeycloakCARoot -storepass $PASSWORD -keypass $PASSWORD -import -file ca-cert -noprompt


echo "#### Sign server certificate (export, sign, add signed to keystore)"
keytool -keystore keycloak.server.keystore.p12 -storetype pkcs12 -alias keycloak -storepass $PASSWORD -keypass $PASSWORD -certreq -file cert-file
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 366 -CAcreateserial -passin pass:$PASSWORD
keytool -keystore keycloak.server.keystore.p12 -alias CARoot -storepass $PASSWORD -keypass $PASSWORD -import -file ca-cert -noprompt
keytool -keystore keycloak.server.keystore.p12 -alias keycloak -storepass $PASSWORD -keypass $PASSWORD -import -file cert-signed -noprompt