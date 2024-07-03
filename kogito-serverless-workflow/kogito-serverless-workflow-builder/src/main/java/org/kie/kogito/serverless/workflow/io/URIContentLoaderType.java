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
package org.kie.kogito.serverless.workflow.io;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

public enum URIContentLoaderType {
    CLASSPATH(ClassPathContentLoader::uriToPath),
    FILE(FileContentLoader::uriToPath, File.separatorChar),
    HTTP(HttpContentLoader::uriToPath),
    HTTPS(HttpContentLoader::uriToPath);

    private final char[] additionalSeparators;
    private final Function<String, String> getPathFunction;

    private final String scheme = name().toLowerCase() + ':';

    private URIContentLoaderType(Function<String, String> getPathFunction, char... additionalSeparators) {
        this.getPathFunction = getPathFunction;
        this.additionalSeparators = additionalSeparators;
    }

    public static Optional<String> scheme(String uri) {
        int indexOf = uri.indexOf(":");
        return indexOf == -1 ? Optional.empty() : Optional.of(uri.substring(0, indexOf).toLowerCase());
    }

    public static URIContentLoaderType from(String uri) {
        return scheme(uri).map(scheme -> {
            switch (scheme) {
                case "file":
                    return FILE;
                case "classpath":
                    return CLASSPATH;
                case "http":
                    return HTTP;
                case "https":
                    return HTTPS;
                default:
                    throw new IllegalArgumentException("Unrecognized protocol " + scheme + "for uri " + uri);
            }
        }).orElse(FILE);
    }

    public String addScheme(String path) {
        String lowerCasePath = path.toLowerCase();
        return lowerCasePath.startsWith(scheme) ? path : scheme + path;
    }

    public boolean isAbsolutePath(String path) {
        if (!path.isBlank()) {
            char firstChar = path.trim().charAt(0);
            boolean result = firstChar == '/';
            for (int i = 0; !result && i < additionalSeparators.length; i++) {
                result = firstChar == additionalSeparators[i];
            }
            return result;
        }
        return false;
    }

    public String concat(String basePath, String additionalPath) {
        char separator = separator();
        if (!basePath.isBlank() && !isAbsolutePath(basePath)) {
            basePath = separator + basePath;
        }
        return basePath + separator + additionalPath;
    }

    public String trimLast(String path) {
        int indexOf = lastIndexOf(path);
        return indexOf != -1 ? path.substring(0, indexOf) : "";
    }

    public String lastPart(String path) {
        int indexOf = lastIndexOf(path);
        return indexOf != -1 ? path.substring(indexOf + 1) : path;
    }

    public String scheme() {
        return scheme;
    }

    public String uriToPath(String uri) {
        return getPathFunction.apply(uri);
    }

    private int lastIndexOf(String path) {
        int indexOf = path.lastIndexOf('/');
        int i = 0;
        while (indexOf == -1 && i < additionalSeparators.length) {
            indexOf = path.lastIndexOf(additionalSeparators[i++]);
        }
        return indexOf;
    }

    private char separator() {
        return additionalSeparators.length > 0 ? additionalSeparators[0] : '/';
    }

}
