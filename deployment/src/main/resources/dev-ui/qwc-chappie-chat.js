import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import { assistantState } from 'assistant-state';
import { observeState } from 'lit-element-state';
import 'qwc-no-data';
import '@vaadin/message-input';
import '@vaadin/message-list';
import '@vaadin/progress-bar';
import '@vaadin/menu-bar';

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
            padding-bottom: 30px;
        }
        
        .inputExistingList {
            overflow-y: scroll;
            max-height: 100%;
        }
        
        .headerNew {
            text-align: center;
            padding-right: 76px;
            color: var(--quarkus-blue);
        }
    `;
    
    static properties = {
        _messages: { state: true },
        _inputIsBlocked: { state: true }
    };

    constructor() { 
        super();
        this._messages = [];
        this._inputIsBlocked = false;
        this._menuItems = [];
    }

    connectedCallback() {
        super.connectedCallback();
        this._menuItems = [
            {
                key: 'new-chat',
                component: this._createMenuItem('font-awesome-regular:pen-to-square', 'New chat')
            }
          ];
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
                            <vaadin-menu-bar theme="small icon end-aligned" .items="${this._menuItems}" @item-selected="${this._menuItemSelected}"></vaadin-menu-bar>
                            <div class="inputExistingList">
                                <vaadin-message-list .items="${this._messages}" markdown></vaadin-message-list>
                            </div>
                        </div>
                        ${this._renderInputOrProgressBar()}
                    </div>
            `;
    }
    
    _renderInputOrProgressBar(){
        if(this._inputIsBlocked){
            return html`<div><vaadin-progress-bar indeterminate></vaadin-progress-bar></div>`;
        }else{
            return html`<vaadin-message-input ?disabled=${this._inputIsBlocked} class="messageInput" @submit="${this._handleSubmit}"></vaadin-message-input>`;
        }
    }
    
    _renderNewChat(){
        return html`<div class="inputNew">
                        <h1 class="headerNew">Welcome to the Assistant Chat</h2>
                        <div style="width:100%;"><vaadin-message-input class="messageInput" @submit="${this._handleSubmit}"></vaadin-message-input></div>
                    </div>
            `;
    }
    
    _renderUnconfigured(){
        return html`<qwc-no-data message="Assistant is not configured.">
                    ${this._renderConfigureNowButton()}
                </qwc-no-data>`;
    }
    
    _menuItemSelected(e){
        const item = e.detail.value;
        switch (item.key) {
            case 'new-chat':
                this._startNewChat();
                break;
        }
    }
    
    _startNewChat(){
        document.body.style.cursor = 'progress'; 
        this.jsonRpc.clearMemory().then(jsonRpcResponse => {
            this._messages = [];
            document.body.style.cursor = 'default'; 
        });
    }
    
    _handleSubmit(event) {
        document.body.style.cursor = 'progress'; 
        let m = event.detail.value;
        this._addUserMessage(m);
        this._addAssistantMessage("Thinking ...");
        this._inputIsBlocked = true;
        this._scrollToBottom();
        this.jsonRpc.chat({message:m}).then(jsonRpcResponse => {
            document.body.style.cursor = 'default';
            this._removeLastMessage();
            this._addAssistantMessage(jsonRpcResponse.result.answer);
            this._inputIsBlocked = false;
        });
    }
    
    _renderConfigureNowButton(){
        return html`<vaadin-button theme="tertiary" @click=${this._configureNow}>
                        <vaadin-icon icon="font-awesome-solid:gears"></vaadin-icon>
                        Configure now
                    </vaadin-button>`;
    }
    
    _configureNow(){
        window.dispatchEvent(new CustomEvent('open-settings-dialog',{detail: {selectedTab : "quarkus-chappie/assistant-tab"}}));
    }
    
    _addUserMessage(message){
        this._addToMessages(message, "You", 6);
    }
    
    _addAssistantMessage(message){
        this._addToMessages(message, "Assistant", 5);
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
    
    _createMenuItem(iconName, text, isChild = false) {
        const item = document.createElement('vaadin-menu-bar-item');
        const icon = document.createElement('vaadin-icon');

        if (isChild) {
            icon.style.width = 'var(--lumo-icon-size-s)';
            icon.style.height = 'var(--lumo-icon-size-s)';
            icon.style.marginRight = 'var(--lumo-space-l)';
        }

        icon.setAttribute('icon', `${iconName}`);
        item.appendChild(icon);
        if (text) {
            item.appendChild(document.createTextNode(text));
        }
        return item;
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
}
customElements.define('qwc-chappie-chat', QwcChappieChat);
