import React, {useEffect, useState} from 'react';
import '../css/MyTable.css';
import {useSortBy, useTable} from 'react-table';
import {format, parseISO} from 'date-fns';
import {getNotMadeOrders, getOrdersBookedInOneDay, getOrdersCreatedInOneDay} from "../http/orderAPI";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {BrowserRouter as Router, useHistory} from "react-router-dom";
import {DatePicker, Divider, Notification, useToaster} from "rsuite";
import addDays from "date-fns/addDays";
import {Button} from "react-bootstrap";
import socketStore from "../store/SocketStore";
import {observer} from "mobx-react-lite";
import currentOrderStatusMapFromEng from "../model/map/CurrentOrderStatusMapFromEng";

const inputStyle = {
    fontWeight: 'bold', display: 'flex',
    fontSize: '17px', justifyContent: 'center', alignItems: 'center', marginTop: '5px'
}


const columns = [
    {
        Header: 'Айди',
        accessor: 'id',
        sortType: 'alphanumeric',
        Cell: ({value}) => {
            const history = useHistory();
            return <div onClick={() => history.push(`/updateOrderInfo/${value}`)}>{value}</div>;
        }
    },
    {
        Header: 'Дата и время начала',
        accessor: 'startTime',
        Cell: ({value}) => (
            value ? (
                <div>
                    <span>{format(parseISO(value), 'dd.MM.yyyy ')}</span>
                    <span style={{fontWeight: 'bold'}}>{format(parseISO(value), 'HH:mm:ss')}</span>
                </div>
            ) : 'Неизвестно'
        ),
        sortType: (rowA, rowB, columnId) => {
            const dateA = parseISO(rowA.values[columnId]);
            const dateB = parseISO(rowB.values[columnId]);

            return dateA.getTime() - dateB.getTime();
        },
    },
    {
        Header: 'Дата и время конца',
        accessor: 'endTime',
        Cell: ({value}) => (
            value ? (
                <div>
                    <span>{format(parseISO(value), 'dd.MM.yyyy ')}</span>
                    <span style={{fontWeight: 'bold'}}>{format(parseISO(value), 'HH:mm:ss')}</span>
                </div>
            ) : 'Неизвестно'
        ),
        sortType: (rowA, rowB, columnId) => {
            const dateA = parseISO(rowA.values[columnId]);
            const dateB = parseISO(rowB.values[columnId]);

            return dateA.getTime() - dateB.getTime();
        },
    },
    {
        Header: 'Тип заказа',
        accessor: 'orderType',
        sortType: 'alphanumeric',
        Cell: ({value}) => value ? orderTypeMap[value] || value : "Неизвестно"
    },
    {
        Header: 'Клиент',
        sortType: 'alphanumeric',
        accessor: 'userNumber',
        Cell: ({value}) => {
            const history = useHistory();
            return <div onClick={() => history.push(`/changeUserInfo/${value}`)}>{value}</div>;
        }
    },
    {
        Header: 'Взятые услуги',
        sortType: 'alphanumeric',
        accessor: 'orders',
        Cell: ({value}) => {
            return value.join(', ');
        }
    },
    {
        Header: 'Номер авто', accessor: 'autoNumber',
        sortType: 'alphanumeric',
        Cell: ({value}) => {
            return value === null ? 'Неизвестно' : value;
        }
    },
    {
        Header: 'Тип кузова', accessor: 'autoType', sortType: 'basic',
        Cell: ({value}) => {
            return value === null ? 'Неизвестно' : value;
        }
    },
    {
        Header: 'Текущие состояние заказа',
        accessor: 'currentStatus',
        sortType: 'basic',
        Cell: ({value}) => {
            const textColor =
                value && value.includes('NotDone') ? 'red' :
                    value && value.includes('cancelled') ? 'blue' :
                        value && value.includes('Done') ? '#008000' : // темно-зеленый
                            'brown';
            return (
                <div style={{color: textColor}}>
                    {value ? currentOrderStatusMapFromEng[value] || value : "Неизвестно"}
                </div>
            );
        }
    },
    {
        Header: 'Админ',
        accessor: 'administrator',
        sortType: 'basic',
        Cell: ({value}) => {
            return value === null ? 'Неизвестно' : value;
        }
    },
    {
        Header: 'Специалист', accessor: 'specialist',
        sortType: 'basic',
        Cell: ({value}) => {
            return value === null ? 'Неизвестно' : value;
        }
    },
    {
        Header: 'Бокс',
        accessor: 'boxNumber',
        sortType: (rowA, rowB) => {
            const boxNumberA = rowA.original.boxNumber;
            const boxNumberB = rowB.original.boxNumber;

            if (boxNumberA === null && boxNumberB === null) {
                return 0;
            } else if (boxNumberA === null) {
                return 1;
            } else if (boxNumberB === null) {
                return -1;
            } else {
                return boxNumberA - boxNumberB;
            }
        },
        Cell: ({value}) => {
            return value === null ? 'Неизвестно' : Number(value);
        },
    },
    {
        Header: 'Комментарии', accessor: 'comments',
        sortType: 'basic',
        Cell: ({value}) => {
            return value === null ? 'Отсутствуют' : value;
        }
    },
    {
        Header: 'Дата создания заказа',
        accessor: 'dateOfCreation',
        Cell: ({value}) => (
            value ? (
                <div>
                    <span>{format(parseISO(value), 'dd.MM.yyyy ')}</span>
                    <span style={{fontWeight: 'bold'}}>{format(parseISO(value), 'HH:mm:ss')}</span>
                </div>
            ) : 'Неизвестно'
        ),
        sortType: (rowA, rowB, columnId) => {
            const dateA = parseISO(rowA.values[columnId]);
            const dateB = parseISO(rowB.values[columnId]);

            return dateA.getTime() - dateB.getTime();
        },
    },
    {
        Header: 'Акция',
        accessor: 'sale',
        sortType: 'alphanumeric',
        Cell: ({value}) => value ? value || value : "Неизвестно"
    },
    {
        Header: 'Цена',
        accessor: 'price',
        sortType: (rowA, rowB) => rowA.original.price - rowB.original.price,
        Cell: ({value}) => Number(value),
    },
];


