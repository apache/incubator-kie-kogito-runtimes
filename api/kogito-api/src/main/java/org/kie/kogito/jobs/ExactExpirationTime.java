/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.jobs;

import java.time.ZonedDateTime;
import java.util.Objects;


public class ExactExpirationTime implements ExpirationTime {

    private final ZonedDateTime expirationTime;
    
    private ExactExpirationTime(ZonedDateTime expirationTime) {
        this.expirationTime = Objects.requireNonNull(expirationTime);
    }
    
    @Override
    public ZonedDateTime get() {
        return expirationTime;
    }

    @Override
    public Long repeatInterval() {     
        return null;
    }
    
    @Override
    public Integer repeatLimit() {
        return 0;
    }
    
    public static ExactExpirationTime of(ZonedDateTime expirationTime) {
        return new ExactExpirationTime(expirationTime);
    }
    
    public static ExactExpirationTime of(String date) {
        return new ExactExpirationTime(ZonedDateTime.parse(date));
    }

    public static ExactExpirationTime now() {
        return new ExactExpirationTime(ZonedDateTime.now());
    }
}
