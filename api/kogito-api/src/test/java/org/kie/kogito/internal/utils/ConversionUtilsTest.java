/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.internal.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.internal.utils.ConversionUtils.concatPaths;
import static org.kie.kogito.internal.utils.ConversionUtils.convert;
import static org.kie.kogito.internal.utils.ConversionUtils.toCamelCase;

class ConversionUtilsTest {

    @Test
    void testConvertBoolean() {
        assertThat(convert("true", Boolean.class)).isTrue();
        assertThat(convert("5", Boolean.class)).isFalse();
    }

    @Test
    void testConvertInteger() {
        assertThat(convert("5", Integer.class)).isEqualTo(5);
    }

    @Test
    void testConvertDouble() {
        assertThat(convert("10.54d", Double.class)).isEqualTo(10.54d);
    }

    @Test
    void testConvertFloat() {
        assertThat(convert("10.54f", Float.class)).isEqualTo(10.54f);
    }

    @Test
    void testConvertLong() {
        assertThat(convert("1000000000", Long.class)).isEqualTo(1000000000L);
    }

    @Test
    void testConvertShort() {
        assertThat(convert("5", Short.class)).isEqualTo((short) 5);
    }

    @Test
    void testConvertByte() {
        assertThat(convert("112", Byte.class)).isEqualTo((byte) 112);
    }

    @Test
    void testCamelCase() {
        assertThat(toCamelCase("myapp.create")).isEqualTo("myappCreate");
        assertThat(toCamelCase("getByName_1")).isEqualTo("getByName1");
        assertThat(toCamelCase("myapp.create")).isNotEqualTo("myappcreate");
    }

    @Test
    public void testConcatPaths() {
        final String expected = "http:localhost:8080/pepe/pepa/pepi";
        assertThat(concatPaths("http:localhost:8080/pepe/", "/pepa/pepi")).isEqualTo(expected);
        assertThat(concatPaths("http:localhost:8080/pepe", "pepa/pepi")).isEqualTo(expected);
        assertThat(concatPaths("http:localhost:8080/pepe/", "pepa/pepi")).isEqualTo(expected);
        assertThat(concatPaths("http:localhost:8080/pepe", "/pepa/pepi")).isEqualTo(expected);

    }
}
