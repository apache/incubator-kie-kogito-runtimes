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

package org.kie.kogito.incubation.common;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * An implementation of a RemoteId that "mounts" a LocalId on a URI
 */
public final class UriRemoteId implements RemoteId {
    private final URI uri;
    private final LocalId localId;

    public UriRemoteId(String appId, String host, int port, LocalId localId) {
        this.localId = localId;
        try {
            // the explicit path parameter url-encodes again; this is the "better" way, apparently...
            this.uri = new URI(String.format("kogito://%s@%s:%d%s", appId, host, port, localId.asPath()));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public URI asUri() {
        return uri;
    }

    @Override
    public LocalId toLocalId() {
        return localId;
    }

}
