import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/button';
import '@vaadin/progress-bar';
import '@vaadin/dialog';
import '@vaadin/confirm-dialog';
import '@vaadin/split-layout';
import '@vaadin/menu-bar';
import '@vaadin/tooltip';
import '@qomponent/qui-code-block';
import '@qomponent/qui-directory-tree';
import '@qomponent/qui-badge';
import MarkdownIt from 'markdown-it';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import { observeState } from 'lit-element-state';
import { themeState } from 'theme-state';
import { dialogFooterRenderer, dialogRenderer } from '@vaadin/dialog/lit.js';
import { notifier } from 'notifier';

/**
 * This component shows the Chappie workspace
 */
export class QwcChappieWorkspace extends observeState(QwcHotReloadElement) { 
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
        .actions {
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
        _actions: {state: true},
        _filteredActions: {state: true},
        _workspaceItems: {state: true},
        _workspaceTreeNames: {state: true},
        
        _showTalkToAiProgressBar: {state: true},
        
        _selectedWorkspaceItem: {state: true},
        _generatedResource: {state: true},
        _markdownContent: {state: true}
    };

    constructor() { 
        super();
        this.md = new MarkdownIt();
        this._workspaceItems = null;
        this._workspaceTreeNames = null;
        
        this._actions = [];
        this._filteredActions = this._actions;
        this._showTalkToAiProgressBar = false; 
        
        this._clearSelectedWorkspaceItem();
        this._clearGeneratedContent();
        this._clearMarkdownContent();
    }

    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    hotReload(){
        this._workspaceItems = null;
        // Get the workspace items
        this._loadWorkspaceItems();
        
        // Get the current list of known actions
        this._loadActions();
    }

    disconnectedCallback() {
        super.disconnectedCallback();      
    }

    render() { 
        if (this._workspaceItems) {
            return html`<div class="split">
                            <vaadin-split-layout>
                                <master-content style="width: 25%;">${this._renderWorkspaceTree()}</master-content>
                                <detail-content style="width: 75%;">${this._renderSelectedSource()}</detail-content>
                            </vaadin-split-layout>
                        </div>
                        ${this._renderLoadingDialog()}
                        ${this._renderGeneratedDialog()}
                        ${this._renderMarkdownDialog()}
                        `;
        } else {
            return html`<div class="nothing">No code found. <span class="checkNow" @click="${this.hotReload}">Check now</span></div>`;
        }
    }
    
