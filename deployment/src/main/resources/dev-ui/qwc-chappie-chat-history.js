import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';
import { observeState } from 'lit-element-state';
import '@vaadin/progress-bar';
import '@qomponent/qui-badge';

/**
 * This component shows the chat history
 */
export class QwcChappieChatHistory extends observeState(QwcHotReloadElement) { 
    
    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            padding: 5px;
        }
        .chatList {
            display: flex;
            flex-direction: column;
            gap: 5px;
        }
        .chat {
            display: flex;
            flex-direction: column;
            cursor: pointer;
            padding: 4px;
        }
        .chat:hover {
            background: var(--lumo-contrast-25pct);
        }
        .title {
            display: flex;
            justify-content: space-between;
        }
        .date {
            font-size: x-small;
            color: var(--lumo-secondary-text-color);
        }
        .nothing {
        }
    `;
    
    static properties = {
        namespace: {type: String},
        limit: {type: Number},
        _chats: { state: true }
    };

    constructor() { 
        super();
        this._chats = [];
        this.namespace = null;
        this.limit = -1;
    }

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc = new JsonRpc(this.namespace);
        this._getChatHistory();
    }
    
    disconnectedCallback() {
        super.disconnectedCallback();
    }
    
    hotReload(){
        document.body.style.cursor = 'default';
        this._getChatHistory();
    }
    
    render() { 
        if(this._chats && this._chats.length > 0){
            return html`<div class="chatList">
                            ${this._chats.map((chat) =>
                                html`<div class="chat" @click="${() => this._selectChat(chat.memoryId)}">
                                    <div class="title">${chat.niceName} <qui-badge level="contrast" pill><span>${chat.messageCount}</span></qui-badge></div>
                                    <div class="date">${this._getNiceDate(chat.lastActivity)}</div>
                                </div>`
                            )}
                        </div>`;
            // TODO: Add more button
            
        }else{
            return html`<div class="nothing">Nothing yet</div>`;
        }
    }
    
    _selectChat(memoryId){
        this.dispatchEvent(new CustomEvent('selectChat', {
            detail: memoryId,
            bubbles: true,
            composed: true
        }));
    }
    
    _getChatHistory(){
        document.body.style.cursor = 'progress'; 
        this.jsonRpc.getChats().then(jsonRpcResponse => {
            this._chats = jsonRpcResponse.result;
            this._reduceToLimit();
            document.body.style.cursor = 'default'; 
        });
    }
    
    _getNiceDate(lastActivity){
        const normalized = lastActivity.replace(/\.(\d{3})\d*Z$/, '.$1Z');
        const d = new Date(normalized);
        const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
        const nice = new Intl.DateTimeFormat(undefined, {
          dateStyle: 'medium',
          timeStyle: 'short',
          timeZone: tz
        }).format(d);
        return nice;
    }
    
    _reduceToLimit(){
        if(this.limit>=0){
            this._chats = this._chats.slice(0, this.limit);
        }
    }
}
customElements.define('qwc-chappie-chat-history', QwcChappieChatHistory);
