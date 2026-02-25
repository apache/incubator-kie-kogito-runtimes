package org.kie.kogito.auth.impl;

import java.util.Optional;

import org.kie.kogito.auth.AuthTokenProvider;

public class NoOpAuthTokenProvider implements AuthTokenProvider {
    @Override
    public Optional<String> getAuthToken() {
        return Optional.empty();
    }
}
