package org.jbpm.serverless.workflow.utils;


import org.jbpm.serverless.workflow.api.mapper.JsonObjectMapper;
import org.jbpm.serverless.workflow.api.mapper.YamlObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class WorkflowTestUtils {
    private static JsonObjectMapper jsonObjectMapper = new JsonObjectMapper();
    private static YamlObjectMapper yamlObjectMapper = new YamlObjectMapper();

    public static final Path resourceDirectory = Paths.get("src",
            "test",
            "resources");
    public static final String absolutePath = resourceDirectory.toFile().getAbsolutePath();

    public static Path getResourcePath(String file) {
        return Paths.get(absolutePath + File.separator + file);
    }

    public static InputStream getInputStreamFromPath(Path path) throws Exception {
        return Files.newInputStream(path);
    }

    public static String readWorkflowFile(String location) {
        return readFileAsString(classpathResourceReader(location));
    }

    public static Reader classpathResourceReader(String location) {
        return new InputStreamReader(WorkflowTestUtils.class.getResourceAsStream(location));
    }

    public static String readFileAsString(Reader reader) {
        try {
            StringBuilder fileData = new StringBuilder(1000);
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf,
                        0,
                        numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
            return fileData.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
