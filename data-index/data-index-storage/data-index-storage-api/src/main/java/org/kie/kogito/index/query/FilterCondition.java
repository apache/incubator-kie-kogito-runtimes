/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.index.query;

public enum FilterCondition {

    IN("in"),
    CONTAINS("contains"),
    CONTAINS_ALL("containsAll"),
    CONTAINS_ANY("containsAny"),
    LIKE("like"),
    IS_NULL("isNull"),
    NOT_NULL("notNull"),
    EQUAL("equal"),
    GT("greaterThan"),
    GTE("greaterThanEqual"),
    LT("lessThan"),
    LTE("lessThanEqual"),
    BETWEEN("between"),
    AND("and"),
    OR("or");

    private String label;

    FilterCondition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static FilterCondition fromLabel(String label) {
        for (FilterCondition c : FilterCondition.values()) {
            if (c.getLabel().equals(label)) {
                return c;
            }
        }
        return null;
    }
}
