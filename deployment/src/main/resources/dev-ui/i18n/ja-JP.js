import { str } from 'localization';

export const templates = {
    // Extension metadata
    'quarkus-chappie-meta-description': 'Quarkus開発のためのAIアシスタント',

    // Page titles
    'quarkus-chappie-chat': 'チャット',
    'quarkus-chappie-assistant': 'アシスタント',
    'quarkus-chappie-exception_help': '例外ヘルプ',

    // Chat page
    'quarkus-chappie-delete': '削除',
    'quarkus-chappie-new-chat': '新しいチャット',
    'quarkus-chappie-history': '履歴',
    'quarkus-chappie-welcome': 'アシスタントチャットへようこそ',
    'quarkus-chappie-not-configured': 'アシスタントが設定されていません。',
    'quarkus-chappie-configure-now': '今すぐ設定',
    'quarkus-chappie-thinking': '考え中 ...',
    'quarkus-chappie-error-see-log': 'エラーが発生しました - 詳細はアシスタントログを参照してください',
    'quarkus-chappie-error-see-log-suffix': '。詳細はアシスタントログを参照してください',
    'quarkus-chappie-you': 'あなた',
    'quarkus-chappie-assistant': 'アシスタント',
    'quarkus-chappie-error': 'エラー',
    'quarkus-chappie-suggested-mcp-tool': '推奨MCPツール',
    'quarkus-chappie-memory-id': 'メモリID: ',

    // Chat history
    'quarkus-chappie-nothing-yet': 'まだありません',

    // Configuration page
    'quarkus-chappie-config-intro': 'Quarkus開発モードでAIアシスタントを使用するには、AIプロバイダーを設定する必要があります',
    'quarkus-chappie-choose-provider': 'プロバイダーを選択',
    'quarkus-chappie-clear-configuration': '設定をクリア',

    // Provider names
    'quarkus-chappie-provider-openai': 'OpenAI',
    'quarkus-chappie-provider-openai-desc': 'ChatGPTなどの高度な言語モデルを構築し、インテリジェントなアプリケーションを駆動する大手AI企業。',
    'quarkus-chappie-provider-ollama': 'Ollama',
    'quarkus-chappie-provider-ollama-desc': '大規模な言語モデルをローカルで簡単に実行および管理するためのプラットフォーム。',
    'quarkus-chappie-provider-podman': 'Podman AI',
    'quarkus-chappie-provider-podman-desc': 'コンテナワークフローにAI機能を統合し、ローカルで安全なコンテナ化されたAIモデルのデプロイを可能にします。',
    'quarkus-chappie-provider-openshift': 'OpenShift AI',
    'quarkus-chappie-provider-openshift-desc': 'Red HatのエンタープライズAIプラットフォーム。OpenAI互換モデルを安全でスケーラブルなデプロイでサポート。',
    'quarkus-chappie-provider-generic': '汎用OpenAI互換',
    'quarkus-chappie-provider-generic-desc': '独自のAPIキーとベースURLを提供して、任意のOpenAI互換エンドポイントに接続します。',
    'quarkus-chappie-provider-gemini': 'Gemini',
    'quarkus-chappie-provider-gemini-desc': 'マルチモーダルタスクと複雑な推論に優れたGoogleの高度なAIモデル。',
    'quarkus-chappie-provider-anthropic': 'Anthropic',
    'quarkus-chappie-provider-anthropic-desc': 'Claudeを構築するAI安全性企業で、信頼性が高く、解釈可能で、制御可能なAIシステムに焦点を当てています。',
    'quarkus-chappie-provider-watsonx': 'WatsonX',
    'quarkus-chappie-provider-watsonx-desc': '機械学習モデルの構築、トレーニング、デプロイのためのIBMのエンタープライズAIおよびデータプラットフォーム。',

    // Provider instructions
    'quarkus-chappie-openai-instructions': 'OpenAIを使用するには、OpenAI APIキーを提供する必要があります',
    'quarkus-chappie-ollama-instructions': 'Ollamaを使用するには、ollamaをインストールして実行する必要があります。ollama.com/downloadを参照してください',
    'quarkus-chappie-podman-instructions': 'Podman AIを使用するには、podmanをインストールして実行する必要があります。podman-desktop.io/docs/installationを参照してください',
    'quarkus-chappie-podman-extension': 'Podman AI Lab拡張機能もインストールする必要があります。podman-desktop.io/docs/ai-lab/installingを参照してください',
    'quarkus-chappie-openshift-instructions': 'redhat.com/en/products/ai/openshift-aiを参照してください',
    'quarkus-chappie-gemini-instructions': 'Geminiを使用するには、Google AI StudioからGemini APIキーを提供する必要があります。',
    'quarkus-chappie-anthropic-instructions': 'Anthropicを使用するには、Anthropic APIキーを提供する必要があります。',
    'quarkus-chappie-watsonx-instructions': 'WatsonXを使用するには、APIキーとプロジェクトIDを提供する必要があります。オプションでベースURLまたはクラウドリージョンを提供してください。',

    // Form labels
    'quarkus-chappie-api-key': 'APIキー',
    'quarkus-chappie-base-url': 'ベースURL',
    'quarkus-chappie-model': 'モデル',
    'quarkus-chappie-temperature': '温度',
    'quarkus-chappie-timeout': 'タイムアウト',
    'quarkus-chappie-save': '保存',
    'quarkus-chappie-rag': 'RAG',
    'quarkus-chappie-storage': 'ストレージ',
    'quarkus-chappie-mcp': 'MCP',
    'quarkus-chappie-enable-rag': 'RAGを有効化',
    'quarkus-chappie-max-results': '最大結果数',
    'quarkus-chappie-min-score': '最小スコア',
    'quarkus-chappie-max-store-messages': '最大保存メッセージ数',
    'quarkus-chappie-enable-mcp': 'MCPを有効化（デフォルトで、利用可能な場合はDev MCPが追加されます）',
    'quarkus-chappie-more-mcp-servers': 'その他のMCPサーバー',
    'quarkus-chappie-project-id': 'プロジェクトID',
    'quarkus-chappie-cloud-region': 'クラウドリージョン',

    // Form placeholders
    'quarkus-chappie-api-key-placeholder': 'sk-....',
    'quarkus-chappie-base-url-placeholder-ollama': 'http://localhost:11434/',
    'quarkus-chappie-mcp-servers-placeholder': 'http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything',

    // Validation messages
    'quarkus-chappie-need-api-key': 'APIキーを提供する必要があります',
    'quarkus-chappie-need-base-url': 'ベースURLを提供する必要があります',
    'quarkus-chappie-need-api-key-project-id': 'APIキーとプロジェクトIDを提供する必要があります',

    // Save/notification messages
    'quarkus-chappie-save-failed': 'プロバイダーの詳細が保存されませんでした。詳細はログを参照してください',
    'quarkus-chappie-saved': 'プロバイダーの詳細が保存されました。',
    'quarkus-chappie-clear-failed': 'プロバイダーの詳細がクリアされませんでした。詳細はログを参照してください',
    'quarkus-chappie-cleared': 'プロバイダーの詳細がクリアされました。',

    // Exception page
    'quarkus-chappie-no-exception': 'まだ例外は検出されていません。',
    'quarkus-chappie-check-now': '今すぐ確認',
    'quarkus-chappie-talking-to-assistant': 'Quarkus AIアシスタントと通信中...',
    'quarkus-chappie-cancel': 'キャンセル',
    'quarkus-chappie-please-hold': '時間がかかる場合があります。お待ちください',
    'quarkus-chappie-suggested-fix': 'Quarkus AIアシスタントからの提案された修正',
    'quarkus-chappie-diff': '差分',
    'quarkus-chappie-suggested-new-code': '提案された新しいコード:',
    'quarkus-chappie-copy': 'コピー',
    'quarkus-chappie-discard': '破棄',
    'quarkus-chappie-updated': str`${0}を更新しました`,
    'quarkus-chappie-no-content': 'コンテンツがありません',
    'quarkus-chappie-content-copied': 'コンテンツをクリップボードにコピーしました',
    'quarkus-chappie-copy-failed': str`コンテンツのコピーに失敗しました: ${0}`,
    'quarkus-chappie-view-in-ide': 'IDEで表示',
    'quarkus-chappie-suggest-fix': 'AIで修正を提案',

    // Build action descriptions
    'quarkus-chappie-search-docs-desc': 'Quarkusドキュメントを検索',
    'quarkus-chappie-get-last-exception-desc': '発生した最後の既知の例外を取得します',
    'quarkus-chappie-help-latest-exception-desc': '最新の例外に関するヘルプ',

    // Error page action
    'quarkus-chappie-get-help-with-this': 'これに関するヘルプを取得'
};
