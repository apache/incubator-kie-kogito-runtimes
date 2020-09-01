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

package org.kie.kogito.quarkus.deployment.graal;

import java.util.Map;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.drools.compiler.compiler.BoundIdentifiers;
import org.drools.compiler.rule.builder.dialect.mvel.MVELAnalysisResult;
import org.drools.core.base.mvel.MVELObjectExpression;
import org.drools.core.rule.Declaration;
import org.drools.modelcompiler.util.MvelUtil;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;

final class MvelSubstitutions {
}


@TargetClass(MVEL.class)
@Delete
final class MVEL_Target {

    @Delete
    public static Class analyze(char[] expression, ParserContext ctx) {
        throw new UnsupportedOperationException("Cannot run MVEL#analyze in native mode");
    }

    @Delete
    public static Class analyze(String expression, ParserContext ctx) {
        throw new UnsupportedOperationException("Cannot run MVEL#analyze in native mode");
    }

    @Delete
    public static Object executeExpression(final Object compiledExpression, final Object ctx) {
        throw new UnsupportedOperationException("Cannot run MVEL#analyze in native mode");
    }

    @Substitute
    public static void DOESNOTEXIST() {

    }

}

@TargetClass(MvelUtil.class)
final class MvelUtil_Target {

    @Substitute
    private static MVELAnalysisResult analyzeExpression(String expr,
                                                        ParserConfiguration conf,
                                                        BoundIdentifiers availableIdentifiers) {
        throw new UnsupportedOperationException();
    }

    @Substitute
    public static MVELObjectExpression createMvelObjectExpression(String expression, ClassLoader classLoader, Map<String, Declaration> decls) {
        throw new UnsupportedOperationException();
    }
}



final class ASM_CL_Target {

    private java.lang.Class loadClass(String className, byte[] b) throws Exception {
        throw new UnsupportedOperationException();
    }
    public static void DOESNOTEXIST() {

    }

}
