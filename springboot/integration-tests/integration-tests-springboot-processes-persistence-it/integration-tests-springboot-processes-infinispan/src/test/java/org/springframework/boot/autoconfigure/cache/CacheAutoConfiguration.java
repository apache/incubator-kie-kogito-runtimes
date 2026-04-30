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
package org.springframework.boot.autoconfigure.cache;

// TODO Spring Boot 4 transition shim. The Spring Boot 4 spring-boot-cache module relocated
// CacheAutoConfiguration to org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration
// and the old org.springframework.boot.autoconfigure.cache package no longer exists.
// However org.infinispan:infinispan-spring-boot3-starter-remote:15.2.6.Final's
// InfinispanRemoteAutoConfiguration declares @AutoConfigureBefore(CacheAutoConfiguration.class)
// pointing at the old FQN. Spring fails to load the annotation attribute -> ClassNotFoundException.
//
// This empty stub satisfies the classpath resolution. The @AutoConfigureBefore ordering
// becomes a no-op against this class (which is not registered as autoconfigure) but is harmless.
// Remove this stub when:
//   - Infinispan ships a Spring Boot 4-compatible starter (e.g. infinispan-spring-boot4-starter-remote), OR
//   - The kogito-dependencies-bom split lands and the Spring-side migration replaces this dep.
public class CacheAutoConfiguration {
}
