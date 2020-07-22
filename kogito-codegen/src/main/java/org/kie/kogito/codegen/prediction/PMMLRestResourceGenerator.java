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

package org.kie.kogito.codegen.prediction;

import java.net.URLEncoder;
import java.util.NoSuchElementException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.core.util.StringUtils;
import org.kie.dmn.feel.codegen.feel11.CodegenStringUtil;
import org.kie.kogito.codegen.BodyDeclarationComparator;
import org.kie.kogito.codegen.CodegenUtils;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.pmml.commons.model.KiePMMLModel;

import static com.github.javaparser.StaticJavaParser.parse;

public class PMMLRestResourceGenerator {

    private static final String TEMPLATE_JAVA = "/class-templates/PMMLRestResourceTemplate.java";

    private final KiePMMLModel kiePMMLModel;
    private final String nameURL;
    private final String packageName;
    private final String relativePath;
    private final String resourceClazzName;
    private final String appCanonicalName;
    private DependencyInjectionAnnotator annotator;
    private boolean useMonitoring;
    private boolean isStronglyTyped = false;

    public PMMLRestResourceGenerator(KiePMMLModel model, String appCanonicalName) {
        this.kiePMMLModel = model;
        this.packageName = "org.kie.kogito." + CodegenStringUtil.escapeIdentifier(model.getClass().getPackage().getName());
        this.nameURL = URLEncoder.encode(model.getName()).replaceAll("\\+", "%20");
        this.appCanonicalName = appCanonicalName;
        String classPrefix = StringUtils.capitalize(model.getName());
        this.resourceClazzName = classPrefix + "Resource";
        this.relativePath = packageName.replace(".", "/") + "/" + resourceClazzName + ".java";
    }

    public String generate() {
        CompilationUnit clazz = parse(this.getClass().getResourceAsStream(TEMPLATE_JAVA));
        clazz.setPackageDeclaration(this.packageName);

        ClassOrInterfaceDeclaration template = clazz
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface " +
                                                                      "declaration!"));

        template.setName(resourceClazzName);

        template.findAll(StringLiteralExpr.class).forEach(this::interpolateStrings);

        interpolateInputType(template);

        if (useInjection()) {
            template.findAll(FieldDeclaration.class,
                             CodegenUtils::isApplicationField).forEach(fd -> annotator.withInjection(fd));
        } else {
            template.findAll(FieldDeclaration.class,
                             CodegenUtils::isApplicationField).forEach(this::initializeApplicationField);
        }

        template.getMembers().sort(new BodyDeclarationComparator());
        return clazz.toString();
    }

    private void interpolateInputType(ClassOrInterfaceDeclaration template) {
        String inputType = isStronglyTyped ? "InputSet" : "java.util.Map<String, Object>";
        template.findAll(ClassOrInterfaceType.class, t -> t.asString().equals("$inputType$"))
                .forEach(type -> type.setName(inputType));
    }

    public String getNameURL() {
        return nameURL;
    }

    public KiePMMLModel getKiePMMLModel() {
        return this.kiePMMLModel;
    }

    public PMMLRestResourceGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        return this;
    }

    public PMMLRestResourceGenerator withMonitoring(boolean useMonitoring) {
        this.useMonitoring = useMonitoring;
        return this;
    }

    public String className() {
        return resourceClazzName;
    }

    private void initializeApplicationField(FieldDeclaration fd) {
        fd.getVariable(0).setInitializer(new ObjectCreationExpr().setType(appCanonicalName));
    }

    private void interpolateStrings(StringLiteralExpr vv) {
        String s = vv.getValue();
        String documentation = "";
        String interpolated = s.replace("$nameURL$", nameURL)
                .replace("$modelName$", kiePMMLModel.getName());
        vv.setString(interpolated);
    }

    public String generatedFilePath() {
        return relativePath;
    }

    protected boolean useInjection() {
        return this.annotator != null;
    }
}