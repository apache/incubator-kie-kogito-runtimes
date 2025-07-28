CREATE TABLE jbpm_user_tasks (
    id varchar(50) NOT NULL,
    user_task_id varchar(255),
    task_priority varchar(50),
    actual_owner varchar(255),
    task_description varchar(255),
    status varchar(255),
    termination_type varchar(255),
    external_reference_id varchar(255),
    task_name varchar(255),
    CONSTRAINT jbpm_user_tasks_pkey PRIMARY KEY (id)
);

CREATE TABLE jbpm_user_tasks_potential_users (
    task_id varchar(50) NOT NULL,
    user_id varchar(255) NOT NULL,
    CONSTRAINT jbpm_user_tasks_potential_users_pkey PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_jbpm_user_fk_tasks_potential_users_tid FOREIGN KEY (task_id)  REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_potential_groups (
    task_id varchar(50) NOT NULL,
    group_id varchar(255) NOT NULL,
    CONSTRAINT jbpm_user_tasks_potential_groups_pkey PRIMARY KEY (task_id, group_id),
    CONSTRAINT fk_jbpm_user_tasks_potential_groups_tid FOREIGN KEY (task_id)  REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_admin_users (
    task_id varchar(50) NOT NULL,
    user_id varchar(255) NOT NULL,
    CONSTRAINT jbpm_user_tasks_admin_users_pkey PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_jbpm_user_tasks_admin_users_tid FOREIGN KEY (task_id)  REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_admin_groups (
    task_id varchar(50) NOT NULL,
    group_id varchar(255) NOT NULL,
    CONSTRAINT jbpm_user_tasks_admin_groups_pkey PRIMARY KEY (task_id, group_id),
    CONSTRAINT fk_jbpm_user_tasks_admin_groups_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_excluded_users (
    task_id varchar(50) NOT NULL,
    user_id varchar(255) NOT NULL,
    CONSTRAINT jbpm_user_tasks_excluded_users_pkey PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_jbpm_user_tasks_excluded_users_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);


CREATE TABLE jbpm_user_tasks_attachments (
    id varchar(50) NOT NULL,
    name varchar(255),
    updated_by varchar(255),
    updated_at datetime2(6),
    url varchar(255),
    task_id varchar(50) NOT NULL,
    CONSTRAINT jbpm_user_tasks_attachments_pkey PRIMARY KEY (id),
    CONSTRAINT fk_user_tasks_attachments_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_comments (
    id varchar(50) NOT NULL,
    updated_by varchar(255),
    updated_at datetime2(6),
    comment varchar(1000),
    task_id varchar(50) NOT NULL,
    CONSTRAINT jbpm_user_tasks_comments_pkey PRIMARY KEY (id),
    CONSTRAINT fk_user_tasks_comments_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_inputs (
    task_id varchar(50) NOT NULL,
    input_name varchar(255) NOT NULL,
    input_value varbinary(MAX),
    java_type varchar(255),
    CONSTRAINT jbpm_user_tasks_inputs_pkey PRIMARY KEY (task_id, input_name),
    CONSTRAINT fk_jbpm_user_tasks_inputs_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_outputs (
    task_id varchar(50) NOT NULL,
    output_name varchar(255) NOT NULL,
    output_value varbinary(MAX),
    java_type varchar(255),
    CONSTRAINT jbpm_user_tasks_outputs_pkey PRIMARY KEY (task_id, output_name),
    CONSTRAINT fk_jbpm_user_tasks_outputs_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_metadata (
    task_id varchar(50) NOT NULL,
    metadata_name varchar(255) NOT NULL,
    metadata_value varchar(512),
    java_type varchar(255),
    CONSTRAINT jbpm_user_tasks_metadata_pkey PRIMARY KEY (task_id, metadata_name),
    CONSTRAINT fk_jbpm_user_tasks_metadata_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_deadline (
    id INT NOT NULL,
    task_id VARCHAR(50) NOT NULL,
    notification_type VARCHAR(255) NOT NULL,
    notification_value VARBINARY(MAX),
    java_type VARCHAR(255),
    CONSTRAINT jbpm_user_tasks_deadline_pkey PRIMARY KEY (id),
    CONSTRAINT fk_jbpm_user_tasks_deadline_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_reassignment (
    id int NOT NULL,
    task_id varchar(50) NOT NULL,
    reassignment_type varchar(255) NOT NULL,
    reassignment_value varbinary(MAX),
    java_type varchar(255),
    CONSTRAINT jbpm_user_tasks_reassignment_pkey PRIMARY KEY (id),
    CONSTRAINT fk_jbpm_user_tasks_reassignment_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_deadline_timer (
    task_id varchar(50) NOT NULL,
    notification_job_id varchar(255) NOT NULL,
    notification_type varchar(255) NOT NULL,
    notification_value varbinary(MAX),
    java_type varchar(255),
    CONSTRAINT jbpm_user_tasks_deadline_timer_pkey PRIMARY KEY (task_id, notification_job_id),
    CONSTRAINT fk_jbpm_user_tasks_deadline_timer_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE
);

CREATE TABLE jbpm_user_tasks_reassignment_timer (
    task_id varchar(50) NOT NULL,
    reassignment_job_id varchar(255) NOT NULL,
    reassignment_type varchar(255) NOT NULL,
    reassignment_value varbinary(MAX),
    java_type varchar(255),
    CONSTRAINT jbpm_user_tasks_reassignment_timer_pkey PRIMARY KEY (task_id, reassignment_job_id),
    CONSTRAINT fk_jbpm_user_tasks_reassignment_timer_tid FOREIGN KEY (task_id) REFERENCES jbpm_user_tasks(id) ON DELETE CASCADE

);

CREATE SEQUENCE jbpm_user_tasks_deadline_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 50;

CREATE SEQUENCE jbpm_user_tasks_reassignment_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 50;