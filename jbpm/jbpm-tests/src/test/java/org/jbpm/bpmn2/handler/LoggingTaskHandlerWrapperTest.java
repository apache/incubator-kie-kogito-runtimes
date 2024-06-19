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
package org.jbpm.bpmn2.handler;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.bpmn2.handler.LoggingTaskHandlerDecorator.InputParameter;
import org.jbpm.bpmn2.subprocess.ExceptionThrowingServiceProcessModel;
import org.jbpm.bpmn2.subprocess.ExceptionThrowingServiceProcessProcess;
import org.jbpm.test.utils.ProcessTestHelper;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.handlers.ExceptionService_throwException__2_Handler;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingTaskHandlerWrapperTest {

    @Test
    public void testLimitExceptionInfoList() throws Exception {
        Application app = ProcessTestHelper.newApplication();

        LoggingTaskHandlerDecorator loggingTaskHandlerWrapper = new LoggingTaskHandlerDecorator(ExceptionService_throwException__2_Handler.class, 2);

        ProcessTestHelper.registerHandler(app, "org.jbpm.bpmn2.objects.ExceptionService_throwException__2_Handler", loggingTaskHandlerWrapper);

        org.kie.kogito.process.Process<ExceptionThrowingServiceProcessModel> definition = ExceptionThrowingServiceProcessProcess.newProcess(app);
        ExceptionThrowingServiceProcessModel model = definition.createModel();
        model.setServiceInputItem("exception message");
        org.kie.kogito.process.ProcessInstance<ExceptionThrowingServiceProcessModel> instance1 = definition.createInstance(model);
        instance1.start();
        assertThat(instance1.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);

        org.kie.kogito.process.ProcessInstance<ExceptionThrowingServiceProcessModel> instance2 = definition.createInstance(model);
        instance2.start();
        assertThat(instance2.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);

        org.kie.kogito.process.ProcessInstance<ExceptionThrowingServiceProcessModel> instance3 = definition.createInstance(model);
        instance3.start();
        assertThat(instance3.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);

        int size = loggingTaskHandlerWrapper.getWorkItemExceptionInfoList().size();
        assertThat(size).as("WorkItemExceptionInfoList is too large: " + size).isEqualTo(2);
    }

    @Test
    public void testFormatLoggingError() throws Exception {
        Application app = ProcessTestHelper.newApplication();

        LoggingTaskHandlerDecorator loggingTaskHandlerWrapper = new LoggingTaskHandlerDecorator(ExceptionService_throwException__2_Handler.class, 2);
        loggingTaskHandlerWrapper.setLoggedMessageFormat("{0} - {1} - {2} - {3}");
        List<InputParameter> inputParameters = new ArrayList<LoggingTaskHandlerDecorator.InputParameter>();
        inputParameters.add(InputParameter.EXCEPTION_CLASS);
        inputParameters.add(InputParameter.WORK_ITEM_ID);
        inputParameters.add(InputParameter.WORK_ITEM_NAME);
        inputParameters.add(InputParameter.PROCESS_INSTANCE_ID);
        loggingTaskHandlerWrapper.setLoggedMessageInput(inputParameters);
        loggingTaskHandlerWrapper.setPrintStackTrace(false);

        ProcessTestHelper.registerHandler(app, "org.jbpm.bpmn2.objects.ExceptionService_throwException__2_Handler", loggingTaskHandlerWrapper);

        org.kie.kogito.process.Process<ExceptionThrowingServiceProcessModel> definition = ExceptionThrowingServiceProcessProcess.newProcess(app);
        ExceptionThrowingServiceProcessModel model = definition.createModel();
        model.setServiceInputItem("exception message");
        org.kie.kogito.process.ProcessInstance<ExceptionThrowingServiceProcessModel> instance1 = definition.createInstance(model);
        instance1.start();
        assertThat(instance1.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);

        org.kie.kogito.process.ProcessInstance<ExceptionThrowingServiceProcessModel> instance2 = definition.createInstance(model);
        instance2.start();
        assertThat(instance2.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);

        org.kie.kogito.process.ProcessInstance<ExceptionThrowingServiceProcessModel> instance3 = definition.createInstance(model);
        instance3.start();
        assertThat(instance3.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);

    }

}
