import React, {useEffect} from 'react';
import {Container} from "react-bootstrap";

import carWashImage from '../assets/carWashImage.png';
import tireServiceImage from "../assets/tireService.png"
import polishingImage from "../assets/polishingImage.png"
import dataBase from "../assets/dataBase.png"
import updateOrderInfo from "../assets/updateOrderInfo.png"
import updateClientInfo from "../assets/updatingClientInfo.png"
import updateServiceInfo from "../assets/updateServiceInfo.png"
import addNewService from "../assets/addService.png"
import SalePhoto from "../assets/SalePhoto.png"
import userOperations from "../assets/UsersOperations.png"


import {
    CHANGE_SERVICE_INFO,
    CHANGE_USERINFO_ROUTE,
    CHECK_SALES,
    CREATE_NEW_SERVICE,
    CREATE_POLISHING_ORDER_ROUTE,
    CREATE_TIRE_ORDER_ROUTE,
    CREATE_WASHING_ORDER_ROUTE,
    ORDERS_TABLE_ROUTE,
    UPDATE_ORDER_INFO_ROUTE,
    USER_OPERATIONS
} from "../utils/consts";
import {observer} from "mobx-react-lite";
import ActionButton from "../model/ActionButton";
import {BrowserRouter as Router} from "react-router-dom";
import {Notification, toaster} from "rsuite";
import socketStore from "../store/SocketStore";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";

const styles = {
    buttonContainer: {display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'},
    image: {width: '100px', height: '60px', marginLeft: '15px', marginTop: '20px'},
    verySmallImage: {width: '85px', height: '60px', marginLeft: '30px', marginTop: '20px'},
};


const Admin = observer(() => {

    const newOrderMessage = (
        <Router>
            <Notification
                type="info"
                header="Новый заказ!"
                closable
                timeout={null}
                style={{border: '1px solid black'}}
            >
                <div style={{width: 320}}>
                    {socketStore.message && (
                        <>
                            <div style={{textAlign: 'left'}}>
                                <p>Тип заказа: {orderTypeMap[JSON.parse(socketStore.message).orderType]}</p>
                                <p>Время начала
                                    заказа: {format(parseISO(JSON.parse(socketStore.message).startTime), 'dd.MM.yyyy HH:mm:ss')}</p>
                                <p>Время конца
                                    заказа: {format(parseISO(JSON.parse(socketStore.message).endTime), 'dd.MM.yyyy HH:mm:ss')}</p>
                            </div>
                        </>
                    )}
                </div>
            </Notification>
        </Router>
    );

    useEffect(() => {
        if (socketStore.message && !socketStore.isAlreadyShown) {
            toaster.push(newOrderMessage, {placement: "bottomEnd"});
            socketStore.isAlreadyShown = true;
        }
    }, [socketStore.message]);

    return (
        <Container className="d-flex flex-column">
            <ActionButton label="Таблица заказов по дням" route={ORDERS_TABLE_ROUTE} imageSrc={dataBase}
                          style={styles.verySmallImage}/>
            <ActionButton label="Добавление заказа на мойку" route={CREATE_WASHING_ORDER_ROUTE} imageSrc={carWashImage}
                          style={styles.image}/>
            <ActionButton label="Добавление заказа на полировку" route={CREATE_POLISHING_ORDER_ROUTE}
                          imageSrc={polishingImage} style={styles.verySmallImage}/>
            <ActionButton label="Добавление заказа на шиномонтаж" route={CREATE_TIRE_ORDER_ROUTE}
                          imageSrc={tireServiceImage} style={styles.image}/>
            <ActionButton label="Изменить информацию о заказе" route={UPDATE_ORDER_INFO_ROUTE}
                          imageSrc={updateOrderInfo} style={styles.verySmallImage}/>
            <ActionButton label="Страница акций" route={CHECK_SALES} imageSrc={SalePhoto}
                          style={styles.verySmallImage}/>
            <ActionButton label="Изменить информацию о человеке" route={CHANGE_USERINFO_ROUTE}
                          imageSrc={updateClientInfo} style={styles.verySmallImage}/>
            <ActionButton label="Просмотр операций" route={USER_OPERATIONS} imageSrc={userOperations}
                          style={styles.verySmallImage}/>
            <ActionButton label="Изменить информацию об услуге" route={CHANGE_SERVICE_INFO} imageSrc={updateServiceInfo}
                          style={styles.verySmallImage}/>
            <ActionButton label="Добавить новую услугу" route={CREATE_NEW_SERVICE} imageSrc={addNewService}
                          style={styles.verySmallImage}/>
        </Container>
    );
});


export default Admin;