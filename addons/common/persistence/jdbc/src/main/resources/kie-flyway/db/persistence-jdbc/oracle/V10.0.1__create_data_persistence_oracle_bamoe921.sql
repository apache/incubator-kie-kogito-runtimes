CREATE TABLE correlation_instances (
    id VARCHAR2(36) NOT NULL,
    encoded_correlation_id VARCHAR2(36) NOT NULL,
    correlated_id VARCHAR2(36) NOT NULL,
    correlation VARCHAR2(255) NOT NULL,
    version NUMBER(19),
    CONSTRAINT correlation_instances_encoded_correlation_id_key UNIQUE (encoded_correlation_id),
    CONSTRAINT correlation_instances_pkey PRIMARY KEY (id)
);

CREATE TABLE process_instances (
    id VARCHAR2(36) NOT NULL,
    payload BLOB NOT NULL,
    process_id VARCHAR2(255) NOT NULL,
    version NUMBER(19),
    process_version VARCHAR2(255),
    CONSTRAINT process_instances_pkey PRIMARY KEY (id)
);

CREATE TABLE business_key_mapping (
    business_key VARCHAR2(255) NOT NULL,
    process_instance_id VARCHAR2(36) NOT NULL,
    CONSTRAINT business_key_mapping_pkey PRIMARY KEY (business_key),
    CONSTRAINT fk_process_instances FOREIGN KEY (process_instance_id) REFERENCES process_instances(id) ON DELETE CASCADE
);

CREATE INDEX idx_business_key_process_instance_id ON business_key_mapping (process_instance_id);

CREATE INDEX idx_correlation_instances_correlated_id ON correlation_instances (correlated_id);

CREATE INDEX idx_process_instances_process_id ON process_instances (process_id, id, process_version);
