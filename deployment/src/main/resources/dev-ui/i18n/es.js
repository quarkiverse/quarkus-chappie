import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'Asistente de IA para desarrollo Quarkus',

    // Page titles
    'quarkus-chappie-chat': 'Chat',
    'quarkus-chappie-assistant': 'Asistente',
    'quarkus-chappie-exception_help': 'Ayuda con excepciones',

    // Chat page
    'quarkus-chappie-delete': 'Eliminar',
    'quarkus-chappie-new-chat': 'Nuevo chat',
    'quarkus-chappie-history': 'Historial',
    'quarkus-chappie-welcome': 'Bienvenido al Chat del Asistente',
    'quarkus-chappie-not-configured': 'El asistente no está configurado.',
    'quarkus-chappie-configure-now': 'Configurar ahora',
    'quarkus-chappie-thinking': 'Pensando ...',
    'quarkus-chappie-error-see-log': 'Ocurrió un error - consulte el registro del Asistente para más detalles',
    'quarkus-chappie-error-see-log-suffix': '. Consulte el registro del Asistente para más detalles',
    'quarkus-chappie-you': 'Tú',
    'quarkus-chappie-assistant': 'Asistente',
    'quarkus-chappie-error': 'Error',
    'quarkus-chappie-suggested-mcp-tool': 'Herramienta MCP sugerida',
    'quarkus-chappie-memory-id': 'ID de memoria: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'Nada todavía',

    // Configuration page
    'quarkus-chappie-config-intro': 'Para usar el Asistente de IA en el Modo de Desarrollo de Quarkus, debe configurar un proveedor de IA',
    'quarkus-chappie-choose-provider': 'Elegir proveedor',
    'quarkus-chappie-clear-configuration': 'Limpiar configuración',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'Empresa líder en IA que construye modelos de lenguaje avanzados como ChatGPT para impulsar aplicaciones inteligentes.',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'Una plataforma para ejecutar y gestionar modelos de lenguaje grandes localmente con facilidad.',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'Integra funciones de IA en flujos de trabajo de contenedores, permitiendo el despliegue local, seguro y en contenedores de modelos de IA.',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Plataforma de IA empresarial de Red Hat, que soporta modelos compatibles con OpenAI con despliegues seguros y escalables.',
    'quarkus-chappie-provider-generic': 'Compatible con OpenAI Genérico',
    'quarkus-chappie-provider-generic-desc': 'Conecte cualquier punto final compatible con OpenAI proporcionando su propia clave API y URL base.',
    'quarkus-chappie-provider-gemini': 'Gemini',
    'quarkus-chappie-provider-gemini-desc': 'Modelo de IA avanzado de Google que destaca en tareas multimodales y razonamiento complejo.',
    'quarkus-chappie-provider-anthropic': 'Anthropic',
    'quarkus-chappie-provider-anthropic-desc': 'Empresa de seguridad de IA que construye Claude, enfocada en sistemas de IA confiables, interpretables y dirigibles.',
    'quarkus-chappie-provider-watsonx': 'WatsonX',
    'quarkus-chappie-provider-watsonx-desc': 'Plataforma empresarial de IA y datos de IBM para construir, entrenar e implementar modelos de aprendizaje automático.',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'Para usar OpenAI necesita proporcionar una clave API de OpenAI',
    'quarkus-chappie-ollama-instructions': 'Para usar Ollama necesita instalar y ejecutar ollama. Vea ollama.com/download',
    'quarkus-chappie-podman-instructions': 'Para usar Podman AI necesita instalar y ejecutar podman. Vea podman-desktop.io/docs/installation',
    'quarkus-chappie-podman-extension': 'También necesita instalar la extensión Podman AI Lab. Vea podman-desktop.io/docs/ai-lab/installing',
    'quarkus-chappie-openshift-instructions': 'Vea redhat.com/en/products/ai/openshift-ai',
    'quarkus-chappie-gemini-instructions': 'Para usar Gemini necesitas proporcionar una clave de API de Gemini desde Google AI Studio.',
    'quarkus-chappie-anthropic-instructions': 'Para usar Anthropic necesitas proporcionar una clave de API de Anthropic.',
    'quarkus-chappie-watsonx-instructions': 'Para usar WatsonX necesitas proporcionar una clave de API y un ID de proyecto. Opcionalmente proporciona una URL base o región de nube.',

    // Form labels
    'quarkus-chappie-api-key': 'Clave API',
    'quarkus-chappie-base-url': 'URL base',
    'quarkus-chappie-model': 'Modelo',
    'quarkus-chappie-temperature': 'Temperatura',
    'quarkus-chappie-timeout': 'Tiempo de espera',
    'quarkus-chappie-save': 'Guardar',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'Almacenamiento',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'Habilitar RAG',
    'quarkus-chappie-max-results': 'Resultados máximos',
    'quarkus-chappie-min-score': 'Puntuación mínima',
    'quarkus-chappie-max-store-messages': 'Mensajes máximos almacenados',
    'quarkus-chappie-enable-mcp': 'Habilitar MCP (Por defecto, si está disponible, se agregará Dev MCP)',
    'quarkus-chappie-more-mcp-servers': 'Más servidores MCP',
    'quarkus-chappie-project-id': 'ID de Proyecto',
    'quarkus-chappie-cloud-region': 'Región de Nube',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'Debe proporcionar una clave API',
    'quarkus-chappie-need-base-url': 'Debe proporcionar una URL base',
    'quarkus-chappie-need-api-key-project-id': 'Necesitas proporcionar una clave de API y un ID de proyecto',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'No se guardaron los detalles del proveedor. Consulte el registro para más detalles',
    'quarkus-chappie-saved': 'Detalles del proveedor guardados.',
    'quarkus-chappie-clear-failed': 'No se limpiaron los detalles del proveedor. Consulte el registro para más detalles',
    'quarkus-chappie-cleared': 'Detalles del proveedor limpiados.',

    // Exception page
    'quarkus-chappie-no-exception': 'Aún no se ha detectado ninguna excepción.',
    'quarkus-chappie-check-now': 'Verificar ahora',
    'quarkus-chappie-talking-to-assistant': 'Comunicándose con el Asistente de IA de Quarkus...',
    'quarkus-chappie-cancel': 'Cancelar',
    'quarkus-chappie-please-hold': 'Esto puede tardar un poco, por favor espere',
    'quarkus-chappie-suggested-fix': 'Solución sugerida por el Asistente de IA de Quarkus',
    'quarkus-chappie-diff': 'Diff',
    'quarkus-chappie-suggested-new-code': 'Nuevo código sugerido:',
    'quarkus-chappie-copy': 'Copiar',
    'quarkus-chappie-discard': 'Descartar',
    'quarkus-chappie-updated': str`${0} actualizado`,
    'quarkus-chappie-no-content': 'No hay contenido',
    'quarkus-chappie-content-copied': 'Contenido copiado al portapapeles',
    'quarkus-chappie-copy-failed': str`Error al copiar contenido: ${0}`,
    'quarkus-chappie-view-in-ide': 'Ver en IDE',
    'quarkus-chappie-suggest-fix': 'Sugerir solución con IA',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Buscar documentación de Quarkus',
    'quarkus-chappie-get-last-exception-desc': 'Obtiene la última excepción conocida que ocurrió',
    'quarkus-chappie-help-latest-exception-desc': 'Ayuda con la última excepción',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'Obtener ayuda con esto'
};
