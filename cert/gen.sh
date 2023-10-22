#!/bin/bash

./clean-keys.sh

########## CA ##########
# Generating a pair of keys with OpenSSL
openssl genrsa -out ca-prv.key                   # Private key
openssl rsa -in ca-prv.key -pubout > ca-pub.key  # Public key

# Generating a self-signed certificate with these keys
openssl req -new -key ca-prv.key -out ca.csr -subj "/C=PT/ST=Lisbon/L=Lisbon/O=IST/OU=Education/CN=localhost/emailAddress= "
openssl x509 -req -days 365 -in ca.csr -signkey ca-prv.key -out ca-cert.pem


########## DAOliberate ##########
# Generating a private key and certificate signing request (CSR) with OpenSSL
openssl req -newkey rsa:4096 -nodes -keyout daoliberate-key.pem -out daoliberate-req.pem -subj "/C=PT/ST=Lisbon/L=Lisbon/O=IST/OU=Education/CN=localhost/emailAddress= "

# CA sign request
openssl x509 -req -in daoliberate-req.pem -days 365 -CA ca-cert.pem -CAkey ca-prv.key -CAcreateserial -out daoliberate-cert.pem


########## Client ##########
# Generating a private key and certificate signing request (CSR) with OpenSSL
openssl req -newkey rsa:4096 -nodes -keyout client-key.pem -out client-req.pem -subj "/C=PT/ST=Lisbon/L=Lisbon/O=IST/OU=Education/CN=localhost/emailAddress= "

# CA sign request
openssl x509 -req -in client-req.pem -days 365 -CA ca-cert.pem -CAkey ca-prv.key -CAcreateserial -out client-cert.pem


########## Register ##########
# Generating a private key and certificate signing request (CSR) with OpenSSL
openssl req -newkey rsa:4096 -nodes -keyout register-key.pem -out register-req.pem -subj "/C=PT/ST=Lisbon/L=Lisbon/O=IST/OU=Education/CN=localhost/emailAddress= "

# CA sign request
openssl x509 -req -in register-req.pem -days 365 -CA ca-cert.pem -CAkey ca-prv.key -CAcreateserial -out register-cert.pem