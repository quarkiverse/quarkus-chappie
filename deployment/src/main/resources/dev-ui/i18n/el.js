import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'AI Βοηθός για ανάπτυξη Quarkus',

    // Page titles
    'quarkus-chappie-chat': 'Συνομιλία',
    'quarkus-chappie-assistant': 'Βοηθός',
    'quarkus-chappie-exception_help': 'Βοήθεια με εξαιρέσεις',

    // Chat page
    'quarkus-chappie-delete': 'Διαγραφή',
    'quarkus-chappie-new-chat': 'Νέα συνομιλία',
    'quarkus-chappie-history': 'Ιστορικό',
    'quarkus-chappie-welcome': 'Καλώς ήρθατε στη Συνομιλία Βοηθού',
    'quarkus-chappie-not-configured': 'Ο βοηθός δεν έχει ρυθμιστεί.',
    'quarkus-chappie-configure-now': 'Ρύθμιση τώρα',
    'quarkus-chappie-thinking': 'Σκέφτομαι ...',
    'quarkus-chappie-error-see-log': 'Προέκυψε σφάλμα - δείτε το αρχείο καταγραφής του Βοηθού για λεπτομέρειες',
    'quarkus-chappie-error-see-log-suffix': '. Δείτε το αρχείο καταγραφής του Βοηθού για λεπτομέρειες',
    'quarkus-chappie-you': 'Εσείς',
    'quarkus-chappie-assistant': 'Βοηθός',
    'quarkus-chappie-error': 'Σφάλμα',
    'quarkus-chappie-suggested-mcp-tool': 'Προτεινόμενο Εργαλείο MCP',
    'quarkus-chappie-memory-id': 'Αναγνωριστικό μνήμης: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'Τίποτα ακόμα',

    // Configuration page
    'quarkus-chappie-config-intro': 'Για να χρησιμοποιήσετε τον AI Βοηθό στη Λειτουργία Ανάπτυξης Quarkus, πρέπει να ρυθμίσετε έναν πάροχο AI',
    'quarkus-chappie-choose-provider': 'Επιλογή παρόχου',
    'quarkus-chappie-clear-configuration': 'Εκκαθάριση ρυθμίσεων',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'Κορυφαία εταιρεία AI που δημιουργεί προηγμένα γλωσσικά μοντέλα όπως το ChatGPT για την τροφοδοσία έξυπνων εφαρμογών.',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': 'Μια πλατφόρμα για την εκτέλεση και διαχείριση μεγάλων γλωσσικών μοντέλων τοπικά με ευκολία.',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'Ενσωματώνει χαρακτηριστικά AI σε ροές εργασίας containers, επιτρέποντας την τοπική, ασφαλή και containerized ανάπτυξη μοντέλων AI.',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Η εταιρική πλατφόρμα AI της Red Hat, υποστηρίζοντας μοντέλα συμβατά με OpenAI με ασφαλείς, επεκτάσιμες αναπτύξεις.',
    'quarkus-chappie-provider-generic': 'Γενικό Συμβατό με OpenAI',
    'quarkus-chappie-provider-generic-desc': 'Συνδεθείτε σε οποιοδήποτε τελικό σημείο συμβατό με OpenAI παρέχοντας το δικό σας κλειδί API και βασική διεύθυνση URL.',
    'quarkus-chappie-provider-gemini': 'Gemini',
    'quarkus-chappie-provider-gemini-desc': 'Προηγμένο μοντέλο AI της Google που διαπρέπει σε πολυτροπικές εργασίες και σύνθετη συλλογιστική.',
    'quarkus-chappie-provider-anthropic': 'Anthropic',
    'quarkus-chappie-provider-anthropic-desc': 'Εταιρεία ασφάλειας AI που κατασκευάζει το Claude, επικεντρωμένη σε αξιόπιστα, ερμηνεύσιμα και ελέγξιμα συστήματα AI.',
    'quarkus-chappie-provider-watsonx': 'WatsonX',
    'quarkus-chappie-provider-watsonx-desc': 'Εταιρική πλατφόρμα AI και δεδομένων της IBM για δημιουργία, εκπαίδευση και ανάπτυξη μοντέλων μηχανικής μάθησης.',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'Για να χρησιμοποιήσετε το OpenAI πρέπει να παρέχετε ένα κλειδί API του OpenAI',
    'quarkus-chappie-ollama-instructions': 'Για να χρησιμοποιήσετε το Ollama πρέπει να εγκαταστήσετε και να εκτελέσετε το ollama. Δείτε το ollama.com/download',
    'quarkus-chappie-podman-instructions': 'Για να χρησιμοποιήσετε το Podman AI πρέπει να εγκαταστήσετε και να εκτελέσετε το podman. Δείτε το podman-desktop.io/docs/installation',
    'quarkus-chappie-podman-extension': 'Πρέπει επίσης να εγκαταστήσετε την επέκταση Podman AI Lab. Δείτε το podman-desktop.io/docs/ai-lab/installing',
    'quarkus-chappie-openshift-instructions': 'Δείτε το redhat.com/en/products/ai/openshift-ai',
    'quarkus-chappie-gemini-instructions': 'Για να χρησιμοποιήσετε το Gemini πρέπει να παρέχετε ένα κλειδί API Gemini από το Google AI Studio.',
    'quarkus-chappie-anthropic-instructions': 'Για να χρησιμοποιήσετε το Anthropic πρέπει να παρέχετε ένα κλειδί API Anthropic.',
    'quarkus-chappie-watsonx-instructions': 'Για να χρησιμοποιήσετε το WatsonX πρέπει να παρέχετε ένα κλειδί API και αναγνωριστικό έργου. Προαιρετικά παρέχετε μια βασική διεύθυνση URL ή περιοχή cloud.',

    // Form labels
    'quarkus-chappie-api-key': 'Κλειδί API',
    'quarkus-chappie-base-url': 'Βασική διεύθυνση URL',
    'quarkus-chappie-model': 'Μοντέλο',
    'quarkus-chappie-temperature': 'Θερμοκρασία',
    'quarkus-chappie-timeout': 'Χρονικό όριο',
    'quarkus-chappie-save': 'Αποθήκευση',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'Αποθήκευση',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'Ενεργοποίηση RAG',
    'quarkus-chappie-max-results': 'Μέγιστα αποτελέσματα',
    'quarkus-chappie-min-score': 'Ελάχιστη βαθμολογία',
    'quarkus-chappie-max-store-messages': 'Μέγιστα αποθηκευμένα μηνύματα',
    'quarkus-chappie-enable-mcp': 'Ενεργοποίηση MCP (Από προεπιλογή, εάν διατίθεται, θα προστεθεί το Dev MCP)',
    'quarkus-chappie-more-mcp-servers': 'Περισσότεροι διακομιστές MCP',
    'quarkus-chappie-project-id': 'Αναγνωριστικό Έργου',
    'quarkus-chappie-cloud-region': 'Περιοχή Cloud',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'Πρέπει να παρέχετε ένα κλειδί API',
    'quarkus-chappie-need-base-url': 'Πρέπει να παρέχετε μια βασική διεύθυνση URL',
    'quarkus-chappie-need-api-key-project-id': 'Πρέπει να παρέχετε ένα κλειδί API και αναγνωριστικό έργου',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'Οι λεπτομέρειες του παρόχου δεν αποθηκεύτηκαν. Δείτε το αρχείο καταγραφής για λεπτομέρειες',
    'quarkus-chappie-saved': 'Οι λεπτομέρειες του παρόχου αποθηκεύτηκαν.',
    'quarkus-chappie-clear-failed': 'Οι λεπτομέρειες του παρόχου δεν εκκαθαρίστηκαν. Δείτε το αρχείο καταγραφής για λεπτομέρειες',
    'quarkus-chappie-cleared': 'Οι λεπτομέρειες του παρόχου εκκαθαρίστηκαν.',

    // Exception page
    'quarkus-chappie-no-exception': 'Δεν έχει εντοπιστεί καμία εξαίρεση ακόμα.',
    'quarkus-chappie-check-now': 'Έλεγχος τώρα',
    'quarkus-chappie-talking-to-assistant': 'Επικοινωνία με τον AI Βοηθό Quarkus...',
    'quarkus-chappie-cancel': 'Ακύρωση',
    'quarkus-chappie-please-hold': 'Αυτό μπορεί να πάρει λίγο χρόνο, παρακαλώ περιμένετε',
    'quarkus-chappie-suggested-fix': 'Προτεινόμενη επιδιόρθωση από τον AI Βοηθό Quarkus',
    'quarkus-chappie-diff': 'Διαφορές',
    'quarkus-chappie-suggested-new-code': 'Προτεινόμενος νέος κώδικας:',
    'quarkus-chappie-copy': 'Αντιγραφή',
    'quarkus-chappie-discard': 'Απόρριψη',
    'quarkus-chappie-updated': str`Ενημερώθηκε ${0}`,
    'quarkus-chappie-no-content': 'Δεν υπάρχει περιεχόμενο',
    'quarkus-chappie-content-copied': 'Το περιεχόμενο αντιγράφηκε στο πρόχειρο',
    'quarkus-chappie-copy-failed': str`Αποτυχία αντιγραφής περιεχομένου: ${0}`,
    'quarkus-chappie-view-in-ide': 'Προβολή στο IDE',
    'quarkus-chappie-suggest-fix': 'Πρόταση επιδιόρθωσης με AI',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Αναζήτηση τεκμηρίωσης Quarkus',
    'quarkus-chappie-get-last-exception-desc': 'Λαμβάνει την τελευταία γνωστή εξαίρεση που συνέβη',
    'quarkus-chappie-help-latest-exception-desc': 'Βοήθεια με την τελευταία εξαίρεση',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'Λήψη βοήθειας για αυτό'
};
