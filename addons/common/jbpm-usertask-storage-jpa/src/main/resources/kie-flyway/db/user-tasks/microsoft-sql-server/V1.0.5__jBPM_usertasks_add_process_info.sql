ALTER TABLE jbpm_user_tasks ADD process_instance_id VARCHAR(50);
ALTER TABLE jbpm_user_tasks ADD process_id VARCHAR(255);
ALTER TABLE jbpm_user_tasks ADD process_version VARCHAR(255);
ALTER TABLE jbpm_user_tasks ADD parent_process_instance_id VARCHAR(50);
ALTER TABLE jbpm_user_tasks ADD root_process_instance_id VARCHAR(50);
ALTER TABLE jbpm_user_tasks ADD root_process_id VARCHAR(255);
GO

UPDATE t
SET t.process_instance_id = REPLACE(m.metadata_value, '"', '')
FROM jbpm_user_tasks t
INNER JOIN jbpm_user_tasks_metadata m ON t.id = m.task_id
WHERE m.metadata_name = 'ProcessInstanceId'
    AND m.metadata_value IS NOT NULL;

UPDATE t
SET t.process_id = REPLACE(m.metadata_value, '"', '')
FROM jbpm_user_tasks t
INNER JOIN jbpm_user_tasks_metadata m ON t.id = m.task_id
WHERE m.metadata_name = 'ProcessId'
    AND m.metadata_value IS NOT NULL;

UPDATE t
SET t.process_version = REPLACE(m.metadata_value, '"', '')
FROM jbpm_user_tasks t
INNER JOIN jbpm_user_tasks_metadata m ON t.id = m.task_id
WHERE m.metadata_name = 'ProcessVersion'
    AND m.metadata_value IS NOT NULL;

UPDATE t
SET t.parent_process_instance_id = REPLACE(m.metadata_value, '"', '')
FROM jbpm_user_tasks t
INNER JOIN jbpm_user_tasks_metadata m ON t.id = m.task_id
WHERE m.metadata_name = 'ParentProcessInstanceId'
    AND m.metadata_value IS NOT NULL;

UPDATE t
SET t.root_process_instance_id = REPLACE(m.metadata_value, '"', '')
FROM jbpm_user_tasks t
INNER JOIN jbpm_user_tasks_metadata m ON t.id = m.task_id
WHERE m.metadata_name = 'RootProcessInstanceId'
    AND m.metadata_value IS NOT NULL;

UPDATE t
SET t.root_process_id = REPLACE(m.metadata_value, '"', '')
FROM jbpm_user_tasks t
INNER JOIN jbpm_user_tasks_metadata m ON t.id = m.task_id
WHERE m.metadata_name = 'RootProcessId'
    AND m.metadata_value IS NOT NULL;

