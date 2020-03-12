/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.dmn;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.ResourceTypePackageRegistry;
import org.drools.core.definitions.impl.KnowledgePackageImpl;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.io.impl.FileSystemResource;
import org.kie.api.io.ResourceType;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.compiler.DMNCompilerImpl;
import org.kie.dmn.core.impl.DMNPackageImpl;
import org.kie.dmn.core.impl.DMNRuntimeImpl;
import org.kie.dmn.core.internal.utils.DMNEvaluationUtils;
import org.kie.dmn.core.internal.utils.DMNEvaluationUtils.DMNEvaluationResult;
import org.kie.kogito.dmn.rest.DMNResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DMNKogito {

    private static final Logger LOG = LoggerFactory.getLogger(DMNKogito.class);

    private DMNKogito() {
        // intentionally private.
    }

    public static DMNRuntime createGenericDMNRuntime(Reader... resources) {
        KnowledgeBaseImpl knowledgeBase = new KnowledgeBaseImpl("", new RuleBaseConfiguration());
        Map<String, InternalKnowledgePackage> pkgs = knowledgeBase.getPackagesMap();
        DMNCompilerImpl compilerImpl = new DMNCompilerImpl();
        for (Reader dmnResource : resources) {
            try {
                DMNModel m = compilerImpl.compile(dmnResource);
                InternalKnowledgePackage pkg = pkgs.computeIfAbsent(m.getNamespace(), KnowledgePackageImpl::new);
                ResourceTypePackageRegistry rpkg = pkg.getResourceTypePackages();
                DMNPackageImpl dmnpkg = rpkg.computeIfAbsent(ResourceType.DMN, rtp -> new DMNPackageImpl(m.getNamespace()));
                dmnpkg.addModel(m.getName(), m);// TODO add profiles? and check dups over namespace/name
            } catch (Exception e) {
                LOG.error("Failed on DMN resource", e);
            }
        }
        return new DMNRuntimeImpl(knowledgeBase);
    }

    public static DMNRuntime createGenericDMNRuntime() {
        KnowledgeBaseImpl knowledgeBase = new KnowledgeBaseImpl("", new RuleBaseConfiguration());
        Map<String, InternalKnowledgePackage> pkgs = knowledgeBase.getPackagesMap();
        DMNCompilerImpl compilerImpl = new DMNCompilerImpl();
        try (Stream<Path> fileStream = Files.walk(Paths.get("."))) {
            List<java.nio.file.Path> files = fileStream
                                                       .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".dmn"))
                                                       .peek(x -> LOG.debug("Adding DMN model {} to runtime", x))
                                                       .collect(Collectors.toList());
            for (java.nio.file.Path file : files) {
                DMNModel m = compilerImpl.compile(new FileSystemResource(file.toFile()));
                InternalKnowledgePackage pkg = pkgs.computeIfAbsent(m.getNamespace(), KnowledgePackageImpl::new);
                ResourceTypePackageRegistry rpkg = pkg.getResourceTypePackages();
                DMNPackageImpl dmnpkg = rpkg.computeIfAbsent(ResourceType.DMN, rtp -> new DMNPackageImpl(m.getNamespace()));
                dmnpkg.addModel(m.getName(), m);// TODO add profiles? and check dups over namespace/name
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DMNRuntimeImpl(knowledgeBase);
    }

    public static DMNModel modelByName(DMNRuntime dmnRuntime, String modelName) {
        List<DMNModel> modelsWithName = dmnRuntime.getModels().stream().filter(m -> modelName.equals(m.getName())).collect(Collectors.toList());
        if (modelsWithName.size() == 1) {
            return modelsWithName.get(0);
        } else {
            throw new RuntimeException("Multiple model with the same name: " + modelName);
        }
    }

    public static DMNResult evaluate(DMNRuntime dmnRuntime, String modelName, Map<String, Object> dmnContext) {
        return evaluate(dmnRuntime, modelByName(dmnRuntime, modelName).getNamespace(), modelName, dmnContext);
    }

    public static DMNResult evaluate(DMNRuntime dmnRuntime, String modelNamespace, String modelName, Map<String, Object> dmnContext) {
        DMNEvaluationResult evaluationResult = DMNEvaluationUtils.evaluate(dmnRuntime,
                                                                           modelNamespace,
                                                                           modelName,
                                                                           dmnContext,
                                                                           null,
                                                                           null,
                                                                           null);
        return new DMNResult(evaluationResult.result);
    }

}
