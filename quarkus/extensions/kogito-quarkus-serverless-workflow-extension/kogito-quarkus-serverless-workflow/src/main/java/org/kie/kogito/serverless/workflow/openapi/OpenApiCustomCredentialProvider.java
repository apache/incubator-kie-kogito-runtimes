package org.kie.kogito.serverless.workflow.openapi;

import java.util.Collections;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Specializes;
import jakarta.ws.rs.client.ClientRequestContext;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkiverse.openapi.generator.providers.ConfigCredentialsProvider;
import io.quarkus.arc.Arc;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClientConfig;
import io.quarkus.oidc.client.OidcClientException;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;
import io.quarkus.runtime.configuration.ConfigurationException;




@Dependent
@Alternative
@Specializes
@Priority(10)
public class OpenApiCustomCredentialProvider extends ConfigCredentialsProvider {
  private static final String CANONICAL_EXCHANGE_TOKEN_PROPERTY_NAME = "sonataflow.security.%s.exchange-token";

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiCustomCredentialProvider.class);


  @Override
  public String getOauth2BearerToken(CredentialsContext input) {
    String accessToken = super.getOauth2BearerToken(input);;
    Optional<Boolean> exchangeToken = ConfigProvider.getConfig().getOptionalValue(getCanonicalExchangeTokenConfigPropertyName(input.getAuthName()), Boolean.class);

    if (exchangeToken.isPresent() && exchangeToken.get()) {
      LOGGER.info("Oauth2 token exchange enabled for {}, will generate a tokens...", input.getAuthName());
      OidcClients clients = Arc.container().instance(OidcClients.class).get();
      OidcClient exchangeTokenClient = clients.getClient();
      OidcClientConfig.Grant.Type exchangeTokenGrantType = ConfigProvider.getConfig().getValue("quarkus.oidc-client." + input.getAuthName() + "grant.type", OidcClientConfig.Grant.Type.class);
      String exchangeTokenProperty;

      if (exchangeTokenGrantType == OidcClientConfig.Grant.Type.EXCHANGE) {
        exchangeTokenProperty = "subject_token";
      } else if (exchangeTokenGrantType == OidcClientConfig.Grant.Type.JWT) {
        exchangeTokenProperty = "assertion";
      } else {
        throw new ConfigurationException("Token exchange is required but OIDC client is configured " + "to use the " + exchangeTokenGrantType.getGrantType() + " grantType");
      }
      accessToken = exchangeTokenIfNeeded(accessToken, exchangeTokenClient, exchangeTokenProperty);
    }
    return accessToken;
  }

  private String exchangeTokenIfNeeded(String token, OidcClient exchangeTokenClient, String exchangeTokenProperty) {
    if (exchangeTokenClient != null) {
      Tokens tokens;
      try {
        tokens = exchangeTokenClient.getTokens(Collections.singletonMap(exchangeTokenProperty, token)).await().indefinitely();
        //TODO store the refresh token in an expiring cache
        //TODO store the access token in an expiring cache
        //TODO cache should expire before access/refresh token expire so they can be refreshed before (need to decode the JWT claim)
        return tokens.getAccessToken();
      } catch (OidcClientException e) {
        // TODO try to refresh the access token with the cached refresh token
        LOGGER.error("Error while exchanging oauth2 token", e);
      }
    }
    return token;
  }

  public static String getCanonicalExchangeTokenConfigPropertyName(String authName) {
    return String.format(CANONICAL_EXCHANGE_TOKEN_PROPERTY_NAME, authName);
  }
}
