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

package org.kie.kogito.codegen.rules;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import org.drools.compiler.lang.descr.QueryDescr;
import org.kie.kogito.codegen.FileGenerator;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;

import static com.github.javaparser.StaticJavaParser.parse;
import static org.drools.modelcompiler.builder.generator.DrlxParseUtil.classToReferenceType;

public class QueryEndpointSourceClass implements FileGenerator {

    private final Class<?> ruleUnit;
    private final QueryDescr query;
    private final DependencyInjectionAnnotator annotator;

    private final String name;
    private final String targetCanonicalName;
    private final String generatedFilePath;

    public QueryEndpointSourceClass( Class<?> ruleUnit, QueryDescr query, DependencyInjectionAnnotator annotator ) {
        this.ruleUnit = ruleUnit;
        this.query = query;
        this.name = toCamelCase(query.getName());
        this.annotator = annotator;

        this.targetCanonicalName = ruleUnit.getSimpleName() + "Query" + name + "Endpoint";
        this.generatedFilePath = (query.getNamespace() + "." + targetCanonicalName).replace('.', '/') + ".java";
    }

    @Override
    public String generatedFilePath() {
        return generatedFilePath;
    }

    @Override
    public String generate() {
        CompilationUnit cu = parse(
                this.getClass().getResourceAsStream("/class-templates/RestQueryTemplate.java"));
        cu.setPackageDeclaration(query.getNamespace());

        ClassOrInterfaceDeclaration clazz =
                cu.findFirst(ClassOrInterfaceDeclaration.class).get();

        clazz.setName( targetCanonicalName );

        cu.findAll(StringLiteralExpr.class).forEach(this::interpolateStrings);

        FieldDeclaration ruleUnitDeclaration = clazz.getFieldByName( "ruleUnit" ).get();
        setUnitGeneric( ruleUnitDeclaration.getElementType() );
        if (annotator != null) {
            annotator.withInjection( ruleUnitDeclaration );
        }

        for (ConstructorDeclaration c : clazz.getConstructors()) {
            c.setName( targetCanonicalName );
            if (c.getParameters().size() > 0) {
                setUnitGeneric( c.getParameter( 0 ).getType() );
            }
        }

        MethodDeclaration queryMethod = clazz.getMethodsByName( "executeQuery" ).get(0);
        queryMethod.getParameter( 0 ).setType(ruleUnit);

        Statement statement = queryMethod.getBody().get().getStatement( 0 );
        statement.findAll( VariableDeclarator.class ).forEach( decl -> setUnitGeneric( decl.getType() ) );

        return cu.toString();
    }

    private void setUnitGeneric(Type type) {
        type.asClassOrInterfaceType().setTypeArguments( classToReferenceType( ruleUnit ) );
    }

    private void interpolateStrings(StringLiteralExpr vv) {
        String interpolated = vv.getValue()
                .replace("$name$", name)
                .replace("$queryName$", query.getName());
        vv.setString(interpolated);
    }

    private static String toCamelCase(String inputString) {
        return Stream.of(inputString.split(" "))
                .map( s -> s.length() > 1 ? s.substring( 0, 1 ).toUpperCase() + s.substring( 1 ) : s.substring( 0, 1 ).toUpperCase() )
                .collect( Collectors.joining() );
    }
}
