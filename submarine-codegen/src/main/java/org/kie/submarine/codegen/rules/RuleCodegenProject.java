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

package org.kie.submarine.codegen.rules;

import java.util.List;
import java.util.function.BiFunction;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.modelcompiler.builder.CanonicalModelCodeGenerationKieProject;
import org.drools.modelcompiler.builder.ModelBuilderImpl;
import org.drools.modelcompiler.builder.PackageModel;
import org.kie.api.builder.KieBuilder;

public class RuleCodegenProject extends CanonicalModelCodeGenerationKieProject implements KieBuilder.ProjectType {

    public static final BiFunction<InternalKieModule, ClassLoader, KieModuleKieProject> SUPPLIER = RuleCodegenProject::new;

    public RuleCodegenProject(InternalKieModule kieModule, ClassLoader classLoader) {
        super(kieModule, classLoader);
    }

    @Override
    public void writeProjectOutput(MemoryFileSystem trgMfs, ResultsImpl messages) {
        super.writeProjectOutput(trgMfs, messages);

        ModuleSourceClass moduleSourceClass =
                new ModuleSourceClass()
                        .withCdi(hasCdi());

        for (ModelBuilderImpl modelBuilder : modelBuilders) {
            List<PackageModel> packageModels = modelBuilder.getPackageModels();
            for (PackageModel packageModel : packageModels) {
                for (Class<?> ruleUnit : packageModel.getRuleUnits()) {

                    moduleSourceClass.addRuleUnit(
                            new RuleUnitSourceClass(
                                    ruleUnit.getPackage().getName(),
                                    ruleUnit.getSimpleName(),
                                    packageModel.getRulesFileName()).withCdi(hasCdi()));
                }
            }
        }

        trgMfs.write(
                moduleSourceClass.generatedFilePath(),
                moduleSourceClass.generate().getBytes());

        for (RuleUnitSourceClass ruleUnit : moduleSourceClass.getRuleUnits()) {
            trgMfs.write(
                    ruleUnit.generatedFilePath(),
                    ruleUnit.generate().getBytes());

            RuleUnitInstanceSourceClass ruleUnitInstance = ruleUnit.instance();
            trgMfs.write(
                    ruleUnitInstance.generatedFilePath(),
                    ruleUnitInstance.generate().getBytes());
        }

    }
}
