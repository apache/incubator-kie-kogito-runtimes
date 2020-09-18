/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.integrationtests;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HelloService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloService.class);

    public String hello(String name) throws IOException {
        if (name.equals("exception")) {
            throw new IOException("what kind of name is that?");
        }
        logMethodCall("hello", name);
        return "Hello " + name + "!";
    }

    public JsonNode jsonHello(JsonNode person) throws IOException {
        logMethodCall("jsonHello", person);

        String retJsonStr = "{\"result\":\"Hello " + person.get("name").textValue() + "\"}";
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(retJsonStr);
    }

    public String goodbye(String name) {
        logMethodCall("goodbye", name);
        return "Goodbye " + name + "!";
    }

    public String helloMulti(String name, String lastName) {
        logMethodCall("helloMulti", name, lastName);
        return "Hello (first and lastname) " + name.concat(" ").concat(lastName).concat("!");
    }

    public void helloNoOutput(String name, Integer age) {
        logMethodCall("helloNoOutput", name, age);
    }

    public String helloOutput(String name, Integer age) {
        logMethodCall("helloOutput", name, age);
        return "Hello " + name.concat(" ").concat(String.valueOf(age)).concat("!");
    }

    private static void logMethodCall(String method, Object... arguments) {
        LOGGER.info("HelloService.{} invoked with params: {}", method, arguments);
    }

}
