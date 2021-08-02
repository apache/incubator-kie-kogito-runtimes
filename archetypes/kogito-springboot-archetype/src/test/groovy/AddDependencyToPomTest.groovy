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

import groovy.xml.XmlParser
import spock.lang.Specification

/*
Use this file to test the changes in the archetype-post-generate.groovy
 */

class AddDependencyToPomTest extends Specification {
    def "Original pom.xml has 5 dependencies"() {
        given:
        Node pomXml = new XmlParser().parse(this.getClass().getResourceAsStream("examplePom.xml"))

        expect:
        pomXml.depthFirst().dependencies.dependency.size() == 5
    }

    def "Add a new dependency to original pom.xml"() {
        given:
        Node pomXml = new XmlParser().parse(this.getClass().getResourceAsStream("examplePom.xml"))

        when:
        Node newDep = new Node(null, "dependency",
                [groupId: "org.kie.kogito", artifactId: "kogito-addons-springboot-cloudevents", version: "2.0.0-SNAPSHOT"])
        pomXml.dependencies[0].children().add(0, newDep)

        then:
        pomXml.depthFirst().dependencies.dependency.size() == 6
    }

    def "Add a list of new dependencies to original pom.xml"() {
        given:
        String[] artifacts = "cloudevents,persistence,monitoring".split(",")
        Node pomXml = new XmlParser().parse(this.getClass().getResourceAsStream("examplePom.xml"))

        when:
        for (String artifact : artifacts) {
            Node depNode = new Node(null, "dependency")
            depNode.appendNode("version", null, '${kogito.version}')
            depNode.appendNode("groupId", null, "org.kie.kogito")
            depNode.appendNode("artifactId", null, "kogito-addons-springboot-" + artifact)
            pomXml.dependencies[0].children().add(0, depNode)
        }

        then:
        pomXml.depthFirst().dependencies.dependency.size() == 8

    }
}