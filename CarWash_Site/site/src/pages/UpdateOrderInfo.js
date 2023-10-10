import '../css/CreatingOrder.css';
import '../css/CommonStyles.css';
import React, {useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import InputField from "../model/InputField";
import {BrowserRouter as Router, useParams} from "react-router-dom";
import {
    deleteOrderById,
    getAllPolishingServicesWithPriceAndTime,
    getAllTireServicesWithPriceAndTime,
    getAllWashingServicesWithPriceAndTime,
    getOrderInfo,
    updateOrderInfo
} from "../http/orderAPI";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {DatePicker, Divider, InputNumber, InputPicker, Notification, useToaster} from "rsuite";
import addDays from "date-fns/addDays";
import Modal from "react-bootstrap/Modal";
import russianToEnglishMap from "../model/map/OrderTypeMapFromRussian";
import {observer} from "mobx-react-lite";
import socketStore from "../store/SocketStore";
import {format, parseISO} from "date-fns";
import currentOrderStatusMapFromEng from "../model/map/CurrentOrderStatusMapFromEng";
import currentOrderStatusMapFromRus from "../model/map/CurrentOrderStatusMapFromRus";
import fileNameFromEngMap from "../model/map/FileNamesFromEngMap";
import {getAllSales} from "../http/userAPI";
import {carTypesArray, orderStatusArray, serviceTypesArray} from "../model/Constants";
import MyCustomModal from "../model/MyCustomModal";

const wheelSizeArray = [
    'R13', 'R14', 'R15', 'R16', 'R17', 'R18', 'R19', 'R20', 'R21', 'R22'].map(item => ({label: item, value: item}));

const stylesForInput = {
    width: 190, marginBottom: 10, marginTop: 5
};

const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};

