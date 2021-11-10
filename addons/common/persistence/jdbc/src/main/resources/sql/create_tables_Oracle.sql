CREATE TABLE process_instances(
    id char(36) NOT NULL,
    payload blob NOT NULL,
    process_id varchar2(4000) NOT NULL,
    version number(19),
    CONSTRAINT process_instances_pkey PRIMARY KEY (id));
CREATE INDEX idx_process_instances_proc_id ON process_instances (process_id);