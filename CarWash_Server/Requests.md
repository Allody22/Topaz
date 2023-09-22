# Тут будут все запросы
Надеюсь, что так будет проще анализировать всю информацию вокруг запросов, синхронизировать андроидроид и сервер лучше.  
Для примеров будут конкретные запросы со входными и выходными данными прямо как в Postam.  
Там где выходные/входные данные слишком большие, я сделал сокращенея, на которые надо нажать, чтобы открыть весь код  
## Запросы для регистрации и токена
1)<strong> ```@PostMapping("/api/auth/signup")``` - регистрация клиента</strong>  
На вход подается джейсон с информации о клиенте, а пример запроса:  
Входные данные : 
```json
{
    "username" : "89635186660",
    "password" : "testPassword"
}
``` 
Выходные данные:
```json
{
    "message": "User registered successfully!"
}
```
Или строка с сообщением об ошибке, что такое пользователь уже существует.  

2)<strong> ```@PostMapping("/api/auth/signin)``` - логин клиента</strong>  
На вход подаётся username (phone) и password  
```json
{
    "username": "89635186660",
    "password": "testPassword"
}
```
Выходные данные - это информация о созданном bearer токене, имя пользователя и его роль
```json
{
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4OTYzNTE4NjY2MCIsImlhdCI6MTY4MTgxNDk5NiwiZXhwIjoxNjgxODIwOTk2fQ.-tYEHbjGR_vWcfKcB-klbJ0EM4bdUTlyGJLeOIJ5ikqarCs15dpnkpKniAgU20GM8wc83Jfq6aE_OvykLjnxSQ",
    "type": "Bearer",
    "refreshToken": "62ef136e-65be-45e9-b3ac-534696211c80",
    "id": 2,
    "username": "89635186660",
    "roles": [
        "ROLE_USER"
    ]
}
```

