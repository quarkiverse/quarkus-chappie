package io.quarkiverse.chappie.runtime.dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.jboss.logging.Logger;

import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.runtime.util.ClassPathUtils;

@ApplicationScoped
public class ChappieServerManager {
    private static final Logger LOG = Logger.getLogger(ChappieServerManager.class);
    private final SubmissionPublisher<String> logPublisher = new SubmissionPublisher<>();
    private ScheduledExecutorService logExecutor;
    private volatile boolean logStreaming = false;
    private Future<?> logTask;

    private ChappieAssistant assistant;
    private String version;
    private Map<String, String> chappieRAGProperties;

    private static Supplier<ProcessHandle> getCurrentProcess;
    private static Consumer<ProcessHandle> setCurrentProcess;

    public static void registerProcessHandler(Supplier<ProcessHandle> getter, Consumer<ProcessHandle> setter) {
        getCurrentProcess = getter;
        setCurrentProcess = setter;
    }

    public static ProcessHandle getProcess() {
        return getCurrentProcess.get();
    }

    public static void setProcess(ProcessHandle process) {
        setCurrentProcess.accept(process);
    }

    public SubmissionPublisher<String> init(String version, ChappieAssistant assistant,
            Map<String, String> chappieRAGProperties) {
        this.assistant = assistant;
        this.version = version;
        this.chappieRAGProperties = chappieRAGProperties;
        if (Files.notExists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        if (Files.notExists(logFile)) {
            try {
                Files.createFile(logFile);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        // Make sure chappie server is installed
        if (!isInstalled()) {
            install(version);
        }

        // Make sure we start the server if it's configured
        if (isConfigured()) {
            start();
        }
        return this.logPublisher;
    }

    @Produces
    public Optional<Assistant> getAssistantIfConfigured() {
        if (isConfigured()) {
            return Optional.of(this.assistant);
        } else {
            return Optional.empty();
        }
    }

    public boolean clearMemory() {
        if (isConfigured()) {
            this.assistant.clearMemoryId();
            return true;
        }
        return false;
    }

    private boolean isInstalled() {
        if (version.endsWith("SNAPSHOT"))
            return false; // Always reinstal snapshot
        Path chappieBase = getChappieBaseDir(version);
        if (Files.exists(chappieBase)) {
            Path chappieServer = getChappieServer(chappieBase);
            return Files.exists(chappieServer);
        }
        return false;
    }

    public void install(String version) {
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

    public boolean isConfigured() {
        Properties properties = this.loadConfiguration();
        return properties.containsKey(KEY_NAME);
    }

    public Properties loadConfigurationFor(String name) {
        return load(name);
    }

    public Properties loadConfiguration() {
        return load(null);
    }

    public CompletionStage<Map> chat(String message) {
        Map<String, String> vars = new HashMap<>();
        vars.put("message", message);
        vars.put("extension", "any");

        return this.assistant.assist(Optional.of(CHAT_SYSTEM_MESSAGE), CHAT_USER_MESSAGE, vars,
                List.of());
    }

    private Properties load(String name) {
        Properties fullProps = new Properties();

        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                fullProps.load(in);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (name == null || name.isBlank()) {
            name = fullProps.getProperty("name");
        }

        if (name == null || name.isBlank()) {
            return new Properties(); // or throw if name is mandatory
        }

        Properties scopedProps = new Properties();
        scopedProps.setProperty("name", name);

        String prefix = name + ".";

        for (String key : fullProps.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                String trimmedKey = key.substring(prefix.length());
                scopedProps.setProperty(trimmedKey, fullProps.getProperty(key));
            }
        }

        return scopedProps;
    }

    public boolean storeConfiguration(Map<String, String> configuration) {
        Properties existingProps = readFullConfiguration();

        String name = configuration.get("name");

        if (name != null && !name.isBlank()) {
            // Add new configuration with prefix (except "name" itself)
            configuration.forEach((key, value) -> {
                if (!"name".equals(key)) {
                    existingProps.setProperty(name + "." + key, value);
                } else {
                    existingProps.setProperty("name", value);
                }
            });
        } else {
            // Remove 'name' key if name is null or blank
            existingProps.remove("name");
        }

        // Store configuration
        return saveFullConfiguration(existingProps, this::start);
    }

    public boolean clearConfiguration() {
        Properties existingProps = readFullConfiguration();
        existingProps.remove("name");
        return saveFullConfiguration(existingProps, this::stop);
    }

    private Properties readFullConfiguration() {
        Properties existingProps = new Properties();

        // Load existing configuration if present
        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                existingProps.load(in);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return existingProps;
    }

    private boolean saveFullConfiguration(Properties p, Runnable postAction) {
        try (OutputStream out = Files.newOutputStream(configFile)) {
            p.store(out, "Chappie Configuration");
            postAction.run();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String getConfiguredProviderName() {
        Properties properties = this.loadConfiguration();
        return properties.getProperty(KEY_NAME, null);
    }

    public boolean isRunning() {
        ProcessHandle process = getProcess();
        return process != null && process.isAlive();
    }

    private Map<String, String> start() {
        Map<String, String> chappieServerArguments = getChappieServerArguments();
        if (isRunning()) {
            Map<String, String> arguments = getCurrentProcessArguments();
            if (containsSameKeyValue(arguments, chappieServerArguments)) {
                LOG.debug("Chappie Server is already running with the same configuration");
                String chappieServerBase = "http://" + chappieServerArguments.get(SERVER_PROPERTY_KEY_HOST) + ":"
                        + chappieServerArguments.get(SERVER_PROPERTY_KEY_PORT);

                setAssistantBaseUrl(chappieServerBase);
                return arguments;
            } else {
                LOG.debug("Chappie Server is already running with a different configuration, restarting...");
            }
            stop();
        }

        try {
            Path chappieServer = getChappieServer(this.version);

            List<String> command = new ArrayList<>();
            command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
            for (Map.Entry<String, String> es : chappieServerArguments.entrySet()) {
                command.add("-D" + es.getKey() + "=" + es.getValue());
            }

            command.add("-jar");
            command.add(chappieServer.toString());

            LOG.debug("Starting Chappie Server with command: " + String.join(" ", command));
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .redirectOutput(logFile.toFile())
                    .redirectErrorStream(true);

            ProcessHandle handle = processBuilder.start().toHandle();
            setProcess(handle);

            chappieServerArguments.put("processId", String.valueOf(handle.pid()));

            String chappieServerBase = "http://" + chappieServerArguments.get(SERVER_PROPERTY_KEY_HOST) + ":"
                    + chappieServerArguments.get(SERVER_PROPERTY_KEY_PORT);

            setAssistantBaseUrl(chappieServerBase);

            startStreamingLog();

            return chappieServerArguments;
        } catch (IOException ex) {
            throw new UncheckedIOException("Problem while starting Chappie server", ex);
        }
    }

    private static boolean containsSameKeyValue(Map<String, String> args, Map<String, String> otherArgs) {
        if (args == null || otherArgs == null) {
            return false;
        }
        if (args.size() != otherArgs.size()) {
            return false;
        }
        return args.entrySet().stream()
                .allMatch(e -> Objects.equals(otherArgs.get(e.getKey()), e.getValue()));
    }

    private Map<String, String> getCurrentProcessArguments() {
        if (isRunning()) {
            Optional<String[]> arguments = getProcess().info().arguments();
            if (arguments.isPresent()) {
                String[] args = arguments.get();
                Map<String, String> currentArgs = new HashMap<>();
                for (String a : args) {
                    if (a.startsWith("-D")) {
                        String[] keyValue = a.substring(2).split("=", 2);
                        if (keyValue.length == 2) {
                            currentArgs.put(keyValue[0], keyValue[1]);
                        }
                    }
                }
                return currentArgs;
            }
        }
        return Map.of();
    }

    public void stop() {
        if (isRunning()) {
            setAssistantBaseUrl(null);
            stopStreamingLog();
        }
    }

    private void startStreamingLog() {
        logStreaming = true;
        this.logExecutor = Executors.newSingleThreadScheduledExecutor();
        logExecutor.scheduleWithFixedDelay(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile.toFile()))) {
                String line;
                while (logStreaming) {
                    if ((line = reader.readLine()) != null) {
                        logPublisher.submit(line);
                    }
                }
            } catch (Exception e) {
                logPublisher.closeExceptionally(e);
                if (logExecutor != null && !logExecutor.isShutdown()) {
                    logExecutor.shutdownNow();
                }
            } finally {
                //publisher.close();
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    private void stopStreamingLog() {
        logStreaming = false;
        if (logTask != null) {
            logTask.cancel(true);
        }
        if (logExecutor != null && !logExecutor.isShutdown()) {
            logExecutor.shutdownNow();
        }
        //publisher.close();
    }

    @PreDestroy
    public void destroy() {
        try (logPublisher) {
            stop();
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
        return configDir.resolve(version);
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

    private Map<String, String> getChappieServerArguments() {
        Properties providerProperties = this.loadConfiguration();
        if (providerProperties.containsKey(KEY_NAME)) {
            String provider = providerProperties.getProperty(KEY_NAME);

            Map<String, String> properties = new HashMap<>();
            int port = findAvailablePort(4315);
            properties.put(SERVER_PROPERTY_KEY_HOST, "localhost");
            properties.put(SERVER_PROPERTY_KEY_PORT, String.valueOf(port));
            properties.put("chappie.log.request", "true");
            properties.put("chappie.log.response", "true");

            String temperature = providerProperties.getProperty("temperature");
            if (temperature != null && !temperature.isBlank()) {
                properties.put("chappie.temperature", temperature);
            }

            String timeout = providerProperties.getProperty("timeout");
            if (timeout != null && !timeout.isBlank()) {
                properties.put("chappie.timeout", timeout);
            }

            String ragEnabled = providerProperties.getProperty("ragEnabled");
            if (ragEnabled != null && !ragEnabled.isBlank() && ragEnabled.equalsIgnoreCase("false")) {
                properties.put("chappie.rag.enabled", "false");
            }

            String ragMaxResults = providerProperties.getProperty("ragMaxResults");
            if (ragMaxResults != null && !ragMaxResults.isBlank()) {
                properties.put("chappie.rag.results.max", ragMaxResults);
            }

            String storeMaxMessages = providerProperties.getProperty("storeMaxMessages");
            if (storeMaxMessages != null && !storeMaxMessages.isBlank()) {
                properties.put("chappie.store.messages.max", storeMaxMessages);
            }

            if (isOpenAiCompatible(provider)) {
                String baseUrl = providerProperties.getProperty("baseUrl");
                if (baseUrl != null && !baseUrl.isBlank()) {
                    properties.put("chappie.openai.base-url", baseUrl);
                }
                String apiKey = providerProperties.getProperty("apiKey");
                if (apiKey != null && !apiKey.isBlank()) {
                    properties.put("chappie.openai.api-key", apiKey);
                }
                String model = providerProperties.getProperty("model");
                if (model != null && !model.isBlank()) {
                    properties.put("chappie.openai.model-name", model);
                }
            } else if (isOllama(provider)) {
                String baseUrl = providerProperties.getProperty("baseUrl");
                if (baseUrl != null && !baseUrl.isBlank()) {
                    properties.put("chappie.ollama.base-url", baseUrl);
                }
                String model = providerProperties.getProperty("model");
                if (model != null && !model.isBlank()) {
                    properties.put("chappie.ollama.model-name", model);
                }
            }

            if (this.chappieRAGProperties != null && !this.chappieRAGProperties.isEmpty()) {

                for (Map.Entry<String, String> entry : this.chappieRAGProperties.entrySet()) {
                    if (!entry.getKey().endsWith(".active")) {
                        properties.put(entry.getKey().replaceFirst(".chappie.", "."), entry.getValue());
                    }
                }
                properties.put("quarkus.datasource.active", "true");
            }

            return properties;
        }
        return null;
    }

    public boolean isOpenAiCompatible(String name) {
        return name != null
                && (name.equals(OPEN_AI) || name.equals(PODMAN_AI) || name.equals(OPENSHIFT_AI) || name.equals(GENERIC_OPENAI));
    }

    public boolean isOllama(String name) {
        return name != null
                && (name.equals(OLLAMA));
    }

    private void setAssistantBaseUrl(String baseUrl) {
        this.assistant.setBaseUrl(baseUrl);

        Map<String, String> m = new HashMap<>();
        if (baseUrl != null) {
            m.put("baseUrl", baseUrl);
        }
        DevConsoleManager.invoke("chappie.setBaseUrl", m);
    }

    private final Path configDir = Paths.get(System.getProperty("user.home"), ".quarkus", "chappie");
    private final Path configFile = configDir.resolve("chappie-assistant.properties");
    private final Path logFile = configDir.resolve("chappie-assistant.log");

    private static final String CHAPPIE_SERVER = "chappie-server.jar";

    public static final String KEY_NAME = "name";

    public static final String OPEN_AI = "OpenAI";
    public static final String PODMAN_AI = "Podman AI";
    public static final String OPENSHIFT_AI = "OpenShift AI";
    public static final String GENERIC_OPENAI = "Generic OpenAI-Compatible";
    public static final String OLLAMA = "Ollama";

    private static final String SERVER_PROPERTY_KEY_HOST = "quarkus.http.host";
    private static final String SERVER_PROPERTY_KEY_PORT = "quarkus.http.port";

    private static final String CHAT_SYSTEM_MESSAGE = """
            You are assisting a Quarkus developer with their project. The developer will ask a question that you should answer as good as possible, using the provided
            RAG and your own knowledge. Reply in a json format, with only one field called answer. The value for that field should be in Markdown format with your answer.

            If a user say hello or ask you what your name is, reply with an nice introduction sentence. Your name is CHAPPiE, you are named after the 2015 Movie called CHAPPiE written and directed by Neill Blomkamp.
            If a user asks what can you do or help with, answer that you can help them with their Quarkus questions, and that you have the up-to-date documentation available.
            If a user just asks a question and nothing about you, you should not add an introduction sentence.
            For any other questions you need to relate it to Quarkus.

            When suggesting code, never suggest that a user needs to add quarkus-dev-ui in their pom.xml.
            """;

    private static final String CHAT_USER_MESSAGE = """
            Here is the user message: {{message}}
            """;
}
