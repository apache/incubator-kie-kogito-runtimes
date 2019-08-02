package org.kie.kogito.codegen.process;

import java.util.NoSuchElementException;

import static com.github.javaparser.StaticJavaParser.parse;
import static org.kie.kogito.codegen.process.CodegenUtils.interpolateArguments;
import static org.kie.kogito.codegen.process.CodegenUtils.interpolateTypes;

import org.drools.core.util.StringUtils;
import org.jbpm.compiler.canonical.TriggerMetaData;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class MessageProducerGenerator {
    private final String relativePath;

    private WorkflowProcess process;
    private final String packageName;
    private final String resourceClazzName;
    private String processId;
    private final String processName;
    private DependencyInjectionAnnotator annotator;
    
    private TriggerMetaData trigger;
    
    public MessageProducerGenerator(WorkflowProcess process, TriggerMetaData trigger) {
        this.process = process;
        this.trigger = trigger;
        this.packageName = process.getPackageName();
        this.processId = process.getId();
        this.processName = processId.substring(processId.lastIndexOf('.') + 1);
        String classPrefix = StringUtils.capitalize(processName);
        this.resourceClazzName = classPrefix + "MessageProducer_" + trigger.getOwnerId();
        this.relativePath = packageName.replace(".", "/") + "/" + resourceClazzName + ".java";
    }

    public MessageProducerGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        return this;
    }

    public String className() {
        return resourceClazzName;
    }
    
    public String generatedFilePath() {
        return relativePath;
    }
    
    protected boolean useInjection() {
        return this.annotator != null;
    }
    
    public String generate() {
        String messageProducerTemplatePath = "/class-templates/MessageProducerTemplate.java";
        CompilationUnit clazz = parse(
                this.getClass().getResourceAsStream(messageProducerTemplatePath));
        clazz.setPackageDeclaration(process.getPackageName());

        ClassOrInterfaceDeclaration template = clazz
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new NoSuchElementException("Cannot find class in template " + messageProducerTemplatePath + "!"));
        template.setName(resourceClazzName);        
        
        template.findAll(ClassOrInterfaceType.class).forEach(cls -> interpolateTypes(cls, trigger.getDataType()));
        template.findAll(MethodDeclaration.class).stream().filter(md -> md.getNameAsString().equals("produce")).forEach(md -> interpolateArguments(md, trigger.getDataType()));
        
        if (useInjection()) {
            annotator.withApplicationComponent(template);
            
            final FieldDeclaration emitterField = template
                    .findFirst(FieldDeclaration.class)
                    .filter(fd -> fd.getVariable(0).getNameAsString().equals("emitter"))
                    .orElseThrow(() -> new NoSuchElementException("Cannot find field emitter in " + messageProducerTemplatePath + "!"));
            annotator.withInjection(emitterField);
            annotator.withOutgoingMessage(emitterField, trigger.getName());
            emitterField.getVariable(0).setType(annotator.emitterType(trigger.getDataType()));
            
            MethodDeclaration produceMethod = template
                    .findFirst(MethodDeclaration.class)
                    .filter(md -> md.getNameAsString().equals("produce"))
                    .orElseThrow(() -> new NoSuchElementException("Cannot find method produce in " + messageProducerTemplatePath + "!"));
            BlockStmt body = new BlockStmt();
            MethodCallExpr sendMethodCall = new MethodCallExpr(new NameExpr("emitter"), "send");
            annotator.withMessageProducer(sendMethodCall, trigger.getName(), "eventData");
            body.addStatement(sendMethodCall);
            produceMethod.setBody(body);
        } 
        return clazz.toString();
    }

}
