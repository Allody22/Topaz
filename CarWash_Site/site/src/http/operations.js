import {$authHost} from "./index";

export const getAllOperationsInOneDay = async (startTime, endTime) => {
    const {data} = await $authHost.get('/api/operations/get_all_day?startTime='
        + encodeURIComponent(startTime) + '&endTime=' + encodeURIComponent(endTime));
    return data
};

export const getAllOperations = async () => {
    const {data} = await $authHost.get('/api/operations/get_all');
    return data
};

export const getAllOperationsByUser = async (phone) => {
    const {data} = await $authHost.get('/api/operations/user?phone=' + encodeURIComponent(phone));
    return data
};

export const getAllOperationsByOperationName = async (operation) => {
    const {data} = await $authHost.get('/api/operations/operation_name?operation=' + encodeURIComponent(operation));
    return data
};

export const getAllNamesOperations = async () => {
    const {data} = await $authHost.get('/api/operations/names');
    return data
};

