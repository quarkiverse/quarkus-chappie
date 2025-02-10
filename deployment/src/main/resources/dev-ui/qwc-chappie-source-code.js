import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/button';
import '@vaadin/progress-bar';
import '@vaadin/dialog';
import '@vaadin/split-layout';
import '@vaadin/menu-bar';
import '@vaadin/tooltip';
import '@qomponent/qui-code-block';
import '@qomponent/qui-directory-tree';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';
import { dialogFooterRenderer, dialogRenderer } from '@vaadin/dialog/lit.js';
import { notifier } from 'notifier';

/**
 * This component shows the source code
 */
export class QwcChappieSourceCode extends observeState(QwcHotReloadElement) { 
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
        
        .selectedSourceHeader {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: var(--lumo-primary-color-50pct);
        }
        .selectedSourceHeaderText {
            margin: 10px;
        }
        .sourceActions {
            background: var(--lumo-contrast);
            margin-right: 5px;
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
        .nothing {
            display: flex;
            
        }
    `;
    
    static properties = {
        _knownClasses: {state: true},
        _knownFiles: {state: true},
        _sourceActions: {state: true},
        _showTalkToAiProgressBar: {state: true},
        
        _selectedResource: {state: true},
        _generatedResource: {state: true}
    };

    constructor() { 
        super();
        this._knownClasses = null;
        this._knownFiles = null;
        this._sourceActions = [];
        this._showTalkToAiProgressBar = false; 
        
        this._clearSelectedResource();
        this._clearGeneratedResource();
    }

    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    hotReload(){
        // Get the current list of known classes
        this._loadKnownClasses();
        // Get the current list of known actions
        this._loadSourceActions();
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
                        </div>
                        ${this._renderLoadingDialog()}
                        ${this._renderSourceDialog()}
                        `;
        } else {
            return html`<div class="nothing">No code found. <span class="checkNow" @click="${this.hotReload}">Check now</span></div>`;
        }
    }
    
    _renderLoadingDialog(){
        if(this._showTalkToAiProgressBar) {
            return html`<vaadin-dialog
                        header-title="Talking to AI..."
                        .opened="${this._showTalkToAiProgressBar}"
                        @opened-changed="${this._preventLoadingDialogClose}"
                        ${dialogRenderer(this._renderLoadingDialogContent, [])}
                    ></vaadin-dialog>`;
        }
    }
    
    _renderSourceDialog(){
        if(this._generatedResource.contents) {
            return html`<vaadin-dialog
                        header-title="Generated code"
                        resizable
                        draggable
                        .opened="${this._generatedResource.contents}"
                        @opened-changed="${this._sourceDialogOpenChanged}"
                        ${dialogRenderer(this._renderSourceDialogContent, [])}
                        ${dialogFooterRenderer(this._renderSourceDialogFooter, [])}
                    ></vaadin-dialog>`;
        }
    }
    
    
    _preventLoadingDialogClose(event) {
        if (this._showTalkToAiProgressBar && event.detail.value === false) {
            // Prevent the dialog from closing
            event.preventDefault();
            event.target.opened = true;
        }
    }
    
    _sourceDialogOpenChanged(event) {
        if (this._generatedResource.path && event.detail.value === false) {
            // Prevent the dialog from closing
            event.preventDefault();
            event.target.opened = true;
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
        if(this._selectedResource.name){
            return html`<div class="selectedSource">
                            <div class="selectedSourceHeader">
                                <div id="header_${this._selectedResource.name}" class="selectedSourceHeaderText">${this._selectedResource.name} ${this._renderSaveButton()}</div>
                                <vaadin-tooltip for="header_${this._selectedResource.name}" text="${this._selectedResource.path}" position="top-start"></vaadin-tooltip>
                                ${this._renderActions()}
                            </div>
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'
                                    .content='${this._selectedResource.contents}'
                                    showLineNumbers>
                                </qui-code-block>
                            </div>
                        </div>`;
        }
    }
    
    _renderSaveButton(){
        if(this._selectedResource.isDirty){
            return html`<vaadin-button theme="small" @click="${this._saveSelectedSource}">
                            <vaadin-icon icon="font-awesome-solid:floppy-disk"></vaadin-icon>
                            Save
                        </vaadin-button>`;
        }
    }
    
    _saveSelectedSource(){
        this.jsonRpc.save({sourceCode:this._selectedResource.contents, path:this._selectedResource.path}).then(jsonRpcResponse => { 
            if(jsonRpcResponse.result.success){
                notifier.showInfoMessage("File [" + jsonRpcResponse.result.path + "] saved successfully");
                this._selectedResource = { ...this._selectedResource, isDirty: false };
            }else {
                notifier.showErrorMessage("File [" + jsonRpcResponse.result.path + "] NOT saved. " + jsonRpcResponse.result.errorMessage);
            }
        });
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
    
    _renderSourceDialogFooter(){
        return html`<div style="margin-right: auto;">
                        <vaadin-button theme="primary" @click="${this._saveGeneratedSource}">
                            <vaadin-icon icon="font-awesome-solid:floppy-disk"></vaadin-icon>
                            Save
                        </vaadin-button>
                        <vaadin-button theme="secondary" @click="${this._copyGeneratedSource}"> 
                            <vaadin-icon icon="font-awesome-solid:copy"></vaadin-icon>
                            Copy
                        </vaadin-button>
                    </div>
                    <vaadin-button theme="tertiary" @click="${this._discardGeneratedSource}">
                        <vaadin-icon icon="font-awesome-solid:trash-can"></vaadin-icon>
                        Discard
                    </vaadin-button>`;
    }
    
    _saveGeneratedSource(){
        this.jsonRpc.save({sourceCode:this._generatedResource.contents, path:this._generatedResource.path}).then(jsonRpcResponse => { 
            if(jsonRpcResponse.result.success){
                notifier.showInfoMessage("File [" + jsonRpcResponse.result.path + "] saved successfully");
                this._clearGeneratedResource();
            }else {
                notifier.showErrorMessage("File [" + jsonRpcResponse.result.path + "] NOT saved. " + jsonRpcResponse.result.errorMessage);
            }
        });
    }
    
    _copyGeneratedSource(){
        const content = this._generatedResource?.contents;
        const path = this._generatedResource?.path;
        if (!content) {
            notifier.showWarningMessage("File [" + path + "] has no content");
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
    
    _discardGeneratedSource(){
        this._clearGeneratedResource();
    }
    
    _renderSourceDialogContent(){
        return html`<div class="codeBlock">
                                <qui-code-block
                                    mode='java'
                                    theme='${themeState.theme.name}'
                                    .content='${this._generatedResource.contents}'
                                    showLineNumbers>
                                </qui-code-block>
                            </div>`;
    }
    
    _renderActions(){
        if(this._sourceActions &&!this._showTalkToAiProgressBar){
            return html`<div class="sourceActions">
                            <vaadin-menu-bar .items="${this._sourceActions}" theme="dropdown-indicators" @item-selected="${(e) => this._sourceMenuSelected(e)}"></vaadin-menu-bar>
                        </div>`;
        }
    }
    
    _sourceMenuSelected(e){
    
        this._showTalkToAiProgressBar = true;
        document.body.style.cursor = 'progress';
        
        this.jsonRpc[e.detail.value.methodName]({className:this._selectedResource.name, path:this._selectedResource.path}).then(jsonRpcResponse => { 
            if(e.detail.value.action === "Manipulation"){
                this._selectedResource = { ...this._selectedResource, path: jsonRpcResponse.result.path, contents: jsonRpcResponse.result.contents, isDirty: true };
            }else if(e.detail.value.action === "Generation"){
                this._generatedResource = { ...this._generatedResource, path: jsonRpcResponse.result.path, contents: jsonRpcResponse.result.contents, isDirty: true };
            }
            this._showTalkToAiProgressBar = false;
            document.body.style.cursor = 'default'; 
        });
    }
    
    _onFileSelect(event) {
        this._clearSelectedResource();
        let className = this._convertDirectoryStructureToPackages(event.detail.file.path);
        this._selectKnownClass(this._knownClasses.get(className));
    }
    
    _clearSelectedResource(){
        this._selectedResource = { name: null, path: null, contents: null, isDirty: false }
    }
    
    _clearGeneratedResource(){
        this._generatedResource = { name: null, path: null, contents: null, isDirty: false }
    }
    
    _selectKnownClass(knownClass){
        this.jsonRpc.getSourceCode({className:knownClass.className}).then(jsonRpcResponse => {
            this._selectedResource = { ...this._selectedResource, 
                contents: jsonRpcResponse.result,
                name:  knownClass.className,
                path:  knownClass.path
            };
        });
    }
    
    _loadKnownClasses(){
        // Get the current list of known classes
        this.jsonRpc.getKnownClasses().then(jsonRpcResponse => {
            if (Array.isArray(jsonRpcResponse.result)) {
                this._knownClasses = new Map(jsonRpcResponse.result.map(obj => [obj.className, obj]));
            } else {
                console.error("Expected an array but got:", jsonRpcResponse.result);
            }
            
            this._explanation = null;
            if(this._knownClasses && this._knownClasses.size>0){
                if(this._selectedResource.name){
                    this._selectKnownClass(this._selectedResource.name);
                } else {
                    this._selectKnownClass([...this._knownClasses.values()][0]);
                }
                this._knownFiles = this._convertPackagesToDirectoryStructure();
            }
        });
    }
    
    _loadSourceActions(){
        // TODO: Add extension name ?
        this.jsonRpc.getSourceActions().then(jsonRpcResponse => { 
            this._sourceActions = [
                {
                  text: "Action", className: 'bg-primary text-primary-contrast', 
                  children: jsonRpcResponse.result.map(item => ({ text: item.label, methodName: item.methodName, action: item.action}))
                }
            ];
        });
        
        
    }
    
    _convertPackagesToDirectoryStructure() {
        const root = [];
        this._knownClasses.forEach((value, key) => {
            const parts = value.className.split('.');
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
                        ...(isFile && { path: value.path }) 
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