3)<strong> ```@PostMapping("/api/auth/refreshtoken)``` - обновление токена</strong>  
На вход подаётся сам refresh token, время действия которого уже вышло    
```json
{
    "refreshToken": "6f442bfe-be67-4af8-8d88-4ce20017f7c9"
}
```
   Выходные данные - мы получаем новый accessToken и всё тот же refreshToken: 
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4OTYzNTE4NjY2MCIsImlhdCI6MTY4MTgxNTg1NCwiZXhwIjoxNjgxODIxODU0fQ.vUj_6JTHyLmpx3VqwMwd5HU8QYlLO0_mnqArlZRAaNGig2PAURYLLkRtmHAbHSrCsbTLE--yHKQ0-h0-OhyJIA",
    "refreshToken": "6f442bfe-be67-4af8-8d88-4ce20017f7c9",
    "tokenType": "Bearer"
}
```

## Запросы пользователей для которых нужен access token в хедере.  Все запросы будут от лица пользователя, которому принадлежит этот токен  
4)<strong> ```@PostMapping("api/user/saveNewCar")``` - пользователь добавляет новую машину</strong>  
На вход подаётся номер машины, тип кузова  
```json
{
    "carNumber": "EN353T",
    "carClass" : "2 class"
}
 ```
На выход информация о машине, айды машины и пользователя, который её добавил  
```json
{
    "carNumber": "EN353T",
    "carId": 1,
    "userId": 2,
    "carClass": "2 class"
}
```

5)<strong> ```@PostMapping("api/orders/bookOrder")``` - бронирование заказа</strong>   
<details>
  <summary><strong>Входные данные:</strong>На вход подаётся вся информация о заказе и машина, для которой заказ</summary>
  <p>
      
```json
{
    "administrator" : "Lesha22",
    "price":1.23,
    "startTime" : "2023-03-01T10:22:11.0+07",
    "endTime" : "2023-03-01T14:53:11.0+07",
    "name" : "moem car",
    "bonuses": 0,
    "specialist": "Misha22",
    "boxNumber":2,
    "autoId" : 1
}
```
            
  </p>
</details>

<strong>Теперь кратко выходные данные</strong>
<details>
  <summary>На выход подаются все поля заказа, какими они сохранились, так как не вся информация обязательная в запросе:</summary>
  <p>
      
```json
{
    "id": 1,
    "price": 1.23,
    "name": "moem car",
    "startTime": "2023-03-01T03:22:11.000+00:00",
    "endTime": "2023-03-01T07:53:11.000+00:00",
    "administrator": "Lesha22",
    "specialist": "Misha22",
    "boxNumber": 2,
    "bonuses": 0,
    "booked": true,
    "executed": false,
    "comments": null,
    "userId": 2
}
```
      
  </p>
</details>

6)<strong> ```@GetMapping("api/user/getUserCars")``` - просмотр своих машин</strong>  
На вход нужен только токен    
<details>
  <summary><strong>Выходные данные:</strong>На выход сначала список машин пользователя, а потом информация о самом юзере</summary>
  <p>
      
```json
{
    "autoList": [
        {
            "id": 1,
            "carNumber": "EN353T",
            "carClass": "2 class"
        },
        {
            "id": 2,
            "carNumber": "УП333T",
            "carClass": "1 class"
        }
    ],
    "user": {
        "id": 2,
        "username": "89635186660",
        "phone": "89635186660",
        "email": null,
        "bonuses": 100,
        "fullName": null
    }
}
```
      
  </p>
</details>

7)<strong>```@GetMapping("api/user/getUserOrders")``` - просмотр своих заказов</strong>  
На вход нужен только токен    
<details>
  <summary><strong>Выходные данные:</strong> На выход сначала список заказов пользователя, а потом информация о самом юзере</summary>
  <p>

```json
{
    "orders": [
        {
            "id": 1,
            "price": 1.23,
            "name": "moem car",
            "startTime": "2023-03-01T03:22:11.000+00:00",
            "endTime": "2023-03-01T07:53:11.000+00:00",
            "administrator": "Lesha22",
            "specialist": "Misha22",
            "boxNumber": 2,
            "bonuses": 0,
            "booked": true,
            "executed": false,
            "comments": null,
            "auto": {
                "id": 1,
                "carNumber": "EN353T",
                "carClass": "2 class"
            },
            "user": {
                "id": 2,
                "username": "89635186660",
                "phone": "89635186660",
                "email": null,
                "bonuses": 100,
                "fullName": null
            }
        },
        {
            "id": 2,
            "price": 1.23,
            "name": "moem car",
            "startTime": "2023-03-01T03:22:11.000+00:00",
            "endTime": "2023-03-01T07:53:11.000+00:00",
            "administrator": "Sasha",
            "specialist": "Andrei",
            "boxNumber": 2,
            "bonuses": 0,
            "booked": true,
            "executed": false,
            "comments": null,
            "auto": {
                "id": 2,
                "carNumber": "УП333T",
                "carClass": "1 class"
            },
            "user": {
                "id": 2,
                "username": "89635186660",
                "phone": "89635186660",
                "email": null,
                "bonuses": 100,
                "fullName": null
            }
        }
    ]
}
```

  </p>
</details>

8)<strong> ```@PutMapping("api/user/updateUserInfo")``` - обновление/добавление какой-то информации о пользователи</strong>  
На вход вместе с токеном та информация, которую надо добавить/обновить (почту, телефон и ФИО)  
```json
{
    "email" : "misha.23123123b32131ogdanov@gmail.com",
    "fullName": "Богданов Михаил Сергеевич213123123123edasd"
}
```
На выход сообщение о новой информации  
```json
{
    "message": "Пользователь 2 получил почту misha.23123123b32131ogdanov@gmail.com и новый телефон null"
}
```

## Запросы для которых нужны права админа (токен админа)  
9)<strong> ```@PostMapping("api/admin/findUserByTelephone")``` - поиск информации о пользователи по его телефону</strong>  
На вход подаётся телефон (юзернейм) пользователя  
```json
{
    "name": "моем машину",
    "date" : "2023-03-01T15:40:11.999",
    "price": 1.234
}
```
На выход пока просто информация, что заказ добавлен  

4) ```@PostMapping("api/orders/newOrder")``` - добавление нового заказа\
На вход подаётся название услуги, дата, цена\
   http://localhost:8080/api/orders/newOrder 
```json
{
    "name": "моем машину",
    "date" : "2023-03-01T15:40:11.999",
    "price": 1.234
}
```
На выход пока просто информация, что заказ добавлен  
5) ```@PostMapping("api/orders//bookOrder")``` - бронирование существующего заказа\
На вход подаётся название услуги, дата, цена\
   http://localhost:8080/api/orders/bookOrder 
```json
   {
    "name": "моем машину",
    "date" : "2023-03-01T15:40:11.999",
    "price": 1.234
   }
```
