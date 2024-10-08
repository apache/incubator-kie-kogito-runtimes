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

import java.util.function.Function;

import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;

public final class CodegenUtil {
    /**
     * Flag used to configure transaction enablement. Default to <code>true</code>
     */
    public static final String TRANSACTION_ENABLED = "transactionEnabled";

    private CodegenUtil() {
        // do nothing
    }

    public static String generatorProperty(Generator generator, String propertyName) {
        return String.format("kogito.%s.%s", generator.name(), propertyName);
    }

    public static String globalProperty(String propertyName) {
        return String.format("kogito.%s", propertyName);
    }

    /**
     * There is a preference order about how to compute transaction enabling.
     * 1. We check the property for the custom generator and
     */
    public static boolean isTransactionEnabled(Generator generator, KogitoBuildContext context) {
        return getProperty(generator, context, TRANSACTION_ENABLED, Boolean::parseBoolean, true);
    }

    /**
     * There is a preference order about how to compute transaction enabling.
     * 1. We check the property for the custom generator and
     */
    public static <T> T getProperty(Generator generator, KogitoBuildContext context, String propertyName, Function<String, T> converter, T defaultValue) {

        String generatorProperty = generatorProperty(generator, propertyName);
        if (isApplicationPropertyDefined(context, generatorProperty)) {
            return converter.apply(getApplicationProperty(context, generatorProperty));
        }

        String globalProperty = globalProperty(propertyName);
        if (isApplicationPropertyDefined(context, globalProperty)) {
            return converter.apply(getApplicationProperty(context, globalProperty));
        }

        return defaultValue;
    }

    private static boolean isApplicationPropertyDefined(KogitoBuildContext context, String property) {
        return context.getApplicationProperty(property).isPresent();
    }

    private static String getApplicationProperty(KogitoBuildContext context, String property) {
        return context.getApplicationProperty(property).orElseThrow(() -> new IllegalArgumentException("Property " + property + " defined but does not contain proper value"));
    }
}
