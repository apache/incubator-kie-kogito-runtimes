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
 *
 */

package org.kie.kogito.quarkus.deployment;

import java.util.Map;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.drools.core.base.mvel.MVELObjectExpression;
import org.drools.core.rule.Declaration;
import org.drools.modelcompiler.util.MvelUtil;

@TargetClass(MvelUtil.class)
@Substitute
public final class MvelUtil_Target {

    @Substitute
    public static MVELObjectExpression createMvelObjectExpression(String expression, ClassLoader classLoader, Map<String, Declaration> decls) {
        throw new UnsupportedOperationException();
    }
}
