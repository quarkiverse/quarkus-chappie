import { LitElement } from 'lit';
import { JsonRpc } from 'jsonrpc';
import { assistantState } from 'assistant-state';

/**
 * This load on browser load (headless components) to set the initial assistant state
 */
class QwcChappieInit extends LitElement {

    static properties = {
        namespace: {attribute: true},
    }

    connectedCallback() {
        super.connectedCallback();
        this._init().then(() => this.remove());
    }

    async _init() {
        const jsonRpc = new JsonRpc(this.namespace);

        jsonRpc.loadConfiguration().then(jsonRpcResponse => { 
            if(jsonRpcResponse.result && jsonRpcResponse.result.name){
                assistantState.ready();
            }else{
                assistantState.available();
            }
        });
    }

    // prevent rendering anything
    createRenderRoot() {
        return this;
    }
}
customElements.define('qwc-chappie-init', QwcChappieInit);