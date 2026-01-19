ALTER TABLE jbpm_user_tasks ADD process_instance_id VARCHAR2(50);
ALTER TABLE jbpm_user_tasks ADD process_id VARCHAR2(255);
ALTER TABLE jbpm_user_tasks ADD process_version VARCHAR2(255);
ALTER TABLE jbpm_user_tasks ADD parent_process_instance_id VARCHAR2(50);
ALTER TABLE jbpm_user_tasks ADD root_process_instance_id VARCHAR2(50);
ALTER TABLE jbpm_user_tasks ADD root_process_id VARCHAR2(255);

MERGE INTO jbpm_user_tasks t
    USING jbpm_user_tasks_metadata m
    ON (t.id = m.task_id AND m.metadata_name = 'ProcessInstanceId')
    WHEN MATCHED THEN UPDATE SET t.process_instance_id = REPLACE(m.metadata_value,'"','')
    WHERE m.metadata_value IS NOT NULL;

MERGE INTO jbpm_user_tasks t
    USING jbpm_user_tasks_metadata m
    ON (t.id = m.task_id AND m.metadata_name = 'ProcessId')
    WHEN MATCHED THEN UPDATE SET t.process_id = REPLACE(m.metadata_value,'"','')
    WHERE m.metadata_value IS NOT NULL;

MERGE INTO jbpm_user_tasks t
    USING jbpm_user_tasks_metadata m
    ON (t.id = m.task_id AND m.metadata_name = 'ProcessVersion')
    WHEN MATCHED THEN UPDATE SET t.process_version = REPLACE(m.metadata_value,'"','')
    WHERE m.metadata_value IS NOT NULL;

MERGE INTO jbpm_user_tasks t
    USING jbpm_user_tasks_metadata m
    ON (t.id = m.task_id AND m.metadata_name = 'ParentProcessInstanceId')
    WHEN MATCHED THEN UPDATE SET t.parent_process_instance_id = REPLACE(m.metadata_value,'"','')
    WHERE m.metadata_value IS NOT NULL;

MERGE INTO jbpm_user_tasks t
    USING jbpm_user_tasks_metadata m
    ON (t.id = m.task_id AND m.metadata_name = 'RootProcessInstanceId')
    WHEN MATCHED THEN UPDATE SET t.root_process_instance_id = REPLACE(m.metadata_value,'"','')
    WHERE m.metadata_value IS NOT NULL;

MERGE INTO jbpm_user_tasks t
    USING jbpm_user_tasks_metadata m
    ON (t.id = m.task_id AND m.metadata_name = 'RootProcessId')
    WHEN MATCHED THEN UPDATE SET t.root_process_id = REPLACE(m.metadata_value,'"','')
    WHERE m.metadata_value IS NOT NULL;