/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.event;

import java.util.Optional;

/*
 * Currently we are not discriminating on type, 
 * but an implementation not based on smallrye might want to do that
 */
public class SubscriptionInfo<S, T> {

    private EventConverter<S, T> converter;
    private Optional<String> type;

    public SubscriptionInfo(EventConverter<S, T> converter) {
        this(converter, Optional.empty());
    }

    public SubscriptionInfo(EventConverter<S, T> converter, Optional<String> type) {
        this.converter = converter;
        this.type = type;
    }

    public Optional<String> getType() {
        return type;
    }

    public EventConverter<S, T> getConverter() {
        return converter;
    }

    @Override
    public String toString() {
        return "SubscriptionInfo [type=" + type + ", converter=" + converter + "]";
    }
}
