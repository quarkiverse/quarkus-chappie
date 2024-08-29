import { LitElement, html, css} from 'lit'; 
import { JsonRpc } from 'jsonrpc';
import MarkdownIt from 'markdown-it';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import '@vaadin/button';
import '@vaadin/progress-bar';
import '@vaadin/item';
import '@vaadin/list-box';
import '@qomponent/qui-code-block';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';

/**
 * This component shows the explanation page
 */
export class QwcChappieExplanation extends observeState(LitElement) { 
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
        .fix {
            display: flex;
            flex-direction: column;
            padding-top: 20px;
        }
        .heading-fix {
            color: var(--lumo-success-color);
            font-size: x-large; 
        }
        .codeBlock {
            width: 100%;
            height: 100%;
        }
        .selectedSource {
            display: flex;
            width: 100%;
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
        }
    `;
    
    static properties = {
        _knownClasses: {state: true},
        _explanation: {state: true},
        _showProgressBar: {state: true},
        _selectedSource: {state: true},
        _selectedClass: {state: true}
    };

    constructor() { 
        super();
        this.md = new MarkdownIt();
        this._knownClasses = null;
        this._selectedSource = null;
        this._selectedClass = null;
        this._explanation = null;
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
                            ${this._renderExplanation()}
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
                            ${this._renderExplainButton()}
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
    
    
    
    _renderExplainButton(){
        if(!this._explanation){
            return html`<vaadin-button theme="primary small" @click="${this._explainSource}">Explain this class</vaadin-button>`;
        }
    }
    
    _renderExplanation(){
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
        }else if(this._explanation){
            const htmlContent = this.md.render(this._explanation);
            return html`<div class="fix">
                            <span class="heading-fix">
                                Explanation from AI
                            </span>
                            <div class="readme">${unsafeHTML(htmlContent)}</div>
                        </div>`;
        }
    }
    
    _onSelectionChanged(event) {
        this._selectedSource = null;
        this._selectedClass = null;
        this._explanation = null;
        const listBox = event.target;
        
        if(listBox && listBox.items){
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
            this._explanation = null;
            if(this._knownClasses && this._knownClasses.length>0){
                this._selectClass(this._knownClasses[0]);
            }
        });
    }
    
    _explainSource(){
        this._showProgressBar = true;
        this.jsonRpc.explainClass({className:this._selectedClass}).then(jsonRpcResponse => { 
            this._showProgressBar = false;
            this._explanation = jsonRpcResponse.result;
        });
    }
    
}
customElements.define('qwc-chappie-explanation', QwcChappieExplanation);
