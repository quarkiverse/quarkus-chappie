import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'Quarkus विकास के लिए AI सहायक',

    // Page titles
    'quarkus-chappie-chat': 'चैट',
    'quarkus-chappie-assistant': 'सहायक',
    'quarkus-chappie-exception_help': 'अपवाद सहायता',

    // Chat page
    'quarkus-chappie-delete': 'हटाएं',
    'quarkus-chappie-new-chat': 'नई चैट',
    'quarkus-chappie-history': 'इतिहास',
    'quarkus-chappie-welcome': 'सहायक चैट में आपका स्वागत है',
    'quarkus-chappie-not-configured': 'सहायक कॉन्फ़िगर नहीं है।',
    'quarkus-chappie-configure-now': 'अभी कॉन्फ़िगर करें',
    'quarkus-chappie-thinking': 'सोच रहे हैं ...',
    'quarkus-chappie-error-see-log': 'एक त्रुटि हुई - विवरण के लिए सहायक लॉग देखें',
    'quarkus-chappie-error-see-log-suffix': '। विवरण के लिए सहायक लॉग देखें',
    'quarkus-chappie-you': 'आप',
    'quarkus-chappie-assistant': 'सहायक',
    'quarkus-chappie-error': 'त्रुटि',
    'quarkus-chappie-suggested-mcp-tool': 'सुझाया गया MCP उपकरण',
    'quarkus-chappie-memory-id': 'मेमोरी ID: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'अभी तक कुछ नहीं',

    // Configuration page
    'quarkus-chappie-config-intro': 'Quarkus डेव मोड में AI सहायक का उपयोग करने के लिए, आपको एक AI प्रदाता कॉन्फ़िगर करना होगा',
    'quarkus-chappie-choose-provider': 'प्रदाता चुनें',
    'quarkus-chappie-clear-configuration': 'कॉन्फ़िगरेशन साफ़ करें',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'अग्रणी AI कंपनी जो बुद्धिमान अनुप्रयोगों को शक्ति देने के लिए ChatGPT जैसे उन्नत भाषा मॉडल बनाती है।',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'स्थानीय रूप से बड़े भाषा मॉडल को आसानी से चलाने और प्रबंधित करने के लिए एक प्लेटफ़ॉर्म।',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'कंटेनर वर्कफ़्लो में AI सुविधाओं को एकीकृत करता है, स्थानीय, सुरक्षित और कंटेनरीकृत AI मॉडल तैनाती को सक्षम करता है।',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Red Hat का एंटरप्राइज़ AI प्लेटफ़ॉर्म, सुरक्षित, स्केलेबल तैनाती के साथ OpenAI-संगत मॉडल का समर्थन करता है।',
    'quarkus-chappie-provider-generic': 'सामान्य OpenAI-संगत',
    'quarkus-chappie-provider-generic-desc': 'अपनी खुद की API कुंजी और बेस URL प्रदान करके किसी भी OpenAI-संगत एंडपॉइंट से कनेक्ट करें।',
    'quarkus-chappie-provider-gemini': 'Gemini',
    'quarkus-chappie-provider-gemini-desc': 'Google का उन्नत AI मॉडल जो मल्टीमॉडल कार्यों और जटिल तर्क में उत्कृष्ट है।',
    'quarkus-chappie-provider-anthropic': 'Anthropic',
    'quarkus-chappie-provider-anthropic-desc': 'AI सुरक्षा कंपनी जो Claude बनाती है, विश्वसनीय, व्याख्यात्मक और नियंत्रणीय AI प्रणालियों पर केंद्रित।',
    'quarkus-chappie-provider-watsonx': 'WatsonX',
    'quarkus-chappie-provider-watsonx-desc': 'मशीन लर्निंग मॉडल बनाने, प्रशिक्षित करने और तैनात करने के लिए IBM का उद्यम AI और डेटा प्लेटफ़ॉर्म।',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'OpenAI का उपयोग करने के लिए आपको एक OpenAI API कुंजी प्रदान करनी होगी',
    'quarkus-chappie-ollama-instructions': 'Ollama का उपयोग करने के लिए आपको ollama इंस्टॉल और चलाना होगा। ollama.com/download देखें',
    'quarkus-chappie-podman-instructions': 'Podman AI का उपयोग करने के लिए आपको podman इंस्टॉल और चलाना होगा। podman-desktop.io/docs/installation देखें',
    'quarkus-chappie-podman-extension': 'आपको Podman AI Lab एक्सटेंशन भी इंस्टॉल करना होगा। podman-desktop.io/docs/ai-lab/installing देखें',
    'quarkus-chappie-openshift-instructions': 'redhat.com/en/products/ai/openshift-ai देखें',
    'quarkus-chappie-gemini-instructions': 'Gemini का उपयोग करने के लिए आपको Google AI Studio से Gemini API कुंजी प्रदान करनी होगी।',
    'quarkus-chappie-anthropic-instructions': 'Anthropic का उपयोग करने के लिए आपको Anthropic API कुंजी प्रदान करनी होगी।',
    'quarkus-chappie-watsonx-instructions': 'WatsonX का उपयोग करने के लिए आपको API कुंजी और प्रोजेक्ट ID प्रदान करना होगा। वैकल्पिक रूप से बेस URL या क्लाउड क्षेत्र प्रदान करें।',

    // Form labels
    'quarkus-chappie-api-key': 'API कुंजी',
    'quarkus-chappie-base-url': 'बेस URL',
    'quarkus-chappie-model': 'मॉडल',
    'quarkus-chappie-temperature': 'तापमान',
    'quarkus-chappie-timeout': 'टाइमआउट',
    'quarkus-chappie-save': 'सहेजें',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'संग्रहण',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'RAG सक्षम करें',
    'quarkus-chappie-max-results': 'अधिकतम परिणाम',
    'quarkus-chappie-min-score': 'न्यूनतम स्कोर',
    'quarkus-chappie-max-store-messages': 'अधिकतम संग्रहीत संदेश',
    'quarkus-chappie-enable-mcp': 'MCP सक्षम करें (डिफ़ॉल्ट रूप से, यदि उपलब्ध हो, तो Dev MCP जोड़ा जाएगा)',
    'quarkus-chappie-more-mcp-servers': 'अधिक MCP सर्वर',
    'quarkus-chappie-project-id': 'प्रोजेक्ट ID',
    'quarkus-chappie-cloud-region': 'क्लाउड क्षेत्र',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'आपको एक API कुंजी प्रदान करनी होगी',
    'quarkus-chappie-need-base-url': 'आपको एक बेस URL प्रदान करना होगा',
    'quarkus-chappie-need-api-key-project-id': 'आपको API कुंजी और प्रोजेक्ट ID प्रदान करना होगा',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'प्रदाता विवरण सहेजे नहीं गए। विवरण के लिए लॉग देखें',
    'quarkus-chappie-saved': 'प्रदाता विवरण सहेजे गए।',
    'quarkus-chappie-clear-failed': 'प्रदाता विवरण साफ़ नहीं हुए। विवरण के लिए लॉग देखें',
    'quarkus-chappie-cleared': 'प्रदाता विवरण साफ़ हो गए।',

    // Exception page
    'quarkus-chappie-no-exception': 'अभी तक कोई अपवाद नहीं मिला।',
    'quarkus-chappie-check-now': 'अभी जांचें',
    'quarkus-chappie-talking-to-assistant': 'Quarkus AI सहायक से बात कर रहे हैं...',
    'quarkus-chappie-cancel': 'रद्द करें',
    'quarkus-chappie-please-hold': 'इसमें कुछ समय लग सकता है, कृपया प्रतीक्षा करें',
    'quarkus-chappie-suggested-fix': 'Quarkus AI सहायक से सुझाया गया समाधान',
    'quarkus-chappie-diff': 'अंतर',
    'quarkus-chappie-suggested-new-code': 'सुझाया गया नया कोड:',
    'quarkus-chappie-copy': 'कॉपी करें',
    'quarkus-chappie-discard': 'त्यागें',
    'quarkus-chappie-updated': str`${0} अपडेट किया गया`,
    'quarkus-chappie-no-content': 'कोई सामग्री नहीं है',
    'quarkus-chappie-content-copied': 'सामग्री क्लिपबोर्ड पर कॉपी की गई',
    'quarkus-chappie-copy-failed': str`सामग्री कॉपी करने में विफल: ${0}`,
    'quarkus-chappie-view-in-ide': 'IDE में देखें',
    'quarkus-chappie-suggest-fix': 'AI से समाधान सुझाएं',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Quarkus दस्तावेज़ीकरण खोजें',
    'quarkus-chappie-get-last-exception-desc': 'हुआ अंतिम ज्ञात अपवाद प्राप्त करता है',
    'quarkus-chappie-help-latest-exception-desc': 'नवीनतम अपवाद के साथ सहायता',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'इसके साथ सहायता प्राप्त करें'
};
