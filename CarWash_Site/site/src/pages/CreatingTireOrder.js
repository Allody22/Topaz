import React, {useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import '../css/CreatingOrder.css';
import '../css/NewStyles.css';
import {DatePicker, Notification, useToaster} from 'rsuite';

import addDays from 'date-fns/addDays';
import {Divider} from 'rsuite';

import 'rsuite/dist/rsuite.css';

import Modal from "react-bootstrap/Modal";

import {InputNumber, InputPicker} from 'rsuite';
import InputField from "../model/InputField";
import {
    createTireOrder,
    getAllTireServicesWithPriceAndTime,
    getPriceAndFreeTime
} from "../http/orderAPI";
import socketStore from "../store/SocketStore";
import {observer} from "mobx-react-lite";
import {BrowserRouter as Router, useHistory} from "react-router-dom";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import currentOrderStatusMapFromRus from "../model/map/CurrentOrderStatusMapFromRus";
import fileNameFromEngMap from "../model/map/FileNamesFromEngMap";
import {getAllSales} from "../http/userAPI";
import InputFieldNear from "../model/InputFieldNear";

const orderStatusArray = [
    "Отменён",
    "Не оплачен и не сделан",
    "Оплачен на 5 процентов и не сделан",
    "Оплачен на 10 процентов и не сделан",
    "Оплачен на 20 процентов и не сделан",
    "Оплачен на 30 процентов и не сделан",
    "Оплачен на 40 процентов и не сделан",
    "Оплачен на 50 процентов и не сделан",
    "Оплачен на 60 процентов и не сделан",
    "Оплачен на 70 процентов и не сделан",
    "Оплачен на 80 процентов и не сделан",
    "Оплачен на 90 процентов и не сделан",
    "Полностью оплачен и не сделан",
    "Не оплачен, но сделан",
    "Оплачен на 5 процентов и сделан",
    "Оплачен на 10 процентов и сделан",
    "Оплачен на 20 процентов и сделан",
    "Оплачен на 30 процентов и сделан",
    "Оплачен на 40 процентов и сделан",
    "Оплачен на 50 процентов и сделан",
    "Оплачен на 60 процентов и сделан",
    "Оплачен на 70 процентов и сделан",
    "Оплачен на 80 процентов и сделан",
    "Оплачен на 90 процентов и сделан",
    "Полностью оплачен и сделан"
].map(item => ({label: item, value: item}));

const inputStyle = {
    fontWeight: 'bold', display: 'flex',
    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '5px'
}

const importantInputStyle = {
    fontWeight: 'bold', display: 'flex', color:'red',
    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '5px'
}

const smallInputStyle = {
    display: 'flex', justifyContent: 'center', alignItems: 'center', marginTop: '5px'
}

const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};


const stylesForInput = {
    width: 190, marginBottom: 10, marginTop: 5
};

const inputStyleForPriceTime = {
    fontWeight: 'bold', display: 'flex',
    fontSize: '17px', justifyContent: 'center', alignItems: 'center',
    margin: '5px', padding: '5px', border: '1px solid #ccc',
    backgroundColor: '#fff', borderRadius: '5px', boxSizing: 'border-box'
};


const wheelSizeArray = [
    'R13', 'R14', 'R15', 'R16', 'R17', 'R18', 'R19', 'R20', 'R21', 'R22'].map(item => ({label: item, value: item}));

