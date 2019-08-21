package org.kie.kogito.codegen.annotations.processor;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.kie.kogito.codegen.ApplicationGenerator;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.di.CDIDependencyInjectionAnnotator;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegen;

@SupportedAnnotationTypes("org.kie.kogito.codegen.annotations.KogitoTest")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class KogitoCodegenProcessor extends AbstractProcessor {

    boolean generationCompleted = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (generationCompleted) {
            return false;
        }
        generationCompleted = true;

        try {
            Path projectPath = inferProjectPath();

            ApplicationGenerator appGen =
                    new ApplicationGenerator(ApplicationGenerator.DEFAULT_PACKAGE_NAME, projectPath.toFile())
                            .withDependencyInjection(null);

            Path resources = projectPath.resolve("src/main/resources");


            appGen.withGenerator(IncrementalRuleCodegen.ofPath(resources));
            appGen.withGenerator(ProcessCodegen.ofPath(resources));

            Collection<GeneratedFile> generate = appGen.generate();
            for (GeneratedFile generatedFile : generate) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, generatedFile.relativePath());
                JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(toJavaName(generatedFile.relativePath()));
                Writer writer = sourceFile.openWriter();
                writer.write(new String(generatedFile.contents(), StandardCharsets.UTF_8));
                writer.close();
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        return true;
    }

    private Path inferProjectPath() throws IOException {
        Filer filer = processingEnv.getFiler();
        FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "tmp");
        Path projectPath = Paths.get(resource.toUri()).getParent().getParent().getParent();
        resource.delete();

        return projectPath;
    }

    public String toJavaName(String fileName) {
        return fileName.replace("/", ".").replace(".java", "");
    }
}