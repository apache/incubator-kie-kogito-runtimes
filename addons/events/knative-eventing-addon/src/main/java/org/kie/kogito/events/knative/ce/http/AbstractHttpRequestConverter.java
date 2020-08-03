package org.kie.kogito.events.knative.ce.http;

import io.cloudevents.core.provider.ExtensionProvider;
import org.kie.kogito.events.knative.ce.extensions.KogitoProcessExtension;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class AbstractHttpRequestConverter {

    AbstractHttpRequestConverter() {
        ExtensionProvider.getInstance().registerExtension(KogitoProcessExtension.class, KogitoProcessExtension::new);
    }

    protected String inputStreamToString(final InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            buf.write((byte) result);
            result = bis.read();
        }
        return buf.toString(StandardCharsets.UTF_8.name());
    }

}
