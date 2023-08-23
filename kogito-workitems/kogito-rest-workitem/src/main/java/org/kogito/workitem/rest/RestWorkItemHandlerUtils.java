/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kogito.workitem.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.ConfigProvider;

import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.client.WebClientOptions;

import static org.kie.kogito.internal.utils.ConversionUtils.convert;

public class RestWorkItemHandlerUtils {

    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_FILE = "quarkus.http.ssl.certificate.file";
    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_KEY_FILE = "quarkus.http.ssl.certificate.key-file";
    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_FILE = "quarkus.http.ssl.certificate.key-store-file";
    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD = "quarkus.http.ssl.certificate.key-store-password";
    public static final String QUARKUS_HTTP_SSL_VERIFY_CLIENT = "quarkus.http.ssl.verify-client";
    public static final String QUARKUS_HTTP_SSL_TRUST_CERTIFICATE_FILE = "quarkus.http.ssl.trust-certificate-file";

    private RestWorkItemHandlerUtils() {
    }

    public static WebClientOptions sslWebClientOptions() {
        return new WebClientOptions().setSsl(true).setVerifyHost(false).setTrustAll(true);
    }

    public static WebClientOptions sslQuarkusWebClientOptions() {
        WebClientOptions webClientOptions = new WebClientOptions();

        String certificateFilePath = ConfigProvider.getConfig().getOptionalValue(QUARKUS_HTTP_SSL_CERTIFICATE_FILE, String.class).orElse(null);
        String keyFilePath = ConfigProvider.getConfig().getOptionalValue(QUARKUS_HTTP_SSL_CERTIFICATE_KEY_FILE, String.class).orElse(null);
        String keystorePath = ConfigProvider.getConfig().getOptionalValue(QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_FILE, String.class).orElse(null);
        String keystorePassword = ConfigProvider.getConfig().getOptionalValue(QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD, String.class).orElse(null);
        Boolean verifyClient = ConfigProvider.getConfig().getOptionalValue(QUARKUS_HTTP_SSL_VERIFY_CLIENT, Boolean.class).orElse(null);
        String trustCertFilePath = ConfigProvider.getConfig().getOptionalValue(QUARKUS_HTTP_SSL_TRUST_CERTIFICATE_FILE, String.class).orElse(null);

        if (keystorePath != null && keystorePassword != null) {
            webClientOptions.setSsl(true)
                    .setVerifyHost(verifyClient)
                    .setKeyStoreOptions(new JksOptions()
                            .setPath(keystorePath)
                            .setPassword(keystorePassword));
        }
        if (certificateFilePath != null && keyFilePath != null) {
            webClientOptions.setPemKeyCertOptions(new PemKeyCertOptions().setCertPath(certificateFilePath).setKeyPath(keyFilePath));
        }
        if (trustCertFilePath != null) {
            webClientOptions.setPemTrustOptions(new PemTrustOptions().addCertPath(trustCertFilePath));
        }

        return webClientOptions;
    }

    public static String getParam(Map<String, Object> parameters, String paramName) {
        return getParam(parameters, paramName, String.class, null);
    }

    public static <T> T getParam(Map<String, Object> parameters, String paramName, Class<T> type, T defaultValue) {
        Object value = parameters.remove(paramName);
        return value == null ? defaultValue : convert(value, type, v -> v.toString().toUpperCase());
    }

    public static <T> Collection<T> getClassListParam(Map<String, Object> parameters, String paramName, Class<T> clazz, Collection<T> defaultValue, Map<String, T> instances) {
        Object param = parameters.remove(paramName);
        if (param == null) {
            return defaultValue;
        } else {
            return param instanceof Collection ? ((Collection<?>) param).stream().filter(Objects::nonNull).map(p -> getClassParam(p, clazz, instances)).collect(Collectors.toList())
                    : Collections.singletonList(getClassParam(param, clazz, instances));
        }
    }

    private static <T> T getClassParam(Object param, Class<T> clazz, Map<String, T> instances) {
        //check if an instance of RestWorkItemHandlerBodyBuilder was set and just return it
        if (clazz.isAssignableFrom(param.getClass())) {
            return clazz.cast(param);
        }
        //in case of String, try to load an instance by the FQN of a RestWorkItemHandlerBodyBuilder
        else if (param instanceof String) {
            return instances.computeIfAbsent(param.toString(), k -> loadClass(k, clazz));
        }
        throw new IllegalArgumentException(param + " is not a valid instance of class " + clazz);
    }

    public static <T> T getClassParam(Map<String, Object> parameters, String paramName, Class<T> clazz, T defaultValue, Map<String, T> instances) {
        Object param = parameters.remove(paramName);
        return param == null ? defaultValue : getClassParam(param, clazz, instances);
    }

    private static <T> T loadClass(String className, Class<T> clazz) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className).asSubclass(clazz).getConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalArgumentException("Problem loading class " + className, e);
        }
    }
}
