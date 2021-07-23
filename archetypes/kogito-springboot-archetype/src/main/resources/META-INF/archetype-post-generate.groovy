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

// Performs the post processing tasks in the generated project

import groovy.xml.XmlParser
import groovy.xml.XmlUtil

/**
 * Verify if the starters in the parameters are valid and transform them into actual artifact ids
 */
def validateAndConvertToArtifactId(String starters) {
    def validStarters = ['processes', 'rules', 'decisions', 'serverless-workflows', 'predictions']
    def startersList = starters.split(",")
    return startersList.findAll { starter -> validStarters.find { it == starter } != null }
            .collect {
                starter -> "kogito-" + starter + "-spring-boot-starter"
            }
}

/**
 * Add the given comma separated list of starters to the generated POM
 */
def addStartersToPOM(String starters) {
    def artifacts = validateAndConvertToArtifactId(starters)
    if (artifacts.isEmpty()) {
        artifacts << "kogito-spring-boot-starter"
    }

    def pomFile = new File(request.getOutputDirectory() + "/" + request.getArtifactId() + "/pom.xml")
    def pomXml = new XmlParser().parse(pomFile)
    artifacts.each { artifact ->
        def depNode = new Node(null, "dependency")
        depNode.appendNode("groupId", null, "org.kie.kogito")
        depNode.appendNode("artifactId", null, artifact)
        depNode.appendNode("version", null, '${kogito.version}')
        pomXml.dependencies[0].children().add(0, depNode)
    }
    def writer = new FileWriter(request.getOutputDirectory() + "/" + request.getArtifactId() + "/pom.xml")
    // removing unnecessary white spaces
    XmlUtil.serialize(XmlUtil.serialize(pomXml).trim().replace("\n", "").replaceAll("( *)<", "<"), writer)
}

Properties properties = request.getProperties()
addStartersToPOM(properties.get("starters"))
