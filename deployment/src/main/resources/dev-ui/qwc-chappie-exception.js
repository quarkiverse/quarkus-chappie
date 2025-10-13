import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/details';
import '@vaadin/button';
import '@vaadin/progress-bar';
import 'qui-ide-link';
import '@vaadin/dialog';
import '@vaadin/confirm-dialog';
import '@qomponent/qui-code-block';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';
import { notifier } from 'notifier';
import { dialogFooterRenderer, dialogRenderer, dialogHeaderRenderer } from '@vaadin/dialog/lit.js';
import 'qwc-no-data';
import 'qui-assistant-warning';
import { assistantState } from 'assistant-state';
import { RouterController } from 'router-controller';

/**
 * This component shows the last exception
 */
export class QwcChappieException extends observeState(QwcHotReloadElement) { 
    jsonRpc = new JsonRpc(this);
    routerController = new RouterController(this);

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
    `;
    
    static properties = {
        _lastException: {state: true},
        _suggestedFix: {state: true},
        _showProgressBar: {state: true},
        _loadedConfiguration: { state: true }
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
        
        
        if(!this._loadedConfiguration)this._init(); 
    }

    hotReload(){
        this._init();
        this._lastException = null;
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
                        ${this._renderLoadingDialog()}
                        ${this._renderAIResponseDialog()}`;
        } else {
            return html`<qwc-no-data message="No exception detected yet.">
                            ${this._renderCheckNowButton()}
                </qwc-no-data>`;
        }
    }
    
    _renderCheckNowButton(){
        return html`<vaadin-button theme="tertiary" @click=${this._checkLastException}>
                        <vaadin-icon icon="font-awesome-solid:repeat"></vaadin-icon>
                        Check now
                    </vaadin-button>`;
    }
    
    _renderLoadingDialog(){
        if(this._showProgressBar) {
            return html`<vaadin-confirm-dialog
                            header="Talking to the Quarkus AI Assistant..."
                            confirm-text="Cancel"
                            confirm-theme="error secondary"
                            .opened="${this._showProgressBar}"
                            @confirm="${this._termTalkToAI}">
                            ${this._renderLoadingDialogContent()}
                        </vaadin-confirm-dialog>`;
        }
    }
    
    _renderLoadingDialogContent(){
        return html`<div class="progress">
                            <vaadin-progress-bar
                                indeterminate
                                aria-labelledby="pblbl"
                                aria-describedby="sublbl"
                            ></vaadin-progress-bar>
                            <span class="text-secondary text-xs" id="sublbl">
                                This can take a while, please hold
                            </span>
                        </div>`;
    }
    
    _renderAIResponseDialog(){
        if(this._suggestedFix) {
            return html`<vaadin-dialog
                            header-title=""
                            resizable
                            draggable
                            .opened="${this._suggestedFix}"
                            @opened-changed="${this._aiResponseDialogOpenChanged}"
                            ${dialogHeaderRenderer(this._renderAIResponseDialogHeader, [])}
                            ${dialogRenderer(this._renderAIResponseDialogContent, [])}
                            ${dialogFooterRenderer(this._renderAIResponseDialogFooter, [])}
                        ></vaadin-dialog>`;
        }
    }
    
    _aiResponseDialogOpenChanged(event) {
        if (this._suggestedFix && event.detail.value === false) {
            // Prevent the dialog from closing
            event.preventDefault();
            event.target.opened = true;
        }
    }
    
    _renderAIResponseDialogHeader() {
        return html`
          <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
            <span style="font-weight: bold;font-size: x-large;">Suggested fix from the Quarkus AI Assistant</span>
            <qui-assistant-warning></qui-assistant-warning>
          </div>
        `;
      }
    
    _renderAIResponseDialogContent(){
        return html`<div class="fix">
                            <p>${this._suggestedFix.response}</p>

                            <p>${this._suggestedFix.explanation}</p>

                            <vaadin-details summary="Diff">
                                <div class="codeBlock">
                                    <qui-code-block
                                        mode='diff'
                                        theme='${themeState.theme.name}'>
                                        <slot>${this._suggestedFix.diff}</slot>
                                    </qui-code-block>
                                </div>
                            </vaadin-details>    

                            <h4>Suggested new code:</h4>
                            
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'>
                                    <slot>${this._suggestedFix.manipulatedContent}</slot>
                                </qui-code-block>
                            </div>
                            
                        </div>`;
    }
    
    _renderAIResponseDialogFooter(){
        return html`<div style="margin-right: auto;">
                        <vaadin-button theme="primary" @click="${this._applyFix}">
                            <vaadin-icon icon="font-awesome-solid:floppy-disk"></vaadin-icon>
                            Save
                        </vaadin-button>
                        <vaadin-button theme="secondary" @click="${this._copyFix}"> 
                            <vaadin-icon icon="font-awesome-solid:copy"></vaadin-icon>
                            Copy
                        </vaadin-button>
                    </div>
                    <vaadin-button theme="tertiary" @click="${this._clearAIResponseDialog}">
                        <vaadin-icon icon="font-awesome-solid:trash-can"></vaadin-icon>
                        Discard
                    </vaadin-button>`;
    }
    
    _clearAIResponseDialog(){
        this._suggestedFix = null;
    }
    
    _renderException(){
        return html`<div class="exception">
                        <pre class="stacktrace">${this._lastException.decorateString}</pre>
                        <pre class="stacktrace">${this._lastException.stackTraceString}</pre>
                    </div>`;
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
    
    _initTalkToAI(){
        this._showProgressBar = true;
        document.body.style.cursor = 'progress';
    }
    
    _termTalkToAI(){
        this._showProgressBar = false;
        document.body.style.cursor = 'default'; 
    }
    
    _checkLastException(){
        // Get the current last know exception
        this.jsonRpc.getLastException().then(jsonRpcResponse => { 
            this._lastException = jsonRpcResponse.result;
            this._suggestedFix = null;
        });
    }
    
    _suggestFix(){
        if(assistantState.current.isConfigured){
            this._initTalkToAI();
            // Get the current last know exception
            this.jsonRpc.suggestFix().then(jsonRpcResponse => {
                
                console.log(jsonRpcResponse);
                
                if(this._showProgressBar){
                    this._suggestedFix = jsonRpcResponse.result;
                }
                this._termTalkToAI();
            });
        } else {
            window.dispatchEvent(new CustomEvent('open-settings-dialog',{detail: {selectedTab : "quarkus-chappie/assistant-tab"}}));
        }
    }
    
    _applyFix(){
        this.jsonRpc.applyFix().then(jsonRpcResponse => { 
            notifier.showInfoMessage("Updated " + jsonRpcResponse.result);
            this._clearAIResponseDialog();
            super.forceRestart();
        });
    }
    
    _copyFix(){
        const content = this._suggestedFix?.manipulatedContent;
        if (!content) {
            notifier.showWarningMessage("There is no content");
            return;
        }
        
        navigator.clipboard.writeText(content)
            .then(() => {
                notifier.showInfoMessage("Content copied to clipboard");
            })
            .catch(err => {
                notifier.showErrorMessage("Failed to copy content:" + err);
            });
    }
    
    _init(){
        this.jsonRpc.loadConfiguration().then(jsonRpcResponse => { 
            this._loadedConfiguration = jsonRpcResponse.result;
            if(this._loadedConfiguration && this._loadedConfiguration.name){
                assistantState.ready();
            }else{
                assistantState.available();
            }
            this._suggestFix();
        });
    }
}
customElements.define('qwc-chappie-exception', QwcChappieException);
