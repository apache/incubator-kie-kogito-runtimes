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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.NotAuthorizedException;
import org.kie.kogito.internal.process.workitem.Policy;

/**
 * Security policy that delivers <code>IdentityProvider</code> to allow to security
 * related policy enforcement.
 *
 */
public class SecurityPolicy implements Policy {

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

    @Override
    public void enforce(KogitoWorkItem workItem) {
        String actualOwner = (String) workItem.getParameter("ActorId");
        String actualRoles = (String) workItem.getParameter("GroupId");
        if (actualOwner != null || actualRoles != null) {
            List<String> owners = actualOwner != null ? List.of(actualOwner.split(",")) : Collections.emptyList();
            List<String> roles = actualRoles != null ? List.of(actualRoles.split(",")) : Collections.emptyList();
            List<String> userRoles = new ArrayList<>(identity.getRoles());
            userRoles.retainAll(roles);
            if (!owners.contains(identity.getName()) && userRoles.isEmpty()) {
                throw new NotAuthorizedException("this work item " + workItem.getStringId() + " is not allows by this owner" + actualOwner);
            }
        }
    }

}
