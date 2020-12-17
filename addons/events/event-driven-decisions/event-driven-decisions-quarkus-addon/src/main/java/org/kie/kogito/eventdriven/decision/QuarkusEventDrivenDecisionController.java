/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.eventdriven.decision;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import io.quarkus.runtime.Startup;
import org.kie.kogito.Application;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.event.CloudEventEmitter;
import org.kie.kogito.event.CloudEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
public class QuarkusEventDrivenDecisionController extends EventDrivenDecisionController {

    Logger LOG = LoggerFactory.getLogger(QuarkusEventDrivenDecisionController.class);

    @Inject
    Application application;

    @Inject
    ConfigBean config;

    @Inject
    CloudEventEmitter eventEmitter;

    @Inject
    CloudEventReceiver eventReceiver;

    @PostConstruct
    private void onPostConstruct() {
        LOG.info("QuarkusEventDrivenDecisionController pre setup");
        setup(application, config, eventEmitter, eventReceiver);
        LOG.info("QuarkusEventDrivenDecisionController post setup");
    }
}
