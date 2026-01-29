import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import { assistantState } from 'assistant-state';
import { observeState } from 'lit-element-state';
import { msg, str, updateWhenLocaleChanges } from 'localization';
import 'qwc-no-data';
import '@vaadin/message-input';
import '@vaadin/message-list';
import '@vaadin/progress-bar';
import '@vaadin/popover';
import { popoverRenderer } from '@vaadin/popover/lit.js';
import './qwc-chappie-chat-history.js';
import {ring} from 'ldrs';

ring.register();

/**
 * This component allows Assistant Chat
 */
export class QwcChappieChat extends observeState(QwcHotReloadElement) { 
    jsonRpc = new JsonRpc(this);
    
    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            height: 100%;
            padding: 10px;
            overflow-y: hidden;
        }
    
        .messageInput {
            width: 100%;
        }
    
        .inputNew {
            display: flex;
            height: 100%;
            justify-content: flex-start;
            flex-direction: column;
            padding-top: 25vh;
        }
    
        .buttonsNew {
            display: flex;
            flex-direction: row-reverse;
        }
    
        .inputExisting {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 100%;
            max-height: 100%;
            overflow-y: hidden;
        }
        
        .inputExistingWithMenu {
            max-height: 100%;
            overflow-y: hidden;
        }
        
        .inputExistingList {
            overflow-y: scroll;
            max-height: 100%;
            padding-bottom: 30px;
        }
        
        .headerNew {
            text-align: center;
            padding-right: 76px;
            color: var(--quarkus-blue);
        }
        
        .header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: larger;
            background: var(--lumo-contrast-5pct);
        }
    
        .headerText {
            padding-left: 5px;
        }
    
        .buttons {
            display: flex;
            gap: 8px;
        }
        
        .buttonIcon {
            height: var(--lumo-icon-size-s); 
            width: var(--lumo-icon-size-s);
        }
    
        .tool {
            display: flex;
            flex-direction: row-reverse;
            color: var(--lumo-contrast-50pct);
            font-family: "Lucida Console", Monaco, monospace;
            font-size: smaller;
        }
    `;
    
    static properties = {
        _heading: { state: true },
        _memoryId: { state: true },
        _messages: { state: true },
        _inputIsBlocked: { state: true }
        
    };

    constructor() {
        super();
        updateWhenLocaleChanges(this);
        this._messages = [];
        this._inputIsBlocked = false;
        this._heading = null;
        this._memoryId = null;
    }

    connectedCallback() {
        super.connectedCallback();
        this._getCurrentMessages();
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
        const pageDiv = document.getElementById('page');
        if (pageDiv) {
            pageDiv.style.overflowY = 'auto';
        }
    }
    
    firstUpdated() {
        const pageDiv = document.getElementById('page');
        if (pageDiv) {
            pageDiv.style.overflowY = 'hidden';
        }
    }
    
    hotReload(){
        document.body.style.cursor = 'default';
        this._inputIsBlocked = false;
        this._getCurrentMessages();
    }
    
    render() { 
        if(assistantState.current.isConfigured){
            if (typeof this._messages !== 'undefined' && this._messages.length > 0) {
                return this._renderExistingChat();
            }else{
                return this._renderNewChat();
            }
        }else {
            return this._renderUnconfigured();
        }
    }
    
    _renderExistingChat(){
        return html`<div class="inputExisting">
                        <div class="inputExistingWithMenu">
                            <div class="header">
                                ${this._renderHeading()}
                                <div class="buttons">
                                    <vaadin-button theme="tertiary error" @click=${this._deleteChat}>
                                        <vaadin-icon class="buttonIcon" icon="font-awesome-solid:trash-can" slot="prefix"></vaadin-icon>
                                        ${msg('Delete', { id: 'quarkus-chappie-delete' })}
                                    </vaadin-button>
                                    <vaadin-button theme="tertiary" @click=${this._startNewChat}>
                                        <vaadin-icon class="buttonIcon" icon="font-awesome-regular:pen-to-square" slot="prefix"></vaadin-icon>
                                        ${msg('New chat', { id: 'quarkus-chappie-new-chat' })}
                                    </vaadin-button>
                                    ${this._renderHistoryButton()}
                                </div>
                            </div>
                            <div class="inputExistingList">
                                <vaadin-message-list .items="${this._messages}" markdown></vaadin-message-list>
                            </div>
                        </div>
                        ${this._renderInputOrProgressBar()}
                    </div>
            `;
    }
    
    _renderHistoryButton(){
        return html`<vaadin-button id="history" theme="tertiary">
                                        <vaadin-icon class="buttonIcon" icon="font-awesome-solid:clock-rotate-left" slot="prefix"></vaadin-icon>
                                        ${msg('History', { id: 'quarkus-chappie-history' })}
                                    </vaadin-button>
                                    <vaadin-popover
                                        for="history"
                                        theme="arrow no-padding"
                                        modal
                                        accessible-name-ref="history-heading"
                                        content-width="300px"
                                        position="bottom"
                                        ${popoverRenderer(this._historyRenderer, [])}
                                    ></vaadin-popover>`;
    }
    
    _renderHeading(){
        if(this._heading){
            return html`<span class="headerText" title="${msg('Memory Id: ', { id: 'quarkus-chappie-memory-id' })}${this._memoryId}">${this._heading}</span>`;
        }else{
            return html`<l-ring size="26" stroke="2" color="var(--lumo-contrast-25pct)" class="headerText"></l-ring>`;
        }
    }
    
    _historyRenderer(){
        return html`<qwc-chappie-chat-history @selectChat="${this._selectChat}" limit=5 namespace=${this.jsonRpc.getExtensionName()}></qwc-chappie-chat-history>`;
    }
    
    _selectChat(e){
        this._messages = [];
        document.body.style.cursor = 'progress'; 
        this.jsonRpc.getChatMessages({memoryId:e.detail}).then(jsonRpcResponse => {
            this._handleChatMessageResponse(jsonRpcResponse);
            document.body.style.cursor = 'default';
        });
    }
    
    _renderInputOrProgressBar(){
        if(this._inputIsBlocked){
            return html`<div><vaadin-progress-bar indeterminate></vaadin-progress-bar></div>`;
        }else{
            return html`<vaadin-message-input ?disabled=${this._inputIsBlocked} class="messageInput" @submit="${this._handleSubmit}"></vaadin-message-input>`;
        }
    }
    
    _renderNewChat(){
        return html`<div class="buttonsNew">${this._renderHistoryButton()}</div>
                    <div class="inputNew">
                        <h1 class="headerNew">${msg('Welcome to the Assistant Chat', { id: 'quarkus-chappie-welcome' })}</h2>
                        <div style="width:100%;"><vaadin-message-input class="messageInput" @submit="${this._handleSubmit}"></vaadin-message-input></div>
                    </div>
            `;
    }
    
    _renderUnconfigured(){
        return html`<qwc-no-data message="${msg('Assistant is not configured.', { id: 'quarkus-chappie-not-configured' })}">
                    ${this._renderConfigureNowButton()}
                </qwc-no-data>`;
    }

    _getCurrentMessages(){
        this._messages = [];
        document.body.style.cursor = 'progress'; 
        this.jsonRpc.getMostRecentChatMessages().then(jsonRpcResponse => {
            this._handleChatMessageResponse(jsonRpcResponse);
            document.body.style.cursor = 'default';
        });    
    }
    
    _handleSubmit(event) {
        document.body.style.cursor = 'progress';
        let m = event.detail.value;
        this._addUserMessage(m);
        this._addAssistantMessage(msg('Thinking ...', { id: 'quarkus-chappie-thinking' }));
        this._inputIsBlocked = true;
        this._scrollToBottom();
        this.jsonRpc.chat({message:m}).then(jsonRpcResponse => {
            document.body.style.cursor = 'default';
            
            this._removeLastMessage();
            
            if(jsonRpcResponse.result?.completedExceptionally){
                this._addServerErrorMessage();
            } else {
                const [key, value] = Object.entries(jsonRpcResponse.result)[0];
                this._memoryId = key;

                if(value?.completedExceptionally && value?.completedExceptionally === true){
                    this._addServerErrorMessage();
                }else{
                    this._heading = value?.niceName;
                    
                    let markdown = value?.answer?.markdown;
                    let action = this._getActionLabel(value?.answer?.action);
                    if(markdown){
                        this._addAssistantMessage(markdown + action);
                    }
                }
            }
            
            this._inputIsBlocked = false;
        }).catch((err) => {
            document.body.style.cursor = 'default';
            console.error(JSON.stringify(err));
            this._removeLastMessage();
            this._inputIsBlocked = false;
            
            
            if(err.error.message){
                this._addErrorMessage(err.error.message + msg('. See the Assistant log for details', { id: 'quarkus-chappie-error-see-log-suffix' }));
            }else {
                this._addServerErrorMessage();
            }
        });
    }

    _handleChatMessageResponse(jsonRpcResponse){
        let r = jsonRpcResponse?.result;
        if(r){
            this._heading = r.summary?.niceName;
            this._memoryId = r.summary?.memoryId;
            
            if(r.messages){
                let messages = r.messages;
                for (const m of messages) {
                    if(m.type === 'USER'){
                       this._addUserMessage(m.contents[0].text);
                    }else if(m.type === 'AI'){
                        const t = m.text?.trim();
                        if(t){
                            const obj = JSON.parse(t);
                            let action = this._getActionLabel(obj?.answer?.action);
                            if(obj?.answer?.markdown){
                                this._addAssistantMessage(obj?.answer?.markdown + action);
                            }else if(obj?.answer?.content && obj?.answer?.path){
                                let ext = this._getExt(obj?.answer?.path);
                                this._addAssistantMessage("```"+ ext + "\n" + obj?.answer?.content + "\n```" + action);
                            }else if(obj?.answer?.content){
                                this._addAssistantMessage(obj?.answer?.content + action);
                            }else if(obj?.answer?.code){
                                this._addAssistantMessage("```\n" + obj?.answer?.code + "\n```" + action);
                            }else if(obj?.answer){
                                // If there is only one field, use that:
                                let f = this._getOnlyNonEmptyField(obj?.answer);
                                if(f && f.value){
                                    this._addAssistantMessage("```\n" + f.value + "\n```" + action);
                                }else if(f){
                                    this._addAssistantMessage("```json\n" + JSON.stringify(f, null, 2) + "\n```" + action);
                                }else{
                                    // Remove methods
                                    let clean = this._stripMethodishKeysStrict(obj?.answer);
                                    this._addAssistantMessage("```json\n" + JSON.stringify(clean, null, 2) + "\n```" + action);
                                }
                            }else {
                                this._addAssistantMessage("```json\n" + JSON.stringify(obj, null, 2) + "\n```" + action);
                            }
                        }
                    }
                }
            }   
        }
    }
    
    _getActionLabel(action){
        if(action){
            return "<br><br><div class='tool' title='" + msg('Suggested MCP Tool', { id: 'quarkus-chappie-suggested-mcp-tool' }) + "'>⚒️ " + action + "</div>";
        }else{
            return "";
        }
    }
    
    _getOnlyNonEmptyField(obj) {
        const entries = Object.entries(obj).filter(([, v]) => v !== null && v !== undefined);
        if (entries.length === 1) {
          const [key, value] = entries[0];
          return { key, value };
        }
        return null;
    }
    
    _getExt(p) {
        const base = p.split(/[\\/]/).pop();
        const dot  = base.lastIndexOf('.');
        return dot > 0 && dot < base.length - 1 ? base.slice(dot + 1) : "";
    }
    
    _deleteChat(){
        if(this._memoryId){
            this.jsonRpc.deleteChat({memoryId:this._memoryId});
        }
        this._heading = null;
        this._memoryId = null;
        this._getCurrentMessages();
    }
    
    _startNewChat(){
        document.body.style.cursor = 'progress'; 
        this.jsonRpc.clearMemory().then(jsonRpcResponse => {
            this._messages = [];
            this._heading = null;
            this._memoryId = null;
            document.body.style.cursor = 'default'; 
        });
    }
   
    _renderConfigureNowButton(){
        return html`<vaadin-button theme="tertiary" @click=${this._configureNow}>
                        <vaadin-icon icon="font-awesome-solid:gears"></vaadin-icon>
                        ${msg('Configure now', { id: 'quarkus-chappie-configure-now' })}
                    </vaadin-button>`;
    }
    
    _configureNow(){
        window.dispatchEvent(new CustomEvent('open-settings-dialog',{detail: {selectedTab : "quarkus-chappie/assistant-tab"}}));
    }
    
    _addUserMessage(message){
        this._addToMessages(message, msg('You', { id: 'quarkus-chappie-you' }), 6);
    }

    _addAssistantMessage(message){
        this._addToMessages(message, msg('Assistant', { id: 'quarkus-chappie-assistant' }), 5);
    }

    _addServerErrorMessage(){
        this._addErrorMessage(msg('An error occured - see the Assistant log for details', { id: 'quarkus-chappie-error-see-log' }));
    }

    _addErrorMessage(message){
        message = "<span style='color:red'>" + message + "</span>";
        this._addToMessages(message, msg('Assistant', { id: 'quarkus-chappie-assistant' }), 5);
        if(!this._heading)this._heading = msg('Error', { id: 'quarkus-chappie-error' });
    }
    
    _addToMessages(message, user, userColorIndex){
        var item = this._toMessageItem(message, user, userColorIndex);
        this._messages = [
            ...this._messages,
            item
        ];
    }
    
    _removeLastMessage() {
        if (this._messages?.length) {
          this._messages = this._messages.slice(0, -1);
        }
    }
    
    _toMessageItem(message, user, userColorIndex){
        var d = new Date();
        let time = d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
        
        return {
            text: message,
            time: time,
            userName: user,
            userColorIndex: userColorIndex
        };
    }
    
    _scrollToBottom(){
        const last = Array.from(
            this.shadowRoot.querySelectorAll('vaadin-message')
        ).pop();

        if(last){
            last.scrollIntoView({
                behavior: "smooth",
                block: "end"
            });
        }
    }
    
    _stripMethodishKeysStrict(obj) {
        const m = /^[A-Za-z_$][\w$]*\s*\([^()]*\)$/;
        return Object.fromEntries(
            Object.entries(obj).filter(([k]) => !m.test(k))
        );
    }
}
customElements.define('qwc-chappie-chat', QwcChappieChat);