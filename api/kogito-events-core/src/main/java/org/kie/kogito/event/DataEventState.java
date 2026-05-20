package org.kie.kogito.event;

import java.net.URI;
import java.time.OffsetDateTime;

import io.cloudevents.SpecVersion;

public record DataEventState<E> (
        E data,
        String dataContentType,
        URI dataSchema,
        String id,
        String kogitoAddons,
        String kogitoBusinessKey,
        String kogitoIdentity,
        String kogitoParentProcessInstanceId,
        String kogitoProcessId,
        String kogitoProcessInstanceId,
        String kogitoProcessInstanceState,
        String kogitoProcessType,
        String kogitoProcessVersion,
        String kogitoReferenceId,
        String kogitoRootProcessId,
        String kogitoRootProcessInstanceId,
        String kogitoRootProcessVersion,
        String kogitoStartFromNode,
        URI source,
        SpecVersion specVersion,
        String subject,
        OffsetDateTime time,
        String type) {
}
