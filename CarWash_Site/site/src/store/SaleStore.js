import {action, makeAutoObservable} from "mobx";
import {getAllSales} from "../http/userAPI";

class SaleStore {
    discounts = [];
    isLoading = false;
    error = null;
    loadDiscounts = action(async () => {
        this.isLoading = true;
        this.error = null;

        try {
            const response = await getAllSales();
            this.discounts = response.map(sale => ({
                name: sale.name,
                description: sale.description
            }));

        } catch (error) {
            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                this.error = messages.join(', '); // Обновляем свойство error
            } else {
                this.error = "Системная ошибка, попробуйте позже.";
            }
        } finally {
            this.isLoading = false;
        }
    });

    constructor() {
        makeAutoObservable(this);
    }

    refreshDiscounts = () => {
        this.loadDiscounts();
    };
}

const saleStore = new SaleStore();
export default saleStore;