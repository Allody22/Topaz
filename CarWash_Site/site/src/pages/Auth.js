import React, {useContext, useEffect, useState} from 'react';
import {Container, Form} from "react-bootstrap";
import Card from "react-bootstrap/Card";
import Button from "react-bootstrap/Button";
import Row from "react-bootstrap/Row";
import {useLocation, useHistory} from "react-router-dom";
import {ADMIN_ROUTE} from "../utils/consts";
import {login} from "../http/userAPI";
import {observer} from "mobx-react-lite";
import {Context} from "../index";
import socketStore from "../store/SocketStore";

const Auth = observer(() => {
    useLocation();

    const {user} = useContext(Context)
    const history = useHistory()
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')

    const loginClick = async () => {
        try {
            const data = await login(email, password);
            console.log(data)
            user.setUser(data)
            user.setIsAuth(true)
            history.push(ADMIN_ROUTE)
            socketStore.connectAndSubscribe();
        } catch (error) {
            console.log(error)
            alert('Произошла ошибка. Пожалуйста, повторите попытку.');
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
                        value={email}
                        onChange={e => setEmail(e.target.value)}
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
            <div

                style={{ display:'flex',alignItems: "center", justifyContent: "center",
                    textAlign: "left", fontFamily: "Arial, sans-serif",
                    position: "absolute", bottom: "10px", width: "90%",
                    height: "70px", backgroundColor: "white", color: "black",
                    border: "1px solid black", boxShadow: "2px 2px 5px rgba(0, 0, 0, 0.2)", padding: "10px",
                }}>
                Если вы считаете, что у вас должны быть права, чтобы пользоваться этой страницей, но вы почему-то не можете на неё зайти,
                то, вам нужно обратиться к своему начальнику, чтобы он дал вам доступ. <br />
                Если вы считаете, что какие-то кнопки неправильно работают, вам не хватает функционала
                или у вас есть какие-то вопросы, пожалуйста, напишите на почту misha.bogdanov.03@gmail.com
            </div>
        </Container>
    );
});

export default Auth;