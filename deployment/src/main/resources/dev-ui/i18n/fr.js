import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'Assistant IA pour le développement Quarkus',

    // Page titles
    'quarkus-chappie-chat': 'Discussion',
    'quarkus-chappie-assistant': 'Assistant',
    'quarkus-chappie-exception_help': 'Aide aux exceptions',

    // Chat page
    'quarkus-chappie-delete': 'Supprimer',
    'quarkus-chappie-new-chat': 'Nouvelle discussion',
    'quarkus-chappie-history': 'Historique',
    'quarkus-chappie-welcome': 'Bienvenue dans le Chat de l\'Assistant',
    'quarkus-chappie-not-configured': 'L\'assistant n\'est pas configuré.',
    'quarkus-chappie-configure-now': 'Configurer maintenant',
    'quarkus-chappie-thinking': 'Réflexion ...',
    'quarkus-chappie-error-see-log': 'Une erreur s\'est produite - consultez le journal de l\'Assistant pour plus de détails',
    'quarkus-chappie-error-see-log-suffix': '. Consultez le journal de l\'Assistant pour plus de détails',
    'quarkus-chappie-you': 'Vous',
    'quarkus-chappie-assistant': 'Assistant',
    'quarkus-chappie-error': 'Erreur',
    'quarkus-chappie-suggested-mcp-tool': 'Outil MCP suggéré',
    'quarkus-chappie-memory-id': 'ID de mémoire : ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'Rien pour le moment',

    // Configuration page
    'quarkus-chappie-config-intro': 'Pour utiliser l\'Assistant IA en Mode Développement Quarkus, vous devez configurer un fournisseur d\'IA',
    'quarkus-chappie-choose-provider': 'Choisir un fournisseur',
    'quarkus-chappie-clear-configuration': 'Effacer la configuration',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'Entreprise leader en IA qui développe des modèles de langage avancés comme ChatGPT pour alimenter des applications intelligentes.',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'Une plateforme pour exécuter et gérer facilement de grands modèles de langage localement.',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'Intègre des fonctionnalités d\'IA dans les flux de travail de conteneurs, permettant un déploiement local, sécurisé et conteneurisé de modèles d\'IA.',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Plateforme d\'IA d\'entreprise de Red Hat, prenant en charge les modèles compatibles OpenAI avec des déploiements sécurisés et évolutifs.',
    'quarkus-chappie-provider-generic': 'Compatible OpenAI Générique',
    'quarkus-chappie-provider-generic-desc': 'Connectez n\'importe quel point de terminaison compatible OpenAI en fournissant votre propre clé API et URL de base.',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'Pour utiliser OpenAI, vous devez fournir une clé API OpenAI',
    'quarkus-chappie-ollama-instructions': 'Pour utiliser Ollama, vous devez installer et exécuter ollama. Voir ollama.com/download',
    'quarkus-chappie-podman-instructions': 'Pour utiliser Podman AI, vous devez installer et exécuter podman. Voir podman-desktop.io/docs/installation',
    'quarkus-chappie-podman-extension': 'Vous devez également installer l\'extension Podman AI Lab. Voir podman-desktop.io/docs/ai-lab/installing',
    'quarkus-chappie-openshift-instructions': 'Voir redhat.com/en/products/ai/openshift-ai',

    // Form labels
    'quarkus-chappie-api-key': 'Clé API',
    'quarkus-chappie-base-url': 'URL de base',
    'quarkus-chappie-model': 'Modèle',
    'quarkus-chappie-temperature': 'Température',
    'quarkus-chappie-timeout': 'Délai d\'expiration',
    'quarkus-chappie-save': 'Enregistrer',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'Stockage',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'Activer RAG',
    'quarkus-chappie-max-results': 'Résultats maximum',
    'quarkus-chappie-min-score': 'Score minimum',
    'quarkus-chappie-max-store-messages': 'Messages maximum stockés',
    'quarkus-chappie-enable-mcp': 'Activer MCP (Par défaut, si disponible, Dev MCP sera ajouté)',
    'quarkus-chappie-more-mcp-servers': 'Plus de serveurs MCP',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'Vous devez fournir une clé API',
    'quarkus-chappie-need-base-url': 'Vous devez fournir une URL de base',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'Détails du fournisseur non enregistrés. Voir le journal pour plus de détails',
    'quarkus-chappie-saved': 'Détails du fournisseur enregistrés.',
    'quarkus-chappie-clear-failed': 'Détails du fournisseur non effacés. Voir le journal pour plus de détails',
    'quarkus-chappie-cleared': 'Détails du fournisseur effacés.',

    // Exception page
    'quarkus-chappie-no-exception': 'Aucune exception détectée pour le moment.',
    'quarkus-chappie-check-now': 'Vérifier maintenant',
    'quarkus-chappie-talking-to-assistant': 'Communication avec l\'Assistant IA Quarkus...',
    'quarkus-chappie-cancel': 'Annuler',
    'quarkus-chappie-please-hold': 'Cela peut prendre un certain temps, veuillez patienter',
    'quarkus-chappie-suggested-fix': 'Solution suggérée par l\'Assistant IA Quarkus',
    'quarkus-chappie-diff': 'Diff',
    'quarkus-chappie-suggested-new-code': 'Nouveau code suggéré :',
    'quarkus-chappie-copy': 'Copier',
    'quarkus-chappie-discard': 'Abandonner',
    'quarkus-chappie-updated': str`${0} mis à jour`,
    'quarkus-chappie-no-content': 'Il n\'y a pas de contenu',
    'quarkus-chappie-content-copied': 'Contenu copié dans le presse-papiers',
    'quarkus-chappie-copy-failed': str`Échec de la copie du contenu : ${0}`,
    'quarkus-chappie-view-in-ide': 'Voir dans l\'IDE',
    'quarkus-chappie-suggest-fix': 'Suggérer une solution avec l\'IA',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Rechercher dans la documentation Quarkus',
    'quarkus-chappie-get-last-exception-desc': 'Obtient la dernière exception connue qui s\'est produite',
    'quarkus-chappie-help-latest-exception-desc': 'Aide avec la dernière exception',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'Obtenir de l\'aide avec ceci'
};
