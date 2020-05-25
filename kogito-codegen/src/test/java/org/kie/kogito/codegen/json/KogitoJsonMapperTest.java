/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.rules.DataHandle;
import org.kie.kogito.rules.DataProcessor;
import org.kie.kogito.rules.DataStore;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KogitoJsonMapperTest {

    @Test
    public void testPojo() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new KogitoModule());

        List<Person> input = Arrays.asList(new Person("Mario", 46), new Person("Sofia", 8));

        String text = objectMapper.writeValueAsString( input );
        text = "{\"store\":" + text + "}";

        MyUnit myUnit = objectMapper.readValue( text, MyUnit.class );

        List<Person> output = new ArrayList<>();

        myUnit.store.subscribe( new DataProcessor() {
            @Override
            public FactHandle insert( DataHandle handle, Object object ) {
                output.add((Person) object);
                return null;
            }

            @Override
            public void update( DataHandle handle, Object object ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete( DataHandle handle ) {
                throw new UnsupportedOperationException();
            }
        } );

        assertEquals( input, output );
    }

    public static class MyUnit {
        private DataStore<Person> store;

        public MyUnit() { }

        public MyUnit( DataStore<Person> store ) {
            this.store = store;
        }

        public DataStore<Person> getStore() {
            return store;
        }

        public void setStore( DataStore<Person> store ) {
            this.store = store;
        }
    }
}
