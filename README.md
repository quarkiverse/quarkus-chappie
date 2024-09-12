# Quarkus Chappie

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.chappie/quarkus-chappie?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse.chappie/quarkus-chappie-parent)

## Welcome to the Chappie Extension

This extension adds some AI to you quarkus dev experience. To use this in your project, simply add the
extension to you project, example in maven:

```
<dependency>
    <groupId>io.quarkiverse.chappie</groupId>
    <artifactId>quarkus-chappie</artifactId>
    <version>0.0.5</version> <!-- Or latest -->
</dependency>
```

Chappie is a Dev Mode only extension, so this does not add anything to your production application.

To use chappie, you need to configure it with either an OpenAI Compatible Service or have Ollama running locally.

## Using OpenAI
To use OpenAI you need to provide an [OpenAI Api Key](https://help.openai.com/en/articles/4936850-where-do-i-find-my-openai-api-key) 
in the `quarkus.assistant.openai.api-key` property OR set a `QUARKUS_ASSISTANT_OPENAI_API_KEY` environment variable. 
                        
Example:

```
mvn quarkus:dev -Dquarkus.assistant.openai.api-key=sk....
```

## Using Podman Desktop AI
You can use Podman Desktop AI by setting the openai base-url and the api key to a dummy value.

Example:

```
mvn quarkus:dev -Dquarkus.assistant.openai.api-key=sk-dummy -Dquarkus.assistant.openai.base-url=http://localhost:46311/v1 -Dquarkus.assistant.openai.model-name=instructlab/granite-7b-lab-GGUF
```

Change the values to your own setup.

# Using MAAS

You can also use [Models as a Service on OpenShift AI](https://maas.apps.prod.rhoai.rh-aiservices-bu.com/)

Example:

```
mvn quarkus:dev -Dquarkus.assistant.openai.api-key=your-key-here -Dquarkus.assistant.openai.base-url=quarkus.assistant.openai.base-url=https://granite-8b-code-instruct-maas-apicast-production.apps.prod.rhoai.rh-aiservices-bu.com:443/v1 -Dquarkus.assistant.openai.model-name=granite-8b-code-instruct-128k
```

Change the values to your own setup.

## Using Ollama
To use Ollama you need to install and run ollama. See [ollama.com/download](https://ollama.com/download)

By default, Ollama will use the `codellama` model. You can configure this with `quarkus.assistant.ollama.model-name` property.

Example:

```
quarkus.assistant.ollama.model-name=instructlab/granite-7b-lab
```

# Building this extension and sample

You can also build this extension locally and run the provided samples (in the sample module)

For this you would need:

- java
- maven
- podman/docker

clone this repo to your local environment:

```
git clone git@github.com:quarkiverse/quarkus-chappie.git
```

Then in the root of the project, run a maven build:

```
cd quarkus-chappie
mvn clean install
```

One done you can run the sample application:

```
cd sample
mvn quarkus:dev -Dquarkus.assistant.openai.api-key=sk....
```

** replace `sk....` with your api key.

You can then navigate to localhost:8080 and simulate some exceptions to get to the exception screen.
