import React, {useContext} from 'react';
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
import '../css/CreatingOrder.css';


const NavBar = observer(() => {
    const {user} = useContext(Context)
    const history = useHistory()


    const logOut = async () => {
        user.setUser({})
        user.setIsAuth(false)
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