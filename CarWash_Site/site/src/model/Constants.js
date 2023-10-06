const createLabelValueArray = (arr) => arr.map(item => ({label: item, value: item}));

export const orderStatuses = [
    "Отменён",
    "Не оплачен и не сделан",
    "Оплачен на 5 процентов и не сделан",
    "Оплачен на 10 процентов и не сделан",
    "Оплачен на 20 процентов и не сделан",
    "Оплачен на 30 процентов и не сделан",
    "Оплачен на 40 процентов и не сделан",
    "Оплачен на 50 процентов и не сделан",
    "Оплачен на 60 процентов и не сделан",
    "Оплачен на 70 процентов и не сделан",
    "Оплачен на 80 процентов и не сделан",
    "Оплачен на 90 процентов и не сделан",
    "Полностью оплачен и не сделан",
    "Не оплачен, но сделан",
    "Оплачен на 5 процентов и сделан",
    "Оплачен на 10 процентов и сделан",
    "Оплачен на 20 процентов и сделан",
    "Оплачен на 30 процентов и сделан",
    "Оплачен на 40 процентов и сделан",
    "Оплачен на 50 процентов и сделан",
    "Оплачен на 60 процентов и сделан",
    "Оплачен на 70 процентов и сделан",
    "Оплачен на 80 процентов и сделан",
    "Оплачен на 90 процентов и сделан",
    "Полностью оплачен и сделан"
];

export const saleDayArray = [
    'Акция на понедельник',
    'Акция на вторник',
    'Акция на среду',
    'Акция на четверг',
    'Акция на пятницу',
    'Акция на субботу',
    'Акция на воскресенье'
];


const serviceTypes = [
    "Мойка",
    "Полировка",
    "Шиномонтаж",
    "Мойка с сайта",
    "Шиномонтаж с сайта",
    "Полировка с сайта",
    "Шиномонтаж с приложения",
    "Полировка с приложения",
    "Мойка ELITE",
    "Мойка VIP",
    "Мойка Комфорт",
    "Мойка Стандарт",
    "Мойка Эконом",
    "Мойка неизвестная",
    "Неизвестно"
];


const carTypes = [
    '1 тип - седан',
    '2 тип - кроссовер',
    '3 тип - джип',
    'Неизвестно'
];

const roles = [
    'Обычный пользователь',
    'Администратор',
    'Модератор',
    'Директор',
    'Специалист (мойщик)'
];

export const rolesArray = createLabelValueArray(roles);

export const orderStatusArray = createLabelValueArray(orderStatuses);
export const saleDay = createLabelValueArray(saleDayArray);
export const serviceTypesArray = createLabelValueArray(serviceTypes);
export const carTypesArray = createLabelValueArray(carTypes);
