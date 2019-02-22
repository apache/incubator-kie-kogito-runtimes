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

public class KieModuleModelClass {

    private Map<String, KieBaseModel> kBaseModels;

    public static final String KMODULE_MODEL_NAME = "kModuleModel";

    public KieModuleModelClass(Map<String, KieBaseModel> kBaseModels) {
        this.kBaseModels = kBaseModels;
    }

    public String toMethod() {
        MethodDeclaration methodDeclaration = createMethodDeclaration();

        BlockStmt stmt = new BlockStmt();

        stmt.addStatement(moduleModelInitialization());

        List<KieBaseModel> values = new ArrayList<>(kBaseModels.values());
        for (int i = 0; i < values.size(); i++) {
            KieBaseModel kieBaseModel = values.get(i);
            String kieBaseModelName = "kieBaseModel" + i;
            NameExpr kieBaseModelNameExpr = new NameExpr(kieBaseModelName);

            kieBaseModel(stmt, kieBaseModel, kieBaseModelName);
            kieBaseModelDefault(stmt, kieBaseModel, kieBaseModelNameExpr);
            eventProcessingType(stmt, kieBaseModel, kieBaseModelNameExpr);
            kieBaseModelPackages(stmt, kieBaseModel, kieBaseModelNameExpr);
            kieSession(stmt, kieBaseModel, kieBaseModelNameExpr);
        }

        stmt.addStatement(JavaParser.parseStatement("return " + KMODULE_MODEL_NAME + ";"));

        methodDeclaration.setBody(stmt);
        return methodDeclaration.toString();
    }

    private MethodDeclaration createMethodDeclaration() {
        return new MethodDeclaration(nodeList(publicModifier()), new ClassOrInterfaceType(null, KieModuleModel.class.getName()), "getKieModuleModel");
    }

    private void kieSession(BlockStmt stmt, KieBaseModel kieBaseModel, NameExpr kieBaseModelNameExpr) {
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

            if (kieSessionModel.isDefault()) {
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

    private void kieBaseModelPackages(BlockStmt stmt, KieBaseModel kieBaseModel, NameExpr kieBaseModelNameExpr) {
        List<String> packages = kieBaseModel.getPackages();
        for (String p : packages) {
            MethodCallExpr addPackage = new MethodCallExpr(kieBaseModelNameExpr, "addPackage", nodeList(new StringLiteralExpr(p)));
            stmt.addStatement(addPackage);
        }
    }

    private void eventProcessingType(BlockStmt stmt, KieBaseModel kieBaseModel, NameExpr kieBaseModelNameExpr) {
        String eventProcessingType = kieBaseModel.getEventProcessingMode().getClass().getCanonicalName() + "." + kieBaseModel.getEventProcessingMode().getMode().toUpperCase();
        FieldAccessExpr eventProcessingTypeEnum = JavaParser.parseExpression(eventProcessingType);
        stmt.addStatement(new MethodCallExpr(kieBaseModelNameExpr, "setEventProcessingMode", nodeList(eventProcessingTypeEnum)));
    }

    private void kieBaseModelDefault(BlockStmt stmt, KieBaseModel kieBaseModel, NameExpr kieBaseModelNameExpr) {
        if (kieBaseModel.isDefault()) {
            stmt.addStatement(new MethodCallExpr(kieBaseModelNameExpr, "setDefault", nodeList(new BooleanLiteralExpr(true))));
        }
    }

    private void kieBaseModel(BlockStmt stmt, KieBaseModel kieBaseModel, String kieBaseModelName) {
        MethodCallExpr newKieBaseModel = new MethodCallExpr(moduleModelNameExpr(), "newKieBaseModel", nodeList(new StringLiteralExpr(kieBaseModel.getName())));
        ClassOrInterfaceType kieBaseModelType = new ClassOrInterfaceType(null, KieBaseModel.class.getName());
        VariableDeclarationExpr kBaseVariable = new VariableDeclarationExpr(kieBaseModelType, kieBaseModelName);
        AssignExpr assignExpr = new AssignExpr(kBaseVariable, newKieBaseModel, AssignExpr.Operator.ASSIGN);
        stmt.addStatement(assignExpr);
    }

    private NameExpr moduleModelNameExpr() {
        return new NameExpr(KMODULE_MODEL_NAME);
    }

    private Statement moduleModelInitialization() {
        return JavaParser.parseStatement("KieModuleModel " +
                                                 KMODULE_MODEL_NAME +
                                                 " = KieServices.get().newKieModuleModel();");
    }
}
