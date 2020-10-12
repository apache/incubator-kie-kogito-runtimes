/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.svg.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.json.JsonObjectBuilder;

import com.google.common.base.Utf8;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.FileSystem;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ProcessSVGsServiceTest {

    private ProcessSVGService tested;

    @Mock
    private Vertx vertx;

    @Mock
    private Instance instance;

    @Test
    public void testQueryOnDataIndexCall() {
        tested = new ProcessSVGService("dataIndexURL",
                                       "/folder/",
                                       "",
                                       "",
                                       "",
                                       vertx,
                                       instance
        );
        WebClient webClientMock = mock(WebClient.class);
        tested.setClient(webClientMock);

        HttpRequest<Buffer> request = mock(HttpRequest.class);
        String processInstanceId = "piId";
        String processId = "processId";
        when(webClientMock.post("/graphql")).thenReturn(request);
        HttpResponse response = mock(HttpResponse.class);
        lenient().when(response.statusCode()).thenReturn(200);
        when(request.sendJson(any())).thenReturn(Uni.createFrom().item(response));

        tested.generateExecutionPathSVG(processId, processInstanceId);
        verify(webClientMock).post("/graphql");
        verify(request).sendJson(any());
    }

    @Test
    public void testDataIndexQueryResultProcess() throws Exception {
        tested = spy(new ProcessSVGService("dataIndexURL",
                                       "",
                                       "",
                                       "",
                                       "",
                                       vertx,
                                       instance
        ));
        WebClient webClientMock = mock(WebClient.class);
        tested.setClient(webClientMock);

        String processInstanceId = "piId";
        String processId = "travels";
        String jsonString= "{\n" +
                "  \"data\": {\n" +
                "    \"ProcessInstances\": [\n" +
                "      {\n" +
                "        \"id\": \"piId\",\n" +
                "        \"processId\": \"processId\",\n" +
                "        \"nodes\": [\n" +
                "          {\n" +
                "            \"definitionId\": \"_1A708F87-11C0-42A0-A464-0B7E259C426F\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.26Z\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_2140F05A-364F-40B3-BB7B-B12927065DF8\",\n" +
                "            \"exit\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_5D0733B5-53FE-40E9-9900-4CC13419C67A\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.288Z\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_F543B3F0-AB44-4A5B-BF17-8D9DEB505815\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.287Z\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_175DC79D-C2F1-4B28-BE2D-B583DFABF70D\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.26Z\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_B34ADDEE-DEA5-47C5-A913-F8B85ED5641F\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.225Z\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_5EA95D17-59A6-4567-92DF-74D36CE7F35A\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.224Z\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_54ABE1ED-61BE-45F9-812C-795A5D4ED35E\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.223Z\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"definitionId\": \"_1B11BEC9-402A-4E73-959A-296BD334CAB0\",\n" +
                "            \"exit\": \"2020-10-11T06:49:47.088Z\"\n" +
                "          }\n" +
                "        ]\n" +
                "      } "+
                "    ]\n" +
                "  }\n" +
                "}";

        HttpResponse response = mock(HttpResponse.class);
        FileSystem fileSystemMock = mock(FileSystem.class);
        Buffer bufferMock = mock(Buffer.class);

        lenient().when(response.statusCode()).thenReturn(200);
        lenient().when(response.bodyAsJsonObject()).thenReturn(new JsonObject(jsonString));
        lenient().when(vertx.fileSystem()).thenReturn(fileSystemMock);
        lenient().when(fileSystemMock.readFileBlocking( processId + ".svg")).thenReturn(bufferMock);
        lenient().when(bufferMock.toString(UTF_8)).thenReturn(getTravelsSVGFile());

        tested.queryResultHandler(response,processId,processInstanceId,"Query");
        List<String> completed = Arrays.asList("_1A708F87-11C0-42A0-A464-0B7E259C426F","_5D0733B5-53FE-40E9-9900-4CC13419C67A","_F543B3F0-AB44-4A5B-BF17-8D9DEB505815",
                                               "_175DC79D-C2F1-4B28-BE2D-B583DFABF70D","_B34ADDEE-DEA5-47C5-A913-F8B85ED5641F", "_5EA95D17-59A6-4567-92DF-74D36CE7F35A",
                                               "_54ABE1ED-61BE-45F9-812C-795A5D4ED35E","_1B11BEC9-402A-4E73-959A-296BD334CAB0");
        List<String> active = Arrays.asList("_2140F05A-364F-40B3-BB7B-B12927065DF8");
        verify(tested).svgTransformUpdate(processId, completed, active);

    }

    public static String getTravelsSVGFile() throws Exception {
        return readFileContent("travels.svg");
    }

    public static String readFileContent(String file) throws URISyntaxException, IOException {
        Path path = Paths.get(Thread.currentThread().getContextClassLoader().getResource(file).toURI());
        return new String(Files.readAllBytes(path));
    }
}