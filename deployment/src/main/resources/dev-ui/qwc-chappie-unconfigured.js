import { LitElement, html, css} from 'lit'; 
import { JsonRpc } from 'jsonrpc';
import '@qomponent/qui-code-block';

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
        a {
            color: var(--lumo-body-text-color);
        }
        .code {
            color: var(--lumo-primary-text-color);
            font-family: Arial;
        }
    `;
    
    static properties = {
        
    };

    render() { 
        return html`To use the AI Assistant in Quarkus Dev Mode, you need to either provide an OpenAI Api Key OR install Ollama
                    <br/>
                    <br/>
                        
                    <h3>Using OpenAI</h3>
                    <p>
                        To use OpenAI you need to provide an <a href="https://help.openai.com/en/articles/4936850-where-do-i-find-my-openai-api-key" target="_blank">OpenAI Api Key</a> 
                        in the <span class="code">quarkus.assistant.openai.api-key</span> property OR set a <span class="code">QUARKUS_ASSISTANT_OPENAI_API_KEY</span> environment variable. 
                        <br/><br/>
                        Example:<br/>
                        <qui-code-block mode="properties">
                            <slot>mvn quarkus:dev -Dquarkus.assistant.openai.api-key=sk....</slot>
                        </qui-code-block>
                        <br/>
                    </p>
        
                    <h3>Using Ollama</h3>
                    <p>
                        To use Ollama you need to install and run ollama. See <a href="https://ollama.com/download" target="_blank">ollama.com/download</a>
                    </p>
                    
                    `;
    }
    
    
}
customElements.define('qwc-chappie-unconfigured', QwcChappieUnconfigured);
