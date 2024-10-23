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

package org.jbpm.bpmn2.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.process.core.timer.BusinessCalendarImpl;
import org.jbpm.test.utils.ProcessTestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.StaticConfig;
import org.kie.kogito.calendar.BusinessCalendar;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.bpmn2.BpmnProcesses;
import org.kie.kogito.process.impl.AbstractProcessConfig;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessCalendarTest {

    private static BusinessCalendar businessHoursCalendar;
    private static BusinessCalendar nonBusinessHoursCalendar;

    @BeforeAll
    public static void createCalendars() {
        businessHoursCalendar = configureBusinessCalendar(true);
        nonBusinessHoursCalendar = configureBusinessCalendar(false);
    }

    @Test
    public void testTimerInBusinessHours() throws InterruptedException {
        BpmnProcesses bpmnProcesses = new BpmnProcesses();
        ProcessConfig config = new MockProcessConfig(businessHoursCalendar);
        Application app = new StaticApplication(new StaticConfig(null, config), bpmnProcesses);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        org.kie.kogito.process.Process<BusinessCalendarTimerModel> processDefinition = BusinessCalendarTimerProcess.newProcess(app);
        BusinessCalendarTimerModel model = processDefinition.createModel();
        org.kie.kogito.process.ProcessInstance<BusinessCalendarTimerModel> instance = processDefinition.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Thread.sleep(2000);
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
    }

    @Test
    public void testTimerInNonBusinessHours() throws InterruptedException {
        BpmnProcesses bpmnProcesses = new BpmnProcesses();
        ProcessConfig config = new MockProcessConfig(nonBusinessHoursCalendar);
        Application app = new StaticApplication(new StaticConfig(null, config), bpmnProcesses);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        org.kie.kogito.process.Process<BusinessCalendarTimerModel> processDefinition = BusinessCalendarTimerProcess.newProcess(app);
        BusinessCalendarTimerModel model = processDefinition.createModel();
        org.kie.kogito.process.ProcessInstance<BusinessCalendarTimerModel> instance = processDefinition.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Thread.sleep(2000);
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
    }

    @Test
    public void testHumanTaskEscalationBusinessHour() throws InterruptedException {
        BpmnProcesses bpmnProcesses = new BpmnProcesses();
        ProcessConfig config = new MockProcessConfig(businessHoursCalendar);
        Application app = new StaticApplication(new StaticConfig(null, config), bpmnProcesses);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        org.kie.kogito.process.Process<BusinessCalendarEscalationModel> processDefinition = BusinessCalendarEscalationProcess.newProcess(app);
        BusinessCalendarEscalationModel model = processDefinition.createModel();
        org.kie.kogito.process.ProcessInstance<BusinessCalendarEscalationModel> instance = processDefinition.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Thread.sleep(2000);
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        assertThat(workItemHandler.getWorkItem().getParameter("ActorId").toString().equalsIgnoreCase("John")).isTrue();
    }

    @Test
    public void testHumanTaskEscalationNonBusinessHour() throws InterruptedException {
        BpmnProcesses bpmnProcesses = new BpmnProcesses();
        ProcessConfig config = new MockProcessConfig(nonBusinessHoursCalendar);
        Application app = new StaticApplication(new StaticConfig(null, config), bpmnProcesses);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ProcessTestHelper.registerHandler(app, "Human Task", workItemHandler);
        org.kie.kogito.process.Process<BusinessCalendarEscalationModel> processDefinition = BusinessCalendarEscalationProcess.newProcess(app);
        BusinessCalendarEscalationModel model = processDefinition.createModel();
        org.kie.kogito.process.ProcessInstance<BusinessCalendarEscalationModel> instance = processDefinition.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        Thread.sleep(2000);
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        assertThat(workItemHandler.getWorkItem().getParameter("ActorId").toString().equalsIgnoreCase("John")).isTrue();
    }

    private static BusinessCalendar configureBusinessCalendar(boolean businessHours) {
        Logger logger = Logger.getLogger("Business calendar");
        Properties businessCalendarConfiguration = new Properties();
        BusinessCalendar businessCalendar;
        Calendar c = Calendar.getInstance();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("calendar.properties")) {

            if (businessHours) {
                businessCalendarConfiguration.load(is);
                c.add(Calendar.DATE, -1);
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                businessCalendarConfiguration.setProperty(BusinessCalendarImpl.WEEKEND_DAYS, Integer.toString(dayOfWeek));
            } else {
                Date today = new Date();
                c.add(Calendar.DATE, 1);
                Date tomorrow = c.getTime();
                String dateFormat = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                businessCalendarConfiguration.setProperty(BusinessCalendarImpl.HOLIDAYS, sdf.format(today) + "," + sdf.format(tomorrow));
                businessCalendarConfiguration.setProperty(BusinessCalendarImpl.HOLIDAY_DATE_FORMAT, dateFormat);
            }
        } catch (IOException e) {
            logger.warning("Error while loading properties for business calendar " + e.getMessage());
            throw new RuntimeException("Error while loading properties for business calendar ", e);
        }
        businessCalendar = new BusinessCalendarImpl(businessCalendarConfiguration);
        return businessCalendar;
    }

    private static class MockProcessConfig extends AbstractProcessConfig {
        private MockProcessConfig(BusinessCalendar businessCalendar) {
            super(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), List.of(businessCalendar));
        }
    }
}
