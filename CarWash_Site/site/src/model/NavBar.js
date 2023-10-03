import React, {useContext, useEffect} from 'react';
import {Context} from "../index";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import {ADMIN_ROUTE, LOGIN_ROUTE} from "../utils/consts";
import {Button} from "react-bootstrap";
import {observer} from "mobx-react-lite";
import Container from "react-bootstrap/Container";
import {NavLink, useHistory} from 'react-router-dom'
import {signOut} from "../http/userAPI";
import socketStore from "../store/SocketStore";
import orderTypeMap from "./map/OrderTypeMapFromEnglish";
import {format, parseISO} from "date-fns";
import '../css/CreatingOrder.css';


const NavBar = observer(() => {
    const {user} = useContext(Context)
    const history = useHistory()


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