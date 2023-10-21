import React, {useContext, useEffect, useState} from 'react';
import {Container, Form} from "react-bootstrap";
import Card from "react-bootstrap/Card";
import {Notification, toaster} from 'rsuite';
import Button from "react-bootstrap/Button";
import Row from "react-bootstrap/Row";
import {useHistory} from "react-router-dom";
import {ADMIN_ROUTE} from "../utils/consts";
import {login} from "../http/userAPI";
import {observer} from "mobx-react-lite";
import {Context} from "../index";
import socketStore from "../store/SocketStore";

const Auth = observer(() => {

    const [errorResponse, setErrorResponse] = useState();
    const [errorFlag, setErrorFlag] = useState(false);

    const {user} = useContext(Context)
    const history = useHistory()
    const [phone, setPhone] = useState('')
    const [password, setPassword] = useState('')

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
            setTimeout(() => {
                toaster.push(errorResponseMessage, {placement: "bottomEnd"});
            }, 100);
        }
    }, [errorFlag]);

    const loginClick = async () => {
        try {
            const data = await login(phone, password);
            user.setUser(data)
            user.setIsAuth(true)
            history.push(ADMIN_ROUTE)
            socketStore.connectAndSubscribe();
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
    };


    return (
        <Container
            className="d-flex justify-content-center align-items-center"
            style={{height: window.innerHeight - 54}}
        >
            <Card style={{width: 600}} className="p-5">
                <h2 className="m-auto">Авторизация</h2>
                <Form className="d-flex flex-column">
                    <Form.Control
                        className="mt-3"
                        placeholder="Введите ваш телефон..."
                        value={phone}
                        onChange={e => setPhone(e.target.value)}
                    />
                    <Form.Control
                        className="mt-3"
                        placeholder="Введите ваш пароль..."
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        type="password"
                    />
                    <Row className="d-flex justify-content-between mt-3 pl-3 pr-3">
                        <Button
                            variant={"outline-success"}
                            onClick={loginClick}
                        >
                            Войти
                        </Button>
                    </Row>
                </Form>

            </Card>
            <div style={{
                display: 'flex',
                alignItems: "center",
                justifyContent: "center",
                textAlign: "left",
                fontFamily: "Arial, sans-serif",
                position: "absolute",
                bottom: "10px",
                width: "90%",
                height: "auto",
                minHeight: "70px",
                backgroundColor: "white",
                color: "black",
                border: "1px solid black",
                boxShadow: "2px 2px 5px rgba(0, 0, 0, 0.2)",
                padding: "10px",
                marginBottom: "20px", // Отступ снизу для некоторого пространства между контейнером и нижним краем экрана или другим элементом
            }}>
                Если вы считаете, что у вас должны быть права, чтобы пользоваться этой страницей, но вы почему-то не
                можете на неё зайти,
                то, вам нужно обратиться к своему начальнику. <br/>
                Если вы считаете, что какие-то кнопки неправильно работают, напишите на почту
                misha.bogdanov.03@gmail.com
            </div>
        </Container>
    );
});

export default Auth;