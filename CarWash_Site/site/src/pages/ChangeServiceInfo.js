import React, {useCallback, useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import {Notification, SelectPicker, useToaster,} from 'rsuite';
import '../css/CreatingOrder.css';
import '../css/NewStyles.css';

import 'rsuite/dist/rsuite.css';
import {
    getAllPolishingServicesWithPriceAndTime,
    getAllTireServicesWithPriceAndTime,
    getAllWashingServicesWithPriceAndTime,
    updatePolishingService,
    updateTireService,
    updateWashingService,
} from "../http/orderAPI";
import {observer} from "mobx-react-lite";
import socketStore from "../store/SocketStore";
import {BrowserRouter as Router, useHistory} from "react-router-dom";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import MyCustomModal from '../model/MyCustomModal';
import InputFieldPriceTimeNumber from "../model/InputFieldPriceTimeNumber";


const inputStyle = {
    fontWeight: 'bold', display: 'flex',
    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '5px'
}

const ChangeServiceInfo = observer(() => {
        const [isSubmitting] = useState(false);
        const [submitTime] = useState(0);
        const [showModal, setShowModal] = useState(false);

        const [showModalB, setShowModalB] = useState(false);
        useHistory();

        const toaster = useToaster();

        const [showConfirmation, setShowConfirmation] = useState(false);

        const [response, setResponse] = useState();
        const [errorResponse, setErrorResponse] = useState();
        const [errorFlag, setErrorFlag] = useState(false);

        const [currentService, setCurrentService] = useState([{
            name: null, priceFirstType: null, priceSecondType: null, priceThirdType: null,
            timeFirstType: null, timeSecondType: null, timeThirdType: null, price_r_13: null,
            price_r_14: null, price_r_15: null, price_r_16: null, price_r_17: null, price_r_18: null,
            price_r_19: null, price_r_20: null, price_r_21: null, price_r_22: null, time_r_13: null,
            time_r_14: null, time_r_15: null, time_r_16: null, time_r_17: null, time_r_18: null,
            time_r_19: null, time_r_20: null, time_r_21: null, time_r_22: null, type: null
        }]);

        const [allOrders, setAllOrders] = useState([]);


        const options = allOrders
            .map((item) => ({
                label: item.name,
                value: item.name,
                type: item.type
            }))
            .flat();

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


        useEffect(() => {
            async function getAllOrders() {
                try {

                    //Версия с ценой и временем
                    const tireOrdersResponse = await getAllTireServicesWithPriceAndTime();
                    const filteredTireOrders = tireOrdersResponse.map(item => ({
                        ...item,
                        name: item.name.replace(/_/g, ' '),
                        type: "Шиномонтаж"
                    }));

                    const polishingOrdersResponse = await getAllPolishingServicesWithPriceAndTime();
                    const filteredPolishingOrders = polishingOrdersResponse.map(item => ({
                        ...item,
                        name: item.name.replace(/_/g, ' '),
                        type: "Полировка"
                    }));

                    const washingOrdersResponse = await getAllWashingServicesWithPriceAndTime();
                    const filteredWashingOrders = washingOrdersResponse.map(item => ({
                        ...item,
                        name: item.name.replace(/_/g, ' '),
                        type: "Мойка"
                    }));

                    setAllOrders([...filteredWashingOrders, ...filteredTireOrders, ...filteredPolishingOrders]);

                } catch (error) {
                    if (error.response) {
                        let messages = [];
                        for (let key in error.response.data) {
                            messages.push(error.response.data[key]);
                        }
                        setErrorResponse(messages.join('\n'));
                        setErrorFlag(flag => !flag);
                    } else {
                        setErrorResponse("Системная ошибка, проверьте правильность " +
                            "введённой информации и попробуйте еще раз")
                        setErrorFlag(flag => !flag)
                    }
                }
            }

            getAllOrders();
        }, []);

        const handleOpenModal = useCallback(() => setShowModal(true), []);
        const handleCloseModal = useCallback(() => setShowModal(false), []);


        const handleOpenModalB = useCallback(() => setShowModalB(true), []);
        const handleCloseModalB = useCallback(() => setShowModalB(false), []);


        const getItemTypeByName = (name) => {
            const item = allOrders.find(item => item.name === name);
            return item || undefined;
        }

        const handleOrderChange = (value) => {
            setCurrentService(null)
            const service = getItemTypeByName(value);
            setCurrentService(service)
        };

        const errorResponseMessage = (
            <Notification
                type="error"
                header="Ошибка!"
                closable
                style={{border: '1px solid black'}}
            >
                <div style={{width: 320, whiteSpace: "pre-line"}}>
                    {errorResponse}
                </div>
            </Notification>
        );

        useEffect(() => {
            if (errorResponse) {
                toaster.push(errorResponseMessage, {placement: "bottomEnd"});
            }
        }, [errorFlag]);

        const message = (
            <Notification
                type="success"
                header="Успешно!"
                closable
                style={{border: '1px solid black'}}
            >
                <div style={{width: 320, whiteSpace: "pre-line"}}>
                    <p>{response}</p>
                    <p>Вы успешно обновили информацию в базе данных.</p>
                </div>
            </Notification>
        );

        useEffect(() => {
            if (response) {
                toaster.push(message, {placement: "bottomEnd"});
            }
        }, [response]);

        const handleSubmit = async (event) => {
            event.preventDefault();
            if (!currentService.name || currentService.name === "") {
                setErrorResponse("Обязательно укажите название услуги")
                setErrorFlag(flag => !flag)
                return;
            }
            const serviceName = currentService.name.replace(/ /g, '_');
            if (showConfirmation) {
                try {
                    let response;
                    switch (currentService.type) {
                        case "Мойка":
                            response = await updateWashingService(currentService.priceFirstType, currentService.priceSecondType,
                                currentService.priceThirdType, currentService.timeFirstType,
                                currentService.timeSecondType, currentService.timeThirdType, serviceName);
                            break;

                        case "Шиномонтаж":
                            response = await updateTireService(currentService.price_r_13, currentService.price_r_14, currentService.price_r_15,
                                currentService.price_r_16, currentService.price_r_17, currentService.price_r_18, currentService.price_r_19,
                                currentService.price_r_20, currentService.price_r_21, currentService.price_r_22, currentService.time_r_13,
                                currentService.time_r_14, currentService.time_r_15, currentService.time_r_16, currentService.time_r_17,
                                currentService.time_r_18, currentService.time_r_19, currentService.time_r_20, currentService.time_r_21, currentService.time_r_22,
                                serviceName);
                            break;

                        case "Полировка":
                            response = await updatePolishingService(currentService.priceFirstType, currentService.priceSecondType,
                                currentService.priceThirdType, currentService.timeFirstType,
                                currentService.timeSecondType, currentService.timeThirdType, serviceName);
                            break;
                    }

                    setResponse(response.message);

                } catch (error) {
                    if (error.response) {
                        let messages = [];
                        for (let key in error.response.data) {
                            messages.push(error.response.data[key]);
                        }
                        setErrorResponse(messages.join('\n'));
                        setErrorFlag(flag => !flag);

                    } else {
                        setErrorResponse("Системная ошибка, проверьте правильность " +
                            "введённой информации и попробуйте еще раз")
                        setErrorFlag(flag => !flag)
                    }
                }
                setShowConfirmation(false);
            } else {
                setShowConfirmation(true);
            }
        };

        return (
            <>
                <p className="input-style-modified">Страница изменения информации об услуге</p>
                <p className="small-input-style">Цена услуг и время их выполнения на сайте и в приложении берётся из базы
                    данных,
                    если эту информацию необходимо обновить, то вы можете это сделать на этой странице</p>

                <p style={inputStyle}>Выберите услугу, информацию о которой хотите поменять</p>
                <SelectPicker
                    data={options}
                    groupBy="type"
                    style={{
                        width: '500px', justifyContent: 'center',
                        margin: '25px auto 0', WebkitTextFillColor: "#000000",
                        alignItems: 'center', display: 'flex',
                    }}
                    value={currentService.name}
                    onSelect={handleOrderChange}
                />
                <Form onSubmit={handleSubmit}>
                    <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                        Посмотреть цену для различных видов шин (доступно только для заказов шиномонтажа)
                    </Button>

                    <MyCustomModal show={showModal} handleClose={handleCloseModal} title="Цены для разных шин">
                        <div key={"ЦЕНА R13"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R13</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR13'
                                value={currentService.price_r_13}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_13: value}))}
                            />
                        </div>
                        <div key={"ЦЕНА R14"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R14</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR14'
                                value={currentService.price_r_14}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_14: value}))}
                            />
                        </div>

                        <div key={"ЦЕНА R15"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R15</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR15'
                                value={currentService.price_r_15}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_15: value}))}
                            />
                        </div>

                        <div key={"ЦЕНА R16"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R16</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR16'
                                value={currentService.price_r_16}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_16: value}))}
                            />
                        </div>

                        <div key={"ЦЕНА R17"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R17</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR17'
                                value={currentService.price_r_17}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_17: value}))}
                            />
                        </div>


                        <div key={"ЦЕНА R18"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R18</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR18'
                                value={currentService.price_r_18}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_18: value}))}
                            />
                        </div>

                        <div key={"ЦЕНА R19"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R19</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR19'
                                value={currentService.price_r_19}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_19: value}))}
                            />
                        </div>

                        <div key={"ЦЕНА R20"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R20</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR20'
                                value={currentService.price_r_20}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_20: value}))}
                            />
                        </div>

                        <div key={"ЦЕНА R21"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R21</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR21'
                                value={currentService.price_r_21}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_21: value}))}
                            />
                        </div>


                        <div key={"ЦЕНА R22"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>ЦЕНА ЗА R22</span>
                            <InputFieldPriceTimeNumber
                                id='priceForR22'
                                value={currentService.price_r_22}
                                onChange={(value) => setCurrentService(prev => ({...prev, price_r_22: value}))}
                            />
                        </div>
                    </MyCustomModal>

                    <Button className='full-width' variant='secondary' onClick={handleOpenModalB}>
                        Посмотреть врем выполнения для различных размеров колёс (доступно только для заказов шиномонтажа)
                    </Button>

                    <MyCustomModal show={showModalB} handleClose={handleCloseModalB} title="Время для разных шин">
                        <div key={"Время R13"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R13</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR13'
                                value={currentService.time_r_13}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_13: value}))}
                            />
                        </div>
                        <div key={"Время R14"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R14</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR14'
                                value={currentService.time_r_14}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_14: value}))}
                            />
                        </div>

                        <div key={"Время R15"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R15</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR15'
                                value={currentService.time_r_15}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_15: value}))}
                            />
                        </div>

                        <div key={"Время R16"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R16</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR16'
                                value={currentService.time_r_16}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_16: value}))}
                            />
                        </div>

                        <div key={"Время R17"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R17</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR17'
                                value={currentService.time_r_17}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_17: value}))}
                            />
                        </div>


                        <div key={"Время R18"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R18</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR18'
                                value={currentService.time_r_18}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_18: value}))}
                            />
                        </div>

                        <div key={"Время R19"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R19</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR19'
                                value={currentService.time_r_19}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_19: value}))}
                            />
                        </div>

                        <div key={"Время R20"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R20</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR20'
                                value={currentService.time_r_20}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_20: value}))}
                            />
                        </div>

                        <div key={"Время R21"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R21</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR21'
                                value={currentService.time_r_21}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_21: value}))}
                            />
                        </div>


                        <div key={"Время R22"}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>Время ЗА R22</span>
                            <InputFieldPriceTimeNumber
                                id='timeForR22'
                                value={currentService.time_r_22}
                                onChange={(value) => setCurrentService(prev => ({...prev, time_r_22: value}))}
                            />
                        </div>
                    </MyCustomModal>

                    <InputFieldPriceTimeNumber
                        label='Цена за 1 тип кузова'
                        id='priceFirstType'
                        value={currentService.priceFirstType}
                        inputStyle={inputStyle}
                        onChange={(value) => setCurrentService(prev => ({...prev, priceFirstType: value}))}
                    />
                    <InputFieldPriceTimeNumber
                        label='Цена за 2 тип кузова'
                        id='priceSecondType'
                        value={currentService.priceSecondType}
                        inputStyle={inputStyle}
                        onChange={(value) => setCurrentService(prev => ({...prev, priceSecondType: value}))}
                    />
                    <InputFieldPriceTimeNumber
                        label='Цена за 3 тип кузова'
                        id='priceThirdType'
                        value={currentService.priceThirdType}
                        inputStyle={inputStyle}
                        onChange={(value) => setCurrentService(prev => ({...prev, priceThirdType: value}))}
                    />
                    <InputFieldPriceTimeNumber
                        label='Примерное время выполнения с 1 типом кузова'
                        id='timeFirstType'
                        value={currentService.timeFirstType}
                        inputStyle={inputStyle}
                        onChange={(value) => setCurrentService(prev => ({...prev, timeFirstType: value}))}
                    />
                    <InputFieldPriceTimeNumber
                        label='Примерное время выполнения со 2 типом кузова'
                        id='timeSecondType'
                        value={currentService.timeSecondType}
                        inputStyle={inputStyle}
                        onChange={(value) => setCurrentService(prev => ({...prev, timeSecondType: value}))}
                    />
                    <InputFieldPriceTimeNumber
                        label='Примерное время выполнения с 3 типом кузова'
                        id='timeThirdType'
                        value={currentService.timeThirdType}
                        inputStyle={inputStyle}
                        onChange={(value) => setCurrentService(prev => ({...prev, timeThirdType: value}))}
                    />

                    {showConfirmation && (
                        <div className='confirmation-container'>
                            <div className='confirmation-message'>
                                <p style={inputStyle}>Вы уверены, что хотите отправить запрос?</p>
                                <p>Это изменит информацию об этом заказе ВО ВСЕЙ базе данных для ВСЕХ</p>
                                <div className='confirmation-buttons'>
                                    <Button onClick={() => setShowConfirmation(false)}
                                            style={{marginRight: '10px', marginTop: '10px'}}>
                                        Отменить
                                    </Button>
                                    <Button variant='primary' style={{marginLeft: '10px', marginTop: '10px'}} type='submit'
                                            onSubmit={handleSubmit}>
                                        Подтвердить
                                    </Button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className='submit-container'>
                        <Button
                            className='btn-submit'
                            variant='primary'
                            type='submit'
                            disabled={isSubmitting || Date.now() < submitTime + 4000}
                            style={{marginBottom: '20px', marginTop: '20px'}}
                        >
                            {isSubmitting ? 'Обработка запроса...' : 'Изменить информацию'}
                        </Button>
                    </div>
                </Form>
            </>
        );
    })
;

export default ChangeServiceInfo;