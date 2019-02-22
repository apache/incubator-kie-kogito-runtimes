/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.modelcompiler.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.drools.javaparser.ast.Modifier.publicModifier;
import org.drools.compiler.commons.jci.compilers.CompilationResult;
import org.drools.compiler.compiler.io.File;
import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieBaseModelImpl;
import org.drools.core.util.Drools;
import org.drools.javaparser.JavaParser;
import org.drools.javaparser.ast.NodeList;
import org.drools.javaparser.ast.body.MethodDeclaration;
import org.drools.javaparser.ast.expr.AssignExpr;
import org.drools.javaparser.ast.expr.BooleanLiteralExpr;
import org.drools.javaparser.ast.expr.Expression;
import org.drools.javaparser.ast.expr.FieldAccessExpr;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.expr.StringLiteralExpr;
import org.drools.javaparser.ast.expr.VariableDeclarationExpr;
import org.drools.javaparser.ast.stmt.BlockStmt;
import org.drools.javaparser.ast.stmt.Statement;
import org.drools.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.javaparser.ast.type.Type;
import org.drools.model.Model;
import org.drools.modelcompiler.CanonicalKieModule;
import org.drools.modelcompiler.CanonicalKieModuleModel;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.jci.CompilationProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import static org.drools.javaparser.ast.NodeList.nodeList;
import static org.drools.modelcompiler.CanonicalKieModule.MODEL_FILE;
import static org.drools.modelcompiler.CanonicalKieModule.MODEL_VERSION;
import static org.drools.modelcompiler.CanonicalKieModule.createFromClassLoader;
import static org.drools.modelcompiler.builder.JavaParserCompiler.getCompiler;

public class CanonicalModelKieProject extends KieModuleKieProject {

    Logger logger = LoggerFactory.getLogger(CanonicalModelKieProject.class);

    public static final String PROJECT_MODEL_CLASS = "org.drools.project.model.ProjectModel";
    public static final String PROJECT_MODEL_RESOURCE_CLASS = PROJECT_MODEL_CLASS.replace( '.', '/' ) + ".class";
    protected static final String PROJECT_MODEL_SOURCE = "src/main/java/" + PROJECT_MODEL_CLASS.replace( '.', '/' ) + ".java";

    private final boolean isPattern;

    public static BiFunction<InternalKieModule, ClassLoader, KieModuleKieProject> create(boolean isPattern) {
        return (internalKieModule, classLoader) -> new CanonicalModelKieProject(isPattern, internalKieModule, classLoader);
    }

    protected List<ModelBuilderImpl> modelBuilders = new ArrayList<>();

    public CanonicalModelKieProject(boolean isPattern, InternalKieModule kieModule, ClassLoader classLoader) {
        super(kieModule instanceof CanonicalKieModule ? kieModule : createFromClassLoader( classLoader, kieModule ), classLoader);
        this.isPattern = isPattern;
    }

    @Override
    protected KnowledgeBuilder createKnowledgeBuilder(KieBaseModelImpl kBaseModel, InternalKieModule kModule) {
        ModelBuilderImpl modelBuilder = new ModelBuilderImpl(getBuilderConfiguration( kBaseModel, kModule ), isPattern);
        modelBuilders.add(modelBuilder);
        return modelBuilder;
    }

