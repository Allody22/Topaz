import {$authHost, $host} from "./index";
import jwt_decode from "jwt-decode";

//Версия с вроде как правильным рефреш токеном
export const login = async (username, password) => {
    const {data} = await $host.post('api/auth/admin/signin_v1', {username, password})
    const {token, refreshToken} = data
    localStorage.setItem('token', token)
    localStorage.setItem('refreshToken', refreshToken)
    return jwt_decode(token)
}

//Функция для рефреша токена
export const refreshAccessToken = async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    const {data} = await $host.post('api/auth/refreshtoken_v1', {refreshToken})
    localStorage.setItem('token', data.token)
    localStorage.setItem('refreshToken', data.refreshToken)
    return jwt_decode(data.token)
}

export const signOut = async () => {
    await $authHost.post('api/auth/signout_v1');
    localStorage.clear();
}

export const getAllSales = async () => {
    const response = await $host.get('/api/files/sales/get_all');
    return await response.data;
};

export const getAllFiles = async () => {
    const response = await $host.get('/api/files/get_all');
    return await response.data;
};

export const uploadImage = async (file, description, status) => {
    const formData = new FormData();
    formData.append('file', file); // 'file' соответствует имени параметра в вашем Spring контроллере
    formData.append('description', description);
    formData.append('status', status);

    const config = {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    };

    try {
        const response = await $authHost.post('/api/files/upload', formData, config);
        return response.data;
    } catch (error) {
        console.error("Ошибка при загрузке изображения:", error);
        throw error;
    }
};



export const check = async () => {
    await $authHost.get('api/admin/users/adminRoleCheck_v1');
}

export const updateUserInfo = async (username, fullName, roles, adminNote, userNote, email) => {
    const requestBody = {
        fullName: fullName,
        username: username,
        roles: roles,
        adminNote: adminNote,
        userNote: userNote,
        email: email
    };
    const response =  await $authHost.post('api/admin/users/updateUserInfo_v1',requestBody);
    return await response.data;
};

export const getAllUsers = async () => {
    const response = await $authHost.get('api/admin/users/getAllUserNames_v1');
    return await response.data;
};

export const findUserByPhone = async (userName) => {
    const response = await $authHost.get('api/admin/users/findUserByTelephone_v1?username=' +  encodeURIComponent(userName));
    return await response.data;
};