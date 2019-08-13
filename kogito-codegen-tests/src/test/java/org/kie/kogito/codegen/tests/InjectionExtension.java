package org.kie.kogito.codegen.tests;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class InjectionExtension implements ParameterResolver {

    private SeContainer container;

    /**
     * boot the CDI context
     */

    public InjectionExtension() {

        container = SeContainerInitializer.newInstance().initialize();
    }

    /**
     * determines weather we can inject all the parameters specified in the test method
     */

    @Override

    public boolean supportsParameter(ParameterContext parameterContext,

                                     ExtensionContext extensionContext) throws ParameterResolutionException {

        Method method = (Method) parameterContext.getDeclaringExecutable();

        Class<?>[] types = method.getParameterTypes();

        return Arrays.stream(types).allMatch(type -> container.select(type).isResolvable());
    }

    /**
     * resolve the return the object to be used in the test method
     */

    @Override

    public Object resolveParameter(ParameterContext parameterContext,

                                   ExtensionContext extensionContext) throws ParameterResolutionException {

        int paramIndex = parameterContext.getIndex();

        Method method = (Method) parameterContext.getDeclaringExecutable();

        Parameter param = method.getParameters()[paramIndex];

        return container.select(param.getType()).get();
    }
}