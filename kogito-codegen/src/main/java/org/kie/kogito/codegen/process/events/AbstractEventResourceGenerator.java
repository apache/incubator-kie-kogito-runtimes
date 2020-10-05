package org.kie.kogito.codegen.process.events;

import com.github.javaparser.ast.CompilationUnit;
import org.kie.kogito.codegen.ApplicationGenerator;

import static com.github.javaparser.StaticJavaParser.parse;

abstract class AbstractEventResourceGenerator {

    abstract protected String getResourceTemplate();

    abstract protected String getClassName();

    public abstract String generate();

    /**
     * @return String with the full class name in path format like <code>org/my/ns/Class.java</code>
     */
    public final String generatedFilePath() {
        return String.format("%s/%s.java", ApplicationGenerator.DEFAULT_PACKAGE_NAME.replace(".", "/"), getClassName());
    }

    protected CompilationUnit parseTemplate() {
        return parse(this.getClass().getResourceAsStream(getResourceTemplate())).setPackageDeclaration(ApplicationGenerator.DEFAULT_PACKAGE_NAME);
    }
}
