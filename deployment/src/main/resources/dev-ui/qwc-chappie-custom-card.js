import { LitElement, html, css} from 'lit';
import { pages } from 'build-time-data';
import { JsonRpc } from 'jsonrpc';
import 'qwc/qwc-extension-link.js';
import '@vaadin/progress-bar';
import '@vaadin/horizontal-layout';
import '@qomponent/qui-badge';
import { llm } from 'build-time-data';
import { modelName } from 'build-time-data';

export class QwcChappieCustomCard extends LitElement {

    jsonRpc = new JsonRpc(this);
    
    static styles = css`
      .identity {
        display: flex;
        justify-content: flex-start;
      }

      .config {
    
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
        _status: {state: true},
        _llm: {state: true},
        _modelName: {state: true}
    };


    constructor() {
        super();
        this._status = null;
        this._llm = llm;
        this._modelName = modelName;
    }

    connectedCallback() {
        super.connectedCallback();
//            this._observer = this.jsonRpc.streamStatus().onNext(jsonRpcResponse => { 
//                this._status = jsonRpcResponse.result;
//            });
//            
//            this.jsonRpc.getStatus().then(jsonRpcResponse => { 
//                this._status = jsonRpcResponse.result;
//            }); 
        
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
            ${this._renderConfig()}
        </div>
        `;
    }

    _renderConfig(){
            return html`
                ${this._renderInfo()}
                ${this._renderCardLinks()}
            `;
        
    }

    _renderInfo(){
        if(this._status){
            if(this._status === "starting"){
                return html`<div>
                                <label class="text-secondary" id="statusprogressbar">Starting ${this._llm} (${this._modelName})</label>
                                <vaadin-progress-bar indeterminate aria-labelledby="statusprogressbar" theme="success"></vaadin-progress-bar>
                            </div>`;
            }else if(this._status === "started"){
                return html`<qui-badge text="Started" level="success" icon="circle-check">
                                <span>${this._llm} (${this._modelName})</span>
                            </qui-badge>`;
            }else {
                return html`<qui-badge text="Error" level="error" icon="circle-exclamation">
                                <span>${this._llm} (${this._modelName})</span>
                            </qui-badge>`;
            }               
        }else {
            return html`<div class="config">Configured to use ${this._llm} (${this._modelName})</div>`;
        }
    }

    _renderCardLinks(){
        return html`
            ${pages.map(page => html`
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
                webcomponent="${page.componentLink}" >
            </qwc-extension-link>
        `)}`;
    }

}
customElements.define('qwc-chappie-custom-card', QwcChappieCustomCard);