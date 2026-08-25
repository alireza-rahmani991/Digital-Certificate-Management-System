CREATE TABLE digital_certificates (
    id UUID PRIMARY KEY,
    serial_number VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    common_name VARCHAR(255) NOT NULL,
    organization VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_to TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    signature_algorithm VARCHAR(50) NOT NULL,
    public_key_algorithm VARCHAR(50) NOT NULL,
    public_key_size INT NOT NULL,
    fingerprint VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);