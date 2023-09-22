import React, {useEffect} from 'react';
import {Button, Container} from "react-bootstrap";
import Image from 'react-bootstrap/Image';

import carWashImage from '../assets/carWashImage.png';
import tireServiceImage from "../assets/tireService.png"
import polishingImage from "../assets/polishingImage.png"
import dataBase from "../assets/dataBase.png"
import updateOrderInfo from "../assets/updateOrderInfo.png"
import updateClientInfo from "../assets/updatingClientInfo.png"
import updateServiceInfo from "../assets/updateServiceInfo.png"
import addNewService from "../assets/addService.png"
import SalePhoto from "../assets/SalePhoto.png"


import {
    CHANGE_SERVICE_INFO,
    CHANGE_USERINFO_ROUTE, CHECK_SALES, CREATE_NEW_SERVICE,
    CREATE_POLISHING_ORDER_ROUTE, CREATE_TIRE_ORDER_ROUTE,
    CREATE_WASHING_ORDER_ROUTE, ORDERS_TABLE_ROUTE, UPDATE_ORDER_INFO_ROUTE
} from "../utils/consts";
import {BrowserRouter as Router, useHistory} from "react-router-dom";
import socketStore from "../store/SocketStore";
import {observer} from "mobx-react-lite";
import {Notification, toaster} from "rsuite";
import {format, parseISO} from "date-fns";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {action} from "mobx";

const imageStyle = {width: '100px', height: '60px', marginLeft: '15px', marginTop: '20px'};
const verySmallImageStyle = {width: '85px', height: '60px', marginLeft: '30px', marginTop: '20px'}

const Admin = observer(() => {
    const history = useHistory()

    const markAsShown = action(() => {
        socketStore.isAlreadyShown = true;
    });

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
                                <p>Время начала заказа: {format(parseISO(JSON.parse(socketStore.message).startTime), 'dd.MM.yyyy HH:mm:ss')}</p>
                                <p>Время конца заказа: {format(parseISO(JSON.parse(socketStore.message).endTime), 'dd.MM.yyyy HH:mm:ss')}</p>
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
            markAsShown();
        }
    }, [socketStore.message]);


    return (
        <Container className="d-flex flex-column">
            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'}}>
                <Button variant="outline-dark"
                        className="mt-4 p-2 flex-grow-1"
                        onClick={() => history.push(ORDERS_TABLE_ROUTE)}>
                    Таблица заказов по дням
                </Button>
                <Image src={dataBase} fluid
                       style={verySmallImageStyle}/>
            </div>
            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'}}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(CREATE_WASHING_ORDER_ROUTE)}
                    style={{marginTop: '10px'}}
                >
                    Добавление заказа на мойку
                </Button>
                <Image src={carWashImage} fluid style={imageStyle}/>
            </div>

            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'}}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(CREATE_POLISHING_ORDER_ROUTE)}
                    style={{marginTop: '10px'}}
                >
                    Добавление заказа на полировку
                </Button>
                <Image src={polishingImage} fluid
                       style={verySmallImageStyle}/>

            </div>

            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'}}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(CREATE_TIRE_ORDER_ROUTE)}
                    style={{marginTop: '10px'}}
                >
                    Добавление заказа на шиномонтаж
                </Button>
                <Image src={tireServiceImage} fluid style={imageStyle}/>
            </div>

            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'}}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(UPDATE_ORDER_INFO_ROUTE)}
                    style={{marginTop: '10px'}}
                >
                    Изменить информацию о заказе
                </Button>
                <Image src={updateOrderInfo} fluid style={verySmallImageStyle}/>
            </div>


            <div style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                marginTop: '10px'
            }}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(CHECK_SALES)}
                    style={{marginTop: '10px'}}
                >
                    Страница акций
                </Button>
                <Image src={SalePhoto} fluid style={verySmallImageStyle}/>
            </div>

            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'}}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(CHANGE_USERINFO_ROUTE)}
                    style={{marginTop: '10px'}}
                >
                    Изменить информацию о человеке
                </Button>
                <Image src={updateClientInfo} fluid style={verySmallImageStyle}/>

            </div>


            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '10px'}}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(CHANGE_SERVICE_INFO)}
                    style={{marginTop: '10px'}}
                >
                    Изменить информацию об услуге
                </Button>
                <Image src={updateServiceInfo} fluid style={verySmallImageStyle}/>
            </div>

            <div style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                marginTop: '10px', marginBottom: '20px'
            }}>
                <Button
                    variant={"outline-dark"}
                    className="mt-4 p-2 flex-grow-1"
                    onClick={() => history.push(CREATE_NEW_SERVICE)}
                    style={{marginTop: '10px'}}
                >
                    Добавить новую услугу
                </Button>
                <Image src={addNewService} fluid style={verySmallImageStyle}/>
            </div>


        </Container>
    );
});

export default Admin;