const UpdateOrderInfo = observer(() => {
    const [showModal, setShowModal] = useState(false);
    const [showModalB, setShowModalB] = useState(false);

    const handleOpenModal = () => {
        if (orderType) {
            if (russianToEnglishMap[orderType].includes("tire")) {
                setShowModalB(true);
            } else {
                setShowModal(true)
            }
        }
    }
    const handleCloseModal = () => {
        if (orderType) {
            if (russianToEnglishMap[orderType].includes("tire")) {
                setShowModalB(false);
            } else {
                setShowModal(false)
            }
        }
    }


    const [userPhone, setUserPhone] = useState('');
    const [wheelR, setWheelR] = useState('');
    const [price, setPrice] = useState(0);
    const [orderType, setOrderType] = useState('');
    const [startTime, setStartTime] = useState(new Date());
    const [endTime, setEndTime] = useState(new Date());
    const [administrator, setAdministrator] = useState('');
    const [autoNumber, setAutoNumber] = useState('');
    const [specialist, setSpecialist] = useState('');
    const [currentStatus, setCurrentStatus] = useState('');


    const [carTypeMap, setCarTypeMap] = useState('');
    const [carType, setCarType] = useState(0);
    const toaster = useToaster();

    const [itemsCount, setItemsCount] = useState([{name: '', value: 0}]);

    const [selectedItems, setSelectedItems] = useState([]);

    const [mainOrders, setMainOrders] = useState([{
        name: null, priceFirstType: null,
        priceSecondType: null, priceThirdType: null, timeFirstType: null, timeSecondType: null, timeThirdType: null
    }]);

    const [mainTireOrders, setMainTireOrders] = useState([{
        name: null, price_r_13: null,
        price_r_14: null, price_r_15: null, price_r_16: null, price_r_17: null, price_r_18: null,
        price_r_19: null, price_r_20: null, price_r_21: null, price_r_22: null,
        time_r_14: null, time_r_15: null, time_r_16: null, time_r_17: null, time_r_18: null,
        time_r_19: null, time_r_20: null, time_r_21: null, time_r_22: null,
    }]);


    const [boxNumber, setBoxNumber] = useState(0);
    const [bonuses, setBonuses] = useState(0);
    const [comments, setComments] = useState('');

    const [selectedSaleId, setSelectedSaleId] = useState(null);


    const [selectedSaleDescription, setSelectedSaleDescription] = useState('');
    const [files, setFiles] = useState([]);

    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);

    const [successResponse, setSuccessResponse] = useState();

    const [isSubmitting] = useState(false);
    const [submitTime] = useState(0);
    const [showConfirmation, setShowConfirmation] = useState(false);

    const [showConfirmationUpdateOrder, setShowConfirmationUpdateOrder] = useState(false);


    const {id} = useParams();
    const [orderId, setOrderId] = useState('')

    const filesOptions = files.map(file => ({
        label: `${fileNameFromEngMap[file.name]} - ${file.description}`,
        value: file.id
    }));

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

    async function getThisOrderInfo(currentId) {
        try {
            if (!isNaN(currentId) && String(currentId).trim() !== '') {
                setItemsCount([])
                const response = await getOrderInfo(parseInt(currentId));
                const responseSales = await getAllSales();
                setFiles(responseSales);

                const countMap = response.orders.map(i => i.replace(/_/g, ' ')).reduce((map, order) => {
                    map.set(order, (map.get(order) || 0) + 1);
                    return map;
                }, new Map());


                countMap.forEach((count, item) => {
                    handleItemChange(item, count.toString());
                })


                const responseCarType = mapCarTypeCodeToString(response.autoType)
                setCarTypeMap(responseCarType)
                setCurrentStatus(currentOrderStatusMapFromEng[response.currentStatus])

                setSelectedSaleDescription(response.sale);
                const selectedFile = files.find(file => file.description === response.sale);
                if (selectedFile) {
                    setSelectedSaleId(selectedFile.id);
                }


                setOrderId(response.id)
                setAutoNumber(response.autoNumber)
                setAdministrator(response.administrator)
                setSpecialist(response.specialist)
                setBonuses(response.bonuses)
                setBoxNumber(response.boxNumber)
                setPrice(response.price)
                setComments(response.comments)

                const startTime = new Date(response.startTime);
                setStartTime(startTime)
                const endTime = new Date(response.endTime);
                setEndTime(endTime)

                setUserPhone(response.userNumber)
                setOrderType(() => response.orderType ? orderTypeMap[response.orderType] || response.orderType : "Неизвестно")
                setWheelR(response.wheelR)
            }
        } catch
            (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join(''));  // Объединяем все сообщения об ошибках через запятую
                setErrorFlag(flag => !flag);

            } else {
                setErrorResponse("Системная ошибка с получением информации о заказе. " +
                    "Попробуйте еще раз")
                setErrorFlag(flag => !flag)
            }
        }
    }


    useEffect(() => {
            getThisOrderInfo(id);
        }, [id]
    );


    useEffect(() => {
            getThisOrderInfo(orderId);
        }, [orderId]
    );

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


    const mapCarTypeCodeToString = (newCarType) => {
        if (newCarType === 1) {
            return "1 тип - седан"
        } else if (newCarType === 2) {
            return "2 тип - кроссовер"
        } else if (newCarType === 3) {
            return "3 тип - джип"
        } else {
            return "Неизвестно"
        }
    };


    async function getAllPolishingServices() {
        try {
            const response = await getAllPolishingServicesWithPriceAndTime();
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
                setErrorResponse("Системная ошибка с получением услуг полировки. " +
                    "Перезагрузите страницу и попробуйте еще раз.")
                setErrorFlag(flag => !flag)
            }
        }
    }


    async function getAllWashingServices() {
        try {
            const response = await getAllWashingServicesWithPriceAndTime();
            const ordersMain = response.map(item => ({
                ...item,
                name: item.name.replace(/_/g, ' ')
            }));
            setMainOrders(ordersMain);
        } catch (error) {
            if (error.response) {
                alert(error.response.data.message)
            } else {
                alert("Системная ошибка, попробуйте позже")
            }
        }
    }

    async function getAllTireServices() {
        try {
            const response = await getAllTireServicesWithPriceAndTime();
            const filteredOrdersMain = response.map(item => ({
                ...item,
                name: item.name.replace(/_/g, ' ')
            }));
            setMainTireOrders(filteredOrdersMain)
        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join(''));  // Объединяем все сообщения об ошибках через запятую
                setErrorFlag(flag => !flag);

            } else {
                setErrorResponse("Системная ошибка. " +
                    "Попробуйте еще раз")
                setErrorFlag(flag => !flag)
            }
        }
    }

    useEffect(() => {
        if (orderType) {
            if (russianToEnglishMap[orderType].includes("wash")) {
                getAllWashingServices();
            } else if (russianToEnglishMap[orderType].includes("polish")) {
                getAllPolishingServices();
            } else if (russianToEnglishMap[orderType].includes("tire")) {
                getAllTireServices();
            }
        }
    }, [orderType]);

    useEffect(() => {
        const carCode = mapCarTypeToCode(carTypeMap);
        setCarType(carCode);
    }, [carTypeMap]);


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

    const errorIdMessage = (
        <Notification
            type="error"
            header="Ошибка!"
            closable
            style={{border: '1px solid black'}}
        >
            <div style={{width: 320}}>
                <p>Пожалуйста, введите номер заказа.</p>
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

    useEffect(() => {
        if (successResponse) {
            toaster.push(successMessage, {placement: "bottomEnd"});
        }
    }, [successResponse]);

    const sendUpdateRequest = async (e) => {
        e.preventDefault();
        if (showConfirmationUpdateOrder && isNumberString(orderId)) {
            try {
                const data = await updateOrderInfo(orderId, userPhone,
                    russianToEnglishMap[orderType], price, wheelR,
                    startTime.toISOString(), administrator,
                    autoNumber, carType, specialist, boxNumber, bonuses,
                    comments, endTime.toISOString(),
                    selectedItems.map(i => i.replace(/ /g, '_')),
                    currentOrderStatusMapFromRus[currentStatus], selectedSaleDescription);
                setSuccessResponse(data.message)
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
            setShowConfirmationUpdateOrder(false)
        } else {
            setShowConfirmationUpdateOrder(true)
        }
    };

    function isNumberString(str) {
        return /^[0-9]+$/.test(str);
    }

    useEffect(() => {
        if (showConfirmation && !isNumberString(orderId)) {
            setShowConfirmation(false)
            toaster.push(errorIdMessage, {placement: "bottomEnd"});
        }
    }, [showConfirmation]);

    const deleteOrder = async (e) => {
        e.preventDefault();
        if (showConfirmation && isNumberString(orderId)) {
            try {
                const data = await deleteOrderById(orderId);
                setSuccessResponse(data.message)
            } catch
                (error) {
                if (error.response) {
                    setErrorResponse(error.response.data.message)
                    setErrorFlag(flag => !flag)
                } else {
                    setErrorResponse("Системная ошибка с удалением заказа. " +
                        "Проверьте правильность введённой информации и попробуйте еще раз")
                    setErrorFlag(flag => !flag)
                }
            }
            setShowConfirmation(false);
        } else {
            setShowConfirmation(true);
        }
    };

    const handleFileChange = (selectedValue) => {
        const selectedFile = files.find(file => file.id === selectedValue);
        setSelectedSaleDescription(selectedFile.description);
        setSelectedSaleId(selectedFile.id);
    }

    const predefinedBottomRanges = [
        {
            label: 'Вчера',
            value: addDays(new Date(), -1),
        },
        {
            label: 'Сегодня',
            value: new Date(),
        },
        {
            label: 'Завтра',
            value: addDays(new Date(), 1),
        }
    ];


    return (
        <>
            <p className="input-style-modified">Страница изменения информации о заказе</p>
            <p className="small-input-style">Вы можете открыть таблицу с заказами за какой-то день,
                выбрать там заказ, информацию о котором хотите обновить, а он сам окажется здесь</p>


            <Form onSubmit={sendUpdateRequest}>
                <InputField
                    className="input-style"
                    label='Айди заказа'
                    id='orderId'
                    value={orderId}
                    onChange={setOrderId}
                />
                <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                    Выберите услуги для поставленного типа заказа
                </Button>
                <Modal show={showModal}
                       onHide={handleCloseModal}
                       dialogClassName="custom-modal-dialog-polishing">
                    <Modal.Header closeButton>
                        <Modal.Title>Выберите заказы</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
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
                                                 onChange={value => handleItemChange(item.name, value)}
                                                 value={getItemValueByName(item.name) || 0}/>
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

                <MyCustomModal show={showModalB} handleClose={handleCloseModal} title="Выберите заказы">
                    <div style={{overflowY: 'auto', maxHeight: '80vh'}}>
                        {mainTireOrders.map((item, index) => (
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
                            Выбранные услуги:
                        </Form.Label>
                        <div className="selected-items">
                            {selectedItems
                                .filter((item, index) => selectedItems.indexOf(item) === index)
                                .map((item) => {
                                    if (getItemValueByName(item) > 0) {
                                        return (
                                            <span key={item} className="item">
                  {`${item} (${getItemValueByName(item) || 0})`}
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
                            Выбранные услуги:
                        </Form.Label>
                        <div className='selected-items-container text-center'>
                        <span className='empty-list' style={{fontSize: '1.1em'}}>
                            Нет выбранных услуг
                        </span>
                        </div>
                    </div>)}

                <Divider></Divider>

                <InputField
                    className="input-style"
                    label='Телефон пользователя'
                    id='userPhone'
                    value={userPhone}
                    onChange={setUserPhone}
                />

                <p style={{
                    fontWeight: 'bold', display: 'flex',
                    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '10px'
                }}>Тип услуги</p>

                <InputPicker
                    data={serviceTypesArray}
                    value={orderType}
                    onChange={setOrderType}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    menuStyle={{fontSize: "17px"}}
                />

                <InputField
                    className="input-style"
                    label='Цена за заказ (целое число)'
                    id='price'
                    value={price}
                    onChange={setPrice}
                />
                <InputField
                    className="input-style"
                    label='Администратор'
                    id='administrator'
                    value={administrator}
                    onChange={setAdministrator}
                />
                <InputField
                    className="input-style"
                    label='Специалист'
                    id='specialist'
                    value={specialist}
                    onChange={setSpecialist}
                />
                <p className="input-style">Выберите тип кузова</p>
                <InputPicker
                    data={carTypesArray}
                    value={carTypeMap}
                    onChange={setCarTypeMap}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    menuStyle={{fontSize: "17px"}}
                />
                <p style={{
                    fontWeight: 'bold', display: 'flex',
                    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '15px'
                }}>Выберите размер колёс</p>
                <InputPicker
                    data={wheelSizeArray}
                    value={wheelR}
                    onChange={setWheelR}
                    style={{
                        width: 500,
                        display: 'block',
                        marginBottom: 10,
                        marginLeft: 'auto',
                        marginRight: 'auto',
                        marginTop: 10,
                        WebkitTextFillColor: "#000000"
                    }}
                    menuStyle={{fontSize: "17px"}}
                />
                <InputField
                    className="input-style"
                    label='Номер автомобиля'
                    id='autoNumber'
                    value={autoNumber}
                    onChange={setAutoNumber}
                />
                <InputField
                    className="input-style"
                    label='Номер бокса'
                    id='boxNumber'
                    value={boxNumber}
                    onChange={setBoxNumber}
                />
                <p className="input-style">Время начала заказа</p>

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
                    format="yyyy-MM-dd HH:mm"
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

                <p className="input-style">Время конца заказа</p>
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
                    format="yyyy-MM-dd HH:mm"
                    oneTap
                    ranges={predefinedBottomRanges}
                    block
                    appearance="default"
                    value={endTime}
                    onChange={setEndTime}
                    style={{
                        width: 500,
                        marginLeft: 'auto',
                        marginRight: 'auto',
                        marginTop: 10,
                        WebkitTextFillColor: "#000000",
                    }}
                />
                <InputField
                    className="input-style"
                    label='Комментарии клиента'
                    id='comments'
                    value={comments}
                    onChange={setComments}
                />
                <p style={{
                    fontWeight: 'bold', display: 'flex',
                    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '15px'
                }}>Выберите новое состояние заказа</p>

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
                    value={selectedSaleId}
                    menuStyle={{fontSize: "17px"}}
                    onChange={handleFileChange}
                />


                <InputField
                    className="input-style"
                    label='Использованные клиентом бонусы'
                    id='bonuses'
                    value={bonuses}
                    onChange={setBonuses}
                />
                {showConfirmationUpdateOrder && (
                    <div className='confirmation-container'>
                        <div className='confirmation-message'>
                            <p className="input-style">Вы уверены, что хотите изменить информацию об этом заказе?</p>
                            <p>Это изменит информацию об этом заказе ВО ВСЕЙ базе данных для ВСЕХ</p>
                            <p>Пожалуйста, предварительно спросите у клиента разрешение на изменение его заказа</p>
                            <div className='confirmation-buttons'>
                                <Button onClick={() => setShowConfirmationUpdateOrder(false)}
                                        style={{marginRight: '10px', marginTop: '10px'}}>
                                    Отменить
                                </Button>
                                <Button variant='primary' style={{marginLeft: '10px', marginTop: '10px'}} type='submit'
                                        onSubmit={sendUpdateRequest}>
                                    Подтвердить
                                </Button>
                            </div>
                        </div>
                    </div>
                )}

                <div className='submit-container'>
                    <Button className='btn-submit' variant='primary'
                            type='submit' style={{marginBottom: '20px', marginTop: '20px'}}>
                        Изменить информацию
                    </Button>
                </div>
            </Form>
            {showConfirmation && (
                <div className='confirmation-container'>
                    <div className='confirmation-message'>
                        <p className="input-style">Вы уверены, что хотите удалить заказ?</p>
                        <p>Это изменит информацию об этом заказе ВО ВСЕЙ базе данных для ВСЕХ</p>
                        <p>Пожалуйста, предварительно спросите у клиента разрешение на удаление его заказа</p>
                        <div className='confirmation-buttons'>
                            <Button onClick={() => setShowConfirmation(false)}
                                    style={{marginRight: '10px', marginTop: '10px'}}>
                                Отменить
                            </Button>
                            <Button variant='primary' style={{marginLeft: '10px', marginTop: '10px'}} type='submit'
                                    onClick={deleteOrder}>
                                Подтвердить
                            </Button>
                        </div>
                    </div>
                </div>
            )}
            <div className='submit-container'>
                <Button
                    className='btn-delete'
                    variant='danger'
                    type='submit'
                    disabled={isSubmitting || Date.now() < submitTime + 4000}
                    onClick={deleteOrder}
                    style={{marginBottom: '20px', marginTop: '20px'}}
                >
                    {isSubmitting ? 'Обработка запроса...' : 'Удалить заказ'}
                </Button>
            </div>
        </>
    );
});

export default UpdateOrderInfo;