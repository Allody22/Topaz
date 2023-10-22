import React, {useEffect, useMemo, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import '../css/CreatingOrder.css';
import '../css/NewStyles.css';
import '../css/CommonStyles.css';

import {DatePicker, Divider, InputNumber, InputPicker, Notification, useToaster} from 'rsuite';
import addDays from 'date-fns/addDays';

import 'rsuite/dist/rsuite.css';
import InputField from "../model/InputField";
import {createWashingOrder, getAllWashingServicesWithPriceAndTime, getFreeTime,} from "../http/orderAPI";
import socketStore from "../store/SocketStore";
import {observer} from "mobx-react-lite";
import {BrowserRouter as Router, useHistory} from "react-router-dom";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import currentOrderStatusMapFromRus from "../model/map/CurrentOrderStatusMapFromRus";
import InputFieldNear from "../model/InputFieldNear";
import saleStore from "../store/SaleStore";
import {carTypesArray, orderStatusArray} from "../model/Constants";
import MyCustomModal from "../model/MyCustomModal";

const stylesForInput = {
    width: 190, marginBottom: 10, marginTop: 5
};

const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};

const CreatingWashingOrder = observer(() => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitTime, setSubmitTime] = useState(0);
    const [showModal, setShowModal] = useState(false);
    const [showModalB, setShowModalB] = useState(false);
    const [newTime, setNewTime] = useState([{startTime: null, endTime: null, box: 0}]);

    const [stringTimeForCurrentDay, setStringTimeForCurrentDay] = useState([]);
    const [currentStatus, setCurrentStatus] = useState('');


    const [selectedItems, setSelectedItems] = useState([{
        name: null, priceFirstType: null,
        priceSecondType: null, priceThirdType: null, timeFirstType: null, timeSecondType: null, timeThirdType: null,
        number: 0
    }]);

    const [mainOrders, setMainOrders] = useState([{
        name: null, priceFirstType: null,
        priceSecondType: null, priceThirdType: null, timeFirstType: null, timeSecondType: null, timeThirdType: null
    }]);

    const [additionalOrders, setAdditionalOrders] = useState([{
        name: null, priceFirstType: null,
        priceSecondType: null, priceThirdType: null, timeFirstType: null, timeSecondType: null, timeThirdType: null
    }]);


    const [userContacts, setUserContacts] = useState('');
    const [price, setPrice] = useState(0);


    const [selectedSaleDescription, setSelectedSaleDescription] = useState('');
    const [files, setFiles] = useState([]);

    const [selectedFileId, setSelectedFileId] = useState(null);

    const [orderTime, setOrderTime] = useState(0);
    const [bonuses, setBonuses] = useState(0);
    const [boxNumber, setBoxNumber] = useState(0);

    const [startTime, setStartTime] = useState(new Date());
    const start = new Date(startTime);
    const end = new Date(startTime);
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    const [endTime, setEndTime] = useState('');

    const [requestEndTime, setRequestEndTime] = useState(new Date());
    const [requestStartTime, setRequestStartTime] = useState(new Date());

    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);
    const [successResponse, setSuccessResponse] = useState();
    const toaster = useToaster();

    const [carNumber, setCarNumber] = useState('');
    const [carTypeMap, setCarTypeMap] = useState('');
    const [carType, setCarType] = useState(0);
    const [specialist, setSpecialist] = useState('');
    const [administrator, setAdministrator] = useState('');
    const [comments, setComments] = useState('');

    useEffect(() => {
        if (saleStore?.error) {
            const errorResponseMessage = (
                <Notification
                    type="error"
                    header="Ошибка!"
                    closable
                    style={{border: '1px solid black'}}
                >
                    <div style={{width: 320}}>
                        {saleStore.error}
                    </div>
                </Notification>
            );

            toaster.push(errorResponseMessage, {placement: "bottomEnd"});
            saleStore.error = null; // Очищаем ошибку после показа
        }
    }, [saleStore?.error]);


    const filesOptions = useMemo(() =>
            files.map(file => ({
                label: `${file.name} - ${file.description}`,
                value: file.id
            }))
        , [files]);


    useEffect(() => {
        if (saleStore.discounts.length === 0) {
            saleStore.loadDiscounts();
        } else {
            setFiles(saleStore.discounts);
        }
    }, [saleStore.discounts]);

    const findSelectedItemByName = (name) => {
        const item = selectedItems.find(item => item.name === name);
        return item ? item.number : undefined;
    }

    useEffect(() => {
        const carCode = mapCarTypeToCode(carTypeMap);
        setCarType(carCode);

        const updatedItems = selectedItems.map((item) => {
            let price = 0;
            let time = 0;

            switch (carCode) {
                case 1:
                    price = item.priceFirstType * item.number;
                    time = item.timeFirstType * item.number;
                    break;
                case 2:
                    price = item.priceSecondType * item.number;
                    time = item.timeSecondType * item.number;
                    break;
                case 3:
                    price = item.priceThirdType * item.number;
                    time = item.timeThirdType * item.number;
                    break;
                default:
                    price = 0;
                    time = 0;
                    break;
            }

            return {
                price,
                time,
            };
        });

        const totalPrice = updatedItems.reduce((total, item) => total + item.price, 0);
        const totalOrderTime = updatedItems.reduce((total, item) => total + item.time, 0);


        setPrice(totalPrice);
        setOrderTime(totalOrderTime);
    }, [carTypeMap, selectedItems]);

    const updateSelectedItems = (itemName, updatedData) => {
        setSelectedItems((prevSelectedItems) => {
            const updatedItems = [...prevSelectedItems];
            const selectedItem = updatedItems.find((item) => item.name === itemName);

            if (selectedItem) {
                // Обновляем существующий элемент
                selectedItem.priceFirstType = updatedData.priceFirstType;
                selectedItem.priceSecondType = updatedData.priceSecondType;
                selectedItem.priceThirdType = updatedData.priceThirdType;
                selectedItem.timeFirstType = updatedData.timeFirstType;
                selectedItem.timeSecondType = updatedData.timeSecondType;
                selectedItem.timeThirdType = updatedData.timeThirdType;
                selectedItem.number = updatedData.number;
                return updatedItems;
            } else {
                // Добавляем новый элемент
                const newItem = {
                    name: itemName,
                    priceFirstType: updatedData.priceFirstType,
                    priceSecondType: updatedData.priceSecondType,
                    priceThirdType: updatedData.priceThirdType,
                    timeFirstType: updatedData.timeFirstType,
                    timeSecondType: updatedData.timeSecondType,
                    timeThirdType: updatedData.timeThirdType,
                    number: updatedData.number
                };
                updatedItems.push(newItem);
                return updatedItems;
            }
        });
    };


    const handleItemChange = (item, value) => {
        if (value === '0') {
            const updatedSelectedItems = selectedItems.filter(selectedItem => selectedItem.name !== item.name);
            setSelectedItems(updatedSelectedItems);
        } else {
            const updatedData = {
                priceFirstType: item.priceFirstType,
                priceSecondType: item.priceSecondType,
                priceThirdType: item.priceThirdType,
                timeFirstType: item.timeFirstType,
                timeSecondType: item.timeSecondType,
                timeThirdType: item.timeThirdType,
                number: value
            };

            updateSelectedItems(item.name, updatedData);
        }
    };

    const mapCarTypeToCode = (newCarType) => {
        switch (newCarType) {
            case "1 тип - седан":
                return 1;
            case "2 тип - кроссовер":
                return 2;
            case "3 тип - джип":
                return 3;
            default:
                return -1;
        }
    };

    useEffect(() => {
        const carCode = mapCarTypeToCode(carTypeMap);
        setCarType(carCode);
    }, [carTypeMap]);


    const handleGetFreeTime = async (e) => {
        e.preventDefault();
        try {
            const response = await getFreeTime(orderTime, "wash", start.toISOString(), end.toISOString());

            const newTimeArray = response.availableTime.map(time => ({
                startTime: time.startTime,
                endTime: time.endTime,
                box: time.box
            }));

            setNewTime(newTimeArray);

            const sentence = `Свободное время успешно получено!`;
            setSuccessResponse(sentence)
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

    const handleOpenModal = () => setShowModal(true);
    const handleCloseModal = () => setShowModal(false);

    const handleOpenModalB = () => setShowModalB(true);
    const handleCloseModalB = () => setShowModalB(false);

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
        const boxNumber = boxStr ? parseInt(boxStr.match(/\d+/)[0]) : null; // проверяем наличие запятой в строке

        setRequestStartTime(start);
        setRequestEndTime(end);

        setBoxNumber(boxNumber);
    };


    useEffect(() => {
        async function getAllService() {
            try {
                const response = await getAllWashingServicesWithPriceAndTime();
                const filteredOrdersMain = response.filter(item => item.role === "main").map(item => ({
                    ...item,
                    name: item.name.replace(/_/g, ' ')
                }));

                const filteredOrdersAdditional = response.filter(item => item.role !== "main").map(item => ({
                    ...item,
                    name: item.name.replace(/_/g, ' ')
                }));

                setMainOrders(filteredOrdersMain);
                setAdditionalOrders(filteredOrdersAdditional)
            } catch (error) {
                if (error.response) {
                    let messages = [];
                    for (let key in error.response.data) {
                        messages.push(error.response.data[key]);
                    }
                    setErrorResponse(messages.join('\n'));  // Объединяем все сообщения об ошибках через запятую
                    setErrorFlag(flag => !flag);

                } else {
                    setErrorResponse("Системная ошибка с получением услуг, " +
                        "попробуйте еще раз")
                    setErrorFlag(flag => !flag)
                }
            }
        }

        getAllService();
    }, []);
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

    const successMessage = (
        <Notification
            type="success"
            header="Успешно!"
            closable
            style={{border: '1px solid black'}}
        >
            <div style={{width: 320, whiteSpace: 'pre-line'}}>
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

    useEffect(() => {
        if (successResponse) {
            toaster.push(successMessage, {placement: "bottomEnd"});
        }
    }, [successResponse]);


    const handleCreateOrder = async (e) => {
        e.preventDefault();
        if (!requestStartTime || !requestEndTime) {
            setErrorResponse("Обязательно укажите время начала и время конца заказа")
            setErrorFlag(flag => !flag)
            return;
        }

        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        setSubmitTime(Date.now());

        try {
            const namesArray = selectedItems.flatMap((item) => {
                const {name, number} = item;
                return Array.from({length: number}, () => name);
            });

            const response = await createWashingOrder(namesArray.map((name) => name.replace(/ /g, '_')),
                userContacts, requestStartTime.toISOString(), requestEndTime.toISOString(),
                administrator, specialist, boxNumber, bonuses, comments,
                carNumber, carType, price, currentOrderStatusMapFromRus[currentStatus], selectedSaleDescription);

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
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join('\n'));
                setErrorFlag(flag => !flag);
            } else {
                setErrorResponse("Системная ошибка с созданием заказа. Проверьте правильность введённой информации" +
                    " и попробуйте еще ")
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
            <p className="input-style-modified">Страница добавления заказов на мойку</p>
            <p className="small-input-style">Здесь вы можете сами создать какой-то заказ мойки на автомойку из всех
                актуальных услуг, а потом получить всю информацию о нём</p>
            <p className="small-input-style"><strong>Обязательно</strong>: все элементы с красными
                под красным текстом</p>

            <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                Основные услуги
            </Button>
            <MyCustomModal show={showModal} handleClose={handleCloseModal} title="Выберите заказы">
                <div style={{overflowY: 'auto', maxHeight: '80vh'}}>
                    {mainOrders.map(item => (
                        <div key={item.name} style={{
                            fontSize: '16px', borderBottom: '1px solid lightgray',
                            paddingBottom: '10px', paddingTop: '10px'
                        }}>
                            <div style={{textAlign: 'center'}}>
                                <span>{item.name}</span>
                            </div>
                            <div style={{display: 'flex', justifyContent: 'space-between', marginTop: '7px'}}>
                                <div>
                                    <span style={{color: "red"}}>Цены: </span>
                                    <span>{`${item.priceFirstType} / ${item.priceSecondType} / ${item.priceThirdType}`}</span>
                                </div>
                                <div style={{marginLeft: 'auto'}}>
                                    <span style={{color: "blue"}}>Время: </span>
                                    <span>{`${item.timeFirstType} / ${item.timeSecondType} / ${item.timeThirdType}`}</span>
                                </div>
                            </div>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'center'
                            }}>
                                <InputNumber size="sm" placeholder="sm"
                                             style={Object.assign({}, stylesForInput, {
                                                 margin: '0 auto',
                                                 marginTop: '10px'
                                             })}
                                             min={0}
                                             onChange={value => handleItemChange(item, value)}
                                             value={findSelectedItemByName(item.name) || 0}/>
                            </div>
                        </div>
                    ))}
                </div>
            </MyCustomModal>

            <Button className='full-width' variant='secondary' onClick={handleOpenModalB}>
                Дополнительные услуги
            </Button>

            <MyCustomModal
                show={showModalB}
                handleClose={handleCloseModalB}
                title="Выберите заказы"
            >
                <div style={{overflowY: 'auto', maxHeight: '80vh'}}>
                    {additionalOrders.map(item => (
                        <div key={item.name} style={{
                            fontSize: '16px', borderBottom: '1px solid lightgray',
                            paddingBottom: '10px', paddingTop: '10px'
                        }}>
                            <div style={{textAlign: 'center'}}>
                                <span>{item.name}</span>
                            </div>
                            <div style={{display: 'flex', justifyContent: 'space-between', marginTop: '7px'}}>
                                <div>
                                    <span style={{color: "red"}}>Цены: </span>
                                    <span>{`${item.priceFirstType} / ${item.priceSecondType} / ${item.priceThirdType}`}</span>
                                </div>
                                <div style={{marginLeft: 'auto'}}>
                                    <span style={{color: "blue"}}>Время: </span>
                                    <span>{`${item.timeFirstType} / ${item.timeSecondType} / ${item.timeThirdType}`}</span>
                                </div>
                            </div>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'center'
                            }}>
                                <InputNumber size="sm" placeholder="sm"
                                             style={Object.assign({}, stylesForInput, {margin: '0 auto'})} min={0}
                                             onChange={value => handleItemChange(item, value)}
                                             value={findSelectedItemByName(item.name) || 0}/>
                            </div>
                        </div>
                    ))}
                </div>
            </MyCustomModal>

            {selectedItems.length > 0 ? (
                <div className="selected-items-container text-center">
                    <Form.Label style={{fontWeight: "bold", fontSize: "1.2em"}}>
                        Выбранные услуги:
                    </Form.Label>
                    <div className="selected-items">
                        {Object.values(
                            selectedItems.reduce((acc, item) => {
                                if (item.number > 0) {
                                    if (!acc[item.name]) {
                                        acc[item.name] = {
                                            name: item.name,
                                            count: 0,
                                        };
                                    }
                                    acc[item.name].count = item.number;
                                }
                                return acc;
                            }, {})
                        ).map((groupedItem) => (
                            <span key={groupedItem.name} className="item">
                    {`${groupedItem.name} (${groupedItem.count})`}
                </span>
                        ))}
                    </div>
                </div>
            ) : (
                <div className='selected-items-container text-center'>
                    <Form.Label style={{fontWeight: 'bold', fontSize: '1.2em'}}>
                        Выбранные услуги:
                    </Form.Label>
                    <div className='selected-items-container text-center'>
            <span className='empty-list' style={{fontSize: '1.1em'}}>
                Нет выбранных услуги
            </span>
                    </div>
                </div>)}

            <Divider></Divider>
            <p className="important-input-style">Выберите тип кузова</p>

            <InputPicker
                data={carTypesArray}
                value={carTypeMap}
                onChange={setCarTypeMap}
                style={{...styles, WebkitTextFillColor: "#000000"}}
                menuStyle={{fontSize: "17px"}}
            />

            <p className="important-input-style">Выберите день заказа</p>
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

            <Button className='full-width' appearance="primary" block onClick={handleGetFreeTime}>
                Узнать доступное расписание
            </Button>


            <div className="label-container">
                <InputFieldNear
                    label='Цена услуги:'
                    id='price'
                    value={price}
                    className="input-style-for-price-time"
                    onChange={setPrice}
                />
                <InputFieldNear
                    label='Время выполнения:'
                    id='time'
                    value={orderTime}
                    className="input-style-for-price-time"
                    onChange={setOrderTime}
                />
            </div>

            <p className="important-input-style">Расписание с доступным временем</p>

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
                    className="input-style"
                    onChange={setUserContacts}
                    maxLength={50}
                />
                <InputField
                    label='Номер автомобиля:'
                    id='carNumber'
                    className="input-style"
                    value={carNumber}
                    onChange={setCarNumber}
                    maxLength={50}
                />
                <p className="important-input-style">Выберите состояние заказа</p>

                <InputPicker
                    data={orderStatusArray}
                    value={currentStatus}
                    onChange={setCurrentStatus}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    menuStyle={{fontSize: "17px"}}
                />

                <p className="input-style">Выберите акцию, если необходимо</p>

                <InputPicker
                    data={filesOptions}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    value={selectedFileId} // здесь изменено на selectedFileId
                    menuStyle={{fontSize: "17px"}}
                    onChange={(selectedValue) => {
                        const selectedFile = files.find(file => file.id === selectedValue);
                        setSelectedFileId(selectedValue); // сохраняем ID файла
                        setSelectedSaleDescription(selectedFile.description);
                    }}
                />

                <InputField
                    label='Специалист:'
                    className="input-style"
                    id='specialist'
                    value={specialist}
                    onChange={setSpecialist}
                    maxLength={50}
                />
                <InputField
                    maxLength={50}
                    label='Администратор:'
                    id='administrator'
                    className="input-style"
                    value={administrator}
                    onChange={setAdministrator}
                />
                <InputField
                    maxLength={50}
                    label='Количество использованных бонусов:'
                    id='bonuses'
                    className="input-style"
                    value={bonuses}
                    onChange={setBonuses}
                />
                <InputField
                    maxLength={255}
                    label='Комментарии:'
                    id='comments'
                    className="input-style"
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

export default CreatingWashingOrder;