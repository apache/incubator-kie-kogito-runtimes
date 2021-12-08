package org.kie.kogito.addons.quarkus.knative.eventing;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class ServerlessWorkflowKnativeResourcesTest {

    @RegisterExtension
    static final QuarkusUnitTest config =
            new QuarkusUnitTest().setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addClass(AcmeApplication.class)
                            .addAsResource("application.properties"));

    @Test
    public void testSinkBindingGenerated() {

    }
}
