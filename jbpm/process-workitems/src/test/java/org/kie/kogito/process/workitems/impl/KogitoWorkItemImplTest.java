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
package org.kie.kogito.process.workitems.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KogitoWorkItemImplTest {

    private static class MyWorkItemHandlerParamResolver implements WorkItemParamResolver<Object> {
        @Override
        public Object apply(KogitoWorkItem t) {
            return t.getParameter("name").toString().concat(" is the best");
        }

    }

    @Test
    public void testPutParameters() {
        KogitoWorkItem workItem = new KogitoWorkItemImpl();
        workItem.getParameters().put("name", "javierito");
        workItem.getParameters().put("resolver", new MyWorkItemHandlerParamResolver());
        assertWI(workItem);
    }

    @Test
    public void testSetParameters() {
        KogitoWorkItemImpl workItem = new KogitoWorkItemImpl();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "javierito");
        map.put("resolver", new MyWorkItemHandlerParamResolver());
        workItem.setParameters(map);
        assertWI(workItem);
    }

    @Test
    public void testSetParameter() {
        KogitoWorkItemImpl workItem = new KogitoWorkItemImpl();
        workItem.setParameter("name", "javierito");
        workItem.setParameter("resolver", new MyWorkItemHandlerParamResolver());
        assertWI(workItem);
    }

    private void assertWI(KogitoWorkItem workItem) {
        assertEqualsWI(workItem, "javierito", "name");
        assertEqualsWI(workItem, "javierito is the best", "resolver");

    }

    private void assertEqualsWI(KogitoWorkItem workItem, Object expected, String parameter) {
        assertEquals(expected, workItem.getParameter(parameter));
        Map<String, Object> map = new HashMap<>(workItem.getParameters());
        assertEquals(expected, map.get(parameter));
        assertEquals(expected, map.entrySet().stream().filter(p -> p.getKey().equals(parameter))
                .findFirst().orElseThrow(IllegalStateException::new).getValue());
    }

}
