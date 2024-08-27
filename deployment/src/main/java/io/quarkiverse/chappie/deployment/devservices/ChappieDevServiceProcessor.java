package io.quarkiverse.chappie.deployment.devservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import io.quarkiverse.chappie.deployment.ChappieClient;
import io.quarkiverse.chappie.deployment.ChappieClientBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.ChappieEnabled;
import io.quarkiverse.chappie.deployment.Feature;
import io.quarkiverse.chappie.deployment.LLM;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.runtime.util.ClassPathUtils;

@BuildSteps(onlyIf = { IsDevelopment.class, ChappieEnabled.class, GlobalDevServicesConfig.Enabled.class })
public class ChappieDevServiceProcessor {
    private static Process process;
    private static ChappieClient chappieClient;
    private static final Logger LOG = Logger.getLogger("Quarkus Assistant");

    @BuildStep
    public void createContainer(BuildProducer<DevServicesResultBuildItem> devServicesResultProducer,
            BuildProducer<ChappieClientBuildItem> chappieClientProducer,
            ExtensionVersionBuildItem extensionVersionBuildItem,
            ChappieConfig config) {

        if (process == null) {

            Map<String, String> properties = new HashMap<>();
            int port = findAvailablePort(4315);

            String jsonRpcBase = "http://localhost:" + port;

            properties.put("quarkus.http.host", "localhost");
            properties.put("quarkus.http.port", String.valueOf(port));
            if (config.llm().equals(LLM.openai)) {
                properties.put("quarkus.langchain4j.chat-model.provider", "openai");
                properties.put("quarkus.langchain4j.openai.api-key", config.openai().apiKey().get());
                properties.put("quarkus.langchain4j.openai.chat-model.model-name", config.openai().modelName());
                properties.put("quarkus.langchain4j.openai.enable-integration", "true");
            }
            // TODO: Ollama

            String extVersion = extensionVersionBuildItem.getVersion();
            if (!chappieServerExists(extVersion) || extVersion.endsWith(SNAPSHOT)) {
                saveChappieServer(extVersion);
            }

            try {
                runServer(extVersion, properties);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (process != null && process.isAlive()) {
                        process.destroy();
                    }
                }));

                LOG.info("Dev Services for Quarkus Assistant started on " + jsonRpcBase);

            } catch (Exception e) {
                LOG.error("Error starting Quarkus Assistant on " + jsonRpcBase, e);
            }

            Map<String, String> props = Map.of(
                    "quarkus.assistant.devservices.url", jsonRpcBase,
                    "quarkus.assistant.devservices.process-id", "" + process.pid());

            devServicesResultProducer
                    .produce(
                            new DevServicesResultBuildItem(Feature.FEATURE, "Dev services for Quarkus Assistant", null, props));

            chappieClient = new ChappieClient(jsonRpcBase);
            chappieClient.connect();
        }
        // TODO: Reconnect ?

        chappieClientProducer.produce(new ChappieClientBuildItem(chappieClient));

    }

    @BuildStep
    public void findExtensionVersion(BuildProducer<ExtensionVersionBuildItem> extensionVersionProducer) {
        try {
            final Yaml yaml = new Yaml();

            ClassPathUtils.consumeAsPaths(YAML_FILE, (Path p) -> {
                try {

                    final String extensionYaml;
                    try (Scanner scanner = new Scanner(Files.newBufferedReader(p, StandardCharsets.UTF_8))) {
                        scanner.useDelimiter("\\A");
                        extensionYaml = scanner.hasNext() ? scanner.next() : null;
                    }
                    if (extensionYaml == null) {
                        // This is a internal extension
                        return;
                    }

                    final Map<String, Object> extensionMap = yaml.load(extensionYaml);

                    if (extensionMap.containsKey("name")) {
                        String artifactId = (String) extensionMap.getOrDefault("artifact", null);
                        if (artifactId != null && artifactId.startsWith(GAV_START)) {
                            String version = artifactId.substring(GAV_START.length());
                            extensionVersionProducer.produce(new ExtensionVersionBuildItem(version));
                            return;
                        }
                    }
                } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException ex) {
            LOG.error("Error getting version for the Quarkus Assistant", ex);
        }
    }

    private boolean chappieServerExists(String version) {
        Path chappieBase = getChappieBaseDir(version);
        if (Files.exists(chappieBase)) {
            Path chappieServer = getChappieServer(chappieBase);
            return Files.exists(chappieServer);
        }
        return false;
    }

    private void saveChappieServer(String version) {
        try {
            ClassPathUtils.consumeAsStreams("/bin/" + CHAPPIE_SERVER, (InputStream t) -> {
                try {
                    Path chappieBase = getChappieBaseDir(version);

                    if (!Files.exists(chappieBase)) {
                        Files.createDirectories(chappieBase);
                    }
                    Path chappieServer = getChappieServer(chappieBase);
                    File extractedFile = chappieServer.toFile();

                    try (FileOutputStream outputStream = new FileOutputStream(extractedFile)) {
                        t.transferTo(outputStream);
                    }
                } catch (IOException ex) {
                    LOG.error("Error saving Quarkus Assistant Server", ex);
                }
            });
        } catch (IOException ioe) {
            LOG.error("Error saving Quarkus Assistant Server", ioe);
        }
    }

    private int findAvailablePort(int startPort) {
        int port = startPort;
        while (true) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                port++;
            }
        }
    }

    private Path getChappieServer(String version) {
        return getChappieServer(getChappieBaseDir(version));
    }

    private Path getChappieServer(Path chappieBase) {
        if (Files.exists(chappieBase)) {
            return chappieBase.resolve(new File(CHAPPIE_SERVER).getName());
        }
        return null;
    }

    private Path getChappieBaseDir(String version) {
        String userHome = System.getProperty("user.home");
        return Path.of(userHome, ".chappie/" + version);
    }

    private void runServer(String version, Map<String, String> properties) throws IOException {
        Path chappieServer = getChappieServer(version);

        List<String> command = new ArrayList<>();
        command.add("java");
        for (Map.Entry<String, String> es : properties.entrySet()) {
            command.add("-D" + es.getKey() + "=" + es.getValue());
        }
        command.add("-jar");
        command.add(chappieServer.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        process = processBuilder.start();

        new Thread(() -> handleStream(process.getInputStream(), "OUTPUT")).start();
        new Thread(() -> handleStream(process.getErrorStream(), "ERROR")).start();
    }

    private void handleStream(java.io.InputStream inputStream, String type) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (type.equals("ERROR")) {
                    LOG.error(line);
                } else {
                    LOG.debug(line);
                }
            }
        } catch (IOException e) {
            LOG.error("Error in Quarkus Assistant", e);
        }
    }

    private static final String YAML_FILE = "/META-INF/quarkus-extension.yaml";
    private static final String GROUP_ID = "io.quarkiverse.chappie";
    private static final String ARTIFACT_ID = "quarkus-chappie";
    private static final String GAV_START = GROUP_ID + ":" + ARTIFACT_ID + "::jar:";
    private static final String CHAPPIE_SERVER = "chappie-server.jar";
    private static final String SNAPSHOT = "-SNAPSHOT";
}
