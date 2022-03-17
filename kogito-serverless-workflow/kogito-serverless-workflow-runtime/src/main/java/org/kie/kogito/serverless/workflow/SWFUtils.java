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
package org.kie.kogito.serverless.workflow;

public class SWFUtils {
    public static String concatPaths(String onePath, String anotherPath) {
        return concatPaths(onePath, anotherPath, "/");
    }

    public static String concatPaths(String onePath, String anotherPath, String concatChars) {
        if (anotherPath.startsWith(concatChars)) {
            if (onePath.endsWith(concatChars)) {
                return onePath.concat(anotherPath.substring(concatChars.length()));
            } else {
                return onePath.concat(anotherPath);
            }
        } else {
            if (onePath.endsWith(concatChars)) {
                return onePath.concat(anotherPath);
            } else {
                return onePath.concat(concatChars).concat(anotherPath);
            }
        }
    }
}
