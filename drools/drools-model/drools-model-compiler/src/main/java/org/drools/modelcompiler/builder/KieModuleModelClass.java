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

    public KieModuleModelClass(Map<String, KieBaseModel> kBaseModels) {
        this.kBaseModels = kBaseModels;
    }

    public String toMethod() {

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
