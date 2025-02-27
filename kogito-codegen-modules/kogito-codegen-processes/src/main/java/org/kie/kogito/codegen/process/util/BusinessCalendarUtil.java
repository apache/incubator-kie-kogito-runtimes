package org.kie.kogito.codegen.process.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.kie.kogito.calendar.BusinessCalendar;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.process.ProcessCodegenException;

import java.lang.reflect.Modifier;

public class BusinessCalendarUtil {

    private static final String BUSINESS_CALENDAR_FIELD_NAME = "businessCalendar";

    public static void processBusinessCalendarProducer(CompilationUnit compilationUnit, KogitoBuildContext context, String businessCalendarClassName) {
        if (businessCalendarClassName == null) {
            return;
        }
        validateBusinessCalendarClass(businessCalendarClassName, context);
        Expression expression = getBusinessCalendarCreation(businessCalendarClassName);

        BlockStmt constructorBody = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new ProcessCodegenException("BusinessCalendarProducer template does not contain a class declaration"))
                .getDefaultConstructor()
                .orElseThrow(() -> new ProcessCodegenException("BusinessCalendarProducer template does not contain a default constructor"))
                .getBody();

        AssignExpr calendarAssignment = getAssignExpr(constructorBody);

        calendarAssignment.setValue(expression);

    }

    public static void validateBusinessCalendarClass(String className, KogitoBuildContext context) {
        try {
            Class<? extends BusinessCalendar> businessCalendarClass = context.getClassLoader().loadClass(className).asSubclass(BusinessCalendar.class);
            int mod = businessCalendarClass.getModifiers();

            if (Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
                throw new ProcessCodegenException(String.format("Custom Business Calendar class %s must be a concrete class", className));
            }
        } catch (ClassNotFoundException e) {
            String message = String.format("Custom Business Calendar class %s not found or it is not an instance of %s", className, BusinessCalendar.class.getCanonicalName());
            throw new ProcessCodegenException(message, e);
        }
    }

    public static Expression getBusinessCalendarCreation(String businessCalendarClassName) {
        return new ObjectCreationExpr(null, StaticJavaParser.parseClassOrInterfaceType(businessCalendarClassName), NodeList.nodeList());
    }

    private static AssignExpr getAssignExpr(BlockStmt constructorBody) {
        Statement calendarFieldInitStatement = constructorBody.getStatements()
                .getFirst()
                .orElseThrow(() -> new ProcessCodegenException("BusinessCalendarProducer constructor does not contain any statements"));

        Node firstNode = calendarFieldInitStatement.getChildNodes().get(0);

        if (!(firstNode instanceof AssignExpr assignExpr)) {
            throw new ProcessCodegenException("BusinessCalendarProducer template does not contain an assign expression");
        }
        if (!(assignExpr.getTarget().isFieldAccessExpr() && BUSINESS_CALENDAR_FIELD_NAME.equals(assignExpr.getTarget().asFieldAccessExpr().getNameAsString()))) {
            throw new ProcessCodegenException("BusinessCalendarProducer template does not contain a assign expression for businessCalendar field");
        }

        return assignExpr;
    }
}
