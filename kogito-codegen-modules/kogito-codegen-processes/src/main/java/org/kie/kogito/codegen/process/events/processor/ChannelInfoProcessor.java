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

package org.kie.kogito.codegen.process.events.processor;

import java.util.List;
import java.util.Map;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.SpringBootKogitoBuildContext;

public abstract class ChannelInfoProcessor {

    private KogitoBuildContext context;

    public ChannelInfoProcessor(KogitoBuildContext context) {
        this.context = context;
    }

    public List<ChannelInfo> process() {
        return toChannelInfo(filter(context.getPropertiesMap()));
    }

    public KogitoBuildContext getKogitoBuildContext() {
        return context;
    }

    public abstract Map<String, String> filter(Map<String, String> applicationProperties);

    public abstract List<ChannelInfo> toChannelInfo(Map<String, String> channelProperties);

    public static ChannelInfoProcessor newChannelInfoProcessor(KogitoBuildContext context) {
        switch (context.name()) {
            case QuarkusKogitoBuildContext.CONTEXT_NAME:
                return new QuarkusChannelInfoProcessor(context);
            case SpringBootKogitoBuildContext.CONTEXT_NAME:
                return new SpringBootChannelInfoProcessor(context);
            case JavaKogitoBuildContext.CONTEXT_NAME:
                return new JavaChannelInfoProcessor(context);
            default:
                throw new IllegalArgumentException("Build context unkown " + context.name());
        }
    }
}
