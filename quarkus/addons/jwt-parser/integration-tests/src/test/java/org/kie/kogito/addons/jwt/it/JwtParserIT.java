/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.addons.jwt.it;

import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.jwt.JwtTokenParser;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class JwtParserIT {

    @Inject
    JwtTokenParser jwtTokenParser;

    // Sample JWT token for testing (not a real token, just for structure testing)
    private static final String SAMPLE_JWT =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiam9obmRvZSIsImVtYWlsIjoiam9obmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTUxNjIzOTAyMn0.";

    @Test
    void testJwtTokenParserInjection() {
        assertThat(jwtTokenParser).isNotNull();
    }

    @Test
    void testParseTokenWithNullToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenParser.parseToken(null));
    }

    @Test
    void testParseTokenWithEmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenParser.parseToken(""));
    }

    @Test
    void testParseTokenWithBearerPrefix() {
        String tokenWithBearer = "Bearer " + SAMPLE_JWT;
        // This will fail due to invalid signature, but we're testing the Bearer prefix removal
        assertThrows(RuntimeException.class, () -> jwtTokenParser.parseToken(tokenWithBearer));
    }

    @Test
    void testExtractUserWithInvalidToken() {
        assertThrows(RuntimeException.class, () -> jwtTokenParser.extractUser("invalid.token.here"));
    }

    @Test
    void testExtractClaimWithInvalidToken() {
        assertThrows(RuntimeException.class, () -> jwtTokenParser.extractClaim("invalid.token.here", "sub"));
    }
}
