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
package org.kie.kogito.auth;

import java.util.*;

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.NotAuthorizedException;
import org.kie.kogito.internal.process.workitem.Policy;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskInstanceNotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security policy that delivers <code>IdentityProvider</code> to allow to security
 * related policy enforcement.
 *
 */
public class SecurityPolicy implements Policy {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPolicy.class);
    public static final String WORKFLOW_ENGINE_USER = "WORKFLOW_ENGINE_USER";

    private IdentityProvider identity;

    /**
     * Creates new SecurityPolicy for given name, roles
     *
     */
    public static SecurityPolicy of(String name, Collection<String> roles) {
        return new SecurityPolicy(IdentityProviders.of(name, roles));
    }

    /**
     * Creates new SecurityPolicy for given identity provider
     * 
     * @param identity non null identity provider
     * @return new instance of SecurityPolicy
     */
    public static SecurityPolicy of(IdentityProvider identity) {
        Objects.requireNonNull(identity);
        return new SecurityPolicy(identity);
    }

    protected SecurityPolicy(IdentityProvider identity) {
        this.identity = identity;
    }

    public String getUser() {
        return identity.getName();
    }

    public Collection<String> getRoles() {
        return identity.getRoles();
    }

    @Override
    public void enforce(KogitoWorkItem workItem) {
        if (workItem.getActualOwner() != null && workItem.getActualOwner().equals(getUser())) {
            return;
        }

        String actualOwner = workItem.getActualOwner();
        String actualOwners = (String) workItem.getParameter("ActorId");
        String actualRoles = (String) workItem.getParameter("GroupId");
        String excludedOwner = (String) workItem.getParameter("ExcludedOwnerId");
        if (actualOwners != null || actualRoles != null) {
            List<String> owners = actualOwners != null ? new ArrayList<>(List.of(actualOwners.split(","))) : new ArrayList<>();
            List<String> excluded = excludedOwner != null ? new ArrayList<>(List.of(excludedOwner.split(","))) : new ArrayList<>();
            owners.removeAll(excluded);
            List<String> roles = actualRoles != null ? List.of(actualRoles.split(",")) : new ArrayList<>();
            List<String> userRoles = new ArrayList<>(identity.getRoles());
            userRoles.retainAll(roles);
            LOGGER.debug("enforcing identity {} and roles {} with potential owners {} and potential groups {} and exclude groups {}",
                    identity.getName(), identity.getRoles(), owners, roles, excluded);
            if (!owners.contains(identity.getName()) && userRoles.isEmpty()) {
                LOGGER.debug("not authorized with owner {} against identity {}", actualOwner, identity.getName());
                throw new NotAuthorizedException("this work item " + workItem.getStringId() + " is not allows by this owner " + actualOwners + " or " + actualRoles);
            } else if (userRoles.isEmpty() && actualOwner != null && !identity.getName().equals(actualOwner)) {
                LOGGER.debug("identity {} with roles {} not authorized in {}", identity.getName(), identity.getRoles(), roles);
                throw new NotAuthorizedException("this work item " + workItem.getStringId() + " is not allows by this owner " + actualOwner);
            }
        }
    }

    public void enforceAdmin(UserTaskInstance userTaskInstance) {
        enforceAdminIfSet(userTaskInstance, false, false);
    }

    public void enforceAdminIfSet(UserTaskInstance userTaskInstance, boolean acceptOwner) {
        enforceAdminIfSet(userTaskInstance, acceptOwner, false);
    }

    public void enforceAdminIfSet(UserTaskInstance userTaskInstance, boolean acceptOwner, boolean acceptPotentitalUsersAndGroups) {
        String user = identity.getName();

        if (WORKFLOW_ENGINE_USER.equals(user)) {
            return;
        }

        if (user == null) {
            LOGGER.debug("No user defined to perform update on user task {}", userTaskInstance.getId());
            throw new UserTaskInstanceNotAuthorizedException("No user defined to perform update on user task " + userTaskInstance.getId());
        }

        Set<String> adminUsers = userTaskInstance.getAdminUsers();
        if (adminUsers.contains(user)) {
            return;
        }

        Set<String> userAdminGroups = new HashSet<>(userTaskInstance.getAdminGroups());
        userAdminGroups.retainAll(identity.getRoles());
        if (!userAdminGroups.isEmpty()) {
            return;
        }

        if (acceptOwner) {
            if (user.equals(userTaskInstance.getActualOwner())) {
                return;
            }
        }

        if (acceptPotentitalUsersAndGroups) {
            Set<String> potUsers = new HashSet<>(userTaskInstance.getPotentialUsers());
            potUsers.removeAll(userTaskInstance.getExcludedUsers());
            if (potUsers.contains(user)) {
                return;
            }

            Set<String> potGroups = new HashSet<>(userTaskInstance.getPotentialGroups());
            potGroups.retainAll(identity.getRoles());
            if (!potGroups.isEmpty()) {
                return;
            }
        }

        LOGGER.debug("identity {} with roles {} not authorized for user task {} with adminUsers {} and adminGroups {}",
                identity.getName(),
                identity.getRoles(),
                userTaskInstance.getId(),
                userTaskInstance.getAdminUsers(),
                userTaskInstance.getAdminGroups());
        throw new UserTaskInstanceNotAuthorizedException("User " + user + " with roles " + identity.getRoles() + " not authorized to perform an operation on user task " + userTaskInstance.getId());
    }

    @Override
    public String toString() {
        return "SecurityPolicy [identity=" + identity.getName() + ", roles=" + identity.getRoles() + "]";
    }

}
