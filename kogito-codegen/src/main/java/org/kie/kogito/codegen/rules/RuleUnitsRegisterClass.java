package org.kie.kogito.codegen.rules;

import java.util.Map;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.kie.kogito.rules.impl.RuleUnitRegistry;

import static org.kie.kogito.codegen.ApplicationGenerator.log;

public class RuleUnitsRegisterClass {

    private static final String RULE_UNIT_REGISTER_CLASS = "org.drools.project.model.RuleUnitRegister";
    private static final String RULE_UNIT_REGISTER_RESOURCE_CLASS = RULE_UNIT_REGISTER_CLASS.replace('.', '/') + ".class";
    static final String RULE_UNIT_REGISTER_SOURCE = "src/main/java/" + RULE_UNIT_REGISTER_CLASS.replace('.', '/') + ".java";

    private final Map<Class<?>, String> unitsMap;

    public RuleUnitsRegisterClass( Map<Class<?>, String> unitsMap) {
        this.unitsMap = unitsMap;
    }

    public void write(MemoryFileSystem srcMfs) {
        srcMfs.write(RULE_UNIT_REGISTER_SOURCE, log( generate() ).getBytes());
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "package org.drools.project.model;\n" +
                "\n" +
                "import static " + RuleUnitRegistry.class.getCanonicalName()  + ".register;\n" +
                "\n" +
                "public class RuleUnitRegister {\n" +
                "\n");
        sb.append("    static {\n");
        unitsMap.forEach( (k, v) -> sb.append( "        register(" + k.getCanonicalName() + ".class, " + v + "::new);\n") );
        sb.append("    }\n");
        sb.append("}" );
        return sb.toString();
    }
}