    _renderLoadingDialog(){
        if(this._showTalkToAiProgressBar) {
            return html`<vaadin-confirm-dialog
                            header="Talking to AI..."
                            confirm-text="Cancel"
                            confirm-theme="error secondary"
                            .opened="${this._showTalkToAiProgressBar}"
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
    
    _renderGeneratedDialog(){
        if(this._generatedResource.contents) {
            return html`<vaadin-dialog
                            header-title="AI generated content"
                            resizable
                            draggable
                            .opened="${this._generatedResource.contents}"
                            @opened-changed="${this._generatedDialogOpenChanged}"
                            ${dialogRenderer(this._renderGeneratedDialogContent, [])}
                            ${dialogFooterRenderer(this._renderGeneratedDialogFooter, [])}
                        ></vaadin-dialog>`;
        }
    }
    
    _renderMarkdownDialog(){
        if(this._markdownContent.contents) {
            return html`<vaadin-dialog
                            header-title="AI response"
                            resizable
                            draggable
                            .opened="${this._markdownContent.contents}"
                            @opened-changed="${this._markdownDialogOpenChanged}"
                            ${dialogRenderer(this._renderMarkdownDialogContent, [])}
                            ${dialogFooterRenderer(this._renderMarkdownDialogFooter, [])}
                        ></vaadin-dialog>`;
        }
    }
    
    _markdownDialogOpenChanged(event) {
        if (this._markdownContent.contents && event.detail.value === false) {
            // Prevent the dialog from closing
            event.preventDefault();
            event.target.opened = true;
        }
    }
    
    _generatedDialogOpenChanged(event) {
        if (this._generatedResource.path && event.detail.value === false) {
            // Prevent the dialog from closing
            event.preventDefault();
            event.target.opened = true;
        }
    }
    
    _renderWorkspaceTree(){
        return html`<qui-directory-tree class="files"
                        .directory="${this._workspaceTreeNames}"
                        header="Source Code"
                        @file-select="${this._onFileSelect}"
                    ></qui-directory-tree>`;
    }

    _renderSelectedSource(){
        if(this._selectedWorkspaceItem.name){
            return html`<div class="selectedSource">
                            <div class="selectedSourceHeader">
                                <div id="header_${this._selectedWorkspaceItem.name}" class="selectedSourceHeaderText">${this._selectedWorkspaceItem.name} ${this._renderSaveButton()}</div>
                                <vaadin-tooltip for="header_${this._selectedWorkspaceItem.name}" text="${this._selectedWorkspaceItem.path}" position="top-start"></vaadin-tooltip>
                                ${this._renderActions()}
                            </div>
                            <div class="codeBlock">
                                <qui-code-block
                                    mode='${this._getMode(this._selectedWorkspaceItem.name)}'
                                    theme='${themeState.theme.name}'
                                    .content='${this._selectedWorkspaceItem.contents}'
                                    showLineNumbers>
                                </qui-code-block>
                            </div>
                            ${this._renderWarningWhenDirty()}
                        </div>`;
        }
    }
    
    _renderWarningWhenDirty(){
        if(this._selectedWorkspaceItem.isDirty){
            return this._renderWarning();
        }
    }
    
    _getMode(fileName) {
        const parts = fileName.split('.');
        if (parts.length > 1) {
            return parts.pop().toLowerCase();
        }
        return null;
    }

    
    _renderSaveButton(){
        if(this._selectedWorkspaceItem.isDirty){
            return html`<vaadin-button theme="small" @click="${this._saveSelectedWorkspaceItem}" style="color:white">
                            <vaadin-icon icon="font-awesome-solid:floppy-disk"></vaadin-icon>
                            Save
                        </vaadin-button>`;
        }
    }
    
    _saveSelectedWorkspaceItem(){
        this.jsonRpc.saveWorkspaceItemContent({content:this._selectedWorkspaceItem.contents, path:this._selectedWorkspaceItem.path}).then(jsonRpcResponse => { 
            if(jsonRpcResponse.result.success){
                notifier.showInfoMessage(jsonRpcResponse.result.path + " saved successfully");
                this._selectedWorkspaceItem = { ...this._selectedWorkspaceItem, isDirty: false };
                super.forceRestart();
            }else {
                notifier.showErrorMessage(jsonRpcResponse.result.path + " NOT saved. " + jsonRpcResponse.result.errorMessage);
            }
        });
    }
    
    _renderGeneratedDialogFooter(){
        return html`<div style="margin-right: auto;">
                        <vaadin-button theme="primary" @click="${this._saveGeneratedContent}">
                            <vaadin-icon icon="font-awesome-solid:floppy-disk"></vaadin-icon>
                            Save
                        </vaadin-button>
                        <vaadin-button theme="secondary" @click="${this._copyGeneratedContent}"> 
                            <vaadin-icon icon="font-awesome-solid:copy"></vaadin-icon>
                            Copy
                        </vaadin-button>
                    </div>
                    <vaadin-button theme="tertiary" @click="${this._clearGeneratedContent}">
                        <vaadin-icon icon="font-awesome-solid:trash-can"></vaadin-icon>
                        Discard
                    </vaadin-button>`;
    }
    
    _renderMarkdownDialogFooter(){
        return html`<div style="margin-right: auto;">
                        <vaadin-button theme="secondary" @click="${this._copyMarkdownContent}"> 
                            <vaadin-icon icon="font-awesome-solid:copy"></vaadin-icon>
                            Copy
                        </vaadin-button>
                    </div>
                    <vaadin-button theme="tertiary" @click="${this._clearMarkdownContent}">
                        <vaadin-icon icon="font-awesome-solid:trash-can"></vaadin-icon>
                        Discard
                    </vaadin-button>`;
    }
    
    _saveGeneratedContent(){
        this.jsonRpc.saveWorkspaceItemContent({content:this._generatedResource.contents, path:this._generatedResource.path}).then(jsonRpcResponse => { 
            if(jsonRpcResponse.result.success){
                notifier.showInfoMessage(jsonRpcResponse.result.path + " saved successfully");
                this._clearGeneratedContent();
                super.forceRestart();
            }else {
                notifier.showErrorMessage(jsonRpcResponse.result.path + " NOT saved. " + jsonRpcResponse.result.errorMessage);
            }
        });
    }
    
    _copyGeneratedContent(){
        const content = this._generatedResource?.contents;
        const path = this._generatedResource?.path;
        if (!content) {
            notifier.showWarningMessage(path + " has no content");
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
    
    _copyMarkdownContent(){
        const content = this._markdownContent?.contents;
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
    
    _renderGeneratedDialogContent(){
        // TODO: Get the file type of the generated content
        return html`<div class="codeBlock">
                        <qui-code-block
                            mode='java'
                            theme='${themeState.theme.name}'
                            .content='${this._generatedResource.contents}'
                            showLineNumbers>
                        </qui-code-block>
                    </div>
                    ${this._renderWarning()}`;
    }
    
    _renderMarkdownDialogContent(){
        const htmlContent = this.md.render(this._markdownContent.contents);
        return html`<div class="markdown">
                        ${unsafeHTML(htmlContent)}
                    </div>
                    ${this._renderWarning()}`;
    }
    
    _renderActions(){
        if(this._filteredActions &&!this._showTalkToAiProgressBar){
            return html`<div class="actions">
                            <vaadin-menu-bar .items="${this._filteredActions}" theme="dropdown-indicators" @item-selected="${(e) => this._sourceMenuSelected(e)}"></vaadin-menu-bar>
                        </div>`;
        }
    }
    
    _sourceMenuSelected(e){
        this._initTalkToAI();
        
        this.jsonRpc[e.detail.value.methodName]({name:this._selectedWorkspaceItem.name, path:this._selectedWorkspaceItem.path}).then(jsonRpcResponse => {
            if(this._showTalkToAiProgressBar) { // Else the user has canceled
                
                const firstEntry = Object.entries(jsonRpcResponse.result)[0];
                if(firstEntry){
                    const [path, contents] = firstEntry;
                    if(e.detail.value.actionType === "Update"){
                        this._selectedWorkspaceItem = { ...this._selectedWorkspaceItem, path: path, contents: contents, isDirty: true };
                    }else if(firstEntry && e.detail.value.actionType === "Create"){
                        this._generatedResource = { ...this._generatedResource, path: path, contents: contents, isDirty: true };
                    }else if(firstEntry && e.detail.value.actionType === "Read"){
                        console.log(path);
                        console.log(contents);
                        this._markdownContent = { ...this._markdownContent, contents: contents};
                    }
                }else {
                    console.warn(JSON.stringify(jsonRpcResponse.result));
                }
                this._termTalkToAI(); 
            }
        });
    }
    
    _initTalkToAI(){
        this._showTalkToAiProgressBar = true;
        document.body.style.cursor = 'progress';
    }
    
    _termTalkToAI(){
        this._showTalkToAiProgressBar = false;
        document.body.style.cursor = 'default'; 
    }
    
    _onFileSelect(event) {
        this._clearSelectedWorkspaceItem();
        this._selectWorkspaceItem(this._workspaceItems.get(event.detail.file));
    }
    
    _clearSelectedWorkspaceItem(){
        this._selectedWorkspaceItem = { name: null, path: null, contents: null, isDirty: false };
    }
    
    _clearGeneratedContent(){
        this._generatedResource = { name: null, path: null, contents: null, isDirty: false };
    }
    
    _clearMarkdownContent(){
        this._markdownContent = { name: null, contents: null};
    }
    
    _selectWorkspaceItem(workspaceItem){
        this.jsonRpc.getWorkspaceItemContent({path:workspaceItem.path}).then(jsonRpcResponse => {
            this._selectedWorkspaceItem = { ...this._selectedWorkspaceItem, 
                contents: jsonRpcResponse.result,
                name:  workspaceItem.name,
                path:  workspaceItem.path
            };
            
            this._filterActions(workspaceItem.name);
        });
    }
    
    _loadWorkspaceItems(){
        this.jsonRpc.getWorkspaceItems().then(jsonRpcResponse => {
            if (Array.isArray(jsonRpcResponse.result)) {
                this._workspaceItems = new Map(jsonRpcResponse.result.map(obj => [obj.name, obj]));                
            } else {
                console.error("Expected an array but got:", jsonRpcResponse.result);
            }
            
            this._explanation = null;
            if(this._workspaceItems && this._workspaceItems.size>0){
                if(this._selectedWorkspaceItem.name){
                    this._selectWorkspaceItem(this._selectedWorkspaceItem.name);
                } else {
                    this._selectWorkspaceItem([...this._workspaceItems.values()][0]);
                }
                this._workspaceTreeNames = this._convertDirectoryStructureToTree();
            }
        });
    }
    
    
    
    _loadActions(){
        // TODO: Add extension name ?
        this.jsonRpc.getActions().then(jsonRpcResponse => { 
            
            this._actions = [
                {
                  text: "Action", className: 'bg-primary text-primary-contrast', 
                  children: jsonRpcResponse.result.map(item => ({ text: item.label, methodName: item.methodName, actionType: item.actionType, pattern: item.pattern}))
                }
            ];
        });
    }
    
    _filterActions(name) {
        this._filteredActions = this._actions.map(actionGroup => {
        
            const filteredChildren = actionGroup.children.filter(child => {
                if(child.pattern){
                    const regex = new RegExp(child.pattern);
                    return regex.test(name);
                }
                return true;
            });

            if (filteredChildren.length > 0) {
                return { ...actionGroup, children: filteredChildren };
            }
            return null;
        }).filter(actionGroup => actionGroup !== null);
    }

    
    _convertDirectoryStructureToTree() {
        const root = [];
        this._workspaceItems.forEach((value, key) => {
            const parts = value.name.split('/');
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

    _renderWarning(){
        return html`<qui-badge text="Warning" level="warning" icon="warning" style="display: flex;flex-direction: column;">
            <span>AI can make mistakes. Check responses.</span>
        </qui-badge>`;
    }

}
customElements.define('qwc-chappie-workspace', QwcChappieWorkspace);
