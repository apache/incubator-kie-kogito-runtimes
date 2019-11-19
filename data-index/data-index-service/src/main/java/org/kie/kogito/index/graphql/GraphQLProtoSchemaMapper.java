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

package org.kie.kogito.index.graphql;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import org.kie.kogito.index.domain.DomainDescriptor;
import org.kie.kogito.index.event.DomainModelRegisteredEvent;
import org.kie.kogito.index.graphql.query.GraphQLInputObjectTypeMapper;
import org.kie.kogito.index.graphql.query.GraphQLOrderByTypeMapper;
import org.kie.kogito.index.graphql.query.GraphQLQueryParserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.kie.kogito.index.graphql.GraphQLObjectTypeMapper.getTypeName;

@ApplicationScoped
public class GraphQLProtoSchemaMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLProtoSchemaMapper.class);

    @Inject
    GraphQLSchemaManager schemaManager;

    public void onDomainModelRegisteredEvent(@Observes DomainModelRegisteredEvent event) {
        LOGGER.debug("Received new domain event: {}", event);
        GraphQLSchema schema = schemaManager.getGraphQLSchema();
        schemaManager.transform(builder -> {
            builder.clearAdditionalTypes();
            Map<String, DomainDescriptor> map = event.getAdditionalTypes().stream().collect(toMap(desc -> getTypeName(desc.getTypeName()), desc -> desc));
            Map<String, GraphQLType> additionalTypes = new ConcurrentHashMap<>();
            GraphQLObjectType rootType = new GraphQLObjectTypeMapper(schema, additionalTypes, map).apply(event.getDomainDescriptor());
            additionalTypes.put(rootType.getName(), rootType);
            GraphQLInputObjectType whereArgumentType = new GraphQLInputObjectTypeMapper(schema, additionalTypes).apply(rootType);
            additionalTypes.put(whereArgumentType.getName(), whereArgumentType);
            GraphQLInputObjectType orderByType = new GraphQLOrderByTypeMapper(schema, additionalTypes).apply(rootType);
            additionalTypes.put(orderByType.getName(), orderByType);
            Set<GraphQLType> newTypes = additionalTypes.entrySet().stream().map(entry -> entry.getValue()).collect(toSet());
            newTypes.addAll(schema.getAdditionalTypes().stream().filter(type -> additionalTypes.containsKey(type.getName()) == false).collect(toSet()));
            LOGGER.debug("New GraphQL types: {}", newTypes);
            builder.additionalTypes(newTypes);

            GraphQLObjectType query = schema.getQueryType();

            //Should use extend instead?
            query = query.transform(qBuilder -> {
                if (qBuilder.hasField(rootType.getName())) {
                    qBuilder.clearFields();
                    qBuilder.fields(schema.getQueryType().getFieldDefinitions().stream().filter(field -> rootType.getName().equals(field.getName()) == false).collect(toList()));
                }

                GraphQLQueryParserRegistry.get().registerParser(whereArgumentType);

                GraphQLArgument where = newArgument().name("where").type(whereArgumentType).build();
                GraphQLArgument orderBy = newArgument().name("orderBy").type(orderByType).build();
                GraphQLArgument pagination = newArgument().name("pagination").type(new GraphQLTypeReference("Pagination")).build();
                qBuilder.field(newFieldDefinition().name(rootType.getName()).type(GraphQLList.list(rootType)).arguments(asList(where, orderBy, pagination)));
            });
            builder.query(query);

            GraphQLObjectType subscription = schema.getSubscriptionType();
            subscription = subscription.transform(sBuilder -> {
                sBuilder.field(newFieldDefinition().name(rootType.getName() + "Added").type(nonNull(rootType)).build());
                sBuilder.field(newFieldDefinition().name(rootType.getName() + "Updated").type(nonNull(rootType)).build());
            });
            builder.subscription(subscription);

            GraphQLCodeRegistry registry = schema.getCodeRegistry().transform(codeBuilder -> {
                codeBuilder.dataFetcher(coordinates("Query", rootType.getName()), schemaManager.getDomainModelDataFetcher(event.getProcessId()));
                codeBuilder.dataFetcher(coordinates("Subscription", rootType.getName() + "Added"), schemaManager.getDomainModelAddedDataFetcher(event.getProcessId()));
                codeBuilder.dataFetcher(coordinates("Subscription", rootType.getName() + "Updated"), schemaManager.getDomainModelUpdatedDataFetcher(event.getProcessId()));
            });

            builder.codeRegistry(registry);
        });
    }
}
