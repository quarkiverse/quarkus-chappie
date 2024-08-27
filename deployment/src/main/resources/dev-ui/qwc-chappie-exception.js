import { LitElement, html, css} from 'lit'; 
import { JsonRpc } from 'jsonrpc';
import '@vaadin/accordion';
import '@vaadin/button';
import '@vaadin/progress-bar';
import 'qui-ide-link';
import '@qomponent/qui-code-block';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';
import { notifier } from 'notifier';
import { devuiState } from 'devui-state';

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
        .fix {
            display: flex;
            flex-direction: column;
            height: 100%;
            padding-top: 20px;
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
        
        .buttons {
            position: fixed;
            right: 14px;
        }
    
        .ide {
            color: var(--lumo-contrast-50pct);
            font-family: var(--lumo-font-family);
            font-size: var(--lumo-font-size-s);
            padding-right: 10px;
        }
        .codeBlockHeader {
            display: flex;
            width: 100%;
            justify-content: space-between;
            align-items: baseline;
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
        if(this._observer)this._observer.cancel();
        super.disconnectedCallback();      
    }

    render() { 
        if (this._lastException) {
            return html`${this._renderButtons()}
                        ${this._renderException()}
                        ${this._renderSuggestedFix()}`;
        } else {
            return html`<div class="nothing">No exception detected yet. <span class="checkNow" @click="${this._checkLastException}">Check now</span></div>`;
        }
    }
    
    _renderException(){
        return html`<vaadin-accordion>
                        <vaadin-accordion-panel summary="Code" theme="filled">
                            <div class="exception">
                                <pre class="stacktrace">${this._lastException.decorateString}</pre>
                            </div>
                        </vaadin-accordion-panel>
                        <vaadin-accordion-panel summary="Stacktrace" theme="filled">
                            <div class="exception">
                                <pre class="stacktrace">${this._lastException.stackTraceString}</pre>
                            </div>
                        </vaadin-accordion-panel>
                    </vaadin-accordion>`;
    }
    
    _renderButtons(){
        return html`<div class="buttons">
                        ${this._renderIDEButton()}
                        ${this._renderFixButton()}
                    </div>`;
    }
    
    _renderIDEButton(){
        return html`<qui-ide-link class="ide" title='Click to view where the exception occurred'
                        fileName='${this._lastException.stackTraceElement.className}'
                        lineNumber=${this._lastException.stackTraceElement.lineNumber}>View in IDE
                    </qui-ide-link>`;
    }
    
    _renderFixButton(){
        if(!this._showProgressBar && !this._suggestedFix){
            return html`<vaadin-button theme="primary small" @click="${this._suggestFix}">Suggest fix with AI</vaadin-button>`;
        }
    }
    
    _renderSuggestedFix(){
        if(this._showProgressBar){
            return html`<div class="fix">
                            <label class="text-secondary" id="pblbl">Talking to AI...</label>
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
                            <span class="heading-fix">
                                Suggested fix from AI
                            </span>
                            <p>${this._suggestedFix.response}</p>

                            <p>${this._suggestedFix.explanation}</p>

                            <h4>Diff:</h4>
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'>
                                    <slot>${this._suggestedFix.diff}</slot>
                                </qui-code-block>
                            </div>
                            
                            <div class="codeBlockHeader">
                                <h4>Suggested new code:</h4>
                                <vaadin-button theme="primary small" @click="${this._applyFix}">Apply fix to your code</vaadin-button>
                            </div>
                            
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'>
                                    <slot>${this._suggestedFix.suggestedSource}</slot>
                                </qui-code-block>
                            </div>
                            
                        </div>`;
        }
    }
    
    _checkLastException(){
        // Get the current last know exception
        this.jsonRpc.getLastException().then(jsonRpcResponse => { 
            this._lastException = jsonRpcResponse.result;
            this._suggestedFix = null;
        });
    }
    
    _suggestFix(){
        this._showProgressBar = true;
        // Get the current last know exception
        this.jsonRpc.suggestFix().then(jsonRpcResponse => { 
            this._showProgressBar = false;
            this._suggestedFix = jsonRpcResponse.result;
        });
    }
    
    _applyFix(){
        this.jsonRpc.applyFix().then(jsonRpcResponse => { 
            fetch(devuiState.applicationInfo.contextRoot);
            notifier.showInfoMessage("Updated " + jsonRpcResponse.result);
        });
    }
    
}
customElements.define('qwc-chappie-exception', QwcChappieException);
