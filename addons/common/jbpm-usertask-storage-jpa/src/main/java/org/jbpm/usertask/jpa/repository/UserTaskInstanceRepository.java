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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbpm.usertask.jpa.model.UserTaskInstanceEntity;
import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskFilter;
import org.kie.kogito.usertask.lifecycle.UserTaskState;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.jbpm.usertask.jpa.model.UserTaskInstanceEntity.DELETE_BY_ID;
import static org.jbpm.usertask.jpa.model.UserTaskInstanceEntity.GET_INSTANCES_BY_IDENTITY;

public class UserTaskInstanceRepository extends BaseRepository<UserTaskInstanceEntity, String> {

    public UserTaskInstanceRepository(UserTaskJPAContext context) {
        super(context);
    }

    public List<UserTaskInstanceEntity> findByIdentity(IdentityProvider identityProvider) {
        TypedQuery<UserTaskInstanceEntity> query = getEntityManager().createNamedQuery(GET_INSTANCES_BY_IDENTITY, UserTaskInstanceEntity.class);
        query.setParameter("userId", identityProvider.getName());
        query.setParameter("roles", identityProvider.getRoles());
        return query.getResultList();
    }

    public List<UserTaskInstanceEntity> findByIdentityAndFilter(IdentityProvider identityProvider, UserTaskFilter filter) {
        // If no filter, use the optimized named query
        if (filter == null) {
            return findByIdentity(identityProvider);
        }

        // Build single query combining identity and filter predicates using Criteria API
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserTaskInstanceEntity> cq = cb.createQuery(UserTaskInstanceEntity.class);
        Root<UserTaskInstanceEntity> root = cq.from(UserTaskInstanceEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Add identity predicates (replicate the logic from GET_INSTANCES_BY_IDENTITY named query)
        String userId = identityProvider.getName();
        Collection<String> roles = identityProvider.getRoles();

        // Use LEFT JOINs to avoid filtering out tasks without groups
        Join<Object, Object> adminGroupsJoin = root.join("adminGroups", JoinType.LEFT);
        Join<Object, Object> potentialGroupsJoin = root.join("potentialGroups", JoinType.LEFT);

        Predicate identityPredicate = cb.or(
                // User is admin
                cb.isMember(userId, root.get("adminUsers")),
                // User's role is in admin groups
                cb.isTrue(adminGroupsJoin.in(roles)),
                // User is actual owner
                cb.equal(root.get("actualOwner"), userId),
                cb.and(
                        cb.isNull(root.get("actualOwner")),
                        cb.isNotMember(userId, root.get("excludedUsers")),
                        cb.or(
                                cb.isMember(userId, root.get("potentialUsers")),
                                cb.isTrue(potentialGroupsJoin.in(roles)))));
        predicates.add(identityPredicate);

        // Add filter predicates
        // Process ID filter (exact match)
        if (filter.getProcessId() != null) {
            predicates.add(cb.equal(root.get("processInfo").get("processId"), filter.getProcessId()));
        }

        // Process Instance ID filter (exact match)
        if (filter.getProcessInstanceId() != null) {
            predicates.add(cb.equal(root.get("processInfo").get("processInstanceId"), filter.getProcessInstanceId()));
        }

        // Status filter (IN clause for multiple statuses - case insensitive)
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            List<String> statusNames = filter.getStatuses().stream()
                    .map(UserTaskState::getName)
                    .map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toList());
            predicates.add(cb.lower(root.get("status")).in(statusNames));
        }

        // Task name filter (contains - case insensitive)
        if (filter.getTaskName() != null) {
            predicates.add(cb.like(cb.lower(root.get("taskName")), "%" + filter.getTaskName().toLowerCase() + "%"));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true); // Ensure distinct results due to joins

        return getEntityManager().createQuery(cq).getResultList();
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
