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
package org.kie.kogito.events.mongodb.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.kie.kogito.event.process.ProcessInstanceDataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceDataEvent;

public class EventMongoDBCodecProvider implements CodecProvider {

    private static final ProcessInstanceDataEventCodec PROCESS_INSTANCE_DATA_EVENT_CODEC = new ProcessInstanceDataEventCodec();
    private static final UserTaskInstanceDataEventCodec USER_TASK_INSTANCE_DATA_EVENT_CODEC = new UserTaskInstanceDataEventCodec();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (ProcessInstanceDataEvent.class.isAssignableFrom(aClass)) {
            return (Codec<T>) PROCESS_INSTANCE_DATA_EVENT_CODEC;
        }
        if (UserTaskInstanceDataEvent.class.isAssignableFrom(aClass)) {
            return (Codec<T>) USER_TASK_INSTANCE_DATA_EVENT_CODEC;
        }
        return null;
    }
}
