import '../css/CreatingOrder.css';
import {findUserByPhone, getAllUsers, updateUserInfo} from "../http/userAPI";
import React, {useEffect, useState} from 'react';
import {Button, Form} from 'react-bootstrap';
import InputField from "../model/InputField";
import {InputPicker, Notification, TagPicker, useToaster} from "rsuite";
import {BrowserRouter as Router, useHistory, useParams} from "react-router-dom";
import {observer} from "mobx-react-lite";
import socketStore from "../store/SocketStore";
import orderTypeMap from "../model/map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import rolesFromEnglishMap from "../model/map/RolesFromEnglishMap";
import rolesFromRussianMap from "../model/map/RolesFromRussianMap";
import {rolesArray} from "../model/Constants";


const styles = {
    width: 500, display: 'block',
    marginBottom: 10, marginLeft: 'auto', marginRight: 'auto', marginTop: 10
};


const ChangeUserInfo = observer(() => {
    const [showConfirmation, setShowConfirmation] = useState(false);
    const [getUsers, setGetUsers] = useState(false);

    const [fullName, setFullName] = useState('');
    const [adminNote, setAdminNote] = useState('');
    const [userNote, setUserNote] = useState('');
    const [email, setEmail] = useState('');
    const [selectedRoles, setSelectedRoles] = useState([]);
    const [enSelectedRoles, setEnSelectedRoles] = useState([]);

    const [usersArray, setUsersArray] = useState([]);

    const {username: paramsUsername} = useParams();
    const [username, setUsername] = useState(paramsUsername);

    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);
    const [successResponse, setSuccessResponse] = useState();
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
        async function getAllPeopleInformation() {
            try {
                if (!getUsers) {
                    const response = await getAllUsers();
                    setUsersArray(response);
                    setGetUsers(true)
                    setSuccessResponse(null)
                    const sentence = `Список всех пользователей успешно получен.`;
                    setSuccessResponse(sentence)
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
                    setErrorResponse("Системная ошибка, проверьте правильность " +
                        "введённой информации и попробуйте еще раз")
                    setErrorFlag(flag => !flag)
                }
            }
        }

        getAllPeopleInformation();
    }, []);

    useEffect(() => {
        async function findUserInfo() {
            if (username != null && username !== "" && typeof username !== "undefined" && username !== ":username") {
                try {
                    const response = await findUserByPhone(username);
                    setFullName(response.fullName || '');
                    setAdminNote(response.adminNotes || '');
                    setUserNote(response.userNotes || '');
                    setEmail(response.email || '')
                    const roles = response.roles.map(role => rolesToRussianMap(role)) || [];

                    setSelectedRoles(roles);

                    setGetUsers(true)
                    setSuccessResponse(null)
                    const sentence = `Информация о пользователе получена.`;
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
                }
            }
        }

        findUserInfo();
    }, [username]);

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
        if (showConfirmation) {
            try {
                const data = (await updateUserInfo(username, fullName, enSelectedRoles,
                    adminNote, userNote, email)).message;

                setSuccessResponse(null)
                setSuccessResponse(data)
            } catch (error) {
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
    }


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
                    className="input-style"
                    label='Имя и фамилия'
                    id='fullName'
                    value={fullName}
                    onChange={setFullName}
                />

                <InputField
                    label='Почта'
                    id='email'
                    value={email}
                    onChange={setEmail}
                />

                <InputField
                    className="input-style"
                    label='Ваша заметка о человеке '
                    id='adminNote'
                    value={adminNote}
                    onChange={setAdminNote}
                />
                <InputField
                    className="input-style"
                    label='Комментарии самого пользователя (возможно, более точная информация о машине и тп)'
                    id='userNote'
                    value={userNote}
                    onChange={setUserNote}
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
        </>
    );
});

export default ChangeUserInfo;