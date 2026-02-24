package org.kie.kogito.auth.impl;

import org.kie.kogito.auth.AuthTokenProvider;

import java.util.Optional;

public class NoOpAuthTokenProvider implements AuthTokenProvider {
    @Override
    public Optional<String> getAuthToken() {
        return Optional.empty();
    }
}
