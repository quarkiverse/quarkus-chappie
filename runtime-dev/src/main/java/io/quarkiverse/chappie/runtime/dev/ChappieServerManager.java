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
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.devui.runtime.spi.McpEvent;
import io.quarkus.devui.runtime.spi.McpServerConfiguration;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.util.ClassPathUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;

@ApplicationScoped
public class ChappieServerManager {
    private static final Logger LOG = Logger.getLogger(ChappieServerManager.class);
    private static Process process;
    private final SubmissionPublisher<String> logPublisher = new SubmissionPublisher<>();
    private ScheduledExecutorService logExecutor;
    private volatile boolean logStreaming = false;
    private Future<?> logTask;

    private ChappieAssistant assistant;
    private String version;
    private Map<String, String> chappieRAGProperties;
    private String devMcpPath;
    private boolean mcpIsEnabled = true; // default

    @Inject
    McpServerConfiguration mcpServerConfiguration;

    @Inject
    Multi<McpEvent> mcpEventStream;

    private Cancellable subscription;

    void start(@Observes StartupEvent ev) {
        subscription = mcpEventStream
                .onOverflow().buffer(256)
                .onFailure().invoke(t -> LOG.error("MCP Event stream failed upstream", t))
                .subscribe().with(
                        evt -> {
                            try {
                                handleMcpEvent(evt);
                            } catch (Throwable t) {
                                LOG.errorf(t, "MCP Handler failed for event: %s", evt);
                            }
                        },
                        t -> LOG.error("MCP Subscription terminated by upstream failure", t));
    }

