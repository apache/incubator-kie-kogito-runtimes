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
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Component
@org.springframework.web.context.annotation.ApplicationScope
public class YaRDRuntime extends org.kie.kogito.yard.AbstractYaRDModels {
    public YaRDRuntime(org.kie.kogito.Application app) {
        initContent();
    }
    private void initContent() {
        loadContent();
    }

    private void loadContent() {
        // populated via codegen
    }

    private static java.io.InputStreamReader readResource(java.io.InputStream stream) {
        if (org.kie.kogito.internal.RuntimeEnvironment.isJdk()) {
            return new java.io.InputStreamReader(stream);
        }

        try {
            byte[] bytes = org.drools.util.IoUtils.readBytesFromInputStream(stream);
            java.io.ByteArrayInputStream byteArrayInputStream = new java.io.ByteArrayInputStream(bytes);
            return new java.io.InputStreamReader(byteArrayInputStream);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}