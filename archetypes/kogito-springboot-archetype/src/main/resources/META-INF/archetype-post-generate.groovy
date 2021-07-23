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

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Verify if the starters in the parameters are valid and convert them into actual artifact ids
 */
static def startersToArtifactIds(String starters) {
    if (starters == "" || starters == null) {
        return []
    }
    def validStarters = ['processes', 'rules', 'decisions', 'serverless-workflows', 'predictions']
    def startersList = starters.split(",")
    return startersList.findAll { starter -> validStarters.find { it == starter } != null }
            .collect {
                starter -> "kogito-" + starter + "-spring-boot-starter"
            }
}

/**
 * Convert the addons list to actual Kogito Addons artifacts Ids
 */
static def addonsToArtifactsIds(String addons) {
    if (addons == "" || addons == null) {
        return []
    }
    return addons.split(",").collect { addon -> "kogito-addons-springboot-" + addon }
}

/**
 * Add the given comma separated list of starters to the generated POM
 */
def addDependenciesToPOM(String starters, String addons) {
    def artifacts = startersToArtifactIds(starters)
    if (artifacts.isEmpty()) {
        artifacts << "kogito-spring-boot-starter"
    }
    artifacts = artifacts.plus(addonsToArtifactsIds(addons))

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

/**
 * Remove the resources that requires a specific starter
 */
def removeUnneededResources(String starters, String appPackage) {
    if (starters == "" || starters == null) {
        // in this case we will have all starters in the project, let's include everything in the final project
        return
    }
    Path projectPath = Paths.get(request.outputDirectory, request.artifactId)
    String packagePath = appPackage.replace(".", "/")
    if (!starters.contains("processes")) {
        // no need to keep BPMN files
        Files.deleteIfExists(projectPath.resolve("src/main/resources/test-process.bpmn2"))
        Files.deleteIfExists(projectPath.resolve("src/test/java/" + packagePath + "/GreetingsTest.java"))
    }
    if (!starters.contains("decisions") || !starters.contains("predictions") || !starters.contains("processes")) {
        // no need to keep DMN files
        Files.deleteIfExists(projectPath.resolve("src/main/resources/Traffic Violation.dmn"))
        Files.deleteIfExists(projectPath.resolve("src/test/java/" + packagePath + "/TrafficViolationTest.java"))
    }
}

Properties properties = request.getProperties()
String startersProps = properties.get("starters")
String addonsProps = properties.get("addons")
String appPackage = properties.get("package")

addDependenciesToPOM(startersProps, addonsProps)
removeUnneededResources(startersProps, appPackage)
