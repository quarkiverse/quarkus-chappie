import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'KI-Assistent für Quarkus-Entwicklung',

    // Page titles
    'quarkus-chappie-chat': 'Chat',
    'quarkus-chappie-assistant': 'Assistent',
    'quarkus-chappie-exception_help': 'Ausnahme-Hilfe',

    // Chat page
    'quarkus-chappie-delete': 'Löschen',
    'quarkus-chappie-new-chat': 'Neuer Chat',
    'quarkus-chappie-history': 'Verlauf',
    'quarkus-chappie-welcome': 'Willkommen beim Assistenten-Chat',
    'quarkus-chappie-not-configured': 'Assistent ist nicht konfiguriert.',
    'quarkus-chappie-configure-now': 'Jetzt konfigurieren',
    'quarkus-chappie-thinking': 'Denke nach ...',
    'quarkus-chappie-error-see-log': 'Ein Fehler ist aufgetreten - siehe Assistenten-Log für Details',
    'quarkus-chappie-error-see-log-suffix': '. Siehe Assistenten-Log für Details',
    'quarkus-chappie-you': 'Sie',
    'quarkus-chappie-assistant': 'Assistent',
    'quarkus-chappie-error': 'Fehler',
    'quarkus-chappie-suggested-mcp-tool': 'Vorgeschlagenes MCP-Tool',
    'quarkus-chappie-memory-id': 'Speicher-ID: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'Noch nichts',

    // Configuration page
    'quarkus-chappie-config-intro': 'Um den KI-Assistenten im Quarkus-Entwicklungsmodus zu verwenden, müssen Sie einen KI-Anbieter konfigurieren',
    'quarkus-chappie-choose-provider': 'Anbieter auswählen',
    'quarkus-chappie-clear-configuration': 'Konfiguration löschen',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'Führendes KI-Unternehmen, das fortschrittliche Sprachmodelle wie ChatGPT entwickelt, um intelligente Anwendungen zu betreiben.',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'Eine Plattform zum einfachen Ausführen und Verwalten großer Sprachmodelle lokal.',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'Integriert KI-Funktionen in Container-Workflows und ermöglicht lokale, sichere und containerisierte KI-Modellbereitstellung.',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Red Hats Enterprise-KI-Plattform, die OpenAI-kompatible Modelle mit sicheren, skalierbaren Bereitstellungen unterstützt.',
    'quarkus-chappie-provider-generic': 'Generisch OpenAI-Kompatibel',
    'quarkus-chappie-provider-generic-desc': 'Verbinden Sie jeden OpenAI-kompatiblen Endpunkt, indem Sie Ihren eigenen API-Schlüssel und Basis-URL angeben.',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'Um OpenAI zu verwenden, benötigen Sie einen OpenAI-API-Schlüssel',
    'quarkus-chappie-ollama-instructions': 'Um Ollama zu verwenden, müssen Sie ollama installieren und ausführen. Siehe ollama.com/download',
    'quarkus-chappie-podman-instructions': 'Um Podman AI zu verwenden, müssen Sie podman installieren und ausführen. Siehe podman-desktop.io/docs/installation',
    'quarkus-chappie-podman-extension': 'Sie müssen auch die Podman AI Lab-Erweiterung installieren. Siehe podman-desktop.io/docs/ai-lab/installing',
    'quarkus-chappie-openshift-instructions': 'Siehe redhat.com/en/products/ai/openshift-ai',

    // Form labels
    'quarkus-chappie-api-key': 'API-Schlüssel',
    'quarkus-chappie-base-url': 'Basis-URL',
    'quarkus-chappie-model': 'Modell',
    'quarkus-chappie-temperature': 'Temperatur',
    'quarkus-chappie-timeout': 'Zeitüberschreitung',
    'quarkus-chappie-save': 'Speichern',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'Speicher',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'RAG aktivieren',
    'quarkus-chappie-max-results': 'Maximale Ergebnisse',
    'quarkus-chappie-min-score': 'Minimale Punktzahl',
    'quarkus-chappie-max-store-messages': 'Maximale Speichernachrichten',
    'quarkus-chappie-enable-mcp': 'MCP aktivieren (Standardmäßig wird Dev MCP hinzugefügt, falls verfügbar)',
    'quarkus-chappie-more-mcp-servers': 'Weitere MCP-Server',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'Sie müssen einen API-Schlüssel angeben',
    'quarkus-chappie-need-base-url': 'Sie müssen eine Basis-URL angeben',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'Anbieterdetails nicht gespeichert. Siehe Log für Details',
    'quarkus-chappie-saved': 'Anbieterdetails gespeichert.',
    'quarkus-chappie-clear-failed': 'Anbieterdetails nicht gelöscht. Siehe Log für Details',
    'quarkus-chappie-cleared': 'Anbieterdetails gelöscht.',

    // Exception page
    'quarkus-chappie-no-exception': 'Noch keine Ausnahme erkannt.',
    'quarkus-chappie-check-now': 'Jetzt prüfen',
    'quarkus-chappie-talking-to-assistant': 'Spreche mit dem Quarkus-KI-Assistenten...',
    'quarkus-chappie-cancel': 'Abbrechen',
    'quarkus-chappie-please-hold': 'Dies kann eine Weile dauern, bitte warten',
    'quarkus-chappie-suggested-fix': 'Vorgeschlagene Lösung vom Quarkus-KI-Assistenten',
    'quarkus-chappie-diff': 'Diff',
    'quarkus-chappie-suggested-new-code': 'Vorgeschlagener neuer Code:',
    'quarkus-chappie-copy': 'Kopieren',
    'quarkus-chappie-discard': 'Verwerfen',
    'quarkus-chappie-updated': str`${0} aktualisiert`,
    'quarkus-chappie-no-content': 'Es gibt keinen Inhalt',
    'quarkus-chappie-content-copied': 'Inhalt in Zwischenablage kopiert',
    'quarkus-chappie-copy-failed': str`Fehler beim Kopieren des Inhalts: ${0}`,
    'quarkus-chappie-view-in-ide': 'In IDE anzeigen',
    'quarkus-chappie-suggest-fix': 'Lösung mit KI vorschlagen',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Quarkus-Dokumentation durchsuchen',
    'quarkus-chappie-get-last-exception-desc': 'Ruft die zuletzt bekannte Ausnahme ab',
    'quarkus-chappie-help-latest-exception-desc': 'Hilfe bei der letzten Ausnahme',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'Hilfe hiermit erhalten'
};
