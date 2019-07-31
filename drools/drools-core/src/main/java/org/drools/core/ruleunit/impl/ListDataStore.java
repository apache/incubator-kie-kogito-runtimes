/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.ruleunit.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.kie.kogito.rules.DataEvent;
import org.kie.kogito.rules.DataHandle;
import org.kie.kogito.rules.DataProcessor;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.impl.DataHandleImpl;

public class ListDataStore<T> implements DataStore<T>, DataProcessor<T> {

    private final Map<DataHandle, T> store = new HashMap<>();

    private final Collection<DataProcessor<T>> subscribers = new ArrayList<>();

    public DataHandle add(T t) {
        DataHandle dh = new DataHandleImpl();
        store.put(dh, t);
        subscribers.forEach( s -> s.process(new DataEvent.Insert<>(dh, t, this)) );
        return dh;
    }

    @Override
    public void update(DataHandle handle, T object) {
        subscribers.forEach( s -> s.process(new DataEvent.Update<>(handle, object) ));
    }

    @Override
    public void remove(DataHandle handle) {
        subscribers.forEach( s -> s.process(new DataEvent.Delete<>(handle) ));
        store.remove( handle );
    }

    @Override
    public void subscribe(DataProcessor<T> subscriber) {
        subscribers.add(subscriber);
        store.forEach( (dh, t) -> subscriber.process(new DataEvent.Insert<>(dh, t, this)) );
    }

    @Override
    public Iterator<T> iterator() {
        return store.values().iterator();
    }

    @Override
    public void process(DataEvent<T> m) {
        if (m instanceof EntryPointUpdate) {
            subscribers.forEach(s -> s.process(m));
        }
    }
}
