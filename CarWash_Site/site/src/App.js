import React from 'react';
import {BrowserRouter} from "react-router-dom";
import AppRouter from "./model/AppRouter";
import NavBar from "./model/NavBar";
import {observer} from "mobx-react-lite";

const App = observer(() => {

    return (
        <BrowserRouter>
            <NavBar/>
            <AppRouter/>
        </BrowserRouter>
    );
});

export default App;
