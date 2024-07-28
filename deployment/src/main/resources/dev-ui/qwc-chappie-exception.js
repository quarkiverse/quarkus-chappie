import { LitElement, html, css} from 'lit'; 
import { JsonRpc } from 'jsonrpc';
import '@vaadin/button';
import '@vaadin/progress-bar';
import 'qui-ide-link';
import '@qomponent/qui-code-block';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';

/**
 * This component shows the last exception
 */
export class QwcChappieException extends observeState(LitElement) { 
    jsonRpc = new JsonRpc(this);

    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            height: 100%;
            padding: 10px;
        }
        .exception {
            display: flex;
            flex-direction: column;
            height: 100%;
        }
        .heading-exception {
            color: var(--lumo-error-color);
            font-size: x-large; 
        }
        .fix {
            display: flex;
            flex-direction: column;
            height: 100%;
        }
        .heading-fix {
            color: var(--lumo-success-color);
            font-size: x-large; 
        }
        .stacktrace {
            color: var(--lumo-error-color);
            padding: 10px;
        }
        .checkNow {
            cursor: pointer;
        }
        .checkNow:hover {
            text-decoration: underline;
        }
        .nothing {
            height: 100%;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
    `;
    
    static properties = {
        _lastException: {state: true},
        _suggestedFix: {state: true},
        _showProgressBar: {state: true}
    };

    constructor() { 
        super();
        this._lastException = null;
        this._suggestedFix = null;
        this._showProgressBar = false;
    }

    connectedCallback() {
        super.connectedCallback();
        // Subscribe to real-time exceptions
        this._observer = this.jsonRpc.streamException().onNext(jsonRpcResponse => { 
            this._lastException = jsonRpcResponse.result;
            this._suggestedFix = null;
        });
        // Get the current last know exception
        this._checkLastException();
    }

    disconnectedCallback() {
        this._observer.cancel();
        super.disconnectedCallback();      
    }

    render() { 
        if (this._lastException) {
            return html`<div class="exception">
                    <qui-ide-link title='Source full class name'
                        class='heading-exception'
                        fileName='${this._lastException.stackTraceElement.className}'
                        lineNumber=${this._lastException.stackTraceElement.lineNumber}>Exception in ${this._lastException.stackTraceElement.fileName} line ${this._lastException.stackTraceElement.lineNumber}:</qui-ide-link>
            
                    <pre class="stacktrace">${this._lastException.stackTraceString}</pre>
                    
                    ${this._renderSuggestedFix()}
                </div>
                `;
        } else {
            return html`<div class="nothing">No exception detected yet. <span class="checkNow" @click="${this._checkLastException}">Check now</span></div>`;
        }
    }
    
    
    
    _renderSuggestedFix(){
        if(this._showProgressBar){
            return html`<div>
                            <label class="text-secondary" id="pblbl">Talking to Chappie...</label>
                            <vaadin-progress-bar
                              indeterminate
                              aria-labelledby="pblbl"
                              aria-describedby="sublbl"
                            ></vaadin-progress-bar>
                            <span class="text-secondary text-xs" id="sublbl">
                              This can take a while, please hold
                            </span>
                        </div>`;
        }else if(this._suggestedFix){
            
            return html`<div class="fix">
                            <span class="heading-fix">Suggested fix from AI</span>
                            <p>
                                ${this._suggestedFix.response}
                            </p>
            
                            <p>
${this._suggestedFix.explanation}
                            </p>
                            
                            Diff:
                            <pre>
${this._suggestedFix.diff}
                            </pre>
            
                            Suggested new code:
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'>
                                    <slot>
                                        ${this._suggestedFix.suggestedSource}
                                    </slot>
                                </qui-code-block>
                            </div>
                        </div>

            `;
        }else {
            return html`<vaadin-button theme="primary" @click="${this._helpFix}">Suggest fix with AI</vaadin-button>`;
        }
    }
    
    _checkLastException(){
        // Get the current last know exception
        this.jsonRpc.getLastException().then(jsonRpcResponse => { 
            this._lastException = jsonRpcResponse.result;
            this._suggestedFix = null;
        });
    }
    
    _helpFix(){
        this._showProgressBar = true;
        // Get the current last know exception
        this.jsonRpc.helpFix().then(jsonRpcResponse => { 
            this._showProgressBar = false;
            this._suggestedFix = jsonRpcResponse.result;
        });
    }
    
}
customElements.define('qwc-chappie-exception', QwcChappieException);
