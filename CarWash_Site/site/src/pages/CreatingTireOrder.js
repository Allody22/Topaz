import React, {useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import '../css/CreatingOrder.css';
import '../css/NewStyles.css';
import '../css/CommonStyles.css';
import {DatePicker, Divider, InputNumber, InputPicker, Notification, useToaster} from 'rsuite';

import addDays from 'date-fns/addDays';

import 'rsuite/dist/rsuite.css';

import InputField from "../model/InputField";
import {createTireOrder, getAllTireServicesWithPriceAndTime, getPriceAndFreeTime} from "../http/orderAPI";
import socketStore from "../store/SocketStore";
import {observer} from "mobx-react-lite";
import {BrowserRouter as Router, useHistory} from "react-router-dom";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import currentOrderStatusMapFromRus from "../model/map/CurrentOrderStatusMapFromRus";
import InputFieldNear from "../model/InputFieldNear";
import saleStore from "../store/SaleStore";
import {orderStatusArray} from "../model/Constants";
import MyCustomModal from "../model/MyCustomModal";

const stylesForInput = {
    width: 190, marginBottom: 10, marginTop: 5
};

const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};

const wheelSizeArray = [
    'R13', 'R14', 'R15', 'R16', 'R17', 'R18', 'R19', 'R20', 'R21', 'R22'].map(item => ({label: item, value: item}));

const CreatingTireOrder = observer(() => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitTime, setSubmitTime] = useState(0);
    const [showModal, setShowModal] = useState(false);
    const [selectedFileId, setSelectedFileId] = useState(null);

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
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join(''));  // Объединяем все сообщения об ошибках через запятую
                setErrorFlag(flag => !flag);

            } else {
                setErrorResponse("Системная ошибка с получением цены. " +
                    "Попробуйте перезагрузить страницу")
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
                    let messages = [];
                    for (let key in error.response.data) {
                        messages.push(error.response.data[key]);
                    }
                    setErrorResponse(messages.join(''));  // Объединяем все сообщения об ошибках через запятую
                    setErrorFlag(flag => !flag);

                } else {
                    setErrorResponse("Системная ошибка с получением акций. " +
                        "Попробуйте перезагрузить страницу")
                    setErrorFlag(flag => !flag)
                }
            }
        }

        getAllServices();
    }, []);

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


    const filesOptions = files.map(file => ({
        label: `${file.name} - ${file.description}`,
        value: file.id
    }));


    useEffect(() => {
        if (saleStore.discounts.length === 0) {
            saleStore.loadDiscounts();
        } else {
            setFiles(saleStore.discounts);
        }
    }, [saleStore.discounts]);


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
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join(''));  // Объединяем все сообщения об ошибках через запятую
                setErrorFlag(flag => !flag);

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
            <p className="input-style-modified">Страница добавления заказов на шиномонтаж</p>
            <p className="small-input-style">Здесь вы можете сами создать какой-то заказ
                на шиномонтаж из всех актуальных услуг, а потом получить всю информацию о нём</p>
            <p className="small-input-style"><strong>Обязательно</strong> выберите все элементы под красным
                текстом</p>

            <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                Выберите услуги
            </Button>

            <MyCustomModal show={showModal} handleClose={handleCloseModal} title="Выберите заказы">
                <div style={{overflowY: 'auto', maxHeight: '80vh'}}>
                    {mainOrders.map((item, index) => (
                        <div key={index} style={{
                            fontSize: '16px',
                            borderBottom: '1px solid lightgray',
                            paddingBottom: '10px',
                            paddingTop: '10px',
                        }}>
                            <div style={{textAlign: 'center'}}>
                                <span>{item.name}</span>
                            </div>
                            <div style={{
                                display: 'grid',
                                gap: '10px',
                                alignItems: 'center',
                                gridTemplateColumns: 'auto 1fr',
                                gridTemplateRows: 'repeat(3, auto)', // Добавляем три строки для Размеров, Времени и Цен
                            }}>
                                <div style={{color: 'green'}}>Размеры:</div>
                                <div style={{
                                    display: 'grid',
                                    gap: '10px',
                                    gridTemplateColumns: 'repeat(10, 1fr)',
                                    overflowX: 'auto'
                                }}>
                                    <div style={{whiteSpace: 'nowrap'}}>R13</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R14</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R15</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R16</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R17</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R18</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R19</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R20</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R21</div>
                                    <div style={{whiteSpace: 'nowrap'}}>R22</div>
                                </div>
                                <div style={{color: 'blue'}}>Время:</div>
                                <div style={{
                                    display: 'grid',
                                    gap: '10px',
                                    gridTemplateColumns: 'repeat(10, 1fr)',
                                    overflowX: 'auto'
                                }}>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_13}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_14}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_15}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_16}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_17}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_18}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_19}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_20}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_21}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.time_r_22}</div>
                                </div>
                                <div style={{color: 'red'}}>Цены:</div>
                                <div style={{
                                    display: 'grid',
                                    gap: '10px',
                                    gridTemplateColumns: 'repeat(10, 1fr)',
                                    overflowX: 'auto'
                                }}>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_13}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_14}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_15}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_16}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_17}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_18}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_19}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_20}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_21}</div>
                                    <div style={{whiteSpace: 'nowrap'}}>{item.price_r_22}</div>
                                </div>
                            </div>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'center',
                                marginTop: '10px',
                            }}>
                                <InputNumber
                                    size="sm"
                                    placeholder="sm"
                                    style={Object.assign({}, stylesForInput, {margin: '0 auto'})}
                                    min={0}
                                    onChange={value => handleItemChange(item.name, value)}
                                    value={getItemValueByName(item.name) || 0}
                                />
                            </div>
                        </div>
                    ))}
                </div>
            </MyCustomModal>

            {selectedItems.length > 0 ? (
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
            <p className="important-input-style">Выберите размер колёс</p>
            <InputPicker
                data={wheelSizeArray}
                value={wheelR}
                onChange={setWheelR}
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

            <Button className='full-width' appearance="primary" block onClick={handleGetPrice}>
                Узнать цену заказа, время и доступное расписание
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
                    className="input-style"
                    value={userContacts}
                    onChange={setUserContacts}
                />
                <InputField
                    label='Номер автомобиля:'
                    id='carNumber'
                    className="input-style"
                    value={carNumber}
                    onChange={setCarNumber}
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
                />
                <InputField
                    label='Администратор:'
                    id='administrator'
                    className="input-style"
                    value={administrator}
                    onChange={setAdministrator}
                />
                <InputField
                    label='Количество использованных бонусов:'
                    id='bonuses'
                    className="input-style"
                    value={bonuses}
                    onChange={setBonuses}
                />
                <InputField
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

export default CreatingTireOrder;