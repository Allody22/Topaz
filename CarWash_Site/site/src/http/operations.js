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
