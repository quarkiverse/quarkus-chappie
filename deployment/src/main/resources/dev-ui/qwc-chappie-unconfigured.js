import { LitElement, html, css} from 'lit'; 
import { JsonRpc } from 'jsonrpc';
import '@vaadin/button';

/**
 * This component shows how to configure Chappie
 */
export class QwcChappieUnconfigured extends LitElement { 
    jsonRpc = new JsonRpc(this);

    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            height: 100%;
            padding: 10px;
        }
    `;
    
    static properties = {
        
    };

    constructor() { 
        super();
    }

    connectedCallback() {
        super.connectedCallback();
    }

    disconnectedCallback() {
        super.disconnectedCallback();      
    }

    render() { 
        return html`You need to add an API Key`;
    }
    
    
}
customElements.define('qwc-chappie-unconfigured', QwcChappieUnconfigured);
