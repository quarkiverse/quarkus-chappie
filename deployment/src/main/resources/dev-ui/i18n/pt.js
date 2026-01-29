import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'Assistente de IA para desenvolvimento Quarkus',

    // Page titles
    'quarkus-chappie-chat': 'Chat',
    'quarkus-chappie-assistant': 'Assistente',
    'quarkus-chappie-exception_help': 'Ajuda com exceções',

    // Chat page
    'quarkus-chappie-delete': 'Excluir',
    'quarkus-chappie-new-chat': 'Novo chat',
    'quarkus-chappie-history': 'Histórico',
    'quarkus-chappie-welcome': 'Bem-vindo ao Chat do Assistente',
    'quarkus-chappie-not-configured': 'O assistente não está configurado.',
    'quarkus-chappie-configure-now': 'Configurar agora',
    'quarkus-chappie-thinking': 'Pensando ...',
    'quarkus-chappie-error-see-log': 'Ocorreu um erro - veja o log do Assistente para mais detalhes',
    'quarkus-chappie-error-see-log-suffix': '. Veja o log do Assistente para mais detalhes',
    'quarkus-chappie-you': 'Você',
    'quarkus-chappie-assistant': 'Assistente',
    'quarkus-chappie-error': 'Erro',
    'quarkus-chappie-suggested-mcp-tool': 'Ferramenta MCP sugerida',
    'quarkus-chappie-memory-id': 'ID de memória: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'Nada ainda',

    // Configuration page
    'quarkus-chappie-config-intro': 'Para usar o Assistente de IA no Modo de Desenvolvimento Quarkus, você precisa configurar um provedor de IA',
    'quarkus-chappie-choose-provider': 'Escolher provedor',
    'quarkus-chappie-clear-configuration': 'Limpar configuração',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'Empresa líder em IA que constrói modelos de linguagem avançados como ChatGPT para alimentar aplicações inteligentes.',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'Uma plataforma para executar e gerenciar grandes modelos de linguagem localmente com facilidade.',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'Integra recursos de IA em fluxos de trabalho de contêineres, permitindo implantação local, segura e em contêineres de modelos de IA.',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Plataforma de IA empresarial da Red Hat, suportando modelos compatíveis com OpenAI com implantações seguras e escaláveis.',
    'quarkus-chappie-provider-generic': 'Compatível com OpenAI Genérico',
    'quarkus-chappie-provider-generic-desc': 'Conecte qualquer endpoint compatível com OpenAI fornecendo sua própria chave API e URL base.',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'Para usar OpenAI você precisa fornecer uma chave API OpenAI',
    'quarkus-chappie-ollama-instructions': 'Para usar Ollama você precisa instalar e executar ollama. Veja ollama.com/download',
    'quarkus-chappie-podman-instructions': 'Para usar Podman AI você precisa instalar e executar podman. Veja podman-desktop.io/docs/installation',
    'quarkus-chappie-podman-extension': 'Você também precisa instalar a extensão Podman AI Lab. Veja podman-desktop.io/docs/ai-lab/installing',
    'quarkus-chappie-openshift-instructions': 'Veja redhat.com/en/products/ai/openshift-ai',

    // Form labels
    'quarkus-chappie-api-key': 'Chave API',
    'quarkus-chappie-base-url': 'URL base',
    'quarkus-chappie-model': 'Modelo',
    'quarkus-chappie-temperature': 'Temperatura',
    'quarkus-chappie-timeout': 'Tempo limite',
    'quarkus-chappie-save': 'Salvar',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'Armazenamento',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'Habilitar RAG',
    'quarkus-chappie-max-results': 'Resultados máximos',
    'quarkus-chappie-min-score': 'Pontuação mínima',
    'quarkus-chappie-max-store-messages': 'Mensagens máximas armazenadas',
    'quarkus-chappie-enable-mcp': 'Habilitar MCP (Por padrão, se disponível, Dev MCP será adicionado)',
    'quarkus-chappie-more-mcp-servers': 'Mais servidores MCP',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'Você precisa fornecer uma chave API',
    'quarkus-chappie-need-base-url': 'Você precisa fornecer uma URL base',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'Detalhes do provedor não salvos. Veja o log para mais detalhes',
    'quarkus-chappie-saved': 'Detalhes do provedor salvos.',
    'quarkus-chappie-clear-failed': 'Detalhes do provedor não limpos. Veja o log para mais detalhes',
    'quarkus-chappie-cleared': 'Detalhes do provedor limpos.',

    // Exception page
    'quarkus-chappie-no-exception': 'Nenhuma exceção detectada ainda.',
    'quarkus-chappie-check-now': 'Verificar agora',
    'quarkus-chappie-talking-to-assistant': 'Conversando com o Assistente de IA Quarkus...',
    'quarkus-chappie-cancel': 'Cancelar',
    'quarkus-chappie-please-hold': 'Isso pode levar um tempo, por favor aguarde',
    'quarkus-chappie-suggested-fix': 'Correção sugerida pelo Assistente de IA Quarkus',
    'quarkus-chappie-diff': 'Diff',
    'quarkus-chappie-suggested-new-code': 'Novo código sugerido:',
    'quarkus-chappie-copy': 'Copiar',
    'quarkus-chappie-discard': 'Descartar',
    'quarkus-chappie-updated': str`${0} atualizado`,
    'quarkus-chappie-no-content': 'Não há conteúdo',
    'quarkus-chappie-content-copied': 'Conteúdo copiado para a área de transferência',
    'quarkus-chappie-copy-failed': str`Falha ao copiar conteúdo: ${0}`,
    'quarkus-chappie-view-in-ide': 'Ver no IDE',
    'quarkus-chappie-suggest-fix': 'Sugerir correção com IA',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Pesquisar documentação do Quarkus',
    'quarkus-chappie-get-last-exception-desc': 'Obtém a última exceção conhecida que aconteceu',
    'quarkus-chappie-help-latest-exception-desc': 'Ajuda com a última exceção',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'Obter ajuda com isso'
};
