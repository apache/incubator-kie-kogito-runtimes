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
package $Package$;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

// TODO Spring Boot 4 autoconfigures only a Jackson 3 (tools.jackson.*) ObjectMapper, while the
// Spring add-ons here still autowire com.fasterxml.jackson.databind.ObjectMapper. This template
// defines a Jackson 2 bean as a transition shim. After the kogito-dependencies-bom split lands
// and the Spring-specific BOM is in place, port this template — and the Spring add-ons that
// consume it — to Jackson 3 / JsonMapperBuilderCustomizer, and remove this shim.
@SpringBootConfiguration
public class GlobalObjectMapper {

    @Autowired
    ConfigBean configBean;

    @Bean
    public ObjectMapper objectMapper() {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        if (!configBean.failOnEmptyBean()) {
            builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        }
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.dateFormat(new StdDateFormat().withColonInTimeZone(true).withTimeZone(TimeZone.getDefault()));
        builder.modulesToInstall(new JavaTimeModule());
        return builder.build();
    }
}
