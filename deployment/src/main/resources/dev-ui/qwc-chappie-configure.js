import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import '@qomponent/qui-code-block';
import '@vaadin/combo-box';
import { comboBoxRenderer } from '@vaadin/combo-box/lit.js';
import '@vaadin/avatar';
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
        this._allProviders = [
            {
                name: "OpenAI", 
                description: "Leading AI company that builds advanced language models like ChatGPT to power intelligent applications.", 
                defaultModel: "gpt-4o-mini",
                defaultBaseUrl: ""
            }, 
            {
                name:"Ollama", 
                description: "A platform for running and managing large language models locally with ease.",
                defaultModel: "codellama",
                defaultBaseUrl: "http://localhost:11434/"
            },
            {
                name:"Podman AI", 
                description: "Integrates AI features into container workflows, enabling local, secure, and containerized AI model deployment.",
                defaultModel: "llama3:instruct",
                defaultBaseUrl: ""
            },
            {
                name: "OpenShift AI", 
                description: "Red Hat’s enterprise AI platform, supporting OpenAI-compatible models with secure, scalable deployments.",
                defaultModel: "",
                defaultBaseUrl: ""
            },
            {
                name: "Generic OpenAI-Compatible", 
                description: "Connect any OpenAI-compatible endpoint by providing your own base URL and API key.",
                defaultModel: "llama3:instruct",
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

            <vaadin-password-field 
                id="openai-api-key" 
                label="Api Key" 
                placeholder="sk-...." 
                .value="${this._loadedConfiguration?.apiKey ?? ''}"
                required>
            </vaadin-password-field>

            <vaadin-text-field 
                id="openai-model" 
                label="Model" 
                placeholder="${this._selectedProvider.defaultModel}"
                .value="${this._loadedConfiguration?.model ?? ''}">
            </vaadin-text-field>

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

            <vaadin-text-field 
                id="podman-base-url" 
                label="Base URL" 
                placeholder="${this._selectedProvider.defaultBaseUrl}"
                .value="${this._loadedConfiguration?.baseUrl ?? ''}"
                required>
            </vaadin-text-field>

            <vaadin-text-field 
                id="ollama-model" 
                label="Model" 
                placeholder="${this._selectedProvider.defaultModel}"
                .value="${this._loadedConfiguration?.model ?? ''}">
            </vaadin-text-field>

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

            <vaadin-text-field 
                id="podman-base-url" 
                label="Base URL" 
                placeholder="http://localhost...."
                .value="${this._loadedConfiguration?.baseUrl ?? ''}"
                required>
            </vaadin-text-field>

            <vaadin-text-field 
                id="podman-model" 
                label="Model" 
                placeholder="${this._selectedProvider.defaultModel}"
                .value="${this._loadedConfiguration?.model ?? ''}">
            </vaadin-text-field>

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

            <vaadin-text-field 
                id="openshift-base-url" 
                label="Base URL" 
                placeholder="https://..."
                .value="${this._loadedConfiguration?.baseUrl ?? ''}"
                required>
            </vaadin-text-field>

            <vaadin-password-field 
                id="openshift-api-key" 
                label="API Key"
                .value="${this._loadedConfiguration?.apiKey ?? ''}">
            </vaadin-password-field>

            <vaadin-text-field 
                id="openshift-model" 
                label="Model"
                .value="${this._loadedConfiguration?.model ?? ''}"
                required>
            </vaadin-text-field>

            <vaadin-button 
                theme="primary" 
                @click="${this._saveOpenShiftAIConfig}">
                Save
            </vaadin-button>
        `;
    }
    
    _renderGeneric(){
        return html`
            <vaadin-text-field 
                id="generic-base-url" 
                label="Base URL" 
                placeholder="https://your-ai-endpoint.com/v1"
                .value="${this._loadedConfiguration?.baseUrl ?? ''}"
                required>
            </vaadin-text-field>

            <vaadin-password-field 
                id="generic-api-key"
                .value="${this._loadedConfiguration?.apiKey ?? ''}"
                label="API Key">
            </vaadin-password-field>

            <vaadin-text-field 
                id="generic-model" 
                .value="${this._loadedConfiguration?.model ?? ''}"
                label="Model"
                required>
            </vaadin-text-field>

            <vaadin-button 
                theme="primary" 
                @click="${this._saveGenericConfig}">
                Save
            </vaadin-button>
        `;
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
    
    _saveOpenAIConfig() {
        let apiKey = this.shadowRoot.querySelector('#openai-api-key')?.value;
        let model = this.shadowRoot.querySelector('#openai-model')?.value;

        if(apiKey){
        
            if(!model)model = this._selectedProvider.defaultModel;

            this._storeConfiguration({
                name: this._selectedProvider.name,
                apiKey,
                model
            });
        }else{
            notifier.showErrorMessage("You need to provide an API Key");
        }
    }
    
    _saveOllamaConfig(){
        let baseUrl = this.shadowRoot.querySelector('#podman-base-url')?.value;
        let model = this.shadowRoot.querySelector('#ollama-model')?.value;
        
        if(!model)model = this._selectedProvider.defaultModel;
        if(!baseUrl)baseUrl = this._selectedProvider.defaultBaseUrl;

        this._storeConfiguration({
            name: this._selectedProvider.name,
            baseUrl,
            model
        });
        
        
    }
    
    _savePodmanAIConfig() {
        let baseUrl = this.shadowRoot.querySelector('#podman-base-url')?.value;
        let model = this.shadowRoot.querySelector('#podman-model')?.value;
        
        if(baseUrl){
            if(!model)model = this._selectedProvider.defaultModel;
        
            this._storeConfiguration({
                name: this._selectedProvider.name,
                apiKey: "sk-dummy",
                baseUrl,
                model
            });
        }else{
            notifier.showErrorMessage("You need to provide a base URL");
        }
    }
    
    _saveOpenShiftAIConfig() {
        let baseUrl = this.shadowRoot.querySelector('#openshift-base-url')?.value;
        let apiKey = this.shadowRoot.querySelector('#openshift-api-key')?.value;
        let model = this.shadowRoot.querySelector('#openshift-model')?.value;

        this._storeConfiguration({
            name: this._selectedProvider.name,
            baseUrl,
            apiKey,
            model
        });
    }

    _saveGenericConfig() {
        let baseUrl = this.shadowRoot.querySelector('#generic-base-url')?.value;
        let apiKey = this.shadowRoot.querySelector('#generic-api-key')?.value;
        let model = this.shadowRoot.querySelector('#generic-model')?.value;

        this._storeConfiguration({
            name: this._selectedProvider.name,
            baseUrl,
            apiKey,
            model
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
