/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.quarkus.runtime;

import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.client.WebClientOptions;

public class SSLWebClientOptionsUtils {

    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_FILE = "quarkus.http.ssl.certificate.file";
    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_KEY_FILE = "quarkus.http.ssl.certificate.key-file";
    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_FILE = "quarkus.http.ssl.certificate.key-store-file";
    public static final String QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD = "quarkus.http.ssl.certificate.key-store-password";
    public static final String QUARKUS_HTTP_SSL_VERIFY_CLIENT = "quarkus.http.ssl.verify-client";
    public static final String QUARKUS_HTTP_SSL_TRUST_CERTIFICATE_FILE = "quarkus.http.ssl.trust-certificate-file";

    public static WebClientOptions sslQuarkusWebClientOptions() {
        WebClientOptions webClientOptions = new WebClientOptions();

        Config config = ConfigProvider.getConfig();

        getOptionalStringValue(config, QUARKUS_HTTP_SSL_CERTIFICATE_FILE)
                .ifPresent(certificateFilePath -> {
                    getOptionalStringValue(config, QUARKUS_HTTP_SSL_CERTIFICATE_KEY_FILE)
                            .ifPresent(keyFilePath -> {
                                webClientOptions.setPemKeyCertOptions(new PemKeyCertOptions()
                                        .setCertPath(certificateFilePath)
                                        .setKeyPath(keyFilePath));
                            });
                });

        getOptionalStringValue(config, QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_FILE)
                .ifPresent(keystorePath -> {
                    getOptionalStringValue(config, QUARKUS_HTTP_SSL_CERTIFICATE_KEY_STORE_PASSWORD)
                            .ifPresent(keystorePassword -> {
                                Boolean verifyClient = getOptionalBooleanValue(config, QUARKUS_HTTP_SSL_VERIFY_CLIENT).orElse(false);

                                webClientOptions.setSsl(true)
                                        .setTrustAll(false)
                                        .setVerifyHost(verifyClient)
                                        .setTrustStoreOptions(new JksOptions()
                                                .setPath(keystorePath)
                                                .setPassword(keystorePassword));
                            });
                });

        getOptionalStringValue(config, QUARKUS_HTTP_SSL_TRUST_CERTIFICATE_FILE)
                .ifPresent(trustCertFilePath -> {
                    webClientOptions.setPemTrustOptions(new PemTrustOptions()
                            .addCertPath(trustCertFilePath));
                });

        return webClientOptions;
    }

    private static Optional<String> getOptionalStringValue(Config config, String key) {
        return config.getOptionalValue(key, String.class);
    }

    private static Optional<Boolean> getOptionalBooleanValue(Config config, String key) {
        return config.getOptionalValue(key, Boolean.class);
    }
}
