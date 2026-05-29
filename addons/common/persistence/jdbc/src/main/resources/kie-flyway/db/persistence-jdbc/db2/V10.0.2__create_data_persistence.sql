-- IBM Confidential
-- PID 5900-AR4
-- Copyright IBM Corp. 2026

CREATE TABLE business_key_mapping (
    business_key CHAR(255) NOT NULL,
    process_instance_id CHAR(36) NOT NULL,
    CONSTRAINT business_key_mapping_pkey PRIMARY KEY (business_key)
);

CREATE TABLE correlation_instances (
    id CHAR(36) NOT NULL,
    encoded_correlation_id VARCHAR(36) NOT NULL,
    correlated_id VARCHAR(36) NOT NULL,
    correlation VARCHAR(255) NOT NULL,
    version BIGINT,
    CONSTRAINT correlation_instances_pkey PRIMARY KEY (id),
    CONSTRAINT correlation_instances_encoded_correlation_id_key UNIQUE (encoded_correlation_id)
);

CREATE TABLE event_types (
    process_instance_id CHAR(36) NOT NULL,
    event_type VARCHAR(256) NOT NULL,
    CONSTRAINT event_types_pk PRIMARY KEY (process_instance_id, event_type)
);

CREATE TABLE process_instances (
    id CHAR(36) NOT NULL,
    payload BLOB(2M) NOT NULL,
    process_id VARCHAR(255) NOT NULL,
    version BIGINT,
    process_version VARCHAR(255) NOT NULL,
    CONSTRAINT process_instances_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_business_key_process_instance_id ON business_key_mapping (process_instance_id);

CREATE INDEX idx_correlation_instances_correlated_id ON correlation_instances (correlated_id);

CREATE INDEX idx_correlation_instances_encoded_id ON correlation_instances (encoded_correlation_id);

CREATE INDEX idx_process_instances_process_id ON process_instances (process_id, id, process_version);

ALTER TABLE business_key_mapping
    ADD CONSTRAINT fk_process_instances FOREIGN KEY (process_instance_id) REFERENCES process_instances(id) ON DELETE CASCADE;
