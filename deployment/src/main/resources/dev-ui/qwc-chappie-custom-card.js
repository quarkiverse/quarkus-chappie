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
        _llm: {state: true},
        _modelName: {state: true}
    };


    constructor() {
        super();
        this._llm = llm;
        this._modelName = modelName;
    }

    connectedCallback() {
        super.connectedCallback();
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
        return html`<div class="config">Using ${this._llm} (${this._modelName})</div>`;
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