package org.drools.modelcompiler.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.javaparser.JavaParser;
import org.drools.javaparser.ast.body.MethodDeclaration;
import org.drools.javaparser.ast.expr.AssignExpr;
import org.drools.javaparser.ast.expr.BooleanLiteralExpr;
import org.drools.javaparser.ast.expr.FieldAccessExpr;
import org.drools.javaparser.ast.expr.MethodCallExpr;
import org.drools.javaparser.ast.expr.NameExpr;
import org.drools.javaparser.ast.expr.StringLiteralExpr;
import org.drools.javaparser.ast.expr.VariableDeclarationExpr;
import org.drools.javaparser.ast.stmt.BlockStmt;
import org.drools.javaparser.ast.stmt.Statement;
import org.drools.javaparser.ast.type.ClassOrInterfaceType;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;

import static org.drools.javaparser.ast.Modifier.publicModifier;
import static org.drools.javaparser.ast.NodeList.nodeList;

public class KieModuleModelMethod {

    private Map<String, KieBaseModel> kBaseModels;

    public static final String KMODULE_MODEL_NAME = "kModuleModel";

    private MethodDeclaration methodDeclaration = createMethodDeclaration();
    private BlockStmt stmt = new BlockStmt();

    public KieModuleModelMethod(Map<String, KieBaseModel> kBaseModels) {
        this.kBaseModels = kBaseModels;
    }

    public String toMethod() {

        stmt.addStatement(moduleModelInitialization());

        List<KieBaseModel> values = new ArrayList<>(kBaseModels.values());
        for (int i = 0; i < values.size(); i++) {
            KieBaseModel kieBaseModel = values.get(i);
            KieBaseModelGenerator kieBaseModelGenerator = new KieBaseModelGenerator(kieBaseModel, i);
            kieBaseModelGenerator.initStatement();

            kieBaseModelPackages(kieBaseModel, kieBaseModelGenerator.getKieBaseModelNameExpr());
            kieSession(kieBaseModel, kieBaseModelGenerator.getKieBaseModelNameExpr());
        }

        stmt.addStatement(JavaParser.parseStatement("return " + KMODULE_MODEL_NAME + ";"));

        methodDeclaration.setBody(stmt);
        return methodDeclaration.toString();
    }

    class KieBaseModelGenerator {

        private KieBaseModel kieBaseModel;
        private String kieBaseModelName;
        private NameExpr kieBaseModelNameExpr;

        KieBaseModelGenerator(KieBaseModel kieBaseModel, int index) {
            this.kieBaseModel = kieBaseModel;
            this.kieBaseModelName = "kieBaseModel" + index;

            kieBaseModelNameExpr = new NameExpr(kieBaseModelName);
        }

        void initStatement() {
            stmt.addStatement(newInstance(KieBaseModel.class.getName(), kieBaseModelName, moduleModelNameExpr(), "newKieBaseModel", kieBaseModel.getName()));

            kieBaseModelDefault();
            eventProcessingType();
        }

        private void kieBaseModelDefault() {
            if (kieBaseModel.isDefault()) {
                stmt.addStatement(new MethodCallExpr(kieBaseModelNameExpr, "setDefault", nodeList(new BooleanLiteralExpr(true))));
            }
        }

        private void eventProcessingType() {
            createEnum(kieBaseModelNameExpr, kieBaseModel.getEventProcessingMode().getClass().getCanonicalName(), kieBaseModel.getEventProcessingMode().getMode().toUpperCase(), "setEventProcessingMode");
        }

        NameExpr getKieBaseModelNameExpr() {
            return kieBaseModelNameExpr;
        }
    }

    private MethodDeclaration createMethodDeclaration() {
        return new MethodDeclaration(nodeList(publicModifier()), new ClassOrInterfaceType(null, KieModuleModel.class.getName()), "getKieModuleModel");
    }

    private void kieSession(KieBaseModel kieBaseModel, NameExpr kieBaseModelNameExpr) {
        List<KieSessionModel> values1 = new ArrayList<>(kieBaseModel.getKieSessionModels().values());
        for (int kieSessionModelIndex = 0; kieSessionModelIndex < values1.size(); kieSessionModelIndex++) {
            KieSessionModel kieSessionModel = values1.get(kieSessionModelIndex);
            String name = "kieSessionModel" + kieSessionModelIndex;
            NameExpr nameExpr = new NameExpr(name);

            stmt.addStatement(newInstance(KieSessionModel.class.getName(), name, kieBaseModelNameExpr, "newKieSessionModel", kieSessionModel.getName()));

            if (kieSessionModel.isDefault()) {
                stmt.addStatement(new MethodCallExpr(nameExpr, "setDefault", nodeList(new BooleanLiteralExpr(true))));
            }

            createEnum(nameExpr, kieSessionModel.getType().getClass().getCanonicalName(), kieSessionModel.getType().toString(), "setType");

            NameExpr type = new NameExpr(kieSessionModel.getClockType().getClass().getCanonicalName());
            MethodCallExpr clockTypeEnum = new MethodCallExpr(type, "get", nodeList(new StringLiteralExpr(kieSessionModel.getClockType().getClockType())));
            stmt.addStatement(new MethodCallExpr(nameExpr, "setClockType", nodeList(clockTypeEnum)));
        }
    }

    private void createEnum(NameExpr nameExpr, String enumType, String enumName, String enumSetter) {
        String sessionType = enumType + "." + enumName;
        FieldAccessExpr sessionTypeEnum = JavaParser.parseExpression(sessionType);
        stmt.addStatement(new MethodCallExpr(nameExpr, enumSetter, nodeList(sessionTypeEnum)));
    }

    private void kieBaseModelPackages(KieBaseModel kieBaseModel, NameExpr kieBaseModelNameExpr) {
        List<String> packages = kieBaseModel.getPackages();
        for (String p : packages) {
            MethodCallExpr addPackage = new MethodCallExpr(kieBaseModelNameExpr, "addPackage", nodeList(new StringLiteralExpr(p)));
            stmt.addStatement(addPackage);
        }
    }

    private NameExpr moduleModelNameExpr() {
        return new NameExpr(KMODULE_MODEL_NAME);
    }

    private Statement moduleModelInitialization() {
        return JavaParser.parseStatement("KieModuleModel " +
                                                 KMODULE_MODEL_NAME +
                                                 " = KieServices.get().newKieModuleModel();");
    }

    AssignExpr newInstance(String type, String variableName, NameExpr scope, String methodName, String parameter) {
        MethodCallExpr initMethod = new MethodCallExpr(scope, methodName, nodeList(new StringLiteralExpr(parameter)));
        VariableDeclarationExpr var = new VariableDeclarationExpr(new ClassOrInterfaceType(null, type), variableName);
        return new AssignExpr(var, initMethod, AssignExpr.Operator.ASSIGN);
    }
}
