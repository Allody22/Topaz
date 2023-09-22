import Admin from "./pages/Admin";
import {
    ADMIN_ROUTE, CHANGE_SERVICE_INFO, CHANGE_USERINFO_ROUTE, CHECK_SALES, CREATE_NEW_SERVICE,
    CREATE_POLISHING_ORDER_ROUTE, CREATE_TIRE_ORDER_ROUTE,
    CREATE_WASHING_ORDER_ROUTE,
    LOGIN_ROUTE, NEW_SALE, ORDERS_TABLE_ROUTE, UPDATE_ORDER_INFO_ROUTE,
} from "./utils/consts";
import Auth from "./pages/Auth";
import OrderTable from "./pages/OrdersTable";
import ChangeUserInfo from "./pages/ChangeUserInfo";
import CreatingWashingOrder from "./pages/CreatingWashingOrder";
import CreatingPolishingOrder from "./pages/CreatingPolishingOrder";
import CreatingTireOrder from "./pages/CreatingTireOrder";
import UpdateOrderInfo from "./pages/UpdateOrderInfo";
import ChangeServiceInfoFromEng from "./pages/ChangeServiceInfoFromEng";
import AddNewService from "./pages/AddNewService";
import SalePage from "./pages/SalePage";


export const authRoutes = [
    {
        path: ADMIN_ROUTE,
        Component: Admin
    },
    {
        path: CREATE_WASHING_ORDER_ROUTE,
        Component: CreatingWashingOrder
    },
    {
        path: CREATE_POLISHING_ORDER_ROUTE,
        Component: CreatingPolishingOrder
    },
    {
        path: CREATE_TIRE_ORDER_ROUTE,
        Component: CreatingTireOrder
    },
    {
        path: ORDERS_TABLE_ROUTE,
        Component: OrderTable
    },
    {
        path:CHANGE_USERINFO_ROUTE,
        Component: ChangeUserInfo
    },
    {
        path:CHECK_SALES,
        Component: SalePage
    },
    {
        path:UPDATE_ORDER_INFO_ROUTE,
        Component: UpdateOrderInfo
    },
    {
        path:CHANGE_SERVICE_INFO,
        Component: ChangeServiceInfoFromEng
    },
    {
        path:CREATE_NEW_SERVICE,
        Component: AddNewService
    }
]

//Тут мы пишем с какой страницы всё начинается (LOGIN_ROUTE)
export const publicRoutes = [
    {
        path: LOGIN_ROUTE,
        Component: Auth
    },
]
