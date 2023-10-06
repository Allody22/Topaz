import {makeAutoObservable} from "mobx";
import {getAllSales} from "../http/userAPI";

class SaleStore {
    discounts = [];
    isLoading = false;
    error = null;

    constructor() {
        makeAutoObservable(this);
    }

    loadDiscounts = async () => {
        this.setLoading(true);
        this.setError(null);

        try {
            const response = await getAllSales();
            this.setDiscounts(response.map(sale => ({
                name: sale.name,
                description: sale.description
            })));

        } catch (error) {
            let errorMessage = "Системная ошибка, попробуйте позже.";

            if (error.response) {
                let messages = [];
                for (let key in error.response.data) {
                    messages.push(error.response.data[key]);
                }
                errorMessage = messages.join('');
            }

            this.setError(errorMessage);
        } finally {
            this.setLoading(false);
        }
    };

    setDiscounts(discounts) {
        this.discounts = discounts;
    }

    setLoading(isLoading) {
        this.isLoading = isLoading;
    }

    setError(error) {
        this.error = error;
    }

    refreshDiscounts = () => {
        this.loadDiscounts();
    };
}

const saleStore = new SaleStore();
export default saleStore;
