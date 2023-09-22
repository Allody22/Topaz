import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import {action, makeAutoObservable} from "mobx";
import {getOrdersCreatedInOneDay} from "../http/orderAPI";

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

            this.stompClient.connect({}, (frame) => {
                this.isConnected = true;

                this.stompClient.subscribe('/notifications/newOrder', (message) => {
                    const messageFromServer = message.body;
                    this.isAlreadyShown = false;
                    this.message = messageFromServer;
                });

                // Вызовите этот метод после установления соединения
                this.getOrdersCreatedAtThisDay();
            });
        }
    }

    getOrdersCreatedAtThisDay = async () => {
        const currentDate = new Date();

        const startTime = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate());
        const endTime = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate() + 1);

        endTime.setMilliseconds(endTime.getMilliseconds() - 1);
        try {
            const response = await getOrdersCreatedInOneDay(startTime.toISOString(), endTime.toISOString());
            for (let i = 0; i < response.length; i++) {
                await new Promise(resolve => setTimeout(resolve, 100));
                action(() => {
                    this.isAlreadyShown = true;
                    this.message = JSON.stringify(response[i]);
                })();
            }
        } catch (error) {
            if (error.response) {
                console.log(error)
            }
        }
    };

    disconnect() {
        if (this.isConnected) {
            this.stompClient.disconnect();
            this.isConnected = false;
        }
    }

}

const socketStore = new SocketStore();
export default socketStore;