    public SubmissionPublisher<String> init(String version, ChappieAssistant assistant,
            Map<String, String> chappieRAGProperties, String devMcpPath) {
        this.assistant = assistant;
        this.version = version;
        this.chappieRAGProperties = chappieRAGProperties;
        this.devMcpPath = devMcpPath;

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

    public String getMemoryId() {
        if (isConfigured()) {
            return this.assistant.getMemoryId();
        }
        return null;
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

    private void handleMcpEvent(McpEvent evt) {
        start();
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

    public CompletionStage<Map> mostRecentChat() {
        return this.assistant.getMostRecentChat();
    }

    public CompletionStage<List<Map>> chats() {
        return this.assistant.getChats();
    }

    public CompletionStage<Map> chat(String message) {
        Map<String, String> vars = new HashMap<>();
        vars.put("message", message);
        vars.put("extension", "any");

        String sm = CHAT_SYSTEM_MESSAGE;
        if (this.mcpIsEnabled) {
            sm = sm + "\n\n" + CHAT_SYSTEM_MESSAGE_MCP;
        }

        return this.assistant.assist(Optional.of(sm), CHAT_USER_MESSAGE, vars,
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
        return process != null && process.isAlive();
    }

    private Map<String, String> start() {
        if (isRunning()) {
            // TODO: Check if the configuration changed
            stop();
        }

        try {
            Path chappieServer = getChappieServer(this.version);
            Map<String, String> chappieServerArguments = getChappieServerArguments();

            List<String> command = new ArrayList<>();
            command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
            for (Map.Entry<String, String> es : chappieServerArguments.entrySet()) {
                command.add("-D" + es.getKey() + "=" + es.getValue());
            }

            command.add("-jar");
            command.add(chappieServer.toString());

            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .redirectOutput(logFile.toFile())
                    .redirectErrorStream(true);

            process = processBuilder.start();

            chappieServerArguments.put("processId", String.valueOf(process.pid()));

            String chappieServerBase = "http://" + chappieServerArguments.get(SERVER_PROPERTY_KEY_HOST) + ":"
                    + chappieServerArguments.get(SERVER_PROPERTY_KEY_PORT);

            setAssistantBaseUrl(chappieServerBase);

            startStreamingLog();

            return chappieServerArguments;
        } catch (IOException ex) {
            throw new UncheckedIOException("Problem while starting Chappie server", ex);
        }
    }

    public long stop() {
        if (isRunning()) {
            long pid = process.pid();
            process.destroyForcibly();

            setAssistantBaseUrl(null);
            stopStreamingLog();

            return pid;
        }
        return -1L;
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
                logExecutor.shutdownNow();
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
        logExecutor.shutdownNow();
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

            String ragMinScore = providerProperties.getProperty("ragMinScore");
            if (ragMinScore != null && !ragMinScore.isBlank()) {
                properties.put("chappie.rag.score.min", ragMinScore);
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

            // RAG Settings
            if (this.chappieRAGProperties != null && !this.chappieRAGProperties.isEmpty()) {

                for (Map.Entry<String, String> entry : this.chappieRAGProperties.entrySet()) {
                    if (!entry.getKey().endsWith(".active")) {
                        properties.put(entry.getKey().replaceFirst(".chappie.", "."), entry.getValue());
                    }
                }
                properties.put("quarkus.datasource.active", "true");
            }

            // MCP Settings
            String mcpEnabled = providerProperties.getProperty("mcpEnabled");
            if (mcpEnabled != null && !mcpEnabled.isBlank() && mcpEnabled.equalsIgnoreCase("false")) {
                this.mcpIsEnabled = false;
            } else {
                this.mcpIsEnabled = true;
            }

            if (this.mcpIsEnabled && mcpServerConfiguration.isEnabled()) {

                String mcpExtraServers = getDevMCPServerUrl();

                String mcpServers = providerProperties.getProperty("mcpExtraServers");
                if (mcpServers != null && !mcpServers.isBlank()) {
                    mcpExtraServers = mcpExtraServers + "," + mcpServers;
                }
                properties.put("chappie.mcp.servers", mcpExtraServers);
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

    private String getDevMCPServerUrl() {
        Config c = ConfigProvider.getConfig();
        String host = c.getValue("quarkus.http.host", String.class);
        int port = c.getValue("quarkus.http.port", Integer.class);
        return "http://" + host + ":" + port + this.devMcpPath;
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
            RAG and your own knowledge. If you don't get a good match in RAG, rather not include it. Reply in a json format, with field called answer. The value for that field should be in Markdown format with your answer.

            If a user say hello or ask you what your name is, reply with an nice introduction sentence. Your name is CHAPPiE, you are named after the 2015 Movie called CHAPPiE written and directed by Neill Blomkamp.
            If a user asks what can you do or help with, answer that you can help them with their Quarkus questions, and that you have the up-to-date documentation available.
            If a user just asks a question and nothing about you, you should not add an introduction sentence.
            For any other questions you need to relate it to Quarkus.

            When suggesting code, never suggest that a user needs to add quarkus-dev-ui in their pom.xml.
            """;

    private static final String CHAT_SYSTEM_MESSAGE_MCP = """
            MCP:
            If MCP Tools is available:
            - First, answer the user directly and concisely.
            - If the request is actionable (e.g., set a config, add an extension, edit a file, run a task), propose the exact change and ASK:
              "Do you want me to apply this now?".
            - Do NOT execute any tool unless the user explicitly consents (e.g., “yes”, “do it”, “apply”).
            - After executing a write on a later turn, verify with an appropriate read/list tool and report the result.
            - Never invent tool names/args; only use tools you actually have.
            - If you can suggest an action from a tool, include the <tool_name> in a field called action.
            - If you can suggest an action from a tool, include the confirmation message in a field called confirm.
            - If the user asks you to do something actionable, just do it (you do not need to ask to confirm in that case)
            - If a use reply yes (or effectively yes) to a message that contains a suggested action, then do the action.
            - Before suggesting adding a Quarkus extension, use the MCP tool(devui-extensions_getInstallableExtensions) to see if the extension is installable (so don't suggest that if the extension is already installed).
            - When doing a config change, use the devui-configuration_updateProperty tool if available. Prefer this over other ways for example devui-workspace_saveWorkspaceItemContent.
            """;

    private static final String CHAT_USER_MESSAGE = """
            {{message}}
            """;
}
