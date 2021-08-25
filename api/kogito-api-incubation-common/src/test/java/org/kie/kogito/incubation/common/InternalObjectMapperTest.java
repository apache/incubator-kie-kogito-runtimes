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

package org.kie.kogito.incubation.common;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InternalObjectMapperTest {

    public static class BareField {
        String bareField;
    }

    @Test
    void bareField() {
        BareField o = new BareField();
        o.bareField = "test";

        Map<String, Object> map = InternalObjectMapper.convertToShallowMap(o);
        assertEquals("test", map.get("bareField"));
    }

    public static class PrivateField {
        private String bareField;
    }

    @Test
    void privateField() {
        PrivateField o = new PrivateField();
        o.bareField = "test";

        Map<String, Object> map = InternalObjectMapper.convertToShallowMap(o);
        assertEquals("test", map.get("bareField"));
    }

}