const OrderTable = observer(() => {

    const [orders, setOrders] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [selectedDate, setSelectedDate] = useState(new Date());
    const start = new Date(selectedDate);
    const end = new Date(selectedDate);
    const [includeCancelled, setIncludeCancelled] = useState(false);

    const [errorResponse, setErrorResponse] = useState("");
    const [errorFlag, setErrorFlag] = useState(false);
    const [successResponse, setSuccessResponse] = useState();
    const toaster = useToaster();
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);

    const getOrderBookedOnThisDay = async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        try {
            const response = await getOrdersBookedInOneDay(start.toISOString(), end.toISOString(), includeCancelled);
            setOrders(response);

            setSuccessResponse(null)
            setSuccessResponse("Yes all orders")
        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join('\n'));
                setErrorFlag(flag => !flag);

            } else {
                setErrorResponse("Системная ошибка.\n" +
                    "Перезагрузите страницу и повторите попытку")
                setErrorFlag(flag => !flag)
            }
        } finally {
            setIsSubmitting(false)
        }
    };

    const getOrdersCreatedAtThisDay = async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        try {
            const response = await getOrdersCreatedInOneDay(start.toISOString(), end.toISOString(), includeCancelled);
            setOrders(response);

            setSuccessResponse(null)
            setSuccessResponse("Yes all orders")
        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join('\n'));
                setErrorFlag(flag => !flag);

            } else {
                setErrorResponse("Системная ошибка.\n" +
                    "Перезагрузите страницу и повторите попытку")
                setErrorFlag(flag => !flag)
            }
        } finally {
            setIsSubmitting(false)
        }
    };

    const successMessage = (
        <Notification
            type="success"
            header="Успешно!"
            closable
            style={{border: '1px solid black'}}
        >
            <div style={{width: 320, whiteSpace: "pre-line"}}>
                <p>Информация успешно получена из базы данных</p>
            </div>
        </Notification>
    );
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

    const handleNotMadeOrders = async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);

        try {
            const response = await getNotMadeOrders(includeCancelled);
            setOrders(response);
            setSuccessResponse(null)
            setSuccessResponse("Yes not made orders")
        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join('\n'));  // Объединяем все сообщения об ошибках через запятую
                setErrorFlag(flag => !flag);

            } else {
                setErrorResponse("Системная ошибка. " +
                    "Еще не выполненные заказы не были получены.")
                setErrorFlag(flag => !flag)
            }
        } finally {
            setIsSubmitting(false)
        }
    };

    const {getTableProps, getTableBodyProps, headerGroups, rows, prepareRow} = useTable({
        columns,
        data: orders,
    }, useSortBy);


    const predefinedBottomRanges = [
        {
            label: 'Позавчера',
            value: addDays(new Date(), -2),
        },
        {
            label: 'Вчера',
            value: addDays(new Date(), -1),
        },
        {
            label: 'Сегодня',
            value: new Date(),
        }
    ];

    return (
        <div>
            <p style={{
                fontWeight: 'bold', display: 'flex', fontSize: '17px', justifyContent: 'center',
                alignItems: 'center', marginTop: '15px'
            }}>Выберите день заказа</p>

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
                value={selectedDate}
                onChange={setSelectedDate}
                style={{
                    width: 500,
                    marginLeft: 'auto',
                    marginRight: 'auto',
                    marginTop: 10,
                    WebkitTextFillColor: "#000000",
                }}
            />


            <div style={{display: 'flex', alignItems: 'center', justifyContent: 'center', marginTop: '10px'}}>
                <input
                    type="checkbox"
                    id="includeCancelledCheckbox"
                    checked={includeCancelled}
                    onChange={(e) => setIncludeCancelled(e.target.checked)}
                />
                <label htmlFor="includeCancelledCheckbox" style={{marginLeft: '10px'}}>
                    Учитывать ли отменённые заказы?
                </label>
            </div>

            <Button
                className='btn-submit'
                variant='primary'
                type='submit'
                onClick={getOrderBookedOnThisDay}
                disabled={isSubmitting}
                style={{marginBottom: '20px', marginTop: '20px'}}>
                {isSubmitting ? 'Поиск заказов...' : 'Получить все заказы, забронированные на этот день'}
            </Button>

            <Button
                className='btn-submit'
                variant='primary'
                type='submit'
                onClick={getOrdersCreatedAtThisDay}
                disabled={isSubmitting}
                style={{marginBottom: '20px', marginTop: '20px'}}>
                {isSubmitting ? 'Поиск заказов...' : 'Получить все заказы, созданные в этот день'}
            </Button>

            <Button
                className='btn-submit'
                variant='primary'
                type='submit'
                onClick={handleNotMadeOrders}
                disabled={isSubmitting}
                style={{marginBottom: '20px', marginTop: '20px'}}>
                {isSubmitting ? 'Поиск заказов...' : 'Получить все не сделанные заказы'}
            </Button>

            <p style={inputStyle}>Вы можете нажать на цифру айди, чтобы перейти на страницу изменения этого заказа</p>
            <Divider></Divider>

            <table {...getTableProps()} className="MyTable" style={{marginBottom: '100px'}}>
                <thead>
                {headerGroups.map((headerGroup) => (
                    <tr {...headerGroup.getHeaderGroupProps()}>
                        {headerGroup.headers.map((column) => (
                            <th {...column.getHeaderProps(column.getSortByToggleProps())}>
                                {column.render('Header')}
                                <span>
              {column.isSorted ? (column.isSortedDesc ? ' 🔽' : ' 🔼') : ''}
            </span>
                            </th>
                        ))}
                    </tr>
                ))}
                </thead>
                <tbody {...getTableBodyProps()}>
                {rows.map((row) => {
                    prepareRow(row);
                    return (
                        <tr {...row.getRowProps()}>
                            {row.cells.map((cell) => {
                                return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>;
                            })}
                        </tr>
                    );
                })}
                </tbody>
            </table>
        </div>
    );
});

export default OrderTable;