import '../css/CreatingOrder.css';
import {findUserByPhone, getAllUsers, getOrdersByUser, updateUserInfo} from "../http/userAPI";
import React, {useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import InputField from "../model/InputField";
import {Divider, InputPicker, Notification, TagPicker, useToaster} from "rsuite";
import {BrowserRouter as Router, useHistory, useParams} from "react-router-dom";
import {observer} from "mobx-react-lite";
import socketStore from "../store/SocketStore";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import rolesFromEnglishMap from "../model/map/RolesFromEnglishMap";
import rolesFromRussianMap from "../model/map/RolesFromRussianMap";
import {rolesArray} from "../model/Constants";
import currentOrderStatusMapFromEng from "../model/map/CurrentOrderStatusMapFromEng";
import {useSortBy, useTable} from "react-table";


const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};

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

const ChangeUserInfo = observer(() => {
    const [showConfirmation, setShowConfirmation] = useState(false);

    const [usersArray, setUsersArray] = useState([]);

    const [orders, setOrders] = useState([]);

    const [fullName, setFullName] = useState('');
    const [adminNote, setAdminNote] = useState('');
    const [email, setEmail] = useState('');
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [enSelectedRoles, setEnSelectedRoles] = useState([]);


    const {userFromParams} = useParams();
    const [username, setUsername] = useState(userFromParams);

    const [errorResponse, setErrorResponse] = useState('');
    const [errorFlag, setErrorFlag] = useState(false);

    const [successFlag, setSuccessFlag] = useState(false);
    const [successResponse, setSuccessResponse] = useState('');


    const toaster = useToaster();
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
            <div style={{width: 320, whiteSpace: "pre-line"}}>
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
    }, [successFlag]);


    useEffect(() => {
        async function getAllPeopleInformation() {
            try {
                const response = await getAllUsers();
                setUsersArray(response);
                const sentence = `Список всех пользователей успешно получен.`;
                setSuccessResponse(sentence)
                setSuccessFlag(flag => !flag);
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

    async function findUserInfo() {

        if ((!username || typeof username !== "string") || username === ":username" || username === ":userFromParams" || username === '') {
            setErrorResponse("Обязательно укажите телефон существующего пользователя")
            setErrorFlag(flag => !flag)
            return;
        }
        try {
            const response = await findUserByPhone(username);
            setFullName(response.fullName || '');
            setAdminNote(response.adminNotes || '');
            setEmail(response.email || '')
            const roles = response.roles.map(role => rolesToRussianMap(role)) || [];

            setSelectedRoles(roles);

            const sentence = `Информация о пользователе получена.`;
            setSuccessResponse(sentence)

            setSuccessFlag(flag => !flag);
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

    async function getUserOrders() {
        if ((!username || typeof username !== "string") || username === ":username" || username === ":userFromParams" || username === '') {
            setErrorResponse("Обязательно укажите телефон существующего пользователя")
            setErrorFlag(flag => !flag)
            return;
        }
        try {
            const response = await getOrdersByUser(username);
            setOrders(response);

            const sentence = `Заказы пользователя успешно получены.`;
            setSuccessResponse(sentence)

            setSuccessFlag(flag => !flag);
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
        getUserOrders();
        findUserInfo();
    }, [username]);

    useEffect(() => {
        if (!(userFromParams === ":username" || userFromParams === ":userFromParams"
            || userFromParams === '')) {
            setUsername(userFromParams)
        }
    }, [userFromParams]);

    useEffect(() => {
        changeRolesToEnglish()
    }, [selectedRoles]);

    const changeRolesToEnglish = () => {
        const arrayOfRoles = []
        selectedRoles.forEach(item => arrayOfRoles.push(rolesToEnglishMap(item)));
        setEnSelectedRoles(arrayOfRoles);
    };

    const rolesToEnglishMap = (item) => {
        const translatedRole = rolesFromRussianMap[item];

        if (translatedRole) {
            return translatedRole;
        } else {
            setErrorResponse("Незивестная роль");
            setErrorFlag(flag => !flag);
        }
    }


    const rolesToRussianMap = (item) => {
        const translatedRole = rolesFromEnglishMap[item];

        if (translatedRole) {
            return translatedRole;
        } else {
            setErrorResponse("Незивестная роль");
            setErrorFlag(flag => !flag);
        }
    }

    const handleTagRemoved = (item) => {
        setSelectedRoles(prevSelectedRoles =>
            prevSelectedRoles.filter(role => role !== item)
        );
    };

    const handleSubmit = async (event) => {
        event.preventDefault()
        if (!username || username === "") {
            setErrorResponse("Обязательно укажите телефон пользователя")
            setErrorFlag(flag => !flag)
            return;
        }
        if (showConfirmation) {
            try {
                const data = (await updateUserInfo(username, fullName, enSelectedRoles,
                    adminNote, email)).message;

                setSuccessResponse(data)

                setSuccessFlag(flag => !flag);
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
    }

    const {getTableProps, getTableBodyProps, headerGroups, rows, prepareRow} = useTable({
        columns,
        data: orders,
    }, useSortBy);


    return (
        <>
            <p className="input-style-modified">Страница изменения информации о человеке в базе данных</p>
            <p className="input-style-modified">Человек с ролью администратор, модератора, специалиста и директора может
                пользоваться
                сайтом</p>

            <p className="input-style-modified">Выберите роли пользователя</p>
            <TagPicker
                data={rolesArray}
                block
                value={selectedRoles}
                onChange={value => setSelectedRoles(value)}
                onClose={handleTagRemoved}
                className="styles"
                style={{WebkitTextFillColor: "#000000"}}
            />

            <Form onSubmit={handleSubmit}>

                <p className="input-style-modified">Все пользователи приложения</p>
                <InputPicker
                    data={usersArray.map(item => ({label: item, value: item}))}
                    value={username}
                    onChange={setUsername}
                    style={{...styles, WebkitTextFillColor: "#000000"}}
                    menuStyle={{fontSize: "17px"}}
                />

                <InputField
                    maxLength={80}
                    className="input-style"
                    label='Имя и фамилия'
                    id='fullName'
                    value={fullName}
                    onChange={setFullName}
                />

                <InputField
                    label='Почта'
                    className="input-style"
                    id='email'
                    value={email}
                    onChange={setEmail}
                />

                <InputField
                    maxLength={120}
                    className="input-style"
                    label='Ваша заметка о человеке '
                    id='adminNote'
                    value={adminNote}
                    onChange={setAdminNote}
                />
                {showConfirmation && (
                    <div className='confirmation-container'>
                        <div className='confirmation-message'>
                            <p className="input-style">Вы уверены, что хотите отправить запрос?</p>
                            <p>Это изменит информацию об этом человеке ВО ВСЕЙ базе данных для ВСЕХ</p>
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
                        style={{marginBottom: '20px', marginTop: '20px'}}
                    >
                        Обновить информацию
                    </Button>
                </div>
            </Form>


            <p style={inputStyle}>Заказы пользователя</p>
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
        </>
    );
});

export default ChangeUserInfo;