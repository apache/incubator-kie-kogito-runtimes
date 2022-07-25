## Kogito
**Kogito** is the next generation of business automation platform focused on cloud-native development, deployment and execution.

<p align="center"><img width=55% height=55% src="docsimg/kogito.png"></p>

[![GitHub Stars](https://img.shields.io/github/stars/kiegroup/kogito-runtimes.svg)](https://github.com/kiegroup/kogito-runtimes/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/kiegroup/kogito-runtimes.svg)](https://github.com/kiegroup/kogito-runtimes/network/members)
[![GitHub Issues](https://img.shields.io/github/issues/kiegroup/kogito-runtimes.svg)]()
[![Pull Requests](https://img.shields.io/github/issues-pr/kiegroup/kogito-runtimes.svg?style=flat-square)](https://github.com/kiegroup/kogito-runtimes/pulls)
[![Zulip chat](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)]( https://kie.zulipchat.com)
[![Contributors](https://img.shields.io/github/contributors/kiegroup/kogito-runtimes.svg?style=flat-square)](https://github.com/kiegroup/kogito-runtimes/graphs/contributors)
[![License](https://img.shields.io/github/license/kiegroup/kogito-runtimes.svg)](https://github.com/kiegroup/kogito-runtimes/blob/main/LICENSE)
[![Twitter Follow](https://img.shields.io/twitter/follow/kogito_kie.svg?label=Follow&style=social)](https://twitter.com/kogito_kie?lang=en)

## Quick Links
**Homepage:** http://kogito.kie.org

**Guides and Documentation:** https://kogito.kie.org/guides/

**JIRA Issues:** https://issues.jboss.org/projects/KOGITO

## Requirements
- [Java](https://openjdk.java.net/install/) 11 or later (devel package)
- [Maven](https://maven.apache.org/) 3.8.6 or later
- optional: Docker installation for running integration tests

## Getting Started
The [Kogito Examples repository](https://github.com/kiegroup/kogito-examples) module contains a number of examples that you can take a look at and try out yourself. Please take a look at the readme of each individual example for more details on how the example works and how to run it yourself (either locally or on Kubernetes).

## Guides
The official guides for Kogito can be found at our main website, these include guides for Quarkus and Spring Boot.

- [Kogito Guides](https://kogito.kie.org/guides/).

If you want to read more about Quarkus:

- [Quarkus - Getting Started](https://quarkus.io/get-started/) - Quarkus Getting Started guide
- [Quarkus - Using Kogito to add business automation capabilities to an application](https://quarkus.io/guides/kogito) - A simple quick start hosted on the Quarkus web site.

## Building and Contributing to Kogito
All contributions are welcome! Before you start please read the [contribution guide](CONTRIBUTING.md).

**NOTE:** This project uses the [Maven wrapper](https://maven.apache.org/wrapper/) so you should not need any default installation of Maven. If needed, the required Maven version can be found in the <version.maven> property of the [dependencies pom file](./kogito-build/kogito-dependencies-bom/pom.xml).

The snapshot artifacts are deployed daily in the Nexus repository.  
In case of a breaking change in the repositories during the day, it may be that those are not up to date...  
In that case, you will need to install locally all artifacts on which this repository depends.

To do so, you have 2 ways:

- Use the [build-chain](https://github.com/kiegroup/github-action-build-chain) that we also use on our PR checks.  
  Build Chain tool does "simple" maven build(s), the builds are just Maven commands, but because the repositories relates and depends on each other and any change in API or class method could affect several of those repositories there is a need to use [build-chain tool](https://github.com/kiegroup/github-action-build-chain) to handle cross repository builds and be sure that we always use latest version of the code for each repository.

- Checkout all upstream and current repositories on correct branch and run maven commands in each of them.

### Use the build-chain tool

To install the build-chain tool, please follow instructions on https://github.com/kiegroup/github-action-build-chain#local-execution

**Build quickly upstream and current repository**

Useful is you want to work just on a specific module of the whole project.

```
build-chain-action -df='https://raw.githubusercontent.com/kiegroup/kogito-pipelines/main/.ci/pull-request-config.yaml' -folder="/tmp/bc$RANDOM" build branch -c='mvn clean install -Dquickly' -sp=kiegroup/kogito-runtimes -b=main -g=kiegroup -spc='kiegroup/kogito-runtimes=./' --skipParallelCheckout -cct '(^mvn .*)||$1 -Dmaven.wagon.http.ssl.insecure=true'
```

NOTE: In case you are not running from the `kogito-runtimes` repository, remove the `-spc='kiegroup/kogito-runtimes=./'` option.

**Build quickly upstream & run all tests on current repository**

```
build-chain-action -df='https://raw.githubusercontent.com/kiegroup/kogito-pipelines/main/.ci/pull-request-config.yaml' -folder="/tmp/bc$RANDOM" build branch -sp=kiegroup/kogito-runtimes -b=main -g=kiegroup -spc='kiegroup/kogito-runtimes=./' --skipParallelCheckout -cct '(^mvn .*)||$1 -Dmaven.wagon.http.ssl.insecure=true'
```

NOTE: In case you are not running from the `kogito-runtimes` repository, remove the `-spc='kiegroup/kogito-runtimes=./'` option.

**Reproduce check from a PR**

```
build-chain-action -df=https://raw.githubusercontent.com/kiegroup/kogito-pipelines/main/.ci/pull-request-config.yaml -folder="/tmp/bc$RANDOM" build pr -url={PR_LINK} -sp=kiegroup/kogito-runtimes --skipParallelCheckout -cct '(^mvn .*)||$1 -Dmaven.wagon.http.ssl.insecure=true'
```

NOTE: Please replace `{PR_LINK}` in the command.

#### Checkout all dependent repositories

You will need to checkout all dependent repositories on the correct branch and run the needed maven command.

**Repositories:**
- 

**Build quickly upstream and current repository**

```bash
./mvnw clean install -Dquickly
```

**Build quickly upstream & run all tests on current repository**

*Run on all upstream repositories*

```
./mvnw clean install -Dquickly
```

*Run on current repository*

```
./mvnw clean install
```

## Getting Help
### Issues
- Do you have a [minimal, reproducible example](https://stackoverflow.com/help/minimal-reproducible-example) for your issue?
  - If so, please open a Jira for it in the [Kogito project](https://issues.redhat.com/projects/KOGITO/summary) with the details of your issue and example.
- Are you encountering an issue but unsure of what is going on? 
  - Start a new conversation in the Kogito [Google Group](https://groups.google.com/g/kogito-development), or open a new thread in the [Kogito stream](https://kie.zulipchat.com/#narrow/stream/232676-kogito) of the KIE Zulip chat.
  - Please provide as much relevant information as you can as to what could be causing the issue, and our developers will help you figure out what's going wrong.

### Requests
- Do you have a feature/enhancement request?
  - Please open a new thread in the [Kogito stream](https://kie.zulipchat.com/#narrow/stream/232676-kogito) of the KIE Zulip chat to start a discussion there.
