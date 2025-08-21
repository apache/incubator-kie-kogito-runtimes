package org.kie.kogito.codegen.process.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kie.kogito.codegen.process.util.CodegenUtil.*;
import static org.kie.kogito.codegen.api.context.ContextAttributesConstants.KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR;

public class CodegenUtilTest {

    private KogitoBuildContext context;

    @BeforeEach
    public void setup() {
        this.context = QuarkusKogitoBuildContext.builder().build();
    }

    @Test
    public void testGetBooleanObjectAccessor() {
        assertEquals("is", getBooleanObjectAccessor(context));

        context.setApplicationProperty(KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR, "javaBeans");
        assertEquals("get", getBooleanObjectAccessor(context));  

        context.setApplicationProperty(KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR, "isPrefix");
        assertEquals("is", getBooleanObjectAccessor(context));  

        context.setApplicationProperty(KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR, "get1");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {getBooleanObjectAccessor(context);});  
        assertEquals("Property " + KOGITO_CODEGEN_BOOLEAN_OBJECT_ACCESSOR_BEHAVIOUR + " defined but does not contain proper value: expected 'isPrefix' or 'javaBeans'", exception.getMessage());
    }
}
