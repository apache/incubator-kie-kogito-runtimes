/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.index.infinispan.query;

import java.util.List;
import java.util.stream.Stream;

import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.index.model.ProcessInstance;
import org.kie.kogito.index.query.AttributeFilter;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.kie.kogito.index.query.QueryFilterFactory.*;
import static org.kie.kogito.index.query.SortDirection.ASC;
import static org.kie.kogito.index.query.SortDirection.DESC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InfinispanQueryTest {

    @Mock
    QueryFactory factory;

    @Mock
    Query mockQuery;

    private static Stream<Arguments> provideFilters() {
        return Stream.of(
                Arguments.of(
                        asList(like("name", "test%")),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.name like 'test%'"
                ),
                Arguments.of(
                        asList(in("id", asList("8035b580-6ae4-4aa8-9ec0-e18e19809e0b", "a1e139d5-4e77-48c9-84ae-34578e904e5a"))),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.id in ('8035b580-6ae4-4aa8-9ec0-e18e19809e0b', 'a1e139d5-4e77-48c9-84ae-34578e904e5a')"
                ),
                Arguments.of(
                        asList(equalTo("id", "8035b580-6ae4-4aa8-9ec0-e18e19809e0b")),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.id = '8035b580-6ae4-4aa8-9ec0-e18e19809e0b'"
                ),
                Arguments.of(
                        asList(contains("name", "test")),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.name = 'test'"
                ),
                Arguments.of(
                        asList(containsAll("name", asList("name1", "name2"))),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.name = 'name1' and o.name = 'name2'"
                ),
                Arguments.of(
                        asList(containsAny("name", asList("name1", "name2"))),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.name = 'name1' or o.name = 'name2'"
                ),
                Arguments.of(
                        asList(isNull("name")),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.name is null"
                ),
                Arguments.of(
                        asList(notNull("name")),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.name is not null"
                ),
                Arguments.of(
                        asList(between("start", "2019-01-01", "2020-01-01")),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.start between '2019-01-01' and '2020-01-01'"
                ),
                Arguments.of(
                        asList(greaterThan("priority", 1)),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.priority > 1"
                ),
                Arguments.of(
                        asList(greaterThanEqual("priority", 1)),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.priority >= 1"
                ),
                Arguments.of(
                        asList(lessThan("priority", 1)),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.priority < 1"
                ),
                Arguments.of(
                        asList(lessThanEqual("priority", 1)),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.priority <= 1"
                ),
                Arguments.of(
                        asList(and(asList(lessThanEqual("priority", 1), greaterThan("priority", 1)))),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.priority <= 1 and o.priority > 1"
                ),
                Arguments.of(
                        asList(or(asList(lessThanEqual("priority", 1), greaterThan("priority", 1)))),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.priority <= 1 or o.priority > 1"
                ),
                Arguments.of(
                        asList(and(asList(notNull("name"), contains("name", "test"))), or(asList(lessThanEqual("priority", 1), greaterThan("priority", 1)))),
                        "from org.kie.kogito.index.model.ProcessInstance o where o.name is not null and o.name = 'test' and o.priority <= 1 or o.priority > 1"
                )
        );
    }

    @BeforeEach
    public void setup() {
        when(factory.create(any())).thenReturn(mockQuery);
    }

    @Test
    public void testNoParameters() {
        InfinispanQuery query = new InfinispanQuery(factory, ProcessInstance.class.getName());

        query.execute();

        verify(factory).create("from org.kie.kogito.index.model.ProcessInstance o");
        verify(mockQuery).list();
    }

    @Test
    public void testEmptyParameters() {
        InfinispanQuery query = new InfinispanQuery(factory, ProcessInstance.class.getName());
        query.filter(emptyList());
        query.sort(emptyList());

        query.execute();

        verify(factory).create("from org.kie.kogito.index.model.ProcessInstance o");
        verify(mockQuery).list();
    }

    @Test
    public void testPagination() {
        InfinispanQuery query = new InfinispanQuery(factory, ProcessInstance.class.getName());
        query.limit(10);
        query.offset(0);

        query.execute();

        verify(factory).create("from org.kie.kogito.index.model.ProcessInstance o");
        verify(mockQuery).startOffset(0);
        verify(mockQuery).maxResults(10);
        verify(mockQuery).list();
    }

    @Test
    public void testOrderBy() {
        InfinispanQuery query = new InfinispanQuery(factory, ProcessInstance.class.getName());
        query.sort(asList(orderBy("name", DESC), orderBy("date", ASC)));

        query.execute();

        verify(factory).create("from org.kie.kogito.index.model.ProcessInstance o order by o.name DESC, o.date ASC");
        verify(mockQuery).list();
    }

    @ParameterizedTest
    @MethodSource("provideFilters")
    public void assertQueryFilters(List<AttributeFilter> filters, String queryString) {
        InfinispanQuery query = new InfinispanQuery(factory, ProcessInstance.class.getName());
        query.filter(filters);

        query.execute();

        verify(factory).create(queryString);
        verify(mockQuery).list();
    }
}
