/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.bpmn2.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceTaskHandler implements KogitoWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServiceTaskHandler.class);

    private String resultVarName;

    public ServiceTaskHandler() {
        this("Result");
    }

    public ServiceTaskHandler(String resultVarName) {
        this.resultVarName = resultVarName;
    }

    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        String service = (String) workItem.getParameter("Interface");
        String interfaceImplementationRef = (String) workItem.getParameter("interfaceImplementationRef");
        String operation = (String) workItem.getParameter("Operation");
        String parameterType = (String) workItem.getParameter("ParameterType");
        Object parameter = workItem.getParameter("Parameter");

        String[] services = { service, interfaceImplementationRef };
        Class<?> c = null;

        for (String serv : services) {
            try {
                c = Class.forName(serv);
                break;
            } catch (ClassNotFoundException cnfe) {
                if (serv.compareTo(services[services.length - 1]) == 0) {
                    handleException(cnfe, service, interfaceImplementationRef, operation, parameterType, parameter);
                }
            }
        }

        try {
            Object instance = c.getDeclaredConstructor().newInstance();
            Class<?>[] classes = null;
            Object[] params = null;
            if (parameterType != null) {
                classes = new Class<?>[] {
                        Class.forName(parameterType)
                };
                params = new Object[] {
                        parameter
                };
            }
            Method method = c.getMethod(operation, classes);
            Object result = method.invoke(instance, params);
            Map<String, Object> results = new HashMap<>();
            results.put(resultVarName, result);
            manager.completeWorkItem(workItem.getStringId(), results);
        } catch (Exception cnfe) {
            handleException(cnfe, service, interfaceImplementationRef, operation, parameterType, parameter);
        }
    }

    private void handleException(Throwable cause, String service, String interfaceImplementationRef, String operation, String paramType, Object param) {
        logger.debug("Handling exception {} inside service {} or {} and operation {} with param type {} and value {}",
                cause.getMessage(), service, interfaceImplementationRef, operation, paramType, param);
        Throwable realCause = cause instanceof InvocationTargetException ? cause.getCause() : cause;
        // Map.of does not accept null values, therefore we need to use the legacy way to build the map 
        Map<String, Object> info = new HashMap<>();
        info.put("Interface", service);
        info.put("InterfaceImplementationRef", interfaceImplementationRef);
        info.put("Operation", operation);
        info.put("ParameterType", paramType);
        info.put("Parameter", param);
        info.put(WorkItemHandlerRuntimeException.WORKITEMHANDLERTYPE, this.getClass().getSimpleName());
        WorkItemHandlerRuntimeException wihRe = new WorkItemHandlerRuntimeException(realCause, info);
        wihRe.setStackTrace(realCause.getStackTrace());
        throw wihRe;
    }

    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        // Do nothing, cannot be aborted
    }
}
