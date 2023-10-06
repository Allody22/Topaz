import React, {useCallback, useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import {Notification, SelectPicker, useToaster,} from 'rsuite';
import '../css/CreatingOrder.css';
import '../css/NewStyles.css';

import 'rsuite/dist/rsuite.css';

import InputField from "../model/InputField";
import {
    getActualPolishingOrders,
    getActualTireOrders,
    getAllWashingOrders,
    getServiceInfo,
    updatePolishingService,
    updateTireService,
    updateWashingService,
} from "../http/orderAPI";
import {observer} from "mobx-react-lite";
import socketStore from "../store/SocketStore";
import {BrowserRouter as Router, useHistory} from "react-router-dom";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import CustomModal from '../model/MyCustomModal';


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
    const [orderName, setOrderName] = useState(null);

    const [prices, setPrices] = useState({
        firstType: 0,
        secondType: 0,
        thirdType: 0
    });

    const [times, setTimes] = useState({
        firstType: 0,
        secondType: 0,
        thirdType: 0
    });


    const toaster = useToaster();

    const [showConfirmation, setShowConfirmation] = useState(false);

    const [response, setResponse] = useState();
    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);


    const [allOrders, setAllOrders] = useState([]);
    const [wheelSizeAndPrice, setWheelSizeAndPrice] = useState([{wheelR: null, price: null}]);
    const [wheelSizeAndTime, setWheelSizeAndTime] = useState([{wheelR: null, time: null}]);


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
                const responseWashing = await getAllWashingOrders();
                const ordersOfWashing = [...responseWashing.mainOrders.map(item => item.replace(/_/g, ' ')),
                    ...responseWashing.additionalOrders.map(item => item.replace(/_/g, ' '))];
                const ordersWithTypesWashing = ordersOfWashing.map((order) => ({name: order, type: "Мойка"}));


                const responsePolishing = await getActualPolishingOrders();
                const responseTire = await getActualTireOrders();

                const ordersOfTireService = responseTire.orders.map(item => item.replace(/_/g, ' '));
                const ordersWithTypesTire = ordersOfTireService.map((order) => ({name: order, type: "Шиномонтаж"}));

                const ordersOfPolishing = responsePolishing.orders.map(item => item.replace(/_/g, ' '));
                const ordersWithTypes = ordersOfPolishing.map((order) => ({name: order, type: "Полировка"}));

                setAllOrders([...ordersWithTypes, ...ordersWithTypesTire, ...ordersWithTypesWashing]);

                setWheelSizeAndPrice([])
            } catch (error) {
                if (error.response) {

                    let messages = [];
                    for (let key in error.response.data) {
                        messages.push(error.response.data[key]);
                    }
                    setErrorResponse(messages.join(''));
                    setErrorFlag(flag => !flag);

                } else {
                    alert("Системная ошибка, попробуйте позже")
                }
            }
        }

        getAllOrders();
    }, []);

    const handleOpenModal = useCallback(() => setShowModal(true), []);
    const handleCloseModal = useCallback(() => setShowModal(false), []);
