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
package org.jbpm.process.core.context.exception;

import javax.ws.rs.WebApplicationException;

public class WebApplicationExceptionPolicy implements ExceptionHandlerPolicy {

    @Override
    public boolean test(String errorCode, Throwable exception) {
        boolean found = false;
        try {
            if (exception instanceof WebApplicationException) {
                int statusCode = ((WebApplicationException) exception).getResponse().getStatus();
                String error[] = errorCode.split(":");
                if (error.length == 1) {
                    // test if errorCode contains the http code, eg. "500"
                    found = Integer.parseInt(error[0]) == statusCode;
                } else if (error.length == 2) {
                    // test if errorCode contains the http code, eg. "HTTP:500"
                    found = Integer.parseInt(error[1]) == statusCode;
                }
            }
        } catch (NoClassDefFoundError error) {
            // ignore CL error
        }
        return found;
    }

}
