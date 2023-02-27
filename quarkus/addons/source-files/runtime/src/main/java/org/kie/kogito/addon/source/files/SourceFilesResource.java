/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.source.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.kogito.resource.exceptions.ExceptionsHandler;

@ApplicationScoped
@Path("/management/processes/")
public final class SourceFilesResource {

    private static final ExceptionsHandler EXCEPTIONS_HANDLER = new ExceptionsHandler();

    SourceFilesProvider sourceFilesProvider;

    @GET
    @Path("sources")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSourceFileByUri(@QueryParam("uri") String uri) {
        return sourceFilesProvider.getSourceFilesByUri(uri)
                .map(sourceFile -> {
                    try (InputStream file = new ByteArrayInputStream(sourceFile.readContents())) {
                        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                                .header("Content-Disposition", "inline; filename=\"" + java.nio.file.Path.of(sourceFile.getUri()).getFileName() + "\"")
                                .build();
                    } catch (Exception e) {
                        return EXCEPTIONS_HANDLER.mapException(e);
                    }
                }).orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("{processId}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<SourceFile> getSourceFilesByProcessId(@PathParam("processId") String processId) {
        return sourceFilesProvider.getProcessSourceFiles(processId);
    }

    @GET
    @Path("{processId}/source")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSourceFileByProcessId(@PathParam("processId") String processId) {
        return sourceFilesProvider.getProcessSourceFile(processId)
                .map(sourceFile -> {
                    try {
                        return Response.ok(sourceFile.readContents()).build();
                    } catch (IOException e) {
                        return EXCEPTIONS_HANDLER.mapException(e);
                    }
                }).orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @Inject
    void setSourceFilesProvider(SourceFilesProvider sourceFilesProvider) {
        this.sourceFilesProvider = sourceFilesProvider;
    }
}
