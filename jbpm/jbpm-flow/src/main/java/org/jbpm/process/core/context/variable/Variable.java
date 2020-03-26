/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.core.context.variable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.core.TypeObject;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.UndefinedDataType;
import org.jbpm.process.core.ValueObject;

/**
 * Default implementation of a variable.
 * 
 */
public class Variable implements TypeObject, ValueObject, Serializable {

    private static final long serialVersionUID = 510l;
        
    public static final String VARIABLE_TAGS = "customTags";
    
    public static final String READONLY_TAG = "readonly";
    public static final String REQUIRED_TAG = "required";
    public static final String INTERNAL_TAG = "internal";
    public static final String INPUT_TAG = "input";
    public static final String OUTPUT_TAG = "output";
    public static final String BUSINESS_RELEVANT = "business-relevant";
    public static final String TRACKED = "tracked";

    private String id;
    private String name;
    private DataType type;
    private Object value;
    private Map<String, Object> metaData = new HashMap<String, Object>();
    
    private List<String> tags = new ArrayList<>();

    public Variable() {
        this.type = UndefinedDataType.getInstance();
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public DataType getType() {
        return this.type;
    }

    public void setType(final DataType type) {
        if ( type == null ) {
            throw new IllegalArgumentException( "type is null" );
        }
        this.type = type;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(final Object value) {
        if ( this.type.verifyDataType( value ) ) {
            this.value = value;
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append( "Value <" );
            sb.append( value );
            sb.append( "> is not valid for datatype: " );
            sb.append( this.type );
            throw new IllegalArgumentException( sb.toString() );
        }
    }

    public void setMetaData(String name, Object value) {
        this.metaData.put(name, value);
        
        if (VARIABLE_TAGS.equals(name) && value != null) {
            tags = Arrays.asList(value.toString().split(","));
        }
    }
    
    public Object getMetaData(String name) {
        return this.metaData.get(name);
    }
    
    public Map<String, Object> getMetaData() {
    	return this.metaData;
    }
    
    public String toString() {
        return this.name;
    }
    
    public List<String> getTags() {
        if (tags.isEmpty() && this.metaData.containsKey(VARIABLE_TAGS)) {
            tags = Arrays.asList(metaData.get(VARIABLE_TAGS).toString().split(","));
            
        }
        return tags;
    }
    
    public boolean hasTag(String tagName) {
        return getTags().contains(tagName);
    }
}
