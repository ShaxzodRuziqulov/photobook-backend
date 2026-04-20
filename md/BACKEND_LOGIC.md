# BACKEND LOGIC

Bu fayl backendning amaldagi logikasini tushuntiradi. Asosiy maqsad front bilan integratsiya uchun real contract va workflowni bir joyda ko'rsatish.

## 1. Asosiy ma'lumot

- Base path: `/api/v1`
- Auth turi: `Authorization: Bearer <access_token>`
- Public yo'llar:
  - `/api/v1/auth/**`
  - `/uploads-storage/**`
  - `/socket.io/**`
  - swagger yo'llari
- Static upload URL: `/uploads-storage/{key}`

## 2. Response format

### Oddiy endpointlar

Ko'p endpointlar DTO ni to'g'ridan-to'g'ri qaytaradi.

### Paging endpointlar

Paging endpointlar `POST /resource/paging` ko'rinishida.

```json
{
  "content": [],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 120,
  "totalPages": 6,
  "last": false
}
```

### Error format

```json
{
  "message": "category_id is required",
  "errors": {
    "request": ["category_id is required"]
  }
}
```

## 3. Auth

### Endpointlar

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`

## 4. Permission logikasi

### `ROLE_ADMIN`

- users
- roles
- customers
- materials
- expense categories
- expenses
- product categories
- orders
- user tasks
- notifications
- uploads
- dashboard

### `ROLE_MANAGER`

- customers
- materials
- expense categories
- expenses
- product categories
- orders
- user tasks
- notifications
- uploads
- dashboard

### `ROLE_OPERATOR`

- `GET /api/v1/orders/**`
- `PUT /api/v1/orders/{id}/status`
- `GET /api/v1/user-tasks/me/{id}`
- `POST /api/v1/user-tasks/me/paging`
- `PUT /api/v1/user-tasks/me/{id}`
- `POST /api/v1/notifications/me/paging`
- `PUT /api/v1/notifications/{id}/read`
- `PUT /api/v1/notifications/read-all`
- `/api/v1/uploads/**`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`

## 5. Orders

Bu backendning asosiy workflow moduli.

### Endpointlar

- `POST /api/v1/orders`
- `PUT /api/v1/orders/{id}`
- `GET /api/v1/orders/{id}`
- `GET /api/v1/orders`
- `POST /api/v1/orders/paging`
- `DELETE /api/v1/orders/{id}`
- `PUT /api/v1/orders/{id}/status`
- `GET /api/v1/orders/{id}/status-history`

### Order contract

- `employees[].stepOrder` workflow navbatini bildiradi
- `employees[].employeeId` unique bo'lishi kerak
- `customerId` bo'lmasa `customerName` ishlatiladi
- `uploadId` bo'lsa upload `ORDER` ga attach qilinadi

### Validation

- `kind` majburiy
- `orderName` majburiy
- `categoryId` majburiy
- `customerId` yoki `customerName` majburiy
- `receiverName` majburiy
- `pageCount >= 0`
- `amount > 0`
- `acceptedDate` majburiy
- `deadline >= acceptedDate`
- `status` majburiy
- `employees` bo'sh bo'lmasligi kerak
- `employees[].employeeId` majburiy
- `employees[].stepOrder` majburiy
- `stepOrder` unique va 1 dan ketma-ket bo'lishi kerak

### Workflow business logic

- order `IN_PROGRESS` bo'lsa birinchi incompleted employee `STARTED`
- order `PENDING`, `PAUSED` yoki `CANCELLED` bo'lsa incompleted employee lar `PENDING`
- order `COMPLETED` bo'lsa barcha employee `COMPLETED`
- `processedCount` final step progressidan olinadi
- `currentStepProcessedCount` active worker progressidan olinadi
- `activeEmployeeId` va `activeEmployeeName` faqat active step bo'lsa qaytadi

### Status transitionlar

Ruxsat etilgan transitionlar:

- `PENDING -> IN_PROGRESS`
- `PENDING -> PAUSED`
- `PENDING -> CANCELLED`
- `IN_PROGRESS -> PAUSED`
- `IN_PROGRESS -> COMPLETED`
- `IN_PROGRESS -> CANCELLED`
- `PAUSED -> IN_PROGRESS`
- `PAUSED -> COMPLETED`
- `PAUSED -> CANCELLED`
- `COMPLETED -> PENDING`
- `COMPLETED -> IN_PROGRESS`
- `COMPLETED -> PAUSED`
- `CANCELLED -> PENDING`
- `CANCELLED -> IN_PROGRESS`

Cheklov:

- `COMPLETED` ga o'tkazishdan oldin barcha employee lar `COMPLETED` bo'lishi kerak

Status o'zgarsa:

- workflow qayta align qilinadi
- `order_status_history` yozuvi yaratiladi
- employee larga `ORDER_STATUS_CHANGED` notification yuboriladi
- active assignment bo'lsa `TASK_ACTIVATED` ham yuboriladi

### Order paging

Endpoint:

- `POST /api/v1/orders/paging?page=0&size=10&sort=updatedAt,desc`

Request body:

```json
{
  "search": "album",
  "status": "IN_PROGRESS",
  "acceptedDate": "2026-03-01",
  "deadline": "2026-03-31"
}
```

Bo'sh filter uchun:

```json
{}
```

Filter qoidalari:

- `search` bo'sh yoki `null` bo'lsa qidiruv o'chadi.
- `search` quyidagi fieldlardan qidiradi: `orderName`, `receiverName`, `customer.fullName`, employee full name, employee username, category name.
- `status` `OrderStatus` enum bo'yicha aniq filterlaydi.
- `acceptedDate` `orders.acceptedDate` bilan teng sana bo'yicha filterlaydi.
- `deadline` `orders.deadline` bilan teng sana bo'yicha filterlaydi.
- `kind`, `customerId`, `employeeId`, `categoryId`, `from`, `to`, `deadlineFrom`, `deadlineTo` order paging requestida ishlatilmaydi.

Query ishlashi:

- Repository `category`, `customer`, `employees`, `employees.user` ni `EntityGraph` orqali yuklaydi.
- Query `DISTINCT` ishlatadi, chunki employee join bir orderni bir necha qatorda qaytarishi mumkin.
- Nullable `status`, `acceptedDate`, `deadline` filterlari PostgreSQL null-param type xatosini bermasligi uchun `COALESCE(:param, field)` orqali qo'llangan.
- Default controller sort belgilanmagan; frontend query param bilan sort yuborishi kerak. Tavsiya: `sort=updatedAt,desc`.

Frontend uchun muhim qoidalar:

- Holat selectida `Hammasi` tanlansa `status` fieldini yubormang yoki `null` yuboring.
- Bo'sh date inputni `""` qilib yubormang; fieldni olib tashlang yoki `null` yuboring.
- Sana filterlari range emas. Range kerak bo'lsa backend contract alohida kengaytiriladi.
- Response `PageResponse<OrderDto>` formatida qaytadi: `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`.

## 6. User Tasks

Bu bo'lim worker login bo'lganda o'ziga tegishli ishlarni ko'rishi va update qilishi uchun ishlaydi.

### Endpointlar

- `GET /api/v1/user-tasks/me/{id}`
- `POST /api/v1/user-tasks/me/paging`
- `PUT /api/v1/user-tasks/me/{id}`

### UserTask logikasi

- worker faqat o'ziga tegishli order taskini ko'radi
- `canWork = true` bo'lishi uchun order `IN_PROGRESS` va assignment `STARTED` bo'lishi kerak
- `processedCount` requestda increment sifatida ishlaydi
- yangi progress `order.amount` dan oshmasligi kerak
- oldingi step progressidan ham oshmasligi kerak
- `notes` trim qilinadi, bo'sh bo'lsa `null`
- `workStatus` faqat `STARTED -> COMPLETED`
- workflow har update dan oldin va keyin qayta hisoblanadi
- next active worker o'zgarsa unga `TASK_ACTIVATED` yuboriladi
- barcha step targetga yetsa order avtomatik `COMPLETED`

## 7. Notifications

### Endpointlar

- `POST /api/v1/notifications/me/paging`
- `GET /api/v1/notifications/me/unread-count`
- `PUT /api/v1/notifications/{id}/read`
- `PUT /api/v1/notifications/read-all`

### Ishlash mantig'i

- notification yaratilganda DB ga saqlanadi
- `POST /notifications/me/paging` current user notificationlarini page ko'rinishida qaytaradi
- paging filterlari: `search`, `type`, `isRead`, `actionRequired`
- notification payloadda route uchun `targetType`, `targetId`, `targetKind`, `route`, `orderKind` qaytadi
- `GET /notifications/me/unread-count` current user uchun unread count qaytaradi
- `PUT /notifications/{id}/read` faqat current userning notificationi uchun ishlaydi
- `PUT /notifications/read-all` current userning barcha unread notificationlarini mark qiladi

### Turlar

- `ORDER_ASSIGNED`
- `TASK_ACTIVATED`
- `ORDER_UPDATED`
- `ORDER_STATUS_CHANGED`

## 8. Uploads

### Endpointlar

- `POST /api/v1/uploads`
- `DELETE /api/v1/uploads/{key}`
- `GET /uploads-storage/{key}`

### Flow

1. Front rasmni upload qiladi.
2. Response dan `uploadId` oladi.
3. Order yoki expense requestida shu `uploadId` yuboriladi.
4. Backend attach qilgach owner va URL ni set qiladi.

## 9. Dashboard

### Endpointlar

- `GET /api/v1/dashboard/orders-by-kind`
- `GET /api/v1/dashboard/orders-by-status?type=ALBUM|VIGNETTE|PICTURE`
- `GET /api/v1/dashboard/orders-by-category`

### Hisoblash logikasi

- Dashboard count qiymatlari order soni hisoblanadi, `orders.amount` yig'indisi emas.
- Service paging data yoki `findAll()` ishlatmaydi.
- `orders-by-kind` `orders` jadvalida `GROUP BY kind` bilan hisoblaydi.
- `orders-by-status?type=...` tanlangan `OrderKind` bo'yicha `GROUP BY status` bilan hisoblaydi.
- `orders-by-category?type=...` `product_categories` dan boshlanib, `orders` ga `LEFT JOIN` qiladi va `GROUP BY category` bilan hisoblaydi.
- `orders-by-kind` barcha `OrderKind` enum qiymatlarini qaytaradi. Bazada yo'q qiymatlar `0`.
- `orders-by-status` barcha `OrderStatus` enum qiymatlarini qaytaradi. Bazada yo'q qiymatlar `0`.
- `orders-by-category` tanlangan type dagi categorylarni qaytaradi. Order yo'q categorylar `0`.

### Rasmda ko'rsatilgan dashboardga moslik

Rasmda `Albom` tabida quyidagi qiymatlar bor:

- yuqoridagi `Jami`
- `Jarayonda`
- `Bajarilgan`
- `Bajarilish foizi`
- `Mahsulot turi bo'yicha hisobot`

Amaldagi APIlar bu UI ni yopadi:

- `Jami`: `GET /api/v1/dashboard/orders-by-kind` dan `kind = ALBUM` item `count` qiymatini olish mumkin.
- `Jarayonda`: `GET /api/v1/dashboard/orders-by-status?type=ALBUM` natijasida `status = IN_PROGRESS` itemlar `count` yig'indisi olinadi.
- `Bajarilgan`: `GET /api/v1/dashboard/orders-by-status?type=ALBUM` natijasida `status = COMPLETED` itemlar `count` yig'indisi olinadi.
- `Bajarilish foizi`: frontend `COMPLETED / total * 100` qilib hisoblaydi. `total` uchun `orders-by-kind` yoki `orders-by-status` yig'indisi ishlatiladi.
- `Mahsulot turi bo'yicha hisobot`: `GET /api/v1/dashboard/orders-by-category?type=ALBUM` mos keladi.

## 10. Socket Notification

Bu backend realtime notification uchun `socket.io` server ishlatadi.

### Socket endpoint

- handshake path: `/socket.io/`
- WebSocket transport ishlaydi, polling fallback uchun HTTP `GET` va `POST` ham qolgan
- namespace: default `/`
- auth: socket ulanganidan keyin `authenticate` event orqali token yuboriladi
- client tavsiya: `socket.io-client`
- transport: tavsiya etiladi `websocket`

### Frontend ulanish oqimi

1. Frontend `socket.io-client` bilan backend URL ga ulanadi.
2. Ulangandan keyin `authenticate` event bilan token yuboradi.
3. Token plain yoki `Bearer ...` formatda bo'lishi mumkin.
4. Backend tokenni tekshiradi.
5. Token to'g'ri bo'lsa socket `user:{userId}` room ga qo'shiladi.
6. Server `authenticated` event yuboradi.
7. O'qilmagan notificationlar replay qilinadi.

WebSocket-only frontend ulanish namunasi:

```js
const socket = io(API_BASE_URL, {
  path: "/socket.io/",
  transports: ["websocket"],
  withCredentials: true,
});
```

### Auth eventlar

- client -> server: `authenticate`
- server -> client: `authenticated`
- server -> client: `auth_error`

`authenticated` payload misoli:

```json
{
  "userId": "uuid",
  "connectedAt": "2026-04-13T11:00:00"
}
```

`auth_error` payload misoli:

```json
{
  "message": "Access token is invalid"
}
```

### Notification event

Server clientga `notification` event yuboradi.

Payload misoli:

```json
{
  "id": "uuid",
  "type": "TASK_ACTIVATED",
  "title": "Yangi ish navbati",
  "message": "Oldingi bosqich tugadi. Buyurtma endi sizning navbatingizda",
  "orderId": "uuid",
  "orderName": "Nikoh albomi",
  "orderKind": "ALBUM",
  "employeeId": "uuid",
  "employeeName": "Ali Valiyev",
  "stepOrder": 2,
  "workStatus": "STARTED",
  "targetType": "ORDER",
  "targetId": "uuid",
  "targetKind": "ALBUM",
  "route": "/album",
  "actionRequired": true,
  "isRead": false,
  "createdAt": "2026-04-13T11:00:00",
  "orderStatus": "IN_PROGRESS"
}
```

Izoh:

- `readAt` faqat o'qilgan notificationda bo'ladi
- online userga yuborilgan notification ham oldin DB ga yoziladi
- unread notificationlar socket authenticate qilinganda qayta yuboriladi

### Notification triggerlar

- `POST /api/v1/orders`: active workerga `TASK_ACTIVATED`, qolganlarga `ORDER_ASSIGNED`
- `PUT /api/v1/orders/{id}`: biriktirilgan employee larga `ORDER_UPDATED`
- `PUT /api/v1/orders/{id}/status`: biriktirilgan employee larga `ORDER_STATUS_CHANGED`
- `PUT /api/v1/user-tasks/me/{id}`: active worker almashsa yangi workerga `TASK_ACTIVATED`

## 11. Frontend uchun tavsiya qilingan oqimlar

### Order yaratish

1. Category, customer, employee listni oling.
2. Employee larni navbat bo'yicha `stepOrder` bilan yuboring.
3. Agar rasm bo'lsa avval upload qiling.
4. `POST /api/v1/orders` da `uploadId` yuboring.
5. Darhol ishga tushsin desangiz `status = IN_PROGRESS`.
6. Kutishga qo'ymoqchi bo'lsangiz `status = PENDING` yoki `PAUSED`.
7. Biriktirilgan employee larda realtime notification chiqishini kuting.

### Worker flow

1. `POST /api/v1/user-tasks/me/paging` bilan o'z tasklarini oling.
2. `canWork = true` bo'lgan taskni ishlang.
3. `processedCount` ni increment qilib yuboring.
4. Step to'liq tugasa `workStatus = COMPLETED` yuboring.
5. Backend workflowni qayta hisoblaydi.
6. Keyingi worker active bo'lsa unga realtime `TASK_ACTIVATED` boradi.

### Notification flow

1. User login bo'lgach `POST /api/v1/notifications/me/paging` bilan tarixni oling.
2. Parallel ravishda socket ulang.
3. `authenticated` kelgach realtime va replay notificationlarni qabul qiling.
4. UI da ko'rsatilgan notificationni `PUT /notifications/{id}/read` bilan mark qiling.
5. Hammasini birdan o'qilgan qilish kerak bo'lsa `PUT /notifications/read-all` ishlating.

## 12. Hozir muhim real qoidalar

- order workflow `stepOrder` bo'yicha yuradi
- `role` yo'q, navbat `stepOrder` bilan ifodalanadi
- worker faqat o'zining `STARTED` taskini update qila oladi
- worker update dagi `processedCount` increment bo'lib ishlaydi
- final progress oxirgi worker processed countidan olinadi
- notification DB ga saqlanadi va keyin socket orqali yuboriladi
- user online bo'lsa socket orqali darhol boradi
- user offline bo'lsa keyin socket authenticate qilganda unread notificationlar qayta yuboriladi

## 13. Migration note

- `employees[].role` olib tashlangan
- `employees[].stepOrder` majburiy bo'lgan
- `employees[].workStatus` response ichida qaytadi
- `orders.status` `PAUSED` va `CANCELLED` ni ham qabul qiladi
- worker update endpointida `status` o'rniga `workStatus` ishlatiladi
- order paging soddalashtirilgan: `search`, `status`, `acceptedDate`, `deadline`
- notificationlar uchun unbounded `GET /notifications/me` o'rniga `POST /notifications/me/paging` ishlatiladi
- notification badge uchun `GET /notifications/me/unread-count` ishlatiladi
- notification payload route maydonlari bilan boyitilgan: `orderKind`, `targetType`, `targetId`, `targetKind`, `route`
- socket notification payloadi ham REST notification DTO bilan moslashtirilgan
