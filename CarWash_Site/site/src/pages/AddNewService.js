import React, {useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import {InputPicker, Notification, TagPicker, useToaster,} from 'rsuite';
import '../css/CreatingOrder.css';
import '../css/NewStyles.css';

import 'rsuite/dist/rsuite.css';

import Modal from "react-bootstrap/Modal";

import InputField from "../model/InputField";
import {createNewService} from "../http/orderAPI";
import orderTypeMapFromRussian from "../model/map/OrderTypeMapFromRussian";
import connectedServiceMap from "../model/map/ConnectedServiceMap";
import includedServiceMap from "../model/map/IncludedServiceMap";
import {observer} from "mobx-react-lite";
import socketStore from "../store/SocketStore";
import {BrowserRouter as Router} from "react-router-dom";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";

const smallInputStyle = {
    display: 'flex', justifyContent: 'center', alignItems: 'center', marginTop: '5px'
}
const inputStyle = {
    fontWeight: 'bold', display: 'flex',
    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '5px'
}

const serviceTypesArray = [
    'Шиномонтаж',
    'Мойка',
    'Полировка'
].map(item => ({label: item, value: item}));

const washingTypesArray = [
    'VIP',
    'ELITE',
    'Стандарт',
    'Эконом'
].map(item => ({label: item, value: item}));

const rolesTypesArray = [
    'Главная',
    'Дополнительная'
].map(item => ({label: item, value: item}));

const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};

const stylesUnderButton = {
    width: 500, display: 'block',
    marginBottom: 35, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};


