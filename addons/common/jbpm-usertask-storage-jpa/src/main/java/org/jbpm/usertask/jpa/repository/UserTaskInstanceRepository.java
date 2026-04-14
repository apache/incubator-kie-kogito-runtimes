/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jbpm.usertask.jpa.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.kie.kogito.auth.IdentityProvider;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import static org.jbpm.usertask.jpa.model.UserTaskInstanceEntity.DELETE_BY_ID;

public class UserTaskInstanceRepository extends BaseRepository<UserTaskInstanceEntity, String> {

    public UserTaskInstanceRepository(UserTaskJPAContext context) {
        super(context);
    }

    @Override
    public List<UserTaskInstanceEntity> findAll() {
        String baseQuery = "SELECT e FROM UserTaskInstanceEntity e";
        TypedQuery<UserTaskInstanceEntity> query = getEntityManager()
                .createQuery(buildQueryWithProcessFiltering(baseQuery, "e"), UserTaskInstanceEntity.class);
        bindProcessFilteringParameters(query);
        return query.getResultList();
    }

    @Override
    public Optional<UserTaskInstanceEntity> findById(String id) {
        String baseQuery = "SELECT e FROM UserTaskInstanceEntity e WHERE id=:userTaskId";
        TypedQuery<UserTaskInstanceEntity> query = getEntityManager().createQuery(
                buildQueryWithProcessFiltering(baseQuery, "e"), UserTaskInstanceEntity.class);
        bindProcessFilteringParameters(query);
        query.setParameter("userTaskId", id);
        return query.getResultList().stream().findFirst();
    }

    public List<UserTaskInstanceEntity> findByIdentity(IdentityProvider identityProvider) {
        String baseQuery = "select userTask from UserTaskInstanceEntity userTask " +
                "left join userTask.adminGroups adminGroup " +
                "left join userTask.potentialGroups potentialGroup " +
                "where (:userId member of userTask.adminUsers " +
                "or adminGroup in (:roles) " +
                "or userTask.actualOwner = :userId " +
                "or (userTask.actualOwner is null " +
                "and :userId not member of userTask.excludedUsers " +
                "and (:userId member of userTask.potentialUsers or potentialGroup in (:roles)" +
                ")))";

        TypedQuery<UserTaskInstanceEntity> query = getEntityManager()
                .createQuery(buildQueryWithProcessFiltering(baseQuery, "userTask"), UserTaskInstanceEntity.class)
                .setParameter("userId", identityProvider.getName())
                .setParameter("roles", identityProvider.getRoles());
        bindProcessFilteringParameters(query);
        return query.getResultList();
    }

    protected Collection<String> getProcessIdsForFiltering() {
        return context.getProcesses() != null ? context.getProcesses().processIds() : null;
    }

    protected boolean isFilterByLocalProcess() {
        Collection<String> processIds = getProcessIdsForFiltering();
        return processIds != null && !processIds.isEmpty();
    }

    protected String buildQueryWithProcessFiltering(String baseQuery, String entityReference) {
        if (!isFilterByLocalProcess()) {
            return baseQuery;
        }

        Collection<String> processIds = getProcessIdsForFiltering();
        String unionSelects = IntStream.range(0, processIds.size())
                .mapToObj(i -> "SELECT CAST(:processId" + i + " AS STRING) AS processId")
                .collect(Collectors.joining(" UNION ALL "));
        String cte = "WITH allowed_processes AS (" + unionSelects + ") ";
        String whereClause = baseQuery.toLowerCase().contains(" where ") ? " AND " : " WHERE ";
        String existsClause = whereClause + String.format(
                "EXISTS (SELECT 1 FROM allowed_processes ap WHERE ap.processId = %s.processInfo.rootProcessId OR (%s.processInfo.rootProcessId IS NULL AND ap.processId = %s.processInfo.processId))",
                entityReference,
                entityReference,
                entityReference);

        return cte + baseQuery + existsClause;
    }

    protected <Q extends Query> void bindProcessFilteringParameters(Q query) {
        if (isFilterByLocalProcess()) {
            List<String> processIdList = List.copyOf(getProcessIdsForFiltering());
            for (int i = 0; i < processIdList.size(); i++) {
                query.setParameter("processId" + i, processIdList.get(i));
            }
        }
    }

    public UserTaskInstanceEntity delete(UserTaskInstanceEntity entity) {
        getEntityManager().detach(entity);
        getEntityManager().createNamedQuery(DELETE_BY_ID)
                .setParameter("taskId", entity.getId())
                .executeUpdate();
        return entity;
    }

    @Override
    public Class<UserTaskInstanceEntity> getEntityClass() {
        return UserTaskInstanceEntity.class;
    }
}
