import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'Assistente AI per lo sviluppo Quarkus',

    // Page titles
    'quarkus-chappie-chat': 'Chat',
    'quarkus-chappie-assistant': 'Assistente',
    'quarkus-chappie-exception_help': 'Aiuto con le eccezioni',

    // Chat page
    'quarkus-chappie-delete': 'Elimina',
    'quarkus-chappie-new-chat': 'Nuova chat',
    'quarkus-chappie-history': 'Cronologia',
    'quarkus-chappie-welcome': 'Benvenuto nella Chat dell\'Assistente',
    'quarkus-chappie-not-configured': 'L\'assistente non è configurato.',
    'quarkus-chappie-configure-now': 'Configura ora',
    'quarkus-chappie-thinking': 'Sto pensando ...',
    'quarkus-chappie-error-see-log': 'Si è verificato un errore - consulta il log dell\'Assistente per i dettagli',
    'quarkus-chappie-error-see-log-suffix': '. Consulta il log dell\'Assistente per i dettagli',
    'quarkus-chappie-you': 'Tu',
    'quarkus-chappie-assistant': 'Assistente',
    'quarkus-chappie-error': 'Errore',
    'quarkus-chappie-suggested-mcp-tool': 'Strumento MCP suggerito',
    'quarkus-chappie-memory-id': 'ID memoria: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'Ancora niente',

    // Configuration page
    'quarkus-chappie-config-intro': 'Per utilizzare l\'Assistente AI in Modalità Sviluppo Quarkus, devi configurare un provider AI',
    'quarkus-chappie-choose-provider': 'Scegli provider',
    'quarkus-chappie-clear-configuration': 'Cancella configurazione',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'Azienda leader nell\'AI che costruisce modelli linguistici avanzati come ChatGPT per alimentare applicazioni intelligenti.',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'Una piattaforma per eseguire e gestire facilmente grandi modelli linguistici in locale.',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'Integra funzionalità AI nei flussi di lavoro dei container, abilitando il deployment locale, sicuro e containerizzato dei modelli AI.',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Piattaforma AI aziendale di Red Hat, che supporta modelli compatibili OpenAI con deployment sicuri e scalabili.',
    'quarkus-chappie-provider-generic': 'Compatibile OpenAI Generico',
    'quarkus-chappie-provider-generic-desc': 'Connetti qualsiasi endpoint compatibile OpenAI fornendo la tua chiave API e URL base.',
    'quarkus-chappie-provider-gemini': 'Gemini',
    'quarkus-chappie-provider-gemini-desc': 'Modello AI avanzato di Google che eccelle in attività multimodali e ragionamento complesso.',
    'quarkus-chappie-provider-anthropic': 'Anthropic',
    'quarkus-chappie-provider-anthropic-desc': 'Azienda di sicurezza AI che costruisce Claude, focalizzata su sistemi AI affidabili, interpretabili e controllabili.',
    'quarkus-chappie-provider-watsonx': 'WatsonX',
    'quarkus-chappie-provider-watsonx-desc': 'Piattaforma AI e dati aziendali di IBM per costruire, addestrare e distribuire modelli di machine learning.',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'Per usare OpenAI devi fornire una chiave API OpenAI',
    'quarkus-chappie-ollama-instructions': 'Per usare Ollama devi installare ed eseguire ollama. Vedi ollama.com/download',
    'quarkus-chappie-podman-instructions': 'Per usare Podman AI devi installare ed eseguire podman. Vedi podman-desktop.io/docs/installation',
    'quarkus-chappie-podman-extension': 'Devi anche installare l\'estensione Podman AI Lab. Vedi podman-desktop.io/docs/ai-lab/installing',
    'quarkus-chappie-openshift-instructions': 'Vedi redhat.com/en/products/ai/openshift-ai',
    'quarkus-chappie-gemini-instructions': 'Per utilizzare Gemini è necessario fornire una chiave API Gemini da Google AI Studio.',
    'quarkus-chappie-anthropic-instructions': 'Per utilizzare Anthropic è necessario fornire una chiave API Anthropic.',
    'quarkus-chappie-watsonx-instructions': 'Per utilizzare WatsonX è necessario fornire una chiave API e un ID progetto. Facoltativamente, fornire un URL di base o una regione cloud.',

    // Form labels
    'quarkus-chappie-api-key': 'Chiave API',
    'quarkus-chappie-base-url': 'URL base',
    'quarkus-chappie-model': 'Modello',
    'quarkus-chappie-temperature': 'Temperatura',
    'quarkus-chappie-timeout': 'Timeout',
    'quarkus-chappie-save': 'Salva',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'Archiviazione',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'Abilita RAG',
    'quarkus-chappie-max-results': 'Risultati massimi',
    'quarkus-chappie-min-score': 'Punteggio minimo',
    'quarkus-chappie-max-store-messages': 'Messaggi massimi archiviati',
    'quarkus-chappie-enable-mcp': 'Abilita MCP (Per impostazione predefinita, se disponibile, verrà aggiunto Dev MCP)',
    'quarkus-chappie-more-mcp-servers': 'Altri server MCP',
    'quarkus-chappie-project-id': 'ID Progetto',
    'quarkus-chappie-cloud-region': 'Regione Cloud',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'Devi fornire una chiave API',
    'quarkus-chappie-need-base-url': 'Devi fornire un URL base',
    'quarkus-chappie-need-api-key-project-id': 'È necessario fornire una chiave API e un ID progetto',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'Dettagli del provider non salvati. Consulta il log per i dettagli',
    'quarkus-chappie-saved': 'Dettagli del provider salvati.',
    'quarkus-chappie-clear-failed': 'Dettagli del provider non cancellati. Consulta il log per i dettagli',
    'quarkus-chappie-cleared': 'Dettagli del provider cancellati.',

    // Exception page
    'quarkus-chappie-no-exception': 'Nessuna eccezione rilevata ancora.',
    'quarkus-chappie-check-now': 'Verifica ora',
    'quarkus-chappie-talking-to-assistant': 'Comunicazione con l\'Assistente AI Quarkus...',
    'quarkus-chappie-cancel': 'Annulla',
    'quarkus-chappie-please-hold': 'Questo può richiedere del tempo, si prega di attendere',
    'quarkus-chappie-suggested-fix': 'Correzione suggerita dall\'Assistente AI Quarkus',
    'quarkus-chappie-diff': 'Diff',
    'quarkus-chappie-suggested-new-code': 'Nuovo codice suggerito:',
    'quarkus-chappie-copy': 'Copia',
    'quarkus-chappie-discard': 'Scarta',
    'quarkus-chappie-updated': str`${0} aggiornato`,
    'quarkus-chappie-no-content': 'Non c\'è contenuto',
    'quarkus-chappie-content-copied': 'Contenuto copiato negli appunti',
    'quarkus-chappie-copy-failed': str`Impossibile copiare il contenuto: ${0}`,
    'quarkus-chappie-view-in-ide': 'Visualizza nell\'IDE',
    'quarkus-chappie-suggest-fix': 'Suggerisci correzione con AI',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Cerca nella documentazione Quarkus',
    'quarkus-chappie-get-last-exception-desc': 'Ottiene l\'ultima eccezione conosciuta che si è verificata',
    'quarkus-chappie-help-latest-exception-desc': 'Aiuto con l\'ultima eccezione',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'Ottieni aiuto con questo'
};
