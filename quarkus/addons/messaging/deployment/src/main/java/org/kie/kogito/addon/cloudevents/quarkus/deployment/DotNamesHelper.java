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
package org.kie.kogito.addon.cloudevents.quarkus.deployment;

import org.jboss.jandex.DotName;

import io.quarkus.arc.impl.ComputingCache;

public class DotNamesHelper {

    private DotNamesHelper() {
    }

    private static ComputingCache<String, DotName> dotNamesMap = new ComputingCache<>(DotNamesHelper::createDotName);

    public static DotName createDotName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return DotName.createComponentized(null, name);
        }
        String prefix = name.substring(0, lastDot);
        DotName prefixName = dotNamesMap.getValue(prefix);
        String local = name.substring(lastDot + 1);
        return DotName.createComponentized(prefixName, local);
    }

    public static DotName createAnnotationName(EventGenerator generator) {
        return createDotName(generator.getFullAnnotationName().orElseThrow(() -> new IllegalArgumentException("No annotation name for bean " + generator.getClassName())));
    }

    public static DotName createClassName(EventGenerator generator) {
        return createDotName(generator.getPackageName() + '.' + generator.getClassName());
    }

}
