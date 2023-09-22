import React, {useContext, useEffect, useState} from 'react';
import {Context} from "../index";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import {
    ADMIN_ROUTE,
    LOGIN_ROUTE
} from "../utils/consts";
import {Button} from "react-bootstrap";
import {observer} from "mobx-react-lite";
import Container from "react-bootstrap/Container";
import {NavLink, useHistory} from 'react-router-dom'
import {signOut} from "../http/userAPI";
import socketStore from "../store/SocketStore";
import Modal from "react-bootstrap/Modal";
import orderTypeMap from "./map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import '../css/CreatingOrder.css';



const NavBar = observer(() => {
    const {user} = useContext(Context)
    const history = useHistory()

    const [showModal, setShowModal] = useState(false);

    const [detectedOrders, setDetectedOrders] = useState([]);


    useEffect(() => {
        if (socketStore.message) {
            const id = JSON.parse(socketStore.message).id;
            const orderType = orderTypeMap[JSON.parse(socketStore.message).orderType]
            const startTime = format(parseISO(JSON.parse(socketStore.message).startTime), 'dd.MM.yyyy HH:mm:ss')
            const endTime = format(parseISO(JSON.parse(socketStore.message).endTime), 'dd.MM.yyyy HH:mm:ss')

            const newOrder = {orderType, startTime, endTime, id};

            setDetectedOrders(prevOrders => [...prevOrders, newOrder]);
        }

    }, [socketStore.message]);

    const removeDetectedOrder = (id) => {
        setDetectedOrders(current =>
            current.filter(item => item.id !== id)
        );
    };


    const handleOpenModal = () => setShowModal(true);
    const handleCloseModal = () => setShowModal(false);

    const handleClick = (id) => {
        history.push(`/updateOrderInfo/${id}`);
        handleCloseModal();
    };


    const logOut = async () => {
        user.setUser({})
        user.setIsAuth(false)
        setDetectedOrders([])
        socketStore.disconnect()
        await signOut();
    }

    return (
        <Navbar bg="dark" variant="dark">
            <Container>
                <div className="d-flex align-items-center">
                    <NavLink style={{color: "white", whiteSpace: "nowrap"}} to={ADMIN_ROUTE}>
                        Главное меню
                    </NavLink>
                </div>
                {user.isAuth ? (
                    <Nav className="ml-auto d-flex align-items-center" style={{color: "orange"}}>
                        <div className="mx-auto">
                            <Button onClick={handleOpenModal} variant={"outline-light"}>
                                Посмотреть все заказы за текущую сессию
                            </Button>
                        </div>
                        <Modal show={showModal}
                               onHide={handleCloseModal}
                               dialogClassName="custom-modal-dialog-polishing">
                            <Modal.Header closeButton>
                                <Modal.Title>Заказы за текущую сессию</Modal.Title>
                            </Modal.Header>
                            <Modal.Body>
                                <div
                                    style={{
                                        fontSize: '16px',
                                        borderBottom: '1px solid lightgray',
                                        paddingBottom: '10px',
                                        paddingTop: '10px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        fontWeight: 'bold'
                                    }}
                                >
                                    <div style={{ flex: 1 }}>Айди</div>
                                    <div style={{ flex: 1 }}>Тип</div>
                                    <div style={{ flex: 1 }}>Время начала</div>
                                    <div style={{ flex: 1 }}>Время конца</div>
                                </div>

                                {detectedOrders.map(item => (
                                    <div
                                        key={item}
                                        style={{
                                            fontSize: '16px',
                                            borderBottom: '1px solid lightgray',
                                            paddingBottom: '10px',
                                            paddingTop: '10px',
                                            display: 'flex',
                                            alignItems: 'center'
                                        }}
                                    >
                                        <div style={{ flex: 1 }}>
                                            <button
                                                onClick={() => handleClick(item.id)}
                                                style={{ color: 'blue' }}
                                                className='notification-button'
                                            >
                                                {item.id}
                                            </button>
                                        </div>

                                        <div style={{ flex: 1, marginRight:'7px' }}>
                                            <span style={{ color: "black" }}>{item.orderType}</span>
                                        </div>

                                        <div style={{ flex: 1 }}>
                                            <span style={{ color: "black" }}>{item.startTime}</span>
                                        </div>

                                        <div style={{ flex: 1 }}>
                                            <span style={{ color: "black" }}>{item.endTime}</span>
                                        </div>

                                        <div style={{ display: 'flex', justifyContent: 'center' }}>
                                            <button onClick={() => removeDetectedOrder(item.id)} style={{ cursor: "pointer" }}>
                                                &#10006;
                                            </button>
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
                        <Button
                            variant={"outline-light"}
                            onClick={() => logOut()}
                            className="ml-2"
                        >
                            Выйти
                        </Button>
                    </Nav>
                ) : (
                    <Nav className="ml-auto" style={{color: "white"}}>
                        <Button
                            variant={"outline-light"}
                            onClick={() => history.push(LOGIN_ROUTE)}
                        >
                            Выйти
                        </Button>
                    </Nav>
                )}
            </Container>
        </Navbar>
    );
});

export default NavBar;