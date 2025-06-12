import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { pages } from 'build-time-data';
import { JsonRpc } from 'jsonrpc';
import { devuiState } from 'devui-state';
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
        justify-content: flex-start;
      }

      .configHeader {
        display: flex;
        align-items: center;
        justify-content: space-between;
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
                <div class="description">${this.description}</div>
            </div>
            ${this._renderContent()}
        </div>
        `;
    }

    _renderContent(){
            return html`
                ${this._renderInfo()}
                ${this._renderCardLinks()}
            `;
    }

    _renderInfo(){
        if(assistantState.current.isConfigured){
            return html`<div class="configHeader">
                            <div class="config">Using ${this._loadedConfiguration?.name} (${this._loadedConfiguration?.model})</div>
                            <vaadin-avatar
                                .img="${this._loadedConfiguration?.logoUrl}"
                                .name="${`${this._loadedConfiguration?.name}`}"
                            ></vaadin-avatar>
                        </div>`;
        }else{
            return html`<div class="config">
                            You need to configure the assistant to make the assistant features available
                        </div>`;
        }
    }

    _renderCardLinks(){
        return html`
            ${pages.map(page => this._renderPageLink(page))}`;
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