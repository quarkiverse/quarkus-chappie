import { LitElement, html, css} from 'lit'; 
import { JsonRpc } from 'jsonrpc';
import '@vaadin/button';
import '@vaadin/progress-bar';
import '@vaadin/split-layout';
import '@qomponent/qui-code-block';
import '@qomponent/qui-directory-tree';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';

/**
 * This component shows the source code
 */
export class QwcChappieSourceCode extends observeState(LitElement) { 
    jsonRpc = new JsonRpc(this);

    static styles = css`
        :host {
            display: flex;
            height: 100%;
        }
        
        .selectedSource {
            display: flex;
            flex-direction: column;
            padding: 10px;
        }
        
        .codeBlock {
            width: 100%;
            height: 100%;
        }
    
        .split {
            display: flex;
            width: 100%;
            height: 100%;
        }
    
        .split vaadin-split-layout {
            width: 100%;
        }
    
        .files {
            height: 100%;
            display: flex;
            padding-left: 10px;
        }
    `;
    
    static properties = {
        _knownClasses: {state: true},
        _knownFiles: {state: true},
        _showProgressBar: {state: true},
        _selectedSource: {state: true},
        _selectedClass: {state: true}
    };

    constructor() { 
        super();
        this._knownClasses = null;
        this._knownFiles = null;
        this._selectedSource = null;
        this._selectedClass = null;
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
            return html`<div class="split">
                            <vaadin-split-layout>
                                <master-content style="width: 25%;">${this._renderKnownClasses()}</master-content>
                                <detail-content style="width: 75%;">${this._renderSelectedSource()}</detail-content>
                            </vaadin-split-layout>
                        </div>`;
        } else {
            return html`<div class="nothing">No code found. <span class="checkNow" @click="${this._loadKnownClasses}">Check now</span></div>`;
        }
    }
    
    _renderKnownClasses(){
        return html`<qui-directory-tree class="files"
                        .directory="${this._knownFiles}"
                        header="Source Code"
                        @file-select="${this._onFileSelect}"
                    ></qui-directory-tree>`;
    }
    
    _renderSelectedSource(){
        if(this._selectedSource){
            return html`<div class="selectedSource">
                            <h4>${this._selectedClass}</h4>
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
    
    _onFileSelect(event) {
        this._selectedSource = null;
        this._selectedClass = null;
        let className = this._convertDirectoryStructureToPackages(event.detail.file.path);
        this._selectClass(className);
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
                this._knownFiles = this._convertPackagesToDirectoryStructure();
            }
        });
    }
    
    _convertPackagesToDirectoryStructure() {
        const root = [];

        this._knownClasses.forEach((packageName) => {
          const parts = packageName.split('.');
          let currentLevel = root;

          parts.forEach((part, index) => {
            const isFile = index === parts.length - 1;
            let existing = currentLevel.find((item) => item.name === part);

            if (existing) {
              currentLevel = existing.children;
            } else {
              const newItem = {
                name: part,
                type: isFile ? 'file' : 'folder',
                children: isFile ? null : [],
              };
              currentLevel.push(newItem);
              currentLevel = isFile ? null : newItem.children;
            }
          });
        });

        return root;
    }


    _convertDirectoryStructureToPackages(item) {
        return item.replace(/\//g, '.');
    }
    
}
customElements.define('qwc-chappie-source-code', QwcChappieSourceCode);
