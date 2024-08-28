import { LitElement, html, css} from 'lit'; 
import { JsonRpc } from 'jsonrpc';
import '@vaadin/button';
import '@vaadin/progress-bar';
import '@vaadin/item';
import '@vaadin/list-box';
import 'qui-ide-link';
import '@qomponent/qui-code-block';
import '@qomponent/qui-badge';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';
import { notifier } from 'notifier';
import { devuiState } from 'devui-state';

/**
 * This component shows the test creation page
 */
export class QwcChappieTesting extends observeState(LitElement) { 
    jsonRpc = new JsonRpc(this);

    static styles = css`
        :host {
            display: flex;
            height: 100%;
            padding: 10px;
        }
        .knownClasses {
            display: flex;
            flex-direction: column;
            padding: 20px;
            gap: 10px;
        }
        .row {
            display: flex;
            gap: 10px;
            justify-content: space-between;
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
        .clickable {
            cursor: pointer;
        }
        .codeBlockHeader {
            display: flex;
            width: 100%;
            justify-content: space-between;
            align-items: baseline;
        }
        .codeBlock {
            width: 100%;
            height: 100%;
        }
        .selectedSource {
            display: flex;
            width: 100%;
            height: 100%;
        }
        .selectedSource vaadin-button{
            position: absolute;
            z-index: 2;
            right: 10px;
        }
        .code {
            display: flex;
            flex-direction: column;
            width: 100%;
            height: 100%
        }
    `;
    
    static properties = {
        _knownClasses: {state: true},
        _suggestedTestSource: {state: true},
        _showProgressBar: {state: true},
        _selectedSource: {state: true},
        _selectedClass: {state: true}
    };

    constructor() { 
        super();
        this._knownClasses = null;
        this._selectedSource = null;
        this._selectedClass = null;
        this._suggestedTestSource = null;
        this._showProgressBar = false;
    }

    connectedCallback() {
        super.connectedCallback();
        // Get the current list of known classes
        this._loadKnownClasses();
    }

    disconnectedCallback() {
        super.disconnectedCallback();      
    }

    render() { 
        if (this._knownClasses) {
            return html`${this._renderKnownClasses()}
                        <div class="code">
                            ${this._renderSelectedSource()}
                            ${this._renderSuggestion()}
                        </div>
                        `;
        } else {
            return html`<div class="nothing">No code found. <span class="checkNow" @click="${this._loadKnownClasses}">Check now</span></div>`;
        }
    }
    
    _renderKnownClasses(){
        return html`<div class="knownClasses">
                        <vaadin-list-box selected="0" @selected-changed="${this._onSelectionChanged}">
                            ${this._knownClasses.map((className) =>
                                html`<vaadin-item>${className}</vaadin-item>`
                            )}
                        </vaadin-list-box>
                    </div>`;
    }
    
    _renderSelectedSource(){
        if(this._selectedSource){
            return html`<div class="selectedSource">
                            ${this._renderCreateTestButton()}
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'>
                                    <slot>${this._selectedSource}</slot>
                                </qui-code-block>
                            </div>
                        </div>`;
        }
    }
    
    
    
    _renderCreateTestButton(){
        if(!this._suggestedTestSource){
            return html`<vaadin-button theme="primary small" @click="${this._createTestSource}">Create test class</vaadin-button>`;
        }
    }
    
    _renderSuggestion(){
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
        }else if(this._suggestedTestSource){
            return html`<div class="fix">
                            <span class="heading-fix">
                                Suggested test class from AI
                            </span>

                            <p>${this._suggestedTestSource.explanation}</p>
                            
                            <div class="codeBlockHeader">
                                <h4>Suggested test code:</h4>
                                <vaadin-button theme="primary small" @click="${this._saveTestSource}">Save this to your project</vaadin-button>
                            </div>
                            
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'>
                                    <slot>${this._suggestedTestSource.suggestedTestSource}</slot>
                                </qui-code-block>
                            </div>
                            
                        </div>`;
        }
    }
    
    _onSelectionChanged(event) {
        this._selectedSource = null;
        this._selectedClass = null;
        this._suggestedTestSource = null;
        const listBox = event.target;
        
        if(listBox){
            const selectedItem = listBox.items[listBox.selected];
            const className = selectedItem.value;
            this._selectClass(className);
        }
    }
    
    _selectClass(className){
        this.jsonRpc.getSourceCode({className:className}).then(jsonRpcResponse => { 
            this._showProgressBar = false;
            this._selectedClass = className;
            this._selectedSource = jsonRpcResponse.result;
        });
    }
    
    _loadKnownClasses(){
        // Get the current list of know classes
        this.jsonRpc.getKnownClasses().then(jsonRpcResponse => { 
            this._knownClasses = jsonRpcResponse.result;
            this._suggestedTestSource = null;
            if(this._knownClasses && this._knownClasses.length>0){
                this._selectClass(this._knownClasses[0]);
            }
        });
    }
    
    _createTestSource(){
        this._showProgressBar = true;
        this.jsonRpc.suggestTestClass({className:this._selectedClass}).then(jsonRpcResponse => { 
            this._showProgressBar = false;
            this._suggestedTestSource = jsonRpcResponse.result;
        });
    }
    
    _saveTestSource(){
        this.jsonRpc.saveSuggestion().then(jsonRpcResponse => { 
            fetch(devuiState.applicationInfo.contextRoot);
            notifier.showInfoMessage("Updated " + jsonRpcResponse.result);
        });
    }
    
}
customElements.define('qwc-chappie-testing', QwcChappieTesting);
