/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.index.infinispan.cache;

import java.io.IOException;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InfinispanCacheManagerTest {

    @Test
    void testConfigureWithNoAditionalProperties() throws IOException {
        final InfinispanCacheManager cm = new InfinispanCacheManager();
        cm.configure();
        // null b/c we're not relying on inject here
        assertThat(cm.manager).isNull();
    }

    @Test
    void testConfigureWithAuthHotRodProperty() throws IOException {
        final InfinispanCacheManager cm = new InfinispanCacheManager();
        cm.hotRodPropertiesFilePath = this.getClass().getResource("/auth-properties").getPath();
        cm.configure();
        assertThat(cm.manager).isNotNull();
        assertThat(cm.manager.getConfiguration().properties().getProperty("infinispan.client.hotrod.use_auth")).isEqualTo("true");
    }
    
    @Test
    void testConfigureWithNoAuthHotRodProperty() throws IOException {
        final InfinispanCacheManager cm = new InfinispanCacheManager();
        cm.hotRodPropertiesFilePath = this.getClass().getResource("/no-auth-properties").getPath();
        cm.configure();
        assertThat(cm.manager).isNotNull();
        assertThat(cm.manager.getConfiguration().properties().getProperty("infinispan.client.hotrod.use_auth")).isEqualTo("false");
    }
    
    @Test
    void testConfigureWithAlreadyConfiguredCache() throws IOException {
        final InfinispanCacheManager cm = new InfinispanCacheManager();
        cm.manager = new RemoteCacheManager();
        assertThat(cm.manager.getConfiguration().properties().getProperty("infinispan.client.hotrod.use_auth")).isEqualTo("false");
        cm.hotRodPropertiesFilePath = this.getClass().getResource("/auth-properties").getPath();
        cm.init();
        assertThat(cm.manager).isNotNull();
        assertThat(cm.manager.getConfiguration().properties().getProperty("infinispan.client.hotrod.use_auth")).isEqualTo("true");
    }

    @Test
    void testConfigureFileDoesNotExist() throws IOException {
        final InfinispanCacheManager cm = new InfinispanCacheManager();
        cm.hotRodPropertiesFilePath = "/this/path/does/not/exist";
        cm.configure();
        assertThat(cm.manager).isNull();
    }
    
}
