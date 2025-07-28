

CREATE TABLE correlation_instances (
    id character(36) NOT NULL,
    encoded_correlation_id character varying(36) NOT NULL,
    correlated_id character varying(36) NOT NULL,
    correlation character varying(255) NOT NULL,
    version bigint,
    CONSTRAINT correlation_instances_pkey PRIMARY KEY (id),
    CONSTRAINT correlation_instances_encoded_correlation_id_key UNIQUE (encoded_correlation_id),
    INDEX idx_correlation_instances_correlated_id  (correlated_id)
);

CREATE TABLE process_instances (
    id character(36) NOT NULL,
    payload varbinary(MAX) NOT NULL,
    process_id character varying(255) NOT NULL,
    version bigint,
    process_version character varying(36),
    CONSTRAINT process_instances_pkey PRIMARY KEY (id),
    INDEX idx_process_instances_process_id  (process_id, id, process_version)
);

CREATE TABLE business_key_mapping (
    business_key character(255) NOT NULL,
    process_instance_id character(36) NOT NULL,
    CONSTRAINT business_key_mapping_pkey PRIMARY KEY (business_key),
    CONSTRAINT fk_process_instances FOREIGN KEY (process_instance_id) REFERENCES process_instances(id) ON DELETE CASCADE,
    INDEX idx_business_key_process_instance_id (process_instance_id)
);

CREATE INDEX idx_correlation_instances_encoded_id ON correlation_instances (encoded_correlation_id);