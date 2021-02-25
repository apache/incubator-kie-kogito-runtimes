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
package org.kie.kogito.codegen.api.utils;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.SpringBootKogitoBuildContext;

public class KogitoContextTestUtils {

    private KogitoContextTestUtils() {
        // utility class
    }

    public static Stream<Arguments> contextBuilders() {
        return Stream.of(
                Arguments.of(JavaKogitoBuildContext.builder()),
                Arguments.of(QuarkusKogitoBuildContext.builder()),
                Arguments.of(SpringBootKogitoBuildContext.builder()));
    }

    public static Predicate<String> mockClassAvailabilityResolver(Collection<String> includedClasses, Collection<String> excludedClasses) {
        return mockClassAvailabilityResolver(includedClasses, excludedClasses, KogitoContextTestUtils.class.getClassLoader());
    }

    public static Predicate<String> mockClassAvailabilityResolver(Collection<String> includedClasses, Collection<String> excludedClasses, ClassLoader classLoader) {
        return className -> {
            if (includedClasses.contains(className)) {
                return true;
            } else if (excludedClasses.contains(className)) {
                return false;
            }
            try {
                classLoader.loadClass(className);
                return true;
            } catch (ClassNotFoundException ex) {
                return false;
            }
        };
    }

}
