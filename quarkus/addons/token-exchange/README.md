<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->
# KIE Add-On Token Exchange

This add-on provides OAuth2 token exchange functionality for Kogito Quarkus applications with caching and database persistence.

## Overview

The token-exchange add-on implements OAuth2 Token Exchange functionality, allowing applications to exchange one token for another. This is useful for scenarios where you need to:

- Exchange an access token for a token with different scope or audience
- Impersonate users or services
- Implement token chaining in microservice architectures

The addon includes:
- **Caffeine-based caching** for token storage with per-token expiration
- **Database persistence** for token durability
- **Proactive token refresh** to minimize authentication delays
- **OpenAPI integration** for automated credential management

## Usage

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>kie-addons-quarkus-token-exchange</artifactId>
</dependency>
```

## Components

### Runtime Module
- **OpenApiCustomCredentialProvider**: Main credential provider with token exchange and caching
- **Cache**:
  - `CachedTokens`: Token wrapper with expiration tracking
  - `TokenPolicyManager`: Expiry policy for per-token cache management
  - `TokenEvictionHandler`: Handles cache eviction and proactive refresh
- **Persistence**:
  - `DatabaseTokenDataStore`: Database-backed token storage
  - `TokenCacheRepository`: Repository interface for token CRUD operations
  - `TokenCacheRecord`: JPA entity for token storage
- **Utilities**:
  - `OidcClientUtils`: OIDC client utilities for token exchange
  - `CacheUtils`: Cache key management utilities
  - `ConfigReaderUtils`: Configuration reading utilities
  - `JwtTokenUtils`: JWT token parsing utilities

### Deployment Module
- **TokenExchangeProcessor**: Quarkus deployment processor for extension configuration

## Configuration

Configure token exchange using properties like:
```properties
sonataflow.security.{authName}.token-exchange.enabled=true
sonataflow.security.{authName}.token-exchange.proactive-refresh-seconds=300
```

## License

Licensed under the Apache License, Version 2.0. 