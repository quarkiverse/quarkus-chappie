# Quarkus Chappie

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.chappie/quarkus-chappie?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse.chappie/quarkus-chappie-parent)

## Welcome to the Chappie Extension

This extension adds some AI to you quarkus dev experience. To use this in your project, simply add the
extension to you project, example in maven:

```
<dependency>
    <groupId>io.quarkiverse.chappie</groupId>
    <artifactId>quarkus-chappie</artifactId>
    <version>1.4.0</version> <!-- Or latest -->
</dependency>
```
| Chappie version | Quarkus version |
| --------------- | --------------- |
| 1.2.x           | 3.26.1+         |
| 1.3.x           | 3.26.1+         |
| 1.4.x           | 3.28.3+         |
| 1.5.x           | 3.29.0+         |
| 1.8.x           | 3.31.0+         |


Chappie is a Dev Mode only extension, so this does not add anything to your production application.

To use chappie, you need to configure it with either an OpenAI Compatible Service or have Ollama running locally. You can configure it in Dev UI.

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
mvn quarkus:dev
```

Configure the provider in Dev UI, and then navigate to localhost:8080 and simulate some exceptions to get to the exception screen.

# Doing work on the Chappie server.

You can make changes to the Chappie server and test this with this extension.

1) Clone https://github.com/chappie-bot/chappie-server
2) Build the chappie server with `mvn clean install -Dquarkus.profile=chappie`
3) Change this chappie-extension to use chappie server version 999-SNAPSHOT 
   In https://github.com/quarkiverse/quarkus-chappie/blob/main/runtime-dev/pom.xml, change `chappie-server.version` to `999-SNAPSHOT` 
4) Everytime you make a change in the server, you need to rebuild the extension to pull in the new version.
