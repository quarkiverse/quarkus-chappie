import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import '@qomponent/qui-code-block';
import '@vaadin/combo-box';
import { comboBoxRenderer } from '@vaadin/combo-box/lit.js';
import '@vaadin/avatar';
import '@vaadin/checkbox';
import '@vaadin/form-layout';
import '@vaadin/password-field';
import '@vaadin/text-field';
import '@vaadin/number-field';
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
        this._navigateBack = null;
        this._ragDefaults = {
            ragMaxResults: "4",
            ragEnabled: "true"
        };
        
        this._storeDefaults = {
            storeMaxMessages: "30"
        };
        
        this._allProviders = [
            {
                name: "OpenAI", 
                description: "Leading AI company that builds advanced language models like ChatGPT to power intelligent applications.", 
                defaultModel: "gpt-5-mini",
                defaultTemperature: "1",
                defaultTimeout: "PT120S",
                defaultBaseUrl: ""
            }, 
            {
                name:"Ollama", 
                description: "A platform for running and managing large language models locally with ease.",
                defaultModel: "codellama",
                defaultTemperature: "0.0",
                defaultTimeout: "PT120S",
                defaultBaseUrl: "http://localhost:11434/"
            },
            {
                name:"Podman AI", 
                description: "Integrates AI features into container workflows, enabling local, secure, and containerized AI model deployment.",
                defaultModel: "llama3:instruct",
                defaultTemperature: "0.2",
                defaultTimeout: "PT120S",
                defaultBaseUrl: ""
            },
            {
                name: "OpenShift AI", 
                description: "Red Hat’s enterprise AI platform, supporting OpenAI-compatible models with secure, scalable deployments.",
                defaultModel: "",
                defaultTemperature: "0.2",
                defaultTimeout: "PT120S",
                defaultBaseUrl: ""
            },
            {
                name: "Generic OpenAI-Compatible", 
                description: "Connect any OpenAI-compatible endpoint by providing your own base URL and API key.",
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
                        <span>To use the AI Assistant in Quarkus Dev Mode, you need to configure an AI provider</span>
                        ${this._renderClearButton()}
                    </div>
        
                    <vaadin-combo-box
                        label="Choose provider"
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
            return html`<vaadin-button theme="icon secondary error" title="Clear configuration" @click="${this._clearConfiguration}">
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
                To use OpenAI you need to provide an <a href="https://help.openai.com/en/articles/4936850-where-do-i-find-my-openai-api-key" target="_blank">OpenAI Api Key</a>
            </div>
            
            ${this._renderApiKeyInput('openai', true, "sk-....")}
            ${this._renderModelInput('openai')}
            ${this._renderTemperatureInput('openai')}
            ${this._renderTimeoutInput('openai')}
            ${this._renderRagSettings()}
            ${this._renderStoreSettings()}
            
            <vaadin-button 
                theme="primary" 
                @click="${this._saveOpenAIConfig}">
                Save
            </vaadin-button>
        `;
    }
    
    _renderOllama(){
        return html`
            <div class="subText">
                To use Ollama you need to install and run ollama. See <a href="https://ollama.com/download" target="_blank">ollama.com/download</a>
            </div>

            ${this._renderBaseUrlInput('ollama', true)}
            ${this._renderModelInput('ollama')}
            ${this._renderTemperatureInput('ollama')}
            ${this._renderTimeoutInput('ollama')}
            ${this._renderRagSettings()}
            ${this._renderStoreSettings()}
            
            <vaadin-button 
                theme="primary" 
                @click="${this._saveOllamaConfig}">
                Save
            </vaadin-button>
        `;
    }
    
    _renderPodmanAI(){
        return html`
            <div class="subText">
                To use Podman AI you need to install and run podman. See <a href="https://podman-desktop.io/docs/installation" target="_blank">podman-desktop.io/docs/installation</a>
                You also need to install the Podman AI Lab extension. See <a href="https://podman-desktop.io/docs/ai-lab/installing" target="_blank">podman-desktop.io/docs/ai-lab/installing</a>
            </div>

            ${this._renderBaseUrlInput('podman', true)}
            ${this._renderModelInput('podman')}
            ${this._renderTemperatureInput('podman')}
            ${this._renderTimeoutInput('podman')}
            ${this._renderRagSettings()}
            ${this._renderStoreSettings()}
            
            <vaadin-button 
                theme="primary" 
                @click="${this._savePodmanAIConfig}">
                Save
            </vaadin-button>
            `;
    }
    
    _renderOpenShiftAI(){
        return html`
            <div class="subText">
                See <a href="https://www.redhat.com/en/products/ai/openshift-ai" target="_blank">redhat.com/en/products/ai/openshift-ai</a>
            </div>

            ${this._renderBaseUrlInput('openshift', true)}
            ${this._renderApiKeyInput('openshift')}
            ${this._renderModelInput('openshift', true)}
            ${this._renderTemperatureInput('openshift')}
            ${this._renderTimeoutInput('openshift')}
            ${this._renderRagSettings()}
            ${this._renderStoreSettings()}
            
            <vaadin-button 
                theme="primary" 
                @click="${this._saveOpenShiftAIConfig}">
                Save
            </vaadin-button>
        `;
    }
    
    _renderGeneric(){
        return html`
        
            ${this._renderBaseUrlInput('generic', true)}
            ${this._renderApiKeyInput('generic')}
            ${this._renderModelInput('generic', true)}
            ${this._renderTemperatureInput('generic')}
            ${this._renderTimeoutInput('generic')}
            ${this._renderRagSettings()}
            ${this._renderStoreSettings()}
            
            <vaadin-button 
                theme="primary" 
                @click="${this._saveGenericConfig}">
                Save
            </vaadin-button>
        `;
    }
    
    _renderApiKeyInput(backend, required = false, placeholder = '') {
        return html`<vaadin-password-field 
                id="${backend}-api-key"
                .value="${this._loadedConfiguration?.apiKey ?? ''}"
                placeholder="${placeholder}"
                label="API Key"
                ?required=${required}>
            </vaadin-password-field>`;
    }
    
    _renderBaseUrlInput(backend, required = false){
        return html`<vaadin-text-field 
                id="${backend}-base-url" 
                label="Base URL" 
                placeholder="${this._selectedProvider.defaultBaseUrl}"
                .value="${this._loadedConfiguration?.baseUrl ?? ''}"
                ?required=${required}>
            </vaadin-text-field>`;
    }
    
    _renderModelInput(backend, required = false){
        return html`<vaadin-text-field 
                id="${backend}-model" 
                label="Model" 
                placeholder="${this._selectedProvider.defaultModel}"
                .value="${this._loadedConfiguration?.model ?? ''}"
                ?required=${required}>
            </vaadin-text-field>`;
    }
    
    _renderTemperatureInput(backend){
        return html`<vaadin-number-field 
                id="${backend}-temperature"
                label="Temperature" 
                placeholder="${this._selectedProvider.defaultTemperature}"
                .value="${this._loadedConfiguration?.temperature ?? ''}"
                min="0"
                step="0.1">
        
            </vaadin-number-field>`;
    }
    
    _renderTimeoutInput(backend){
        return html`<vaadin-text-field 
                id="${backend}-timeout" 
                label="Timeout" 
                placeholder="${this._selectedProvider.defaultTimeout}"
                .value="${this._loadedConfiguration?.timeout ?? ''}">
            </vaadin-text-field>`;
    }
    
    _renderRagSettings(){
        
        let c = this._asBool(this._loadedConfiguration?.ragEnabled ?? this._ragDefaults?.ragEnabled);
        
        console.log(c);
        
        return html`
            
            <vaadin-checkbox
                id="rag-enabled"
                .checked=${c}
                label="Enable RAG">
            </vaadin-checkbox>
        
            <vaadin-number-field
                id="rag-max-results" 
                label="Max RAG results" 
                placeholder="${this._ragDefaults.ragMaxResults}"
                .value="${this._loadedConfiguration?.ragMaxResults ?? ''}"
                min="1"
                step="1">
            </vaadin-number-field>`;
    }
    
    _renderStoreSettings(){
        return html`<vaadin-text-field 
                id="store-max-messages" 
                label="Max store messages" 
                placeholder="${this._storeDefaults.storeMaxMessages}"
                .value="${this._loadedConfiguration?.storeMaxMessages ?? ''}">
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
    
    _getStoreMaxMessagesInput(){
        let storeMaxMessages = this.shadowRoot.querySelector('#store-max-messages')?.value;
        if(!storeMaxMessages)storeMaxMessages = this._storeDefaults.storeMaxMessages;
        return storeMaxMessages;
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
                ragEnabled: this._getRagEnabled(),
                storeMaxMessages: this._getStoreMaxMessagesInput()
            });
        }else{
            notifier.showErrorMessage("You need to provide an API Key");
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
            ragEnabled: this._getRagEnabled(),
            storeMaxMessages: this._getStoreMaxMessagesInput()
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
                ragEnabled: this._getRagEnabled(),
                storeMaxMessages: this._getStoreMaxMessagesInput()
            });
        }else{
            notifier.showErrorMessage("You need to provide a base URL");
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
            ragEnabled: this._getRagEnabled(),
            storeMaxMessages: this._getStoreMaxMessagesInput()
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
            ragEnabled: this._getRagEnabled(),
            storeMaxMessages: this._getStoreMaxMessagesInput()
        });
    }
    
    _storeConfiguration(params){
        this.jsonRpc.storeConfiguration({configuration:params}).then(jsonRpcResponse => { 
            if(!jsonRpcResponse.result){
                notifier.showErrorMessage("Provider details not saved. See log for details");
            }else{
                notifier.showSuccessMessage("Provider details saved.");
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
                notifier.showErrorMessage("Provider details not cleared. See log for details");
            }else {
                assistantState.available();
                this._selectedProvider = null;
                this._loadedConfiguration = null;
                notifier.showSuccessMessage("Provider details cleared.");
            }
        });
    }
}
customElements.define('qwc-chappie-configure', QwcChappieConfigure);
