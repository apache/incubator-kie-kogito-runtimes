package org.kie.kogito.quarkus.deployment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.kie.kogito.codegen.ApplicationGenerator;
import org.kie.kogito.codegen.Generator;
import org.kie.kogito.codegen.process.ProcessCodegen;

public class ProcessCompilationProvider extends KogitoCompilationProvider {

    @Override
    public Set<String> handledExtensions() {
        return ProcessCodegen.SUPPORTED_BPMN_EXTENSIONS;
    }

    @Override
    protected Generator addGenerator(ApplicationGenerator appGen, Set<File> filesToCompile, Context context)
        throws IOException {
        return appGen.withGenerator(
            ProcessCodegen.ofFiles(new ArrayList<>(filesToCompile)))
            .withClassLoader(Thread.currentThread().getContextClassLoader());
    }
}
