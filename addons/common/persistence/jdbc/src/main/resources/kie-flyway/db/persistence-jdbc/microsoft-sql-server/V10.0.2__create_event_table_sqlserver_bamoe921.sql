CREATE TABLE event_types
(
    process_instance_id character(36) NOT NULL,
    event_type character varying(256) NOT NULL,

    CONSTRAINT event_types_pk PRIMARY KEY (process_instance_id, event_type)
);