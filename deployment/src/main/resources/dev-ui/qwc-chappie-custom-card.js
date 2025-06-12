import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { pages } from 'build-time-data';
import { JsonRpc } from 'jsonrpc';
import { devuiState } from 'devui-state';
import { themeState } from 'theme-state';
import 'qwc/qwc-extension-link.js';
import '@vaadin/progress-bar';
import '@vaadin/horizontal-layout';
import '@qomponent/qui-badge';
import { assistantState } from 'assistant-state';

export class QwcChappieCustomCard extends QwcHotReloadElement {

    jsonRpc = new JsonRpc(this);
    
    static styles = css`
      .identity {
        display: flex;
        padding-bottom: 10px;
        color: var(--lumo-contrast-50pct);
        gap: 10px;
      }

      .config {
        color: var(--lumo-contrast-40pct);
        font-size: smaller;
        padding-bottom: 10px;
      }

      .description {
        padding-bottom: 10px;
        color: var(--lumo-contrast-50pct);
      }

      .card-content {
        color: var(--lumo-contrast-90pct);
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        padding: 2px 2px;
        height: 100%;
      }

      .card-content slot {
        display: flex;
        flex-flow: column wrap;
        padding-top: 5px;
      }
    
      .cardContents {
        display: flex;
        justify-content: space-between;
        flex-direction: column;
        height: 100%;
      }
    
      .modelInfo {
        display: flex;
        justify-content: center;
      }
    `;

    static properties = {
        extensionName: {attribute: true},
        description: {attribute: true},
        guide: {attribute: true},
        namespace: {attribute: true},
        _loadedConfiguration: { state: true }
    };


    constructor() {
        super();
        this._loadedConfiguration = null;
    }

    connectedCallback() {
        super.connectedCallback();
        if(!this._loadedConfiguration)this._loadConfiguration();   
    }

    hotReload(){
        this._loadConfiguration();
    }

    disconnectedCallback() {
        if(this._observer)this._observer.cancel();
        super.disconnectedCallback();
    }

    render() {
        return html`<div class="card-content" slot="content">
            <div class="identity">
                ${this._renderLogo()}
                <div class="description">${this.description}</div>
            </div>
            ${this._renderContent()}
        </div>
        `;
    }

    _renderContent(){
            return html`<div class="cardContents">
                            ${this._renderCardLinks()}
                            ${this._renderInfo()}
                        </div>`;
    }

    _renderInfo(){
        if(assistantState.current.isConfigured){
            return html`<div class="modelInfo">
                            <qui-badge small>
                                <span>${this._loadedConfiguration?.name} (${this._loadedConfiguration?.model})</span>
                            </qui-badge>
                        </div>`;
        }else{
            return html`<div class="config">
                            You need to configure the assistant to make the assistant features available
                        </div>`;
        }
    }

    _renderLogo(){
        if(themeState.theme.name && this._loadedConfiguration && this._loadedConfiguration.name){
            let logo = "./" + this.namespace + "/" + this._loadedConfiguration.name.replace(/ /g, "_") + "_" + themeState.theme.name + ".svg";
            if(logo){
                return html`<img src="${logo}" height="45" @error="${(e) => e.target.style.display = 'none'}">`;
            }
        }
    }

    _renderCardLinks(){
        return html`<div>
            ${pages.map(page => this._renderPageLink(page))}</div>`;
    }

    _renderPageLink(page){
        if(assistantState.current.isConfigured || page.metadata.alwaysVisible){
        
            return html`
                <qwc-extension-link slot="link"
                    namespace="${this.namespace}"
                    extensionName="${this.extensionName}"
                    iconName="${page.icon}"
                    displayName="${page.title}"
                    staticLabel="${page.staticLabel}"
                    dynamicLabel="${page.dynamicLabel}"
                    streamingLabel="${page.streamingLabel}"
                    path="${page.id}"
                    ?embed=${page.embed}
                    externalUrl="${page.metadata.externalUrl}"
                    dynamicUrlMethodName="${page.metadata.dynamicUrlMethodName}"
                    webcomponent="${page.componentLink}" 
                    draggable="true" @dragstart="${this._handleDragStart}">
                </qwc-extension-link>
            `;
        };
    }

    _handleDragStart(event) {
        const extensionNamespace = event.currentTarget.getAttribute('namespace');
        const pageId = event.currentTarget.getAttribute('path');
        const extension = devuiState.cards.active.find(obj => obj.namespace === extensionNamespace);
        const page = extension.cardPages.find(obj => obj.id === pageId);
        const jsonData = JSON.stringify(page);
        event.dataTransfer.setData('application/json', jsonData);
    }
    
    _loadConfiguration(){
        this.jsonRpc.loadConfiguration().then(jsonRpcResponse => { 
            this._loadedConfiguration = jsonRpcResponse.result;
            if(this._loadedConfiguration && this._loadedConfiguration.name){
                assistantState.ready();
            }else{
                assistantState.available();
            }
        });
    }
}
customElements.define('qwc-chappie-custom-card', QwcChappieCustomCard);