/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.tracing.decision.event.common;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.kie.api.builder.Message.Level;
import org.kie.dmn.api.core.DMNMessage;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class Message {
    private final Level level;
    private final String type;
    private final String sourceId;
    private final String text;
    @JsonInclude(NON_NULL)
    private final MessageFEELEvent feelEvent;
    @JsonInclude(NON_NULL)
    private final MessageException exception;

    public Message(Level level, String type, String sourceId, String text, MessageFEELEvent feelEvent, MessageException exception) {
        this.level = level;
        this.type = type;
        this.sourceId = sourceId;
        this.text = text;
        this.feelEvent = feelEvent;
        this.exception = exception;
    }

    public Level getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getText() {
        return text;
    }

    public MessageFEELEvent getFeelEvent() {
        return feelEvent;
    }

    public MessageException getException() {
        return exception;
    }

    public static Message from(DMNMessage message) {
        if (message == null) {
            return null;
        }
        return new Message(
                message.getLevel(),
                String.format("DMN_%s", message.getMessageType().name()),
                message.getSourceId(),
                message.getText(),
                MessageFEELEvent.from(message.getFeelEvent()),
                MessageException.from(message.getException())
        );
    }

    public static List<Message> from(List<DMNMessage> messages) {
        if (messages == null) {
            return null;
        }
        return messages.stream().map(Message::from).collect(Collectors.toList());
    }
}
