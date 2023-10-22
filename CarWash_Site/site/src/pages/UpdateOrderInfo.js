import '../css/CreatingOrder.css';
import '../css/CommonStyles.css';
import React, {useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import InputField from "../model/InputField";
import {BrowserRouter as Router, useParams} from "react-router-dom";
import {deleteOrderById, getAllServicesWithPriceAndTime, getOrderInfo, updateOrderInfo} from "../http/orderAPI";
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
import {carTypesArray, orderStatusArray, serviceTypesArray} from "../model/Constants";
import MyCustomModal from "../model/MyCustomModal";
import saleStore from "../store/SaleStore";
import ModalContent from "../model/ModalContent"
import InputFieldPriceTimeNumber from "../model/InputFieldPriceTimeNumber";

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

    const [selectedItems, setSelectedItems] = useState([{
        name: null, priceFirstType: null, priceSecondType: null, priceThirdType: null,
        timeFirstType: null, timeSecondType: null, timeThirdType: null, price_r_13: null,
        price_r_14: null, price_r_15: null, price_r_16: null, price_r_17: null, price_r_18: null,
        price_r_19: null, price_r_20: null, price_r_21: null, price_r_22: null, time_r_13: null,
        time_r_14: null, time_r_15: null, time_r_16: null, time_r_17: null, time_r_18: null,
        time_r_19: null, time_r_20: null, time_r_21: null, time_r_22: null, type: null, number: null
    }]);


    const [boxNumber, setBoxNumber] = useState(0);
    const [bonuses, setBonuses] = useState(0);
    const [comments, setComments] = useState('');

    const [selectedSale, setSelectedSale] = useState(null);

    const [selectedSaleDescription, setSelectedSaleDescription] = useState('');
    const [files, setFiles] = useState([]);
    const [allServices, setAllServices] = useState([]);

    const [errorResponse, setErrorResponse] = useState("");
    const [errorFlag, setErrorFlag] = useState(false);

    const [successResponse, setSuccessResponse] = useState();

    const [isSubmitting] = useState(false);
    const [submitTime] = useState(0);
    const [showConfirmation, setShowConfirmation] = useState(false);

    const [showConfirmationUpdateOrder, setShowConfirmationUpdateOrder] = useState(false);


    const {id} = useParams();
    const [orderId, setOrderId] = useState('')

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
            saleStore.error = null;
        }
    }, [saleStore?.error]);


    const saleOptions = files.map(file => ({
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
                selectedItem.price_r_13 = updatedData.price_r_13;
                selectedItem.price_r_14 = updatedData.price_r_14;
                selectedItem.price_r_15 = updatedData.price_r_15;
                selectedItem.price_r_16 = updatedData.price_r_16;
                selectedItem.price_r_17 = updatedData.price_r_17;
                selectedItem.price_r_18 = updatedData.price_r_18;
                selectedItem.price_r_19 = updatedData.price_r_19;
                selectedItem.price_r_20 = updatedData.price_r_20;
                selectedItem.price_r_21 = updatedData.price_r_21;
                selectedItem.price_r_22 = updatedData.price_r_22;
                selectedItem.time_r_13 = updatedData.time_r_13;
                selectedItem.time_r_14 = updatedData.time_r_14;
                selectedItem.time_r_15 = updatedData.time_r_15;
                selectedItem.time_r_16 = updatedData.time_r_16;
                selectedItem.time_r_17 = updatedData.time_r_17;
                selectedItem.time_r_18 = updatedData.time_r_18;
                selectedItem.time_r_19 = updatedData.time_r_19;
                selectedItem.time_r_20 = updatedData.time_r_20;
                selectedItem.time_r_21 = updatedData.time_r_21;
                selectedItem.time_r_22 = updatedData.time_r_22;
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
                    price_r_13: updatedData.price_r_13,
                    price_r_14: updatedData.price_r_14,
                    price_r_15: updatedData.price_r_15,
                    price_r_16: updatedData.price_r_16,
                    price_r_17: updatedData.price_r_17,
                    price_r_18: updatedData.price_r_18,
                    price_r_19: updatedData.price_r_19,
                    price_r_20: updatedData.price_r_20,
                    price_r_21: updatedData.price_r_21,
                    price_r_22: updatedData.price_r_22,
                    time_r_13: updatedData.time_r_13,
                    time_r_14: updatedData.time_r_14,
                    time_r_15: updatedData.time_r_15,
                    time_r_16: updatedData.time_r_16,
                    time_r_17: updatedData.time_r_17,
                    time_r_18: updatedData.time_r_18,
                    time_r_19: updatedData.time_r_19,
                    time_r_20: updatedData.time_r_20,
                    time_r_21: updatedData.time_r_21,
                    time_r_22: updatedData.time_r_22,
                    number: updatedData.number
                };
                updatedItems.push(newItem);
                return updatedItems;
            }
        });
    };


    const handleItemChange = (itemName, value) => {
        const item = getItemValueByNameInAllServices(itemName);
        if (!item) {
            return;
        }
        if (value === '0') {
            const updatedSelectedItems = selectedItems.filter(selectedItem => selectedItem.name !== itemName);
            setSelectedItems(updatedSelectedItems);
        } else {
            const updatedData = {
                priceFirstType: item.priceFirstType,
                priceSecondType: item.priceSecondType,
                priceThirdTypeType: item.priceThirdType,
                timeFirstType: item.timeFirstType,
                timeSecondType: item.timeSecondType,
                timeThirdTypeType: item.timeThirdType,
                price_r_13: item.price_r_13,
                price_r_14: item.price_r_14,
                price_r_15: item.price_r_15,
                price_r_16: item.price_r_16,
                price_r_17: item.price_r_17,
                price_r_18: item.price_r_18,
                price_r_19: item.price_r_19,
                price_r_20: item.price_r_20,
                price_r_21: item.price_r_21,
                price_r_22: item.price_r_22,
                time_r_13: item.time_r_13,
                time_r_14: item.time_r_14,
                time_r_15: item.time_r_15,
                time_r_16: item.time_r_16,
                time_r_17: item.time_r_17,
                time_r_18: item.time_r_18,
                time_r_19: item.time_r_19,
                time_r_20: item.time_r_20,
                time_r_21: item.time_r_21,
                time_r_22: item.time_r_22,
                number: value
            };

            updateSelectedItems(item.name, updatedData);
        }
    };

    const getItemValueByNameInAllServices = (name) => {
        const item = allServices.find(item => item.name === name);
        return item || undefined;
    }

    const getValueByNameInSelectedItems = (name) => {
        const item = selectedItems.find(item => item.name === name);
        return item ? item.number : undefined;
    }

    async function getThisOrderInfo(currentId) {
        await getAllServices();
        try {
            if (!isNaN(currentId) && String(currentId).trim() !== '') {
                const response = await getOrderInfo(parseInt(currentId));

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
                setErrorResponse(messages.join('\n'));
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

    async function getAllServices() {
        const allServicesResponse = await getAllServicesWithPriceAndTime();
        const filteredServices = allServicesResponse.map(item => ({
            ...item,
            name: item.name.replace(/_/g, ' '),
        }));

        setAllServices([...filteredServices]);
        return true;
    }


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


    const successMessage = (
        <Notification
            type="success"
            header="Успешно!"
            closable
            style={{border: '1px solid black'}}
        >
            <div style={{width: 320, whiteSpace: "pre-line"}}>
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
                const prepareSelectedItems = (selectedItems) => {
                    return selectedItems
                        .filter(item => item && item.name !== null)
                        .reduce((acc, item) => {
                            const names = Array(parseInt(item.number, 10)).fill(item.name.replace(/ /g, '_'));
                            return acc.concat(names);
                        }, []);
                };
                const filteredAndFormattedItems = prepareSelectedItems(selectedItems);

                const data = await updateOrderInfo(orderId, userPhone, russianToEnglishMap[orderType],
                    price, wheelR, startTime.toISOString(), administrator, autoNumber, carType, specialist,
                    boxNumber, bonuses, comments, endTime.toISOString(), filteredAndFormattedItems,
                    currentOrderStatusMapFromRus[currentStatus], selectedSaleDescription);

                setSuccessResponse(data.message);
            } catch (error) {
                if (error.response) {
                    let messages = [];
                    for (let key in error.response.data) {
                        messages.push(error.response.data[key]);
                    }
                    setErrorResponse(messages.join('\n'));
                    setErrorFlag(flag => !flag);

                } else {
                    setErrorResponse("Системная ошибка с загрузкой файлов. " +
                        "Перезагрузите страницу и попробуйте еще раз")
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
            } catch (error) {
                if (error.response) {
                    let messages = [];
                    for (let key in error.response.data) {
                        messages.push(error.response.data[key]);
                    }
                    setErrorResponse(messages.join('\n'));
                    setErrorFlag(flag => !flag);

                } else {
                    setErrorResponse("Системная ошибка с загрузкой файлов. " +
                        "Перезагрузите страницу и попробуйте еще раз")
                    setErrorFlag(flag => !flag)
                }
            }
            setShowConfirmation(false);
        } else {
            setShowConfirmation(true);
        }
    };

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

    const handleOpenModal = () => {
        if (orderType.includes("Шино")) {
            setShowModalB(true);
        } else {
            setShowModal(true);
        }
    };

    const handleCloseModal = () => {
        setShowModalB(false);
        setShowModal(false);
    };


    return (
        <>
            <p className="input-style-modified">Страница изменения информации о заказе</p>
            <p className="small-input-style">Вы можете открыть таблицу с заказами за какой-то день,
                выбрать там заказ, информацию о котором хотите обновить, а он сам окажется здесь</p>
            <Form onSubmit={sendUpdateRequest}>
                <InputFieldPriceTimeNumber
                    className="input-style"
                    label='Айди заказа'
                    id='orderId'
                    value={orderId}
                    onChange={setOrderId}
                />
                <Button className='full-width' variant='secondary' onClick={handleOpenModal}>
                    Выберите услуги для поставленного типа заказа
                </Button>
                <Modal show={showModal} onHide={handleCloseModal} dialogClassName="custom-modal-dialog-polishing">
                    <Modal.Header closeButton>
                        <Modal.Title>Выберите заказы</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {orderType.includes("Мойка") && allServices.length > 0 &&
                            <ModalContent filterType="Wash" handleItemChange={handleItemChange}
                                          getValueByNameInSelectedItems={getValueByNameInSelectedItems}
                                          allServices={allServices}/>
                        }
                        {orderType.includes("Полировка") && allServices.length > 0 &&
                            <ModalContent filterType="Polish" handleItemChange={handleItemChange}
                                          getValueByNameInSelectedItems={getValueByNameInSelectedItems}
                                          allServices={allServices}/>
                        }
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant='secondary' onClick={handleCloseModal}>
                            Закрыть
                        </Button>
                    </Modal.Footer>
                </Modal>

                <MyCustomModal show={showModalB} handleClose={handleCloseModal} title="Выберите заказы">
                    <div style={{overflowY: 'auto', maxHeight: '80vh'}}>
                        {orderType.includes("Шиномон") && allServices.length > 0
                            && allServices.filter(item => item.type.includes("Tire")).map((item, index) => (
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
                                            size="sm" placeholder="sm"
                                            style={Object.assign({}, stylesForInput, {margin: '0 auto'})}
                                            min={0}
                                            onChange={value => handleItemChange(item.name, value)}
                                            value={getValueByNameInSelectedItems(item.name) || 0}
                                        />
                                    </div>
                                </div>
                            ))}
                    </div>
                </MyCustomModal>

                {selectedItems.length > 0 ? (
                    <div className="text-center">
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
                    <div className='text-center'>
                        <Form.Label style={{fontWeight: 'bold', fontSize: '1.2em'}}>
                            Выбранные услуги:
                        </Form.Label>
                        <div className='text-center'>
            <span className='empty-list' style={{fontSize: '1.1em'}}>
                Нет выбранных услуги
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
                    maxLength={50}
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
                    maxLength={30}
                />

                <InputFieldPriceTimeNumber
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
                    maxLength={50}
                />
                <InputField
                    className="input-style"
                    label='Специалист'
                    id='specialist'
                    value={specialist}
                    maxLength={50}
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
                    maxLength={50}
                    onChange={setAutoNumber}
                />
                <InputFieldPriceTimeNumber
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
                    maxLength={255}
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
                    data={saleOptions}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    value={selectedSale}
                    menuStyle={{fontSize: "17px"}}
                    onChange={(selectedValue) => {
                        const newSelectedSale = files.find(file => file.id === selectedValue);
                        setSelectedSale(selectedValue); // сохраняем ID файла
                        setSelectedSaleDescription(newSelectedSale.description);
                    }}
                />

                <InputFieldPriceTimeNumber
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