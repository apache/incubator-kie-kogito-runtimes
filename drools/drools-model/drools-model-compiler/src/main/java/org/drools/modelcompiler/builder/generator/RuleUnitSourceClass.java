package org.drools.modelcompiler.builder.generator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.kie.submarine.rules.RuleUnit;
import org.kie.submarine.rules.impl.AbstractRuleUnit;

import static com.github.javaparser.ast.NodeList.nodeList;

public class RuleUnitSourceClass {

    private final String packageName;
    private final String typeName;
    private final String generatedSourceFile;
    private final String sourceFilePath;
    private final String completePath;
    private final String canonicalName;
    private final String targetCanonicalName;
    private String targetTypeName;
    private boolean hasCdi;

    public RuleUnitSourceClass(String packageName, String typeName, String generatedSourceFile) {
        this.packageName = packageName;
        this.typeName = typeName;
        this.generatedSourceFile = generatedSourceFile;
        this.canonicalName = packageName + "." + typeName;
        this.targetTypeName = typeName + "RuleUnit";
        this.targetCanonicalName = packageName + "." + targetTypeName;
        this.sourceFilePath = targetCanonicalName.replace('.', '/') + ".java";
        this.completePath = "src/main/java/" + sourceFilePath;
    }

    public String targetCanonicalName() {
        return targetCanonicalName;
    }

    public String targetTypeName() {
        return targetTypeName;
    }

    public void write(MemoryFileSystem srcMfs) {
        srcMfs.write(completePath, generate().getBytes());
    }

    public String generate() {
        return compilationUnit().toString();
    }

    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = new CompilationUnit(packageName);
        compilationUnit.getTypes().add(classDeclaration());
        return compilationUnit;
    }

    private MethodDeclaration createInstanceMethod( String ruleUnitInstanceFQCN) {
        MethodDeclaration methodDeclaration = new MethodDeclaration();

        ReturnStmt returnStmt = new ReturnStmt(
                new ObjectCreationExpr()
                        .setType(ruleUnitInstanceFQCN)
                        .setArguments(nodeList(
                                new ThisExpr(),
                                new NameExpr("value"),
                                newKieSession())));

        methodDeclaration.setName("createInstance")
                .addModifier( Modifier.Keyword.PUBLIC)
                .addParameter(canonicalName, "value")
                .setType(ruleUnitInstanceFQCN)
                .setBody(new BlockStmt()
                                 .addStatement(returnStmt));
        return methodDeclaration;
    }

    private MethodCallExpr newKieSession() {
        /*
        KieBaseBuilder.createKieBaseFromModel(
                asList(new $GENERATED_RULE_ID(), ...))
                .newKieSession();
         */
        MethodCallExpr createKieBaseFromModel = createKieBaseFromModel();
        return new MethodCallExpr(createKieBaseFromModel, "newKieSession");
    }

    private MethodCallExpr createKieBaseFromModel() {
        MethodCallExpr args = args();

        return new MethodCallExpr(
                new NameExpr("org.drools.modelcompiler.builder.KieBaseBuilder"),
                "createKieBaseFromModel").addArgument(args);
    }

    private MethodCallExpr args() {
        return new MethodCallExpr(
                new NameExpr("java.util.Collections"),
                "singletonList").addArgument(new ObjectCreationExpr().setType(generatedSourceFile));
    }

    public static ClassOrInterfaceType ruleUnitType( String canonicalName) {
        return new ClassOrInterfaceType(null, RuleUnit.class.getCanonicalName())
                .setTypeArguments(new ClassOrInterfaceType(null, canonicalName));
    }

    public static ClassOrInterfaceType abstractRuleUnitType(String canonicalName) {
        return new ClassOrInterfaceType(null, AbstractRuleUnit.class.getCanonicalName())
                .setTypeArguments(new ClassOrInterfaceType(null, canonicalName));
    }

    public ClassOrInterfaceDeclaration classDeclaration() {
        ClassOrInterfaceDeclaration cls = new ClassOrInterfaceDeclaration()
                .setName(targetTypeName)
                .setModifiers(Modifier.Keyword.PUBLIC);
        cls.addAnnotation("javax.inject.Singleton");
        String ruleUnitInstanceFQCN = RuleUnitInstanceSourceClass.qualifiedName(packageName, typeName);

        MethodDeclaration methodDeclaration = createInstanceMethod(ruleUnitInstanceFQCN);
        cls.addExtendedType(abstractRuleUnitType(canonicalName))
                .addMember(methodDeclaration);
        return cls;
    }

    public RuleUnitSourceClass withCdi(boolean hasCdi) {
        this.hasCdi = hasCdi;
        return this;
    }
}
