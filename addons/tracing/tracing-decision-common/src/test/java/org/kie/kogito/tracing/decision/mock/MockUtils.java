/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.tracing.decision.mock;

import java.util.Collections;

import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.core.impl.DMNResultImpl;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEvent;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUtils {

    public static final String TEST_MODEL_NAME = "modelName";
    public static final String TEST_MODEL_NAMESPACE = "modelNamespace";

    public static EvaluateEvent beforeEvaluateAllEvent(String id) {
        DMNContextImpl context = new DMNContextImpl();
        DecisionExecutionIdUtils.inject(context, () -> id);
        DMNResultImpl result = new DMNResultImpl(new DMNModelImpl());
        result.setContext(context);
        return new EvaluateEvent(EvaluateEvent.Type.BEFORE_EVALUATE_ALL, System.nanoTime(), result, TEST_MODEL_NAMESPACE, TEST_MODEL_NAME);
    }

    public static EvaluateEvent afterEvaluateAllEvent(String id) {
        DMNContextImpl context = new DMNContextImpl();
        DecisionExecutionIdUtils.inject(context, () -> id);
        DMNResultImpl result = new DMNResultImpl(new DMNModelImpl());
        result.setContext(context);
        return new EvaluateEvent(EvaluateEvent.Type.AFTER_EVALUATE_ALL, System.nanoTime(), result, TEST_MODEL_NAMESPACE, TEST_MODEL_NAME);
    }

    public static DMNModel mockedModel() {
        DMNModel mockedModel = mock(DMNModel.class);
        when(mockedModel.getNamespace()).thenReturn(TEST_MODEL_NAMESPACE);
        when(mockedModel.getName()).thenReturn(TEST_MODEL_NAME);
        when(mockedModel.getInputs()).thenReturn(Collections.emptySet());
        when(mockedModel.getDecisionById(anyString())).thenReturn(null);
        return mockedModel;
    }
}
