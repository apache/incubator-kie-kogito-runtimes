package org.kie.kogito.spring.auth;

import java.util.List;
import java.util.Optional;

import org.kie.kogito.auth.AuthTokenProvider;
import org.kie.kogito.spring.auth.token.AuthTokenReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass({ SecurityContextHolder.class })
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SpringAuthTokenProvider implements AuthTokenProvider {

    private final List<AuthTokenReader> authTokenReaders;

    public SpringAuthTokenProvider(@Autowired List<AuthTokenReader> authTokenReaders) {
        this.authTokenReaders = authTokenReaders;
    }

    @Override
    public Optional<String> getAuthToken() {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext == null || securityContext.getAuthentication() == null) {
            return Optional.empty();
        }

        Object principal = securityContext.getAuthentication().getPrincipal();

        return this.authTokenReaders.stream()
                .filter(reader -> reader.acceptsPrincipal(principal)).findFirst()
                .map(reader -> reader.readToken(principal));
    }
}
