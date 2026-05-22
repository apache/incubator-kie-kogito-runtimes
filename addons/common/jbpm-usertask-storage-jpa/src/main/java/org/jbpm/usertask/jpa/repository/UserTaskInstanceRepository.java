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
import java.util.stream.Collectors;

import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskFilter;
import org.kie.kogito.usertask.lifecycle.UserTaskState;

import jakarta.persistence.TypedQuery;

import static org.jbpm.usertask.jpa.model.UserTaskInstanceEntity.BASE_IDENTITY_QUERY;
import static org.jbpm.usertask.jpa.model.UserTaskInstanceEntity.DELETE_BY_ID;
import static org.jbpm.usertask.jpa.model.UserTaskInstanceEntity.STATUS_FILTER_CLAUSE;
import static org.jbpm.usertask.jpa.model.UserTaskInstanceEntity.TASKNAME_FILTER_CLAUSE;

public class UserTaskInstanceRepository extends BaseRepository<UserTaskInstanceEntity, String> {

    public UserTaskInstanceRepository(UserTaskJPAContext context) {
        super(context);
    }

    public List<UserTaskInstanceEntity> findByIdentity(IdentityProvider identityProvider) {
        return findByIdentity(identityProvider, null);
    }

    public List<UserTaskInstanceEntity> findByIdentity(IdentityProvider identityProvider, UserTaskFilter filter) {
        String userId = identityProvider.getName();
        Collection<String> roles = identityProvider.getRoles();

        // Build query dynamically - start with base query
        String jpql = BASE_IDENTITY_QUERY;

        boolean hasTaskNameFilter = filter != null
                && filter.taskName() != null
                && !filter.taskName().isEmpty();

        boolean hasStatusFilter = filter != null
                && filter.statuses() != null
                && !filter.statuses().isEmpty();

        // Conditionally append filter clauses
        if (hasTaskNameFilter) {
            jpql = jpql.concat(TASKNAME_FILTER_CLAUSE);
        }

        if (hasStatusFilter) {
            jpql = jpql.concat(STATUS_FILTER_CLAUSE);
        }

        // Create typed query with the final JPQL
        TypedQuery<UserTaskInstanceEntity> query = getEntityManager()
                .createQuery(jpql, UserTaskInstanceEntity.class)
                .setParameter("userId", userId)
                .setParameter("roles", roles);

        if (filter != null) {
            query.setParameter("processId", filter.processId());
            query.setParameter("processInstanceId", filter.processInstanceId());

            // Only set taskName if we added it to the query
            if (hasTaskNameFilter) {
                query.setParameter("taskName", filter.taskName());
            }

            // Only set statusFilter if we added it to the query
            if (hasStatusFilter) {
                List<String> statusFilter = filter.statuses().stream()
                        .map(UserTaskState::getName)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
                query.setParameter("statusFilter", statusFilter);
            }
        } else {
            query.setParameter("processId", null);
            query.setParameter("processInstanceId", null);
        }

        return query.getResultList();
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
