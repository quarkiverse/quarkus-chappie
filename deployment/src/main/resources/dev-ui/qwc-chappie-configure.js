import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import { msg, str, updateWhenLocaleChanges } from 'localization';
import '@qomponent/qui-code-block';
import '@vaadin/combo-box';
import { comboBoxRenderer } from '@vaadin/combo-box/lit.js';
import '@vaadin/avatar';
import '@vaadin/checkbox';
import '@vaadin/form-layout';
import '@vaadin/password-field';
import '@vaadin/text-field';
import '@vaadin/number-field';
import '@vaadin/details';
import { notifier } from 'notifier';
import { RouterController } from 'router-controller';
import { assistantState } from 'assistant-state';
import { themeState } from 'theme-state';
import { observeState } from 'lit-element-state';

/**
 * This component allows Chappie configuration
 */
export class QwcChappieConfigure extends observeState(QwcHotReloadElement) { 
    jsonRpc = new JsonRpc(this);
    routerController = new RouterController(this);
    
    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            height: 100%;
            padding: 10px;
        }
    
        .steps {
            display: flex;
            flex-direction: column;
        }
    
        .step1{
            display: flex;
            flex-direction: column;
            padding: 20px 20px;
        }
    
        .step1intro {
            display: flex;
            justify-content: space-between;
        }
        .step2{
            display: flex;
            flex-direction: column;
            gap: 10px;
            padding: 20px 20px;
        }
    
        .formheader{
            display: flex;
            gap: 20px;
            border-top: 1px dotted var(--lumo-contrast-10pct);
            align-items: center;
        }
    
        .form {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }
    
        .subText {
            color: var(--lumo-contrast-40pct);
        }
    
        a {
            color: var(--lumo-body-text-color);
        }
        .code {
            color: var(--lumo-primary-text-color);
            font-family: Arial;
        }
        .unlistedLinks {
            display: flex;
            gap: 30px;
            justify-content: flex-end;
        }
        .unlistedLink {
            cursor: pointer;
        }
        
        .unlistedLink:hover {
            filter: brightness(150%);
        }
    
        .fullWidth {
            width: 100%;
        }
    
        .rag{
            display: flex;
            align-items: baseline;
            gap: 25px;
        }
        .storage {
            display: flex;
            align-items: baseline;
            gap: 25px;
        }
        .mcp{
            
        }
    
        .tt {
            display: flex;
            justify-content: space-between;
            gap: 5px;
        }
    `;
    
    static properties = {
        namespace: {type: String},
        _allProviders: {state: true},
        _selectedProvider: { state: true },
        _loadedConfiguration: { state: true },
        _navigateBack: {state: true}
    };

    constructor() {
        super();
        updateWhenLocaleChanges(this);
        this._navigateBack = null;
        this._ragDefaults = {
            ragMaxResults: "4",
            ragMinScore: "0.82",
            ragEnabled: "true"
        };
        
        this._mcpDefaults = {
            mcpEnabled: "true"
        };
        
        this._storeDefaults = {
            storeMaxMessages: "30"
        };
        
        this._allProviders = [
            {
                name: msg('OpenAI', { id: 'quarkus-chappie-provider-openai' }),
                description: msg('Leading AI company that builds advanced language models like ChatGPT to power intelligent applications.', { id: 'quarkus-chappie-provider-openai-desc' }),
                defaultModel: "gpt-5-mini",
                defaultTemperature: "1",
                defaultTimeout: "PT120S",
                defaultBaseUrl: ""
            },
            {
                name: msg('Ollama', { id: 'quarkus-chappie-provider-ollama' }),
                description: msg('A platform for running and managing large language models locally with ease.', { id: 'quarkus-chappie-provider-ollama-desc' }),
                defaultModel: "codellama",
                defaultTemperature: "0.0",
                defaultTimeout: "PT120S",
                defaultBaseUrl: "http://localhost:11434/"
            },
            {
                name: msg('Podman AI', { id: 'quarkus-chappie-provider-podman' }),
                description: msg('Integrates AI features into container workflows, enabling local, secure, and containerized AI model deployment.', { id: 'quarkus-chappie-provider-podman-desc' }),
                defaultModel: "llama3:instruct",
                defaultTemperature: "0.2",
                defaultTimeout: "PT120S",
                defaultBaseUrl: ""
            },
            {
                name: msg('OpenShift AI', { id: 'quarkus-chappie-provider-openshift' }),
                description: msg('Red Hat\'s enterprise AI platform, supporting OpenAI-compatible models with secure, scalable deployments.', { id: 'quarkus-chappie-provider-openshift-desc' }),
                defaultModel: "",
                defaultTemperature: "0.2",
                defaultTimeout: "PT120S",
                defaultBaseUrl: ""
            },
            {
                name: msg('Generic OpenAI-Compatible', { id: 'quarkus-chappie-provider-generic' }),
                description: msg('Connect any OpenAI-compatible endpoint by providing your own base URL and API key.', { id: 'quarkus-chappie-provider-generic-desc' }),
                defaultModel: "llama3:instruct",
                defaultTemperature: "0.2",
                defaultTimeout: "PT120S",
                defaultBaseUrl: ""
            }
        ];
        
        this._selectedProvider = null;
        this._loadedConfiguration = null;
    }

    connectedCallback() {
        super.connectedCallback();
        if(!this._loadedConfiguration)this._loadConfiguration();
        
        const urlParams = new URLSearchParams(window.location.search);
        this._navigateBack = urlParams.get('back');
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
    }
    
    hotReload(){
        this._loadConfiguration();
    }
    
    render() { 
        return html`<div class="steps">
                ${this._renderStep1()}
                ${this._renderStep2()}
                </div>
        `;
    }
    
    _renderStep1(){
        return html`<div class="step1">
                    <div class="step1intro">
                        <span>${msg('To use the AI Assistant in Quarkus Dev Mode, you need to configure an AI provider', { id: 'quarkus-chappie-config-intro' })}</span>
                        ${this._renderClearButton()}
                    </div>

                    <vaadin-combo-box
                        label="${msg('Choose provider', { id: 'quarkus-chappie-choose-provider' })}"
                        item-label-path="name"
                        item-value-path="name"
                        .items="${this._allProviders}"
                        .value="${this._selectedProvider?.name}"
                        style="--vaadin-combo-box-overlay-width: 64em"
                        @value-changed="${this._providerChanged}"
                        ${comboBoxRenderer(this._providerComboRenderer, [])}
                    ></vaadin-combo-box>
                    `;
    }

    _renderClearButton(){
        if(assistantState.current.isConfigured){
            return html`<vaadin-button theme="icon secondary error" title="${msg('Clear configuration', { id: 'quarkus-chappie-clear-configuration' })}" @click="${this._clearConfiguration}">
                            <vaadin-icon icon="font-awesome-solid:trash"></vaadin-icon>
                        </vaadin-button>`;
        }
    }
    
    _renderStep2(){
        if(this._selectedProvider){
            return html`<div class="step2">
                <div class="formheader">
                    ${this._renderLogo()}
                    <h3>${this._selectedProvider.name}</h3>
                </div>
                <div class="form">
                    ${this._renderProviderForm()}
                </div>
                ${this._renderUnlistedPages()}
            `;
        }
            
    }
    
    _renderUnlistedPages() {
        if(assistantState.current.isConfigured) {
            let unlistedPages = this.routerController.getPagesForNamespace(this.namespace);
            return html`<div class="unlistedLinks">
                        ${unlistedPages.map((page) =>
                            html`${this._renderUnlistedPageLink(page)}`
                        )}
                    </div>`;
            
            
        }
    }
    
    _renderUnlistedPageLink(page){
        return html`<div class="unlistedLink" style="color:${page.color};" @click=${() => this._navigateToPage(page)}>
                        <vaadin-icon icon="${page.icon}"></vaadin-icon> <span>${page.title}</span>
                    </div>`;
        
    }
    
    _navigateToPage(page){
        window.dispatchEvent(new CustomEvent('close-settings-dialog'));
        this.routerController.go(page);
    }
    
    _renderLogo(name){
        if(!name && this._selectedProvider && this._selectedProvider.name)name = this._selectedProvider.name;
        
        if(name){
            let logo = "./" + name.replace(/ /g, "_") + "_" + themeState.theme.name + ".svg";
            if(logo){
                return html`<img src="${logo}" height="45" @error="${(e) => e.target.style.display = 'none'}">`;
            }
        }
    }
    
    _renderProviderForm(){
        if(this._selectedProvider.name === "OpenAI"){
            return this._renderOpenAI();
        } else if(this._selectedProvider.name === "Ollama"){
            return this._renderOllama();
        } else if(this._selectedProvider.name === "Podman AI"){
            return this._renderPodmanAI();
        } else if(this._selectedProvider.name === "OpenShift AI"){
            return this._renderOpenShiftAI();
        } else if(this._selectedProvider.name === "Generic OpenAI-Compatible"){
            return this._renderGeneric();
        }   
    }
    
    _renderOpenAI(){
        return html`
            <div class="subText">
                ${msg('To use OpenAI you need to provide an OpenAI Api Key', { id: 'quarkus-chappie-openai-instructions' })}
            </div>

            ${this._renderApiKeyInput('openai', true, msg('sk-....', { id: 'quarkus-chappie-api-key-placeholder' }))}
            ${this._renderModelTemperatureAndTimeoutInput('openai')}
            ${this._renderCommonSettings()}

            <vaadin-button
                theme="primary"
                @click="${this._saveOpenAIConfig}">
                ${msg('Save', { id: 'quarkus-chappie-save' })}
            </vaadin-button>
        `;
    }
    
    _renderOllama(){
        return html`
            <div class="subText">
                ${msg('To use Ollama you need to install and run ollama. See ollama.com/download', { id: 'quarkus-chappie-ollama-instructions' })}
            </div>

            ${this._renderBaseUrlInput('ollama', true)}
            ${this._renderModelTemperatureAndTimeoutInput('ollama')}
            ${this._renderCommonSettings()}

            <vaadin-button
                theme="primary"
                @click="${this._saveOllamaConfig}">
                ${msg('Save', { id: 'quarkus-chappie-save' })}
            </vaadin-button>
        `;
    }
    
    _renderPodmanAI(){
        return html`
            <div class="subText">
                ${msg('To use Podman AI you need to install and run podman. See podman-desktop.io/docs/installation', { id: 'quarkus-chappie-podman-instructions' })}
                ${msg('You also need to install the Podman AI Lab extension. See podman-desktop.io/docs/ai-lab/installing', { id: 'quarkus-chappie-podman-extension' })}
            </div>

            ${this._renderBaseUrlInput('podman', true)}
            ${this._renderModelTemperatureAndTimeoutInput('podman')}
            ${this._renderCommonSettings()}

            <vaadin-button
                theme="primary"
                @click="${this._savePodmanAIConfig}">
                ${msg('Save', { id: 'quarkus-chappie-save' })}
            </vaadin-button>
            `;
    }

    _renderOpenShiftAI(){
        return html`
            <div class="subText">
                ${msg('See redhat.com/en/products/ai/openshift-ai', { id: 'quarkus-chappie-openshift-instructions' })}
            </div>

            ${this._renderBaseUrlInput('openshift', true)}
            ${this._renderApiKeyInput('openshift')}
            ${this._renderModelTemperatureAndTimeoutInput('openshift', true)}
            ${this._renderCommonSettings()}

            <vaadin-button
                theme="primary"
                @click="${this._saveOpenShiftAIConfig}">
                ${msg('Save', { id: 'quarkus-chappie-save' })}
            </vaadin-button>
        `;
    }

    _renderGeneric(){
        return html`

            ${this._renderBaseUrlInput('generic', true)}
            ${this._renderApiKeyInput('generic')}
            ${this._renderModelTemperatureAndTimeoutInput('generic', true)}
            ${this._renderCommonSettings()}

            <vaadin-button
                theme="primary"
                @click="${this._saveGenericConfig}">
                ${msg('Save', { id: 'quarkus-chappie-save' })}
            </vaadin-button>
        `;
    }
    
    _renderCommonSettings(){
        return html`<vaadin-details summary="${msg('RAG', { id: 'quarkus-chappie-rag' })}">
                        <div class="rag">
                            ${this._renderRagSettings()}
                        </div>
                    </vaadin-details>
                    <vaadin-details summary="${msg('Storage', { id: 'quarkus-chappie-storage' })}">
                        <div class="storage">
                            ${this._renderStoreSettings()}
                        </div>
                    </vaadin-details>
                    <vaadin-details summary="${msg('MCP', { id: 'quarkus-chappie-mcp' })}">
                        <div class="mcp">
                            ${this._renderMCPSettings()}
                        </div>
                    </vaadin-details>
                    `;
    }
    
    _renderApiKeyInput(backend, required = false, placeholder = '') {
        return html`<vaadin-password-field
                id="${backend}-api-key"
                .value="${this._loadedConfiguration?.apiKey ?? ''}"
                placeholder="${placeholder}"
                label="${msg('API Key', { id: 'quarkus-chappie-api-key' })}"
                ?required=${required}>
            </vaadin-password-field>`;
    }

    _renderModelTemperatureAndTimeoutInput(backend, required = false){
        return html`<div class="tt">
                        ${this._renderModelInput(backend, required)}
                        ${this._renderTemperatureInput(backend)}
                        ${this._renderTimeoutInput(backend)}
                    </div>`;
    }

    _renderBaseUrlInput(backend, required = false){
        return html`<vaadin-text-field
                id="${backend}-base-url"
                label="${msg('Base URL', { id: 'quarkus-chappie-base-url' })}"
                placeholder="${this._selectedProvider.defaultBaseUrl}"
                .value="${this._loadedConfiguration?.baseUrl ?? ''}"
                ?required=${required}>
            </vaadin-text-field>`;
    }
    
    _renderModelInput(backend, required = false){
        return html`<vaadin-text-field class="fullWidth"
                id="${backend}-model"
                label="${msg('Model', { id: 'quarkus-chappie-model' })}"
                placeholder="${this._selectedProvider.defaultModel}"
                .value="${this._loadedConfiguration?.model ?? ''}"
                ?required=${required}>
            </vaadin-text-field>`;
    }

    _renderTemperatureInput(backend){
        return html`<vaadin-number-field class="fullWidth"
                id="${backend}-temperature"
                label="${msg('Temperature', { id: 'quarkus-chappie-temperature' })}"
                placeholder="${this._selectedProvider.defaultTemperature}"
                .value="${this._loadedConfiguration?.temperature ?? ''}"
                min="0"
                step="0.1">

            </vaadin-number-field>`;
    }

    _renderTimeoutInput(backend){
        return html`<vaadin-text-field class="fullWidth"
                id="${backend}-timeout"
                label="${msg('Timeout', { id: 'quarkus-chappie-timeout' })}"
                placeholder="${this._selectedProvider.defaultTimeout}"
                .value="${this._loadedConfiguration?.timeout ?? ''}">
            </vaadin-text-field>`;
    }
    
    _renderRagSettings(){

        let c = this._asBool(this._loadedConfiguration?.ragEnabled ?? this._ragDefaults?.ragEnabled);

        return html`

            <vaadin-checkbox
                id="rag-enabled"
                .checked=${c}
                label="${msg('Enable RAG', { id: 'quarkus-chappie-enable-rag' })}">
            </vaadin-checkbox>

            <vaadin-number-field
                id="rag-max-results"
                label="${msg('Max results', { id: 'quarkus-chappie-max-results' })}"
                placeholder="${this._ragDefaults.ragMaxResults}"
                .value="${this._loadedConfiguration?.ragMaxResults ?? ''}"
                min="1"
                step="1">
            </vaadin-number-field>

            <vaadin-number-field
                id="rag-min-score"
                label="${msg('Min score', { id: 'quarkus-chappie-min-score' })}"
                placeholder="${this._ragDefaults.ragMinScore}"
                .value="${this._loadedConfiguration?.ragMinScore ?? ''}"
                min="0"
                step="0.01">
            </vaadin-number-field>
            `;
    }

    _renderStoreSettings(){
        return html`<vaadin-text-field
                id="store-max-messages"
                label="${msg('Max store messages', { id: 'quarkus-chappie-max-store-messages' })}"
                placeholder="${this._storeDefaults.storeMaxMessages}"
                .value="${this._loadedConfiguration?.storeMaxMessages ?? ''}">
            </vaadin-text-field>`;
    }

    _renderMCPSettings(){

        let c = this._asBool(this._loadedConfiguration?.mcpEnabled ?? this._mcpDefaults?.mcpEnabled);

        return html`
            <vaadin-checkbox
                id="mcp-enabled"
                .checked=${c}
                label="${msg('Enable MCP (By default, if available, Dev MCP will be added)', { id: 'quarkus-chappie-enable-mcp' })}">
            </vaadin-checkbox>

            <vaadin-text-field class="fullWidth"
                id="extra-mcp-servers"
                label="${msg('More MCP Servers', { id: 'quarkus-chappie-more-mcp-servers' })}"
                placeholder="${msg('http://localhost:3001/mcp, https://my-mcp.example.com/mcp, stdio:npm exec @modelcontextprotocol/server-everything', { id: 'quarkus-chappie-mcp-servers-placeholder' })}"
                .value="${this._loadedConfiguration?.mcpservers ?? ''}">
            </vaadin-text-field>`;
    }
    
    _asBool(v) {
        if (typeof v === 'boolean') return v;
        if (typeof v === 'string') return v.trim().toLowerCase() === 'true' || v === '1';
        if (typeof v === 'number') return v !== 0;
        return false;
    }
    
    _providerComboRenderer(provider){
        return html`<div style="display: flex;justify-content: space-between;">
            <div>
              ${provider.name}
              <div style="font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);">
                ${provider.description}
              </div>
            </div>
            ${this._renderLogo(provider.name)}
          </div>`;
    }
    
    _providerChanged(e) {
        if(e.detail.value){
            const selectedName = e.detail.value;
            this.jsonRpc.loadConfigurationFor({name: selectedName}).then(jsonRpcResponse => { 
                this._loadedConfiguration = jsonRpcResponse.result;
                if(this._loadedConfiguration && this._loadedConfiguration.name !== selectedName){
                    this._loadedConfiguration = null;
                }
                this._selectedProvider = this._allProviders.find(p => p.name === selectedName);
            });
        }else{
            this._selectedProvider = null;
        }
    }
    
    _getRagEnabled() {
        const el = this.renderRoot?.querySelector('#rag-enabled');
        const enabled = el?.checked ?? (this._loadedConfiguration?.ragEnabled ?? this._ragDefaults?.ragEnabled ?? false);
        return enabled ? 'true' : 'false';
    }
    
    _getRagMaxResultsInput(){
        let ragMaxResults = this.shadowRoot.querySelector('#rag-max-results')?.value;
        if(!ragMaxResults)ragMaxResults = this._ragDefaults.ragMaxResults;
        return ragMaxResults;
    }
    
    _getRagMinScoreInput(){
        let ragMinScore = this.shadowRoot.querySelector('#rag-min-score')?.value;
        if(!ragMinScore)ragMinScore = this._ragDefaults.ragMinScore;
        return ragMinScore;
    }
    
    _getStoreMaxMessagesInput(){
        let storeMaxMessages = this.shadowRoot.querySelector('#store-max-messages')?.value;
        if(!storeMaxMessages)storeMaxMessages = this._storeDefaults.storeMaxMessages;
        return storeMaxMessages;
    }
    
    _getMcpEnabled() {
        const el = this.renderRoot?.querySelector('#mcp-enabled');
        const enabled = el?.checked ?? (this._loadedConfiguration?.mcpEnabled ?? this._mcpDefaults?.mcpEnabled ?? false);
        return enabled ? 'true' : 'false';
    }
    
    _getExtraMcpServersInput(){
        let storeExtraMcpServer = this.shadowRoot.querySelector('#extra-mcp-servers')?.value;
        if(!storeExtraMcpServer)storeExtraMcpServer = '';
        return storeExtraMcpServer;
    }
    
    _getModelInput(selector){
        let model = this.shadowRoot.querySelector(selector)?.value;
        if(!model)model = this._selectedProvider.defaultModel;
        return model;
    }
    
    _getTemperatureInput(selector){
        let temperature = this.shadowRoot.querySelector(selector)?.value;
        if(!temperature)temperature = this._selectedProvider.defaultTemperature;
        return temperature;
    }
    
    _getTimeoutInput(selector){
        let timeout = this.shadowRoot.querySelector(selector)?.value;
        if(!timeout)timeout = this._selectedProvider.defaultTimeout;
        return timeout;
    }
    
    _getBaseUrlInput(selector){
        let baseUrl = this.shadowRoot.querySelector(selector)?.value;
        if(!baseUrl)baseUrl = this._selectedProvider.defaultBaseUrl;
        return baseUrl;
    }
    
    _getApiKeyInput(selector){
        return this.shadowRoot.querySelector(selector)?.value;
    }
    
    _saveOpenAIConfig() {
        let apiKey = this._getApiKeyInput('#openai-api-key');
        if(apiKey){
            this._storeConfiguration({
                name: this._selectedProvider.name,
                apiKey,
                model: this._getModelInput('#openai-model'),
                temperature: this._getTemperatureInput('#openai-temperature'),
                timeout: this._getTimeoutInput('#openai-timeout'),
                ragMaxResults: this._getRagMaxResultsInput(),
                ragMinScore: this._getRagMinScoreInput(),
                ragEnabled: this._getRagEnabled(),
                storeMaxMessages: this._getStoreMaxMessagesInput(),
                mcpEnabled: this._getMcpEnabled(),
                mcpExtraServers: this._getExtraMcpServersInput()
            });
        }else{
            notifier.showErrorMessage(msg('You need to provide an API Key', { id: 'quarkus-chappie-need-api-key' }));
        }
    }
    
    _saveOllamaConfig(){
        this._storeConfiguration({
            name: this._selectedProvider.name,
            baseUrl: this._getBaseUrlInput('#ollama-base-url'),
            model: this._getModelInput('#ollama-model'),
            temperature: this._getTemperatureInput('#ollama-temperature'),
            timeout: this._getTimeoutInput('#ollama-timeout'),
            ragMaxResults: this._getRagMaxResultsInput(),
            ragMinScore: this._getRagMinScoreInput(),
            ragEnabled: this._getRagEnabled(),
            storeMaxMessages: this._getStoreMaxMessagesInput(),
            mcpEnabled: this._getMcpEnabled(),
            mcpExtraServers: this._getExtraMcpServersInput()
        });
    }
    
    _savePodmanAIConfig() {
        let baseUrl = this.shadowRoot.querySelector('#podman-base-url')?.value;

        if(baseUrl){
            this._storeConfiguration({
                name: this._selectedProvider.name,
                apiKey: "sk-dummy",
                baseUrl: this._getBaseUrlInput('#podman-base-url'),
                model: this._getModelInput('#podman-model'),
                temperature: this._getTemperatureInput('#podman-temperature'),
                timeout: this._getTimeoutInput('#podman-timeout'),
                ragMaxResults: this._getRagMaxResultsInput(),
                ragMinScore: this._getRagMinScoreInput(),
                ragEnabled: this._getRagEnabled(),
                storeMaxMessages: this._getStoreMaxMessagesInput(),
                mcpEnabled: this._getMcpEnabled(),
                mcpExtraServers: this._getExtraMcpServersInput()
            });
        }else{
            notifier.showErrorMessage(msg('You need to provide a base URL', { id: 'quarkus-chappie-need-base-url' }));
        }
    }
    
    _saveOpenShiftAIConfig() {
        this._storeConfiguration({
            name: this._selectedProvider.name,
            baseUrl: this._getBaseUrlInput('#openshift-base-url'),
            apiKey: this._getApiKeyInput('#openshift-api-key'),
            model: this._getModelInput('#openshift-model'),
            temperature: this._getTemperatureInput('#openshift-temperature'),
            timeout: this._getTimeoutInput('#openshift-timeout'),
            ragMaxResults: this._getRagMaxResultsInput(),
            ragMinScore: this._getRagMinScoreInput(),
            ragEnabled: this._getRagEnabled(),
            storeMaxMessages: this._getStoreMaxMessagesInput(),
            mcpEnabled: this._getMcpEnabled(),
            mcpExtraServers: this._getExtraMcpServersInput()
        });
    }

    _saveGenericConfig() {
        this._storeConfiguration({
            name: this._selectedProvider.name,
            baseUrl: this._getBaseUrlInput('#generic-base-url'),
            apiKey: this._getApiKeyInput('#generic-api-key'),
            model: this._getModelInput('#generic-model'),
            temperature: this._getTemperatureInput('#generic-temperature'),
            timeout: this._getTimeoutInput('#generic-timeout'),
            ragMaxResults: this._getRagMaxResultsInput(),
            ragMinScore: this._getRagMinScoreInput(),
            ragEnabled: this._getRagEnabled(),
            storeMaxMessages: this._getStoreMaxMessagesInput(),
            mcpEnabled: this._getMcpEnabled(),
            mcpExtraServers: this._getExtraMcpServersInput()
        });
    }
    
    _storeConfiguration(params){
        this.jsonRpc.storeConfiguration({configuration:params}).then(jsonRpcResponse => {
            if(!jsonRpcResponse.result){
                notifier.showErrorMessage(msg('Provider details not saved. See log for details', { id: 'quarkus-chappie-save-failed' }));
            }else{
                notifier.showSuccessMessage(msg('Provider details saved.', { id: 'quarkus-chappie-saved' }));
                assistantState.ready();
                if(this._navigateBack){
                    this.routerController.navigate(this._navigateBack);
                }
            }
        });
    }

    _loadConfiguration(){
        this.jsonRpc.loadConfiguration().then(jsonRpcResponse => {
            this._loadedConfiguration = jsonRpcResponse.result;
            if(this._loadedConfiguration && this._loadedConfiguration.name){
                assistantState.ready();
                this._selectedProvider = this._allProviders.find(
                    p => p.name === this._loadedConfiguration.name
                );
            }else{
                assistantState.available();
            }
        });
    }

    _clearConfiguration(){
        this.jsonRpc.clearConfiguration().then(jsonRpcResponse => {
            if(!jsonRpcResponse.result){
                notifier.showErrorMessage(msg('Provider details not cleared. See log for details', { id: 'quarkus-chappie-clear-failed' }));
            }else {
                assistantState.available();
                this._selectedProvider = null;
                this._loadedConfiguration = null;
                notifier.showSuccessMessage(msg('Provider details cleared.', { id: 'quarkus-chappie-cleared' }));
            }
        });
    }
}
customElements.define('qwc-chappie-configure', QwcChappieConfigure);
