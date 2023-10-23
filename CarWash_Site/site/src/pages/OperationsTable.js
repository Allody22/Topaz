import React, {useEffect, useState} from 'react';
import '../css/MyTable.css';
import {useSortBy, useTable} from 'react-table';
import {format, parseISO} from 'date-fns';
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {BrowserRouter as Router} from "react-router-dom";
import {DatePicker, InputPicker, Notification, useToaster} from "rsuite";
import addDays from "date-fns/addDays";
import {Button} from "react-bootstrap";
import socketStore from "../store/SocketStore";
import {observer} from "mobx-react-lite";
import {
    getAllNamesOperations,
    getAllOperations,
    getAllOperationsByOperationName,
    getAllOperationsByUser,
    getAllOperationsInOneDay
} from "../http/operations";
import {getAllUsers} from "../http/userAPI";
import operationsNameMapFromEng from "../model/map/OperationsNameMapFromEng";

const columns = [
    {
        Header: 'Айди',
        accessor: 'id',
        sortType: 'alphanumeric',
    },
    {
        Header: 'Номер телефона',
        sortType: 'alphanumeric',
        accessor: 'username'
    },
    {
        Header: 'Описание',
        sortType: 'alphanumeric',
        accessor: 'description',
    },
    {
        Header: 'Дата и время операции',
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
    }
];


const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};

const OperationsTable = observer(() => {

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [selectedDate, setSelectedDate] = useState(new Date());
    const start = new Date(selectedDate);
    const end = new Date(selectedDate);

    const [userPhones, setUserPhones] = useState([]);


    const [operationsName, setOperationsName] = useState([]);
    const [currentOperation, setCurrentOperation] = useState('');


    const [userContacts, setUserContacts] = useState('');


    const [operations, setOperations] = useState([]);

    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);
    const [successResponse, setSuccessResponse] = useState();
    const toaster = useToaster();
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);

    const getOperationsInDay = async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        try {
            const response = await getAllOperationsInOneDay(start.toISOString(), end.toISOString());
            setOperations(response)

            setSuccessResponse(null)
            setSuccessResponse("Все операции успешно получены!")
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

    const getAllInAllTimeOperations = async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        try {
            const response = await getAllOperations(start.toISOString(), end.toISOString());

            setOperations(response)

            setSuccessResponse(null)
            setSuccessResponse("Yes all orders")
        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join('\n'));  // Объединяем все сообщения об ошибках через запятую
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


    const getAllUserOperations = async (event) => {
        event.preventDefault();
        if (userContacts === '' || !userContacts) {
            setErrorResponse("Пожалуйста, введите телефон пользователя.")
            setErrorFlag(flag => !flag)
            return;
        }
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        try {
            const response = await getAllOperationsByUser(userContacts);
            setOperations(response)

            setSuccessResponse(null)
            setSuccessResponse("Yes all orders")
        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join('\n'));  // Объединяем все сообщения об ошибках через запятую
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


    const getAllOperationsByName = async (event) => {
        event.preventDefault();

        if (currentOperation === '') {
            setErrorResponse("Пожалуйста, выберите операцию.")
            setErrorFlag(flag => !flag)
            return;
        }
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        try {
            const response = await getAllOperationsByOperationName(currentOperation);
            setOperations(response)

            setSuccessResponse(null)
            setSuccessResponse("Yes all orders")
        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                setErrorResponse(messages.join('\n'));  // Объединяем все сообщения об ошибках через запятую
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

    useEffect(() => {
        async function getAllPeopleInformation() {
            try {
                const response = await getAllUsers();

                const transformedOperations = response.map(name => ({label: name, value: name}));
                setUserPhones(transformedOperations)
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

        getAllPeopleInformation();
    }, []);


    useEffect(() => {
        async function getAllOperationNames() {
            try {
                const response = await getAllNamesOperations();

                const transformedOperations = response.map(name => {
                    const translatedName = operationsNameMapFromEng[name] || name; // используйте имя из словаря или оригинальное имя, если перевода нет
                    return {label: translatedName, value: name};
                });

                setOperationsName(transformedOperations);
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

        getAllOperationNames();
    }, []);

    const successMessage = (
        <Notification
            type="success"
            header="Успешно!"
            closable
            style={{border: '1px solid black'}}
        >
            <div style={{width: 320, whiteSpace: 'pre-line'}}>
                <p>Информация успешно получена из базы данных</p>
            </div>
        </Notification>
    );

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
            <div style={{width: 320, whiteSpace: 'pre-line'}}>
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


    const {getTableProps, getTableBodyProps, headerGroups, rows, prepareRow} = useTable({
        columns,
        data: operations,
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
            }}>Выберите день совершения операции</p>

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


            <Button
                className='btn-submit'
                variant='primary'
                type='submit'
                onClick={getOperationsInDay}
                disabled={isSubmitting}
                style={{marginBottom: '20px', marginTop: '20px'}}>
                {isSubmitting ? 'Поиск заказов...' : 'Получить все операции, созданные в этот день'}
            </Button>

            <Button
                className='btn-submit'
                variant='primary'
                type='submit'
                onClick={getAllInAllTimeOperations}
                disabled={isSubmitting}
                style={{marginBottom: '20px', marginTop: '20px'}}>
                {isSubmitting ? 'Поиск заказов...' : 'Получить все операции за всё время'}
            </Button>


            <p className="input-style-modified">Все пользователи приложения</p>
            <InputPicker
                data={userPhones}
                value={userContacts}
                onChange={setUserContacts}
                style={{...styles, WebkitTextFillColor: "#000000"}}
                menuStyle={{fontSize: "17px"}}
            />

            <Button
                className='btn-submit'
                variant='primary'
                type='submit'
                onClick={getAllUserOperations}
                disabled={isSubmitting}
                style={{marginBottom: '20px', marginTop: '20px'}}>
                {isSubmitting ? 'Поиск заказов...' : 'Получить все операции по номеру телефона'}
            </Button>

            <p className="input-style-modified">Все операции пользователей</p>

            <InputPicker
                data={operationsName}
                value={currentOperation}
                onChange={setCurrentOperation}
                style={{...styles, WebkitTextFillColor: "#000000"}}
                menuStyle={{fontSize: "17px"}}
            />

            <Button
                className='btn-submit'
                variant='primary'
                type='submit'
                onClick={getAllOperationsByName}
                disabled={isSubmitting}
                style={{marginBottom: '20px', marginTop: '20px'}}>
                {isSubmitting ? 'Поиск заказов...' : 'Получить все операции по названию операции'}
            </Button>

            <table {...getTableProps()} className="MyTableOperations" style={{marginBottom: '100px'}}>
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

export default OperationsTable;