    @Override
    public void writeProjectOutput(MemoryFileSystem trgMfs, ResultsImpl messages) {
        MemoryFileSystem srcMfs = new MemoryFileSystem();
        ModelWriter modelWriter = new ModelWriter();
        List<String> modelFiles = new ArrayList<>();
        List<String> sourceFiles = new ArrayList<>();

        for (ModelBuilderImpl modelBuilder : modelBuilders) {
            final ModelWriter.Result result = modelWriter.writeModel( srcMfs, modelBuilder.getPackageModels() );
            modelFiles.addAll(result.getModelFiles());
            sourceFiles.addAll(result.getSources());
        }

        if (!sourceFiles.isEmpty()) {
            String[] sources = sourceFiles.toArray( new String[sourceFiles.size()+1] );

            String sourceClass = buildModelSourceClass(modelFiles);
            logger.debug(sourceClass);
            srcMfs.write(PROJECT_MODEL_SOURCE, sourceClass.getBytes());
            sources[sources.length-1] = PROJECT_MODEL_SOURCE;

            CompilationResult res = getCompiler().compile(sources, srcMfs, trgMfs, getClassLoader());

            Stream.of(res.getErrors()).collect(groupingBy( CompilationProblem::getFileName))
                    .forEach( (name, errors) -> {
                        errors.forEach( messages::addMessage );
                        File srcFile = srcMfs.getFile( name );
                        if ( srcFile instanceof MemoryFile ) {
                            String src = new String ( srcMfs.getFileContents( ( MemoryFile ) srcFile ) );
                            messages.addMessage( Message.Level.ERROR, name, "Java source of " + name + " in error:\n" + src);
                        }
                    } );

            for (CompilationProblem problem : res.getWarnings()) {
                messages.addMessage(problem);
            }
        } else {
            srcMfs.write(PROJECT_MODEL_SOURCE, buildModelSourceClass( modelFiles ).getBytes());
            CompilationResult res = getCompiler().compile(new String[] { PROJECT_MODEL_SOURCE }, srcMfs, trgMfs, getClassLoader());
            System.out.println(res.getErrors());
        }

        writeModelFile(modelFiles, trgMfs);
    }

    protected void writeModelFile( List<String> modelSources, MemoryFileSystem trgMfs) {
        String pkgNames = MODEL_VERSION + Drools.getFullVersion() + "\n";
        if(!modelSources.isEmpty()) {
            pkgNames += modelSources.stream().collect( Collectors.joining("\n"));
        }
        trgMfs.write( MODEL_FILE, pkgNames.getBytes() );
    }

    protected String buildModelSourceClass( List<String> modelSources ) {
        ReleaseId releaseId = getInternalKieModule().getReleaseId();

        StringBuilder sb = new StringBuilder();
        sb.append(
                "package org.drools.project.model;\n" +
                        "\n" +
                        "import " + Model.class.getCanonicalName()  + ";\n" +
                        "import " + CanonicalKieModuleModel.class.getCanonicalName()  + ";\n" +
                        "import " + ReleaseId.class.getCanonicalName()  + ";\n" +
                        "import " + ReleaseIdImpl.class.getCanonicalName()  + ";\n" +
                        "import " + KieModuleModel.class.getCanonicalName()  + ";\n" +
                        "import " + KieServices.class.getCanonicalName()  + ";\n" +
                        "\n" +
                        "public class ProjectModel implements CanonicalKieModuleModel {\n" +
                        "\n" +
                        "    public String getVersion() {\n" +
                        "        return \"" );
        sb.append( Drools.getFullVersion() );
        sb.append(
                "\";\n" +
                        "    }\n" +
                        "\n" +
                        "    public java.util.List<Model> getModels() {\n" +
                        "        return java.util.Arrays.asList(" );
        sb.append( modelSources.isEmpty() ? "" : modelSources.stream().collect( joining("(), new ", "new ", "()") ) );
        sb.append(
                ");\n" +
                        "    }\n" +
                        "\n" +
                        "    public ReleaseId getReleaseId() {\n" +
                        "        return new ReleaseIdImpl(\"" );
        sb.append( releaseId.getGroupId() ).append( "\", \"" );
        sb.append( releaseId.getArtifactId() ).append( "\", \"" );
        sb.append( releaseId.getVersion() ).append( "\"" );
        sb.append(
                ");\n" +
                        "    }\n");
        sb.append(generateKieModuleModelMethod());
        sb.append("}" );
        return sb.toString();
    }

    private String generateKieModuleModelMethod() {

        if(!kBaseModels.isEmpty()) {
            MethodDeclaration methodDeclaration = new MethodDeclaration(nodeList(publicModifier()), new ClassOrInterfaceType(null , KieModuleModel.class.getName()), "getKieModuleModel");

            BlockStmt stmt = new BlockStmt();

            Statement kProjReference = JavaParser.parseStatement("KieModuleModel kModuleModel = KieServices.get().newKieModuleModel();");
            stmt.addStatement(kProjReference);

            NameExpr kproj = new NameExpr("kModuleModel");

            List<KieBaseModel> values = new ArrayList<>(kBaseModels.values());
            for (int i = 0; i < values.size(); i++) {
                KieBaseModel kieBaseModel = values.get(i);
                String kieBaseModelName = "kieBaseModel" + i;
                NameExpr kieBaseModelNameExpr = new NameExpr(kieBaseModelName);

                MethodCallExpr newKieBaseModel = new MethodCallExpr(kproj, "newKieBaseModel", nodeList(new StringLiteralExpr(kieBaseModel.getName())));
                ClassOrInterfaceType kieBaseModelType = new ClassOrInterfaceType(null, KieBaseModel.class.getName());
                VariableDeclarationExpr kBaseVariable = new VariableDeclarationExpr(kieBaseModelType, kieBaseModelName);
                AssignExpr assignExpr = new AssignExpr(kBaseVariable, newKieBaseModel, AssignExpr.Operator.ASSIGN);
                stmt.addStatement(assignExpr);

                if(kieBaseModel.isDefault()) {
                    stmt.addStatement(new MethodCallExpr(kieBaseModelNameExpr, "setDefault", nodeList(new BooleanLiteralExpr(true))));
                }


                String eventProcessingType = kieBaseModel.getEventProcessingMode().getClass().getCanonicalName() + "." + kieBaseModel.getEventProcessingMode().getMode().toUpperCase();
                FieldAccessExpr eventProcessingTypeEnum = JavaParser.parseExpression(eventProcessingType);
                stmt.addStatement(new MethodCallExpr(kieBaseModelNameExpr, "setEventProcessingMode", nodeList(eventProcessingTypeEnum)));

                List<String> packages = kieBaseModel.getPackages();
                for(String p : packages) {
                    MethodCallExpr addPackage = new MethodCallExpr(kieBaseModelNameExpr, "addPackage", nodeList(new StringLiteralExpr(p)));
                    stmt.addStatement(addPackage);

                }

                List<KieSessionModel> values1 = new ArrayList<>(kieBaseModel.getKieSessionModels().values());
                for (int kieSessionModelIndex = 0; kieSessionModelIndex < values1.size(); kieSessionModelIndex++) {
                    KieSessionModel kieSessionModel = values1.get(kieSessionModelIndex);
                    String name = "kieSessionModel" + kieSessionModelIndex;
                    NameExpr nameExpr = new NameExpr(name);

                    ClassOrInterfaceType kieSessionModelType = new ClassOrInterfaceType(null, KieSessionModel.class.getName());
                    VariableDeclarationExpr kieSessionModelVariable = new VariableDeclarationExpr(kieSessionModelType, name);

                    MethodCallExpr newKieSessionModel = new MethodCallExpr(kieBaseModelNameExpr, "newKieSessionModel", nodeList(new StringLiteralExpr(kieSessionModel.getName())));
                    AssignExpr kieSessionModelAssignExpr = new AssignExpr(kieSessionModelVariable, newKieSessionModel, AssignExpr.Operator.ASSIGN);
                    stmt.addStatement(kieSessionModelAssignExpr);

                    if(kieSessionModel.isDefault()) {
                        stmt.addStatement(new MethodCallExpr(nameExpr, "setDefault", nodeList(new BooleanLiteralExpr(true))));
                    }

                    String sessionType = kieSessionModel.getType().getClass().getCanonicalName() + "." + kieSessionModel.getType().toString();
                    FieldAccessExpr sessionTypeEnum = JavaParser.parseExpression(sessionType);
                    stmt.addStatement(new MethodCallExpr(nameExpr, "setType", nodeList(sessionTypeEnum)));

                    NameExpr type = new NameExpr(kieSessionModel.getClockType().getClass().getCanonicalName());
                    MethodCallExpr clockTypeEnum = new MethodCallExpr(type, "get", nodeList(new StringLiteralExpr(kieSessionModel.getClockType().getClockType())));
                    stmt.addStatement(new MethodCallExpr(nameExpr, "setClockType", nodeList(clockTypeEnum)));

                }
            }

            stmt.addStatement(JavaParser.parseStatement("return kModuleModel;"));


            methodDeclaration.setBody(stmt);
            return methodDeclaration.toString();
        } else {

            return "";
        }

    }
}