const AddNewService = observer(() => {
    const [isSubmitting] = useState(false);
    const [submitTime] = useState(0);
    const [showModal, setShowModal] = useState(false);

    const [showModalB, setShowModalB] = useState(false);

    const [serviceType, setServiceType] = useState('');
    const [washingTypeIncluded, setWashingTypeIncluded] = useState([]);
    const [washingTypeConnected, setWashingTypeConnected] = useState([]);
    const [washingTypeIncludedEnglish, setWashingTypeIncludedEnglish] = useState([]);
    const [washingTypeConnectedEnglish, setWashingTypeConnectedEnglish] = useState([]);
    const [role, setRole] = useState('');
    const [orderName, setOrderName] = useState(null);


    const [priceFirstType, setPriceFirstType] = useState(0);
    const [priceSecondType, setPriceSecondType] = useState(0);
    const [priceThirdType, setPriceThirdType] = useState(0);

    const [timeFirstType, setTimeFirstType] = useState(0);
    const [timeSecondType, setTimeSecondType] = useState(0);
    const [timeThirdType, setTimeThirdType] = useState(0);

    const toaster = useToaster();

    const [showConfirmation, setShowConfirmation] = useState(false);

    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);
    const [successResponse, setSuccessResponse] = useState();


    const [wheelSizeAndPrice, setWheelSizeAndPrice] = useState([{wheelR: null, price: null}]);
    const [wheelSizeAndTime, setWheelSizeAndTime] = useState([{wheelR: null, time: null}]);


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
                setWheelSizeAndPrice([])
                const prices = [];
                const time = [];

                for (let i = 13; i <= 22; i++) {
                    prices.push({
                        level: `${i}`,
                        price: 0,
                    });
                    time.push({
                        level: `${i}`,
                        time: 0,
                    });
                }

                setWheelSizeAndPrice(prices);
                setWheelSizeAndTime(time);

                setPriceFirstType(0)
                setPriceSecondType(0)
                setPriceThirdType(0)
                setTimeFirstType(0)
                setTimeSecondType(0)
                setTimeThirdType(0)
            } catch (error) {
                if (error.response) {
                    alert(error.response.data.message)
                } else {
                    alert("Системная ошибка, попробуйте позже")
                }
            }
        }

        getAllOrders();
    }, []);

    const handleOpenModal = () => setShowModal(true);
    const handleCloseModal = () => setShowModal(false);

    const handleOpenModalB = () => setShowModalB(true);
    const handleCloseModalB = () => setShowModalB(false);


    const getPriceByWheelR = (name) => {
        const item = wheelSizeAndPrice.find(item => item.level === name);
        return item ? item.price : "Нет информации про цену";
    }

    const getTimeByWheelR = (name) => {
        const item = wheelSizeAndTime.find(item => item.level === name);
        return item ? item.time : "Нет информации про цену";
    }


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

    const successMessage = (
        <Notification
            type="success"
            header="Успешно!"
            closable
            style={{border: '1px solid black'}}
        >
            <div style={{width: 320}}>
                <p>{successResponse}</p>
            </div>
        </Notification>
    );

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

    useEffect(() => {
        if (successResponse) {
            toaster.push(successMessage, {placement: "bottomEnd"});
        }
    }, [successResponse]);

    useEffect(() => {
        setWashingTypeIncludedEnglish(washingTypeIncluded.map(item => includedServiceMap[item]))
    }, [washingTypeIncluded]);

    useEffect(() => {
        setWashingTypeConnectedEnglish(washingTypeIncluded.map(item => connectedServiceMap[item]))
    }, [washingTypeConnected]);
    const handleSubmit = async (event) => {
        event.preventDefault();
        if (showConfirmation) {
            try {
                let response;
                response = await createNewService(orderTypeMapFromRussian[serviceType], orderName.replaceAll(' ', '_'), priceFirstType,
                    priceSecondType, priceThirdType, timeFirstType, timeSecondType, timeThirdType,
                    getPriceByWheelR('13'), getPriceByWheelR('14'), getPriceByWheelR('15'),
                    getPriceByWheelR('16'), getPriceByWheelR('17'), getPriceByWheelR('18'),
                    getPriceByWheelR('19'), getPriceByWheelR('20'), getPriceByWheelR('21'),
                    getPriceByWheelR('22'), getTimeByWheelR('13'), getTimeByWheelR('14'),
                    getTimeByWheelR('15'), getTimeByWheelR('16'), getTimeByWheelR('17'),
                    getTimeByWheelR('18'), getTimeByWheelR('19'), getTimeByWheelR('20'),
                    getTimeByWheelR('21'), getTimeByWheelR('22'),
                    orderTypeMapFromRussian[role], [...washingTypeIncludedEnglish, ...washingTypeConnectedEnglish])
                setSuccessResponse(null)
                setSuccessResponse("В базе данных появилась услуга '" + (response.name).replaceAll('_', ' ') + "'")
            } catch
                (error) {
                if (error.response) {
                    setErrorResponse(error.response.data.message)
                    setErrorFlag(flag => !flag)
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

    const handleSetConnected = (item) => {
        setWashingTypeConnected(prevSelectedRoles =>
            prevSelectedRoles.filter(role => role !== item)
        );
    };

    const handleSetIncluded = (item) => {
        setWashingTypeIncluded(prevSelectedRoles =>
            prevSelectedRoles.filter(role => role !== item)
        );
    };

    return (
        <>
            <p style={{...inputStyle, marginTop: '15px'}}>Страница добавления новой услуги</p>
            <p style={smallInputStyle}>Вся информация о новой услуге появится в базе данных</p>

            <p style={{
                fontWeight: 'bold', display: 'flex',
                fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '15px'
            }}>Выберите тип услуги</p>
            <InputPicker
                data={serviceTypesArray}
                value={serviceType}
                onChange={setServiceType}
                style={{...styles, WebkitTextFillColor: "#000000"}}
                menuStyle={{fontSize: "17px"}}
            />

            <InputField
                label='Название услуги'
                id='orderName'
                value={orderName}
                inputStyle={inputStyle}
                onChange={setOrderName}
            />

            <Form onSubmit={handleSubmit}>
                <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                    Написать цену для различных видов шин (необходимо только для шиномонтажа)
                </Button>
                <Modal show={showModal}
                       onHide={handleCloseModal}
                       dialogClassName="custom-modal-dialog">
                    <Modal.Header closeButton>
                        <Modal.Title>Цены для различных диаметров колёс</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
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
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant='secondary' onClick={handleCloseModal}>
                            Закрыть
                        </Button>
                    </Modal.Footer>
                </Modal>
                <Button className='full-width' variant='secondary' onClick={handleOpenModalB}>
                    Написать врем выполнения для различных размеров колёс (необходимо только для шиномонтажа)
                </Button>
                <Modal show={showModalB}
                       onHide={handleCloseModalB}
                       dialogClassName="custom-modal-dialog">
                    <Modal.Header closeButton>
                        <Modal.Title>Время для различных диаметров колёс</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {wheelSizeAndTime.map(item => `Размер шин: R${item.level}`).sort().map(item => (
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
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant='secondary' onClick={handleCloseModalB}>
                            Закрыть
                        </Button>
                    </Modal.Footer>
                </Modal>
                <InputField
                    label='Цена за 1 тип кузова'
                    id='priceFirstType'
                    value={priceFirstType}
                    inputStyle={inputStyle}
                    onChange={setPriceFirstType}
                />
                <InputField
                    label='Цена за 2 тип кузова'
                    id='priceSecondType'
                    value={priceSecondType}
                    inputStyle={inputStyle}
                    onChange={setPriceSecondType}
                />
                <InputField
                    label='Цена за 3 тип кузова'
                    id='priceFirstType'
                    value={priceThirdType}
                    inputStyle={inputStyle}
                    onChange={setPriceThirdType}
                />
                <InputField
                    label='Примерное время выполнения с 1 типом кузова'
                    id='timeFirstType'
                    value={timeFirstType}
                    inputStyle={inputStyle}
                    onChange={setTimeFirstType}
                />
                <InputField
                    label='Примерное время выполнения со 2 типом кузова'
                    id='timeSecondType'
                    value={timeSecondType}
                    inputStyle={inputStyle}
                    onChange={setTimeSecondType}
                />
                <InputField
                    label='Примерное время выполнения с 3 типом кузова'
                    id='timeThirdType'
                    value={timeThirdType}
                    inputStyle={inputStyle}
                    onChange={setTimeThirdType}
                />

                <p style={{
                    fontWeight: 'bold', display: 'flex',
                    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '15px'
                }}>Выберите услугу, в которую она включена</p>
                <p style={smallInputStyle}>Например "Мойка кузова 2 фазы без протирки" включена в Стандарт</p>

                <TagPicker data={washingTypesArray}
                           block
                           onChange={value => setWashingTypeIncluded(value)}
                           onClose={handleSetIncluded}
                           style={{
                               width: '500px',
                               display: 'block',
                               marginBottom: 10,
                               marginLeft: 'auto',
                               marginRight: 'auto',
                               marginTop: 10,
                               WebkitTextFillColor: "#000000"
                           }}
                />

                <p style={{
                    fontWeight: 'bold', display: 'flex',
                    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '15px'
                }}>Выберите услугу, к которой она идёт как дополнительная</p>
                <p style={smallInputStyle}>Например "Чернение шин 4" можно взять вместе со Стандартной мойкой</p>

                <TagPicker data={washingTypesArray}
                           block
                           onChange={value => setWashingTypeConnected(value)}
                           onClose={handleSetConnected}
                           style={{
                               width: '500px',
                               display: 'block',
                               marginBottom: 10,
                               marginLeft: 'auto',
                               marginRight: 'auto',
                               marginTop: 10,
                               WebkitTextFillColor: "#000000"
                           }}
                />

                <p style={{
                    fontWeight: 'bold', display: 'flex',
                    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '10px'
                }}>Выберите роль услуги</p>
                <p style={smallInputStyle}>Например "Турбо сушка кузова" имеют роль "дополнительная"</p>

                <InputPicker
                    data={rolesTypesArray}
                    value={role}
                    onChange={setRole}
                    style={{...stylesUnderButton, WebkitTextFillColor: "#000000"}}
                    menuStyle={{fontSize: "14px"}}
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
                        style={{marginBottom: '40px', marginTop: '20px'}}
                    >
                        {isSubmitting ? 'Обработка запроса...' : 'Добавить новую услугу'}
                    </Button>
                </div>
            </Form>
        </>
    );
});

export default AddNewService;