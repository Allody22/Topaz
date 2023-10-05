import SockJS from "sockjs-client";
import {Stomp} from "@stomp/stompjs";
import {makeAutoObservable} from "mobx";

class SocketStore {
    socket = null;
    stompClient = null;
    isConnected = false;
    message = null;
    isAlreadyShown = false;

    constructor() {
        makeAutoObservable(this);
    }

    connectAndSubscribe() {
        if (!this.isConnected) {
            const baseUrl = process.env.REACT_APP_API_URL;
            const socket = new SockJS(baseUrl + 'websockets');
            this.stompClient = Stomp.over(socket);

            this.stompClient.connect({}, () => {
                this.isConnected = true;

                this.stompClient.subscribe('/notifications/newOrder', (message) => {
                    const messageFromServer = message.body;
                    this.isAlreadyShown = false;
                    this.message = messageFromServer;
                });

            });
        }
    }

    disconnect() {
        if (this.isConnected) {
            this.stompClient.disconnect();
            this.isConnected = false;
        }
    }

}

const socketStore = new SocketStore();
export default socketStore;