const CreatingTireOrder = observer(() => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitTime, setSubmitTime] = useState(0);
    const [showModal, setShowModal] = useState(false);

    const [itemsCount, setItemsCount] = useState([{name: '', value: 0}]);
    const [newTime, setNewTime] = useState([{startTime: null, endTime: null, box: 0}]);

    const [stringTimeForCurrentDay, setStringTimeForCurrentDay] = useState([]);

    const [selectedItems, setSelectedItems] = useState([]);
    const [currentStatus, setCurrentStatus] = useState('');

    const [mainOrders, setMainOrders] = useState([{
        name: null, price_r_13: null,
        price_r_14: null, price_r_15: null, price_r_16: null, price_r_17: null, price_r_18: null,
        price_r_19: null, price_r_20: null, price_r_21: null, price_r_22: null,
        time_r_14: null, time_r_15: null, time_r_16: null, time_r_17: null, time_r_18: null,
        time_r_19: null, time_r_20: null, time_r_21: null, time_r_22: null,
    }]);

    const [userContacts, setUserContacts] = useState('');
    const [price, setPrice] = useState(0);

    const [orderTime, setOrderTime] = useState(0);
    const [bonuses, setBonuses] = useState(0);
    const [boxNumber, setBoxNumber] = useState(0);

    const [startTime, setStartTime] = useState(new Date());
    const start = new Date(startTime);
    const end = new Date(startTime);
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    const [endTime, setEndTime] = useState('');

    const [selectedSaleDescription, setSelectedSaleDescription] = useState('');
    const [files, setFiles] = useState([]);

    const [requestEndTime, setRequestEndTime] = useState(new Date());
    const [requestStartTime, setRequestStartTime] = useState(new Date());

    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);
    const [successResponse, setSuccessResponse] = useState();
    const toaster = useToaster();

    const [carNumber, setCarNumber] = useState('');
    const [wheelR, setWheelR] = useState('');

    const [specialist, setSpecialist] = useState('');
    const [administrator, setAdministrator] = useState('');
    const [comments, setComments] = useState('');

    const updateItem = (name, value) => {
        if (!checkIfItemExists(name)) {
            const newItemToAdd = {name: name, value: value};
            setItemsCount(prevItems => [...prevItems, newItemToAdd]);
        } else {
            setItemsCount(current =>
                current.map(item => {
                    if (item.name === name) {
                        return {...item, value};
                    } else {
                        return item;
                    }
                })
            );
        }
    };

    const getItemValueByName = (name) => {
        const item = itemsCount.find(item => item.name === name);
        return item ? item.value : undefined;
    }

    const checkIfItemExists = (name) => {
        const item = itemsCount.find(item => item.name === name);
        return !!item;
    };

    const removeItem = (name) => {
        setItemsCount(current =>
            current.filter(item => item.name !== name)
        );
    };

    useEffect(() => {
        const newSelectedItems = [];
        for (let item of itemsCount) {
            for (let i = 0; i < item.value; i++) {
                newSelectedItems.push(item.name);
            }
        }
        setSelectedItems(newSelectedItems);
    }, [itemsCount]);

    const handleItemChange = (item, value) => {
        updateItem(item, value);

        if (value === '0') {
            removeItem(item);
        }
    };


    const handleGetPrice = async (e) => {
        e.preventDefault();
        try {

            const response = await getPriceAndFreeTime(selectedItems.map(i => i.replace(/ /g, '_')),
                null, "tire", wheelR, start.toISOString(), end.toISOString());

            setPrice(response.price);
            setOrderTime(response.time);

            const newTimeArray = response.availableTime.map(time => ({
                startTime: time.startTime,
                endTime: time.endTime,
                box: time.box
            }));

            setNewTime(newTimeArray);
        } catch (error) {
            if (error.response) {
                alert(error.response.data.message)
            } else {
                alert("Системная ошибка, попробуйте позже")
            }
        }
    }


    useEffect(() => {
        updateStringTimeForCurrentDay()
    }, [newTime]);

    const updateStringTimeForCurrentDay = () => {
        const timeStrings = newTime.map((time) => {
            const {startTime, endTime, box} = time;
            const start = new Date(startTime);
            const end = new Date(endTime);
            const startDateString = `${start.getHours().toString().padStart(2, '0')}:${start.getMinutes().toString().padStart(2, '0')}`;
            const endDateString = `${end.getHours().toString().padStart(2, '0')}:${end.getMinutes().toString().padStart(2, '0')}`;
            return `${startDateString} - ${endDateString}, бокс: ${box}`;
        });

        setStringTimeForCurrentDay(timeStrings);
    };


    useEffect(() => {
        async function getAllServices() {
            try {
                const response = await getAllTireServicesWithPriceAndTime();
                const filteredOrdersMain = response.map(item => ({
                    ...item,
                    name: item.name.replace(/_/g, ' ')
                }));
                setMainOrders(filteredOrdersMain);
            } catch (error) {
                if (error.response) {
                    alert(error.response.data.message)
                } else {
                    alert("Системная ошибка, попробуйте позже")
                }
            }
        }

        getAllServices();
    }, []);

    const filesOptions = files.map(file => ({
        label: `${fileNameFromEngMap[file.name]} - ${file.description}`,
        value: file.id
    }));

    async function getAllImages() {
        try {
            const response = await getAllSales();
            setFiles(response);
        } catch (error) {
            if (error.response) {
                alert(error.response.data.message)
            } else {
                alert("Системная ошибка, попробуйте позже")
            }
        }
    }

    useEffect(() => {
        getAllImages();
    }, []);

    const handleOpenModal = () => setShowModal(true);
    const handleCloseModal = () => setShowModal(false);

    useEffect(() => {
        updateStartEndTimeAndBox()
    }, [endTime]);

    const updateStartEndTimeAndBox = () => {
        const [timeStr, boxStr] = endTime.split(",");
        const [startStr, endStr] = timeStr.trim().split(" - ");

        const year = startTime.getFullYear();
        const month = startTime.getMonth() + 1;
        const day = startTime.getDate();

        const formattedDate = `${year}-${month}-${day}`;

        const start = new Date(`${formattedDate} ${startStr}`);
        const end = new Date(`${formattedDate} ${endStr}`);
        const boxNumber = boxStr ? parseInt(boxStr.match(/\d+/)[0]) : null;

        setRequestStartTime(start);
        setRequestEndTime(end);

        setBoxNumber(boxNumber);
    };
    useHistory();
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
            socketStore.isAlreadyShown = true;
        }
    }, [socketStore.message]);

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


    const handleCreateOrder = async (e) => {
        e.preventDefault();
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        setSubmitTime(Date.now());
        try {

            const response = await createTireOrder(selectedItems.map(i => i.replace(/ /g, '_')), userContacts,
                wheelR, requestStartTime.toISOString(),
                requestEndTime.toISOString(), administrator, specialist,
                boxNumber, bonuses, comments, carNumber, null, price,
                currentOrderStatusMapFromRus[currentStatus], selectedSaleDescription);
            setSuccessResponse(null)

            const ordersForResponse = response.orders.map(order => `"${order}"`);
            const ordersSentence = ordersForResponse.join(" и ");

            let formattedStartTime = new Date(response.startTime).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit",
            });
            let formattedEndTime = new Date(response.endTime).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit",
            });

            const sentence = `Заказы ${ordersSentence.replace(/_/g, ' ')} забронированы с ${formattedStartTime} до ${formattedEndTime}.`;
            setSuccessResponse(sentence)
        } catch (error) {
            if (error.response) {
                setErrorResponse(error.response.data.message)
                setErrorFlag(flag => !flag)
            } else {
                setErrorResponse("Системная ошибка, проверьте правильность " +
                    "введённой информации и попробуйте еще раз")
                setErrorFlag(flag => !flag)
            }
        } finally {
            setTimeout(() => setIsSubmitting(false), 4000);
        }
    };

    const timeStringToMinutes = (timeString) => {
        const [hours, minutes] = timeString.split(':');
        return parseInt(hours) * 60 + parseInt(minutes);
    };

    const compareTimeIntervals = (a, b) => {
        const aStartTime = timeStringToMinutes(a.split(' - ')[0]);
        const bStartTime = timeStringToMinutes(b.split(' - ')[0]);
        if (aStartTime < bStartTime) {
            return -1;
        }
        if (aStartTime > bStartTime) {
            return 1;
        }
        const aEndTime = timeStringToMinutes(a.split(' - ')[1]);
        const bEndTime = timeStringToMinutes(b.split(' - ')[1]);
        if (aEndTime < bEndTime) {
            return -1;
        }
        if (aEndTime > bEndTime) {
            return 1;
        }
        return a.localeCompare(b);
    };


    const predefinedBottomRanges = [
        {
            label: 'Сегодня',
            value: new Date(),
        },
        {
            label: 'Завтра',
            value: addDays(new Date(), 1),
        },
        {
            label: 'Послезавтра',
            value: addDays(new Date(), 2),
        },
    ];
    return (
        <>
            <p style={{...inputStyle, marginTop: '15px'}}>Страница добавления заказов на шиномонтаж</p>
            <p style={smallInputStyle}>Здесь вы можете сами создать какой-то заказ
                на шиномонтаж из всех актуальных услуг, а потом получить всю информацию о нём</p>
            <p style={smallInputStyle}> &nbsp;<strong>Обязательно</strong>&nbsp;выберите время заказа, диаметр колёс,
                набор услуг и состояние заказа</p>

            <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                Выберите услуги
            </Button>
            <Modal show={showModal}
                   onHide={handleCloseModal}
                   dialogClassName="custom-modal-dialog-tire"
            className="">
                <Modal.Header closeButton>
                    <Modal.Title>Выберите заказы</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {mainOrders.map((item, index) => (
                        <div key={index} style={{
                            fontSize: '16px',
                            borderBottom: '1px solid lightgray',
                            paddingBottom: '10px',
                            paddingTop: '10px'
                        }}>
                            <div style={{textAlign: 'center'}}>
                                <span>{item.name}</span>
                            </div>
                            <div style={{
                                display: 'grid',
                                gridTemplateColumns: 'auto 1fr',
                                gap: '10px',
                                alignItems: 'center'
                            }}>
                                <div style={{color: 'green'}}>Размеры:</div>
                                <div style={{display: 'grid', gridTemplateColumns: 'repeat(10, 1fr)', gap: '10px'}}>
                                    <div>R13</div>
                                    <div>R14</div>
                                    <div>R15</div>
                                    <div>R16</div>
                                    <div>R17</div>
                                    <div>R18</div>
                                    <div>R19</div>
                                    <div>R20</div>
                                    <div>R21</div>
                                    <div>R22</div>
                                </div>
                            </div>
                            <div style={{
                                display: 'grid',
                                gridTemplateColumns: 'auto 1fr',
                                gap: '10px',
                                alignItems: 'center'
                            }}>
                                <div style={{color: 'blue', gridColumn: '1'}}>Время:</div>
                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(10, 1fr)',
                                    gap: '10px',
                                    gridColumn: '2'
                                }}>
                                    <div
                                        style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0\u00A0'}{item.time_r_13}</div>
                                    <div
                                        style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0\u00A0'}{item.time_r_14}</div>
                                    <div
                                        style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0\u00A0'}{item.time_r_15}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0'}{item.time_r_16}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.time_r_17}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.time_r_18}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.time_r_19}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.time_r_20}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0'}{item.time_r_21}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0'}{item.time_r_22}</div>
                                </div>
                            </div>
                            <div style={{
                                display: 'grid',
                                gridTemplateColumns: 'auto 1fr',
                                gap: '10px',
                                alignItems: 'center'
                            }}>
                                <div style={{color: 'red', gridColumn: '1'}}>Цены:</div>
                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(10, 1fr)',
                                    gap: '10px',
                                    gridColumn: '2'
                                }}>
                                    <div
                                        style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0'}{item.price_r_13}</div>
                                    <div
                                        style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0'}{item.price_r_14}</div>
                                    <div
                                        style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0\u00A0'}{item.price_r_15}</div>
                                    <div
                                        style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0\u00A0\u00A0'}{item.price_r_16}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.price_r_17}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.price_r_18}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.price_r_19}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0\u00A0'}{item.price_r_20}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0'}{item.price_r_21}</div>
                                    <div style={{whiteSpace: 'pre'}}>{'\u00A0\u00A0'}{item.price_r_22}</div>
                                </div>
                            </div>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'center'
                            }}>
                                <InputNumber
                                    size="sm"
                                    placeholder="sm"
                                    style={Object.assign({}, stylesForInput, {margin: '0 auto', marginTop: '10px'})}
                                    min={0}
                                    onChange={value => handleItemChange(item.name, value)}
                                    value={getItemValueByName(item.name) || 0}
                                />
                            </div>
                        </div>
                    ))}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant='secondary' onClick={handleCloseModal}>
                        Закрыть
                    </Button>
                </Modal.Footer>
            </Modal>
            {
                selectedItems.length > 0 ? (
                    <div className="selected-items-container text-center">
                        <Form.Label style={{fontWeight: "bold", fontSize: "1.2em"}}>
                            Доп услуги:
                        </Form.Label>
                        <div className="selected-items">
                            {selectedItems
                                .filter((item, index) => selectedItems.indexOf(item) === index)
                                .map((item) => {
                                    if (getItemValueByName(item) > 0) {
                                        return (
                                            <span key={item} className="item">
                  {`${item} (${getItemValueByName(item)})`}
                </span>
                                        );
                                    }
                                    return null;
                                })}
                        </div>
                    </div>
                ) : (
                    <div className='selected-items-container text-center'>
                        <Form.Label style={{fontWeight: 'bold', fontSize: '1.2em'}}>
                            Дополнительные услуги:
                        </Form.Label>
                        <div className='selected-items-container text-center'>
                        <span className='empty-list' style={{fontSize: '1.1em'}}>
                            Нет дополнительных услуг
                        </span>
                        </div>
                    </div>
                )
            }
            <Divider></Divider>
            <p style={importantInputStyle}>Выберите размер колёс</p>
            <InputPicker
                data={wheelSizeArray}
                value={wheelR}
                onChange={setWheelR}
                style={{...styles, WebkitTextFillColor: "#000000"}}
                menuStyle={{fontSize: "17px"}}
            />

            <p style={importantInputStyle}>Выберите день заказа</p>
            <DatePicker
                isoWeek
                locale={{
                    sunday: 'Вск',
                    monday: 'Пн',
                    tuesday: 'Вт',
                    wednesday: 'Ср',
                    thursday: 'Чт',
                    friday: 'Пт',
                    saturday: 'Сб',
                    ok: 'OK',
                    today: 'Сегодня',
                    yesterday: 'Вчера',
                    hours: 'Часы',
                    minutes: 'Минуты',
                    seconds: 'Секунды'
                }}
                format="yyyy-MM-dd"
                oneTap
                ranges={predefinedBottomRanges}
                block
                appearance="default"
                value={startTime}
                onChange={setStartTime}
                style={{
                    width: 500,
                    marginLeft: 'auto',
                    marginRight: 'auto',
                    marginTop: 10,
                    WebkitTextFillColor: "#000000",
                }}
            />

            <Button className='full-width' appearance="primary" block onClick={handleGetPrice}>
                Узнать цену заказа, время и доступное расписание
            </Button>


            <div className="label-container">
                <InputFieldNear
                    label='Цена услуги:'
                    id='price'
                    value={price}
                    inputStyle={inputStyleForPriceTime}
                    onChange={setPrice}
                />
                <InputFieldNear
                    label='Время выполнения:'
                    id='time'
                    value={orderTime}
                    inputStyle={inputStyleForPriceTime}
                    onChange={setOrderTime}
                />
            </div>

            <p style={importantInputStyle}>Расписание с доступным временем</p>

            <InputPicker
                data={stringTimeForCurrentDay.sort(compareTimeIntervals).map((item) => ({label: item, value: item}))}
                style={{
                    width: 500,
                    marginLeft: 'auto',
                    marginRight: 'auto',
                    marginTop: 10,
                    alignItems: 'center',
                    justifyContent: 'center',
                    WebkitTextFillColor: "#000000",
                    display: 'flex',
                }}
                menuStyle={{fontSize: "17px"}}
                value={endTime}
                onChange={(value) => {
                    if (value) {
                        setEndTime(value);
                    }
                }}
            />
            <Form onSubmit={handleCreateOrder}>
                <InputField
                    label='Номер телефона клиента:'
                    id='name'
                    value={userContacts}
                    inputStyle={inputStyle}
                    onChange={setUserContacts}
                />
                <InputField
                    label='Номер автомобиля:'
                    id='carNumber'
                    inputStyle={inputStyle}
                    value={carNumber}
                    onChange={setCarNumber}
                />

                <p style={importantInputStyle}>Выберите состояние заказа</p>

                <InputPicker
                    data={orderStatusArray}
                    value={currentStatus}
                    onChange={setCurrentStatus}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    menuStyle={{fontSize: "17px"}}
                />

                <p style={inputStyle}>Выберите акцию, если необходимо</p>

                <InputPicker
                    data={filesOptions}
                    inputStyle={inputStyle}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    value={selectedSaleDescription}
                    menuStyle={{fontSize: "17px"}}

                    onChange={(selectedValue) => {
                        const selectedFile = files.find(file => file.id === selectedValue);
                        setSelectedSaleDescription(selectedFile.description);
                    }}
                />

                <InputField
                    label='Специалист:'
                    inputStyle={inputStyle}
                    id='specialist'
                    value={specialist}
                    onChange={setSpecialist}
                />
                <InputField
                    label='Администратор:'
                    id='administrator'
                    inputStyle={inputStyle}
                    value={administrator}
                    onChange={setAdministrator}
                />
                <InputField
                    label='Количество использованных бонусов:'
                    id='bonuses'
                    inputStyle={inputStyle}
                    value={bonuses}
                    onChange={setBonuses}
                />
                <InputField
                    label='Комментарии:'
                    id='comments'
                    inputStyle={inputStyle}
                    value={comments}
                    onChange={setComments}
                />
                <div className='submit-container'>
                    <Button
                        className='btn-submit'
                        variant='primary'
                        type='submit'
                        disabled={isSubmitting || Date.now() < submitTime + 2000}
                        style={{marginBottom: '20px', marginTop: '20px'}}>
                        {isSubmitting ? 'Обработка заказа...' : 'Сделать заказ'}
                    </Button>
                </div>
            </Form>
        </>
    );
});

export default CreatingTireOrder;