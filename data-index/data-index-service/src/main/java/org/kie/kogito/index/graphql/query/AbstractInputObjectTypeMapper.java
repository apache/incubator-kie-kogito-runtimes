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

package org.kie.kogito.index.graphql.query;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInputObjectTypeMapper implements Function<GraphQLObjectType, GraphQLInputObjectType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInputObjectTypeMapper.class);

    private GraphQLSchema schema;
    private Map<String, GraphQLType> additionalTypes;

    public AbstractInputObjectTypeMapper(GraphQLSchema schema, Map<String, GraphQLType> additionalTypes) {
        this.schema = schema;
        this.additionalTypes = additionalTypes;
    }

    @Override
    public GraphQLInputObjectType apply(GraphQLObjectType domain) {
        LOGGER.debug("GraphQL mapping order by for: {}", domain);
        String typeName = getTypeName(domain);
        final GraphQLInputObjectType existingType = getInputObjectType(typeName);
        if (existingType == null) {
            GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject().name(typeName);
            build(domain).accept(builder);
            return builder.build();
        } else {
            return existingType.transform(builder -> {
                builder.clearFields();
                build(domain).accept(builder);
            });
        }
    }

    protected abstract Consumer<GraphQLInputObjectType.Builder> build(GraphQLObjectType domain);

    protected abstract String getTypeName(GraphQLObjectType type);

    protected GraphQLSchema getSchema() {
        return schema;
    }

    protected Map<String, GraphQLType> getAdditionalTypes() {
        return additionalTypes;
    }

    protected GraphQLInputObjectType getInputObjectType(String name) {
        return (GraphQLInputObjectType) schema.getType(name);
    }
}
