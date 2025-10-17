package org.kie.kogito.usertask.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UserTaskUtil {

    private final Properties properties;

    public UserTaskUtil() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            this.properties = new Properties();
            if (inputStream != null) {
                this.properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }
}