// и так далее для других функций


    const handleOpenModalB = useCallback(() => setShowModalB(true), []);
    const handleCloseModalB = useCallback(() => setShowModalB(false), []);


    const getItemTypeByName = (name) => {
        const item = allOrders.find(item => item.name === name);
        return item ? item.type : undefined;
    }

    const getPriceByWheelR = (name) => {
        const item = wheelSizeAndPrice.find(item => item.level === name);
        return item ? item.price : "Нет информации про цену";
    }

    const getTimeByWheelR = (name) => {
        const item = wheelSizeAndTime.find(item => item.level === name);
        return item ? item.time : "Нет информации про цену";
    }


    const mapOrderTypeToCode = (orderType) => {
        switch (orderType) {
            case "Мойка":
                return "Wash";
            case "Полировка":
                return "Polishing";
            case "Шиномонтаж":
                return "Tire";
            default:
                return -1;
        }
    };

    function setPricesValues({firstType, secondType, thirdType}) {
        setPrices(prevState => ({
            ...prevState,
            firstType,
            secondType,
            thirdType,
        }));
    }

    function setTimesValues({firstType, secondType, thirdType}) {
        setTimes(prevState => ({
            ...prevState,
            firstType,
            secondType,
            thirdType,
        }));
    }


    useEffect(() => {
        async function getServiceInfoRequest() {
            const enOrderType = mapOrderTypeToCode(getItemTypeByName(orderName));
            if (enOrderType !== -1) {
                try {
                    const responseOfServiceInfo = await getServiceInfo(orderName.replace(/ /g, '_'), enOrderType);

                    if (getItemTypeByName(orderName) === "Мойка") {
                        setPricesValues({
                            firstType: responseOfServiceInfo.priceFirstType,
                            secondType: responseOfServiceInfo.priceSecondType,
                            thirdType: responseOfServiceInfo.priceThirdType,
                        });
                        setTimesValues({
                            firstType: responseOfServiceInfo.timeFirstType,
                            secondType: responseOfServiceInfo.timeSecondType,
                            thirdType: responseOfServiceInfo.timeThirdType,
                        });
                        setWheelSizeAndTime([]);
                        setWheelSizeAndPrice([]);

                    } else if (getItemTypeByName(orderName) === "Шиномонтаж") {
                        const prices = [];
                        const time = [];
                        Object.keys(responseOfServiceInfo).forEach((key) => {
                            if (key.startsWith('price_r_')) {
                                prices.push({
                                    level: key.replace('price_r_', ''),
                                    price: responseOfServiceInfo[key],
                                });
                            } else if (key.startsWith('time_r_')) {
                                time.push({
                                    level: key.replace('time_r_', ''),
                                    time: responseOfServiceInfo[key],
                                });
                            }
                        });
                        setWheelSizeAndPrice(prices);
                        setWheelSizeAndTime(time);
                        setPricesValues({firstType: 0, secondType: 0, thirdType: 0});
                        setTimesValues({firstType: 0, secondType: 0, thirdType: 0});

                    } else if (getItemTypeByName(orderName) === "Полировка") {
                        setPricesValues({
                            firstType: responseOfServiceInfo.priceFirstType,
                            secondType: responseOfServiceInfo.priceSecondType,
                            thirdType: responseOfServiceInfo.priceThirdType,
                        });
                        setTimesValues({
                            firstType: responseOfServiceInfo.timeFirstType,
                            secondType: responseOfServiceInfo.timeSecondType,
                            thirdType: responseOfServiceInfo.timeThirdType,
                        });
                        setWheelSizeAndPrice([]);
                        setWheelSizeAndTime([]);
                    }
                } catch (error) {
                    if (error.response) {
                        let messages = [];
                        for (let key in error.response.data) {
                            messages.push(error.response.data[key]);
                        }
                        setErrorResponse(messages.join(''));
                        setErrorFlag(flag => !flag);

                    } else {
                        alert("Системная ошибка, попробуйте позже")
                    }
                }
            }
        }

        getServiceInfoRequest();
    }, [orderName]);


    const setPriceByWheelRForNewInfo = (event, level) => {
        setWheelSizeAndPrice(prevState => {
            const index = prevState.findIndex(item => item.level === level);
            if (index === -1) {
                return prevState;
            } else {
                const newArray = [...prevState];
                newArray[index].price = Number(event);
                return newArray;
            }
        });
    };

    const setTimeByWheelRForNewInfo = (event, level) => {
        setWheelSizeAndTime(prevState => {
            const index = prevState.findIndex(item => item.level === level);
            if (index === -1) {
                return prevState;
            } else {
                const newArray = [...prevState];
                newArray[index].time = Number(event);
                return newArray;
            }
        });
    };

    const handleOrderChange = (value) => {
        setOrderName(value);
    };

    const errorResponseMessage = (
        <Notification
            type="error"
            header="Ошибка!"
            closable
            style={{border: '1px solid black'}}
        >
            <div style={{width: 320}}>
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
            <div style={{width: 320}}>
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

        if (showConfirmation) {
            try {
                let response;
                const serviceName = orderName.replace(/ /g, '_');

                switch (getItemTypeByName(orderName)) {
                    case "Мойка":
                        response = await updateWashingService(prices.firstType, prices.secondType, prices.thirdType, times.firstType,
                            times.secondType, times.thirdType, serviceName);
                        break;

                    case "Шиномонтаж":
                        const pricesByWheelR = [13, 14, 15, 16, 17, 18, 19, 20, 21, 22].map(size => getPriceByWheelR(String(size)));
                        const timesByWheelR = [13, 14, 15, 16, 17, 18, 19, 20, 21, 22].map(size => getTimeByWheelR(String(size)));
                        response = await updateTireService(...pricesByWheelR, ...timesByWheelR, serviceName);
                        break;

                    case "Полировка":
                        response = await updatePolishingService(prices.firstType, prices.secondType, prices.thirdType, times.firstType,
                            times.secondType, times.thirdType, serviceName);
                        break;

                    default:
                        throw new Error("Unknown service type");
                }

                setResponse(response.message);

            } catch (error) {
                if (error.response) {
                    setErrorResponse(error.response.data.message);
                } else {
                    setErrorResponse("Системная ошибка, проверьте правильность введённой информации и попробуйте еще раз");
                }
                setErrorFlag(flag => !flag);
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
                value={orderName}
                onSelect={handleOrderChange}
            />
            <Form onSubmit={handleSubmit}>
                <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                    Посмотреть цену для различных видов шин (доступно только для заказов шиномонтажа)
                </Button>

                <CustomModal show={showModal} handleClose={handleCloseModal} title="Цены для различных диаметров колёс">
                    {wheelSizeAndPrice.map(item => `Размер шин: R${item.level}`).sort().map(item => (
                        <div key={item}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>{item}</span>
                            <InputField
                                id='priceForR'
                                value={getPriceByWheelR(item.slice(-2))}
                                onChange={(event) => setPriceByWheelRForNewInfo(event, item.slice(-2))}
                            />
                        </div>
                    ))}
                </CustomModal>

                <Button className='full-width' variant='secondary' onClick={handleOpenModalB}>
                    Посмотреть врем выполнения для различных размеров колёс (доступно только для заказов шиномонтажа)
                </Button>

                <CustomModal show={showModalB} handleClose={handleCloseModalB}
                             title="Время для различных диаметров колёс">
                    {wheelSizeAndPrice.map(item => `Размер шин: R${item.level}`).sort().map(item => (
                        <div key={item}
                             style={{
                                 display: 'flex',
                                 alignItems: 'center',
                                 justifyContent: 'space-between',
                                 fontSize: '16px'
                             }}>
                            <span className='text' style={{marginRight: '8px'}}>{item}</span>
                            <InputField
                                id='timeForR'
                                value={getTimeByWheelR(item.slice(-2))}
                                onChange={(event) => setTimeByWheelRForNewInfo(event, item.slice(-2))}
                            />
                        </div>
                    ))}
                </CustomModal>

                <InputField
                    label='Цена за 1 тип кузова'
                    id='priceFirstType'
                    value={prices.firstType}
                    inputStyle={inputStyle}
                    onChange={(value) => setPrices(prev => ({...prev, firstType: value}))}
                />
                <InputField
                    label='Цена за 2 тип кузова'
                    id='priceSecondType'
                    value={prices.secondType}
                    inputStyle={inputStyle}
                    onChange={(value) => setPrices(prev => ({...prev, secondType: value}))}
                />
                <InputField
                    label='Цена за 3 тип кузова'
                    id='priceThirdType'
                    value={prices.thirdType}
                    inputStyle={inputStyle}
                    onChange={(value) => setPrices(prev => ({...prev, thirdType: value}))}
                />
                <InputField
                    label='Примерное время выполнения с 1 типом кузова'
                    id='timeFirstType'
                    value={times.firstType}
                    inputStyle={inputStyle}
                    onChange={(value) => setTimes(prev => ({...prev, firstType: value}))}
                />
                <InputField
                    label='Примерное время выполнения со 2 типом кузова'
                    id='timeSecondType'
                    value={times.secondType}
                    inputStyle={inputStyle}
                    onChange={(value) => setTimes(prev => ({...prev, secondType: value}))}
                />
                <InputField
                    label='Примерное время выполнения с 3 типом кузова'
                    id='timeThirdType'
                    value={times.thirdType}
                    inputStyle={inputStyle}
                    onChange={(value) => setTimes(prev => ({...prev, thirdType: value}))}
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
});

export default ChangeServiceInfo;