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
package org.kie.kogito.pmml;

import java.util.Collections;
import java.util.List;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.PackageRegistry;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.ResourceTypePackageRegistry;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.evaluator.api.container.PMMLPackage;
import org.kie.pmml.evaluator.assembler.container.PMMLPackageImpl;
import org.kie.pmml.evaluator.core.executor.PMMLModelEvaluatorFinderImpl;
import org.kie.pmml.evaluator.core.service.PMMLRuntimeImpl;

import static org.kie.pmml.evaluator.assembler.service.PMMLCompilerService.getKiePMMLModelsCompiledFromResource;
import static org.kie.pmml.evaluator.assembler.service.PMMLLoaderService.getKiePMMLModelsLoadedFromResource;

/**
 * Utility class to replace the <b>Assembler</b> mechanism where this is not available
 */
public class PMMLRuntimeBuilder {

    public static PMMLRuntimeImpl fromResources(final List<Resource> resources,
                                                final PMMLModelEvaluatorFinderImpl pmmlModelExecutorFinder) {
        final InternalKnowledgeBase knowledgeBase = new KnowledgeBaseImpl("PMML", null);
        KnowledgeBuilderImpl kbuilderImpl = new KnowledgeBuilderImpl(knowledgeBase);
        resources.forEach(resource -> {
            resource.setSourcePath("org/kie/kogito/pmml/test_regression.pmml");
            List<KiePMMLModel> toAdd = getKiePMMLModelsLoadedFromResource(kbuilderImpl, resource);
            if (toAdd.isEmpty()) {
                toAdd = getKiePMMLModelsCompiledFromResource(kbuilderImpl, resource);
            }
            for (KiePMMLModel kiePMMLModel : toAdd) {
                PackageDescr pkgDescr = new PackageDescr(kiePMMLModel.getKModulePackageName());
                PackageRegistry pkgReg = kbuilderImpl.getOrCreatePackageRegistry(pkgDescr);
                InternalKnowledgePackage kpkgs = pkgReg.getPackage();
                ResourceTypePackageRegistry rpkg = kpkgs.getResourceTypePackages();
                PMMLPackage pmmlPkg = rpkg.computeIfAbsent(ResourceType.PMML, rtp -> new PMMLPackageImpl());
                pmmlPkg.addAll(Collections.singletonList(kiePMMLModel));
            }
        });
        return new PMMLRuntimeImpl(knowledgeBase, pmmlModelExecutorFinder);
    }
}
