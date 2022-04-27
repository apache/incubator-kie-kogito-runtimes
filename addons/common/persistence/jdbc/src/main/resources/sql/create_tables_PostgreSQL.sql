-- To be used with kogito-addons-quarkus-persistence-jdbc for Quarkus or kogito-addons-springboot-persistence-jdbc for SpringBoot
CREATE TABLE process_instances
(
    id         character(36)     NOT NULL,
    payload    bytea             NOT NULL,
    process_id character varying NOT NULL,
    version    bigint,
    CONSTRAINT process_instances_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_process_instances_process_id ON process_instances (process_id, id);