package org.drools.modelcompiler.builder;

import java.util.Collection;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.util.Drools;
import org.drools.model.Model;
import org.kie.api.builder.ReleaseId;

import static java.util.stream.Collectors.joining;

import static org.drools.modelcompiler.builder.PackageModel.log;

public class ModelSourceClass {

    private final Collection<String> modelSources;
    private final KieModuleModelMethod modelMethod;
    private final ReleaseId releaseId;

    public ModelSourceClass(
            ReleaseId releaseId,
            KieModuleModelMethod modelMethod,
            Collection<String> modelSources) {
        this.releaseId = releaseId;
        this.modelSources = modelSources;
        this.modelMethod = modelMethod;
    }

    public void write(MemoryFileSystem srcMfs) {
        srcMfs.write(getName(), log(generate() ).getBytes());
    }

    public String getName() {
        return CanonicalModelKieProject.PROJECT_MODEL_SOURCE;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "package org.drools.project.model;\n" +
                        "\n" +
                        "import " + Model.class.getCanonicalName()  + ";\n" +
                        "import " + ReleaseId.class.getCanonicalName()  + ";\n" +
                        "import " + ReleaseIdImpl.class.getCanonicalName()  + ";\n" +
                        "\n" +
                        "public class ProjectModel implements org.drools.modelcompiler.CanonicalKieModuleModel {\n" +
                        "\n");
        sb.append(
                "    @Override\n" +
                        "    public String getVersion() {\n" +
                        "        return \"" );
        sb.append( Drools.getFullVersion() );
        sb.append(
                "\";\n" +
                        "    }\n" +
                        "\n" +
                        "    @Override\n" +
                        "    public java.util.List<Model> getModels() {\n" +
                        "        return java.util.Arrays.asList(" );
        sb.append( modelSources.isEmpty() ? "" : modelSources.stream().collect( joining("(), new ", "new ", "()") ) );
        sb.append(
                ");\n" +
                        "    }\n" +
                        "\n" +
                        "    @Override\n" +
                        "    public ReleaseId getReleaseId() {\n" +
                        "        return new ReleaseIdImpl(\"" );
        sb.append( releaseId.getGroupId() ).append( "\", \"" );
        sb.append( releaseId.getArtifactId() ).append( "\", \"" );
        sb.append( releaseId.getVersion() ).append( "\"" );
        sb.append(
                ");\n" +
                        "    }\n");
        sb.append("\n");
        sb.append(modelMethod.toGetKieModuleModelMethod());
        sb.append("\n}" );
        return sb.toString();
    }
}
