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
package org.kie.kogito.codegen.process.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kie.kogito.codegen.api.context.ContextAttributesConstants.KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR;
import static org.kie.kogito.codegen.process.util.CodegenUtil.*;

public class CodegenUtilTest {

    private KogitoBuildContext context;

    @BeforeEach
    public void setup() {
        this.context = QuarkusKogitoBuildContext.builder().build();
    }

    @Test
    public void testGetBooleanObjectAccessor() {
        assertEquals("is", getBooleanObjectAccessor(context));

        context.setApplicationProperty(KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR, "javaBeans");
        assertEquals("get", getBooleanObjectAccessor(context));

        context.setApplicationProperty(KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR, "isPrefix");
        assertEquals("is", getBooleanObjectAccessor(context));

        context.setApplicationProperty(KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR, "get1");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            getBooleanObjectAccessor(context);
        });
        assertEquals("Property " + KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR + " defined but does not contain proper value: expected 'isPrefix' or 'javaBeans'", exception.getMessage());
    }
}
