import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'AI Assistant for Quarkus development',

    // Page titles
    'quarkus-chappie-chat': 'Chat',
    'quarkus-chappie-assistant': 'Assistant',
    'quarkus-chappie-exception_help': 'Exception help',

    // Chat page
    'quarkus-chappie-delete': 'Delete',
    'quarkus-chappie-new-chat': 'New chat',
    'quarkus-chappie-history': 'History',
    'quarkus-chappie-welcome': 'Welcome to the Assistant Chat',
    'quarkus-chappie-not-configured': 'Assistant is not configured.',
    'quarkus-chappie-configure-now': 'Configure now',
    'quarkus-chappie-thinking': 'Thinking ...',
    'quarkus-chappie-error-see-log': 'An error occured - see the Assistant log for details',
    'quarkus-chappie-error-see-log-suffix': '. See the Assistant log for details',
    'quarkus-chappie-you': 'You',
    'quarkus-chappie-assistant': 'Assistant',
    'quarkus-chappie-error': 'Error',
    'quarkus-chappie-suggested-mcp-tool': 'Suggested MCP Tool',
    'quarkus-chappie-memory-id': 'Memory Id: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'Nothing yet',

    // Configuration page
    'quarkus-chappie-config-intro': 'To use the AI Assistant in Quarkus Dev Mode, you need to configure an AI provider',
    'quarkus-chappie-choose-provider': 'Choose provider',
    'quarkus-chappie-clear-configuration': 'Clear configuration',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'Leading AI company that builds advanced language models like ChatGPT to power intelligent applications.',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'A platform for running and managing large language models locally with ease.',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'Integrates AI features into container workflows, enabling local, secure, and containerized AI model deployment.',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Red Hat\'s enterprise AI platform, supporting OpenAI-compatible models with secure, scalable deployments.',
    'quarkus-chappie-provider-generic': 'Generic OpenAI-Compatible',
    'quarkus-chappie-provider-generic-desc': 'Connect any OpenAI-compatible endpoint by providing your own API key and base URL.',
    'quarkus-chappie-provider-gemini': 'Gemini',
    'quarkus-chappie-provider-gemini-desc': 'Google\'s advanced AI model that excels at multimodal tasks and complex reasoning.',
    'quarkus-chappie-provider-anthropic': 'Anthropic',
    'quarkus-chappie-provider-anthropic-desc': 'AI safety company that builds Claude, focused on reliable, interpretable, and steerable AI systems.',
    'quarkus-chappie-provider-watsonx': 'WatsonX',
    'quarkus-chappie-provider-watsonx-desc': 'IBM\'s enterprise AI and data platform for building, training, and deploying machine learning models.',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'To use OpenAI you need to provide an OpenAI Api Key',
    'quarkus-chappie-ollama-instructions': 'To use Ollama you need to install and run ollama. See ollama.com/download',
    'quarkus-chappie-podman-instructions': 'To use Podman AI you need to install and run podman. See podman-desktop.io/docs/installation',
    'quarkus-chappie-podman-extension': 'You also need to install the Podman AI Lab extension. See podman-desktop.io/docs/ai-lab/installing',
    'quarkus-chappie-openshift-instructions': 'See redhat.com/en/products/ai/openshift-ai',
    'quarkus-chappie-gemini-instructions': 'To use Gemini you need to provide a Gemini API Key from Google AI Studio.',
    'quarkus-chappie-anthropic-instructions': 'To use Anthropic you need to provide an Anthropic API Key.',
    'quarkus-chappie-watsonx-instructions': 'To use WatsonX you need to provide an API Key and Project ID. Optionally provide either a Base URL or Cloud Region.',

    // Form labels
    'quarkus-chappie-api-key': 'API Key',
    'quarkus-chappie-use-env-var': 'Use environment variable',
    'quarkus-chappie-env-var-name': 'Environment Variable Name',
    'quarkus-chappie-base-url': 'Base URL',
    'quarkus-chappie-model': 'Model',
    'quarkus-chappie-temperature': 'Temperature',
    'quarkus-chappie-timeout': 'Timeout',
    'quarkus-chappie-save': 'Save',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'Storage',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'Enable RAG',
    'quarkus-chappie-max-results': 'Max results',
    'quarkus-chappie-min-score': 'Min score',
    'quarkus-chappie-max-store-messages': 'Max store messages',
    'quarkus-chappie-enable-mcp': 'Enable MCP (By default, if available, Dev MCP will be added)',
    'quarkus-chappie-more-mcp-servers': 'More MCP Servers',
    'quarkus-chappie-project-id': 'Project ID',
    'quarkus-chappie-cloud-region': 'Cloud Region',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'You need to provide an API Key',
    'quarkus-chappie-need-base-url': 'You need to provide a base URL',
    'quarkus-chappie-need-api-key-project-id': 'You need to provide an API Key and Project ID',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'Provider details not saved. See log for details',
    'quarkus-chappie-saved': 'Provider details saved.',
    'quarkus-chappie-clear-failed': 'Provider details not cleared. See log for details',
    'quarkus-chappie-cleared': 'Provider details cleared.',

    // Exception page
    'quarkus-chappie-no-exception': 'No exception detected yet.',
    'quarkus-chappie-check-now': 'Check now',
    'quarkus-chappie-talking-to-assistant': 'Talking to the Quarkus AI Assistant...',
    'quarkus-chappie-cancel': 'Cancel',
    'quarkus-chappie-please-hold': 'This can take a while, please hold',
    'quarkus-chappie-suggested-fix': 'Suggested fix from the Quarkus AI Assistant',
    'quarkus-chappie-diff': 'Diff',
    'quarkus-chappie-suggested-new-code': 'Suggested new code:',
    'quarkus-chappie-copy': 'Copy',
    'quarkus-chappie-discard': 'Discard',
    'quarkus-chappie-updated': str`Updated ${0}`,
    'quarkus-chappie-no-content': 'There is no content',
    'quarkus-chappie-content-copied': 'Content copied to clipboard',
    'quarkus-chappie-copy-failed': str`Failed to copy content: ${0}`,
    'quarkus-chappie-view-in-ide': 'View in IDE',
    'quarkus-chappie-suggest-fix': 'Suggest fix with AI',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Search for Quarkus documentation',
    'quarkus-chappie-get-last-exception-desc': 'Gets the last known exception that happend',
    'quarkus-chappie-help-latest-exception-desc': 'Help with the latest exception',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'Get help with this'
};
