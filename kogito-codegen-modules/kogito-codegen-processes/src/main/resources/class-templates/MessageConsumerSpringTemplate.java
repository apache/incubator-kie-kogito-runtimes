/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package $Package$;

import java.util.Optional;
import java.util.function.Function;

import org.kie.kogito.process.Process;
import org.kie.kogito.addon.cloudevents.spring.SpringMessageConsumer;
import org.kie.kogito.event.EventReceiver;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


@org.springframework.stereotype.Component()
public class $Type$MessageConsumer extends SpringMessageConsumer<$Type$, $DataType$> {

    @Autowired
    @Qualifier("$ProcessName$") 
    Process<$Type$> process;
    
    
    @Autowired
    EventReceiver eventReceiver;

    
    @PostConstruct
    void init() { 
    	init (process, "$Trigger$", $DataType$.class, eventReceiver);
    }

    private $Type$ eventToModel($DataType$ event) {
        $Type$ model = new $Type$();
        if(event != null) {
            model.$SetModelMethodName$(event);
        }
        return model;
    }

    @Override()
    protected Optional<Function<$DataType$, $Type$>> getModelConverter() {
        return Optional.of(this::eventToModel);
    }
}
