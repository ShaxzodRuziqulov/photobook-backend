# BACKEND LOGIC

Bu fayl hozirgi backendning amaldagi logikasini tushuntiradi. Asosiy maqsad front bilan integratsiya uchun real contractni ko'rsatish.

## 1. Asosiy ma'lumot

- Base path: `/api/v1`
- Auth turi: `Authorization: Bearer <access_token>`
- Public yo'llar:
  - `/api/v1/auth/**`
  - `/uploads-storage/**`
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

### Login response

```json
{
  "access_token": "jwt",
  "refresh_token": "jwt",
  "user": {
    "id": "uuid",
    "name": "Ali Valiyev",
    "email": null,
    "roles": ["ROLE_ADMIN"],
    "avatar_url": "/uploads-storage/file.png",
    "phone": "+998901234567",
    "bio": "text"
  }
}
```

## 4. Permission logikasi

### `ROLE_ADMIN`

Ruxsat:
- users
- roles
- customers
- materials
- expense categories
- expenses
- product categories
- orders
- user tasks
- uploads
- dashboard

### `ROLE_MANAGER`

Ruxsat:
- customers
- materials
- expense categories
- expenses
- product categories
- orders
- user tasks
- uploads
- dashboard

### `ROLE_OPERATOR`

Ruxsat:
- `GET /api/v1/orders/**`
- `PUT /api/v1/orders/{id}/status`
- `GET /api/v1/user-tasks/me/{id}`
- `POST /api/v1/user-tasks/me/paging`
- `PUT /api/v1/user-tasks/me/{id}`
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

### OrderDto

```json
{
  "id": "uuid",
  "kind": "ALBUM",
  "categoryId": "uuid",
  "categoryName": "Premium Album",
  "orderName": "Nikoh albomi",
  "itemType": "Premium",
  "customerId": "uuid",
  "customerName": "Ali Valiyev",
  "receiverName": "Ali",
  "employees": [
    {
      "employeeId": "uuid",
      "employeeName": "Vali",
      "processedCount": 0,
      "stepOrder": 1,
      "workStatus": "STARTED"
    },
    {
      "employeeId": "uuid",
      "employeeName": "Sardor",
      "processedCount": 0,
      "stepOrder": 2,
      "workStatus": "PENDING"
    }
  ],
  "processedCount": 0,
  "currentStepProcessedCount": 0,
  "activeEmployeeId": "uuid",
  "activeEmployeeName": "Vali",
  "pageCount": 20,
  "amount": 50,
  "acceptedDate": "2026-03-16",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "imageUrl": "/uploads-storage/file.png",
  "notes": "text",
  "uploadId": "uuid",
  "statusHistory": []
}
```

### Create va update request contract

Frontend `employees` ni worker navbati bilan yuboradi:

```json
{
  "kind": "ALBUM",
  "categoryId": "uuid",
  "orderName": "Nikoh albomi",
  "itemType": "Premium",
  "customerId": "uuid",
  "receiverName": "Ali",
  "employees": [
    {
      "employeeId": "uuid",
      "stepOrder": 1
    },
    {
      "employeeId": "uuid",
      "stepOrder": 2
    },
    {
      "employeeId": "uuid",
      "stepOrder": 3
    }
  ],
  "pageCount": 20,
  "amount": 50,
  "acceptedDate": "2026-03-16",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "notes": "text",
  "uploadId": "uuid"
}
```

### Validation

- `orderName` majburiy
- `categoryId` majburiy
- `customerId` yoki `customerName` majburiy
- `employees` majburiy va bo'sh bo'lmasligi kerak
- `employees` ichida `null` bo'lmasligi kerak
- har bir `employees[].employeeId` majburiy
- har bir `employees[].stepOrder` majburiy
- `stepOrder` unique bo'lishi kerak
- `stepOrder` 1 dan ketma-ket bo'lishi kerak
- `amount > 0`
- `acceptedDate` majburiy
- `deadline` majburiy
- `deadline >= acceptedDate`

### Workflow business logic

- `categoryId` bo'yicha category topiladi
- `customerId` bo'lsa mavjud customer ishlatiladi
- `customerId` bo'lmasa `customerName` dan yangi customer yaratiladi
- `employees[].employeeId` dagi userlar resolve qilinadi
- `uploadId` bo'lsa upload `ORDER` ga attach qilinadi
- attachdan keyin `imageUrl` avtomatik set qilinadi
- order `IN_PROGRESS` bo'lsa birinchi `stepOrder` dagi employee `STARTED`, qolganlari `PENDING`
- order `PAUSED` yoki `PENDING` bo'lsa incompletelar `PENDING`
- hamma employee `COMPLETED` bo'lsa order `COMPLETED`

### Progress maydonlari

- `processedCount`: oxirgi step employee'ning processed counti, ya'ni tayyor bo'lgan yakuniy son
- `currentStepProcessedCount`: hozir ishlayotgan employee progressi
- `activeEmployeeId`, `activeEmployeeName`: ayni paytda ishlayotgan worker

### Status o'zgarishi

Request:

```json
{
  "toStatus": "PAUSED"
}
```

Ruxsat etilgan transitionlar:
- `PENDING -> IN_PROGRESS`
- `PENDING -> PAUSED`
- `IN_PROGRESS -> PAUSED`
- `IN_PROGRESS -> COMPLETED`
- `PAUSED -> IN_PROGRESS`
- `PAUSED -> COMPLETED`

Cheklov:
- `COMPLETED`ga o'tkazishdan oldin barcha employee lar `COMPLETED` bo'lishi kerak

Status o'zgarsa:
- order status update qilinadi
- workflow qayta align qilinadi
- `order_status_history` yozuvi yaratiladi
- `changedBy` current user dan olinadi

### Paging filter

```json
{
  "search": "nikoh",
  "kind": "ALBUM",
  "status": "IN_PROGRESS",
  "customerId": "uuid",
  "employeeId": "uuid",
  "categoryId": "uuid",
  "from": "2026-03-01",
  "to": "2026-03-31",
  "deadlineFrom": "2026-03-10",
  "deadlineTo": "2026-03-20"
}
```

### Delete

- order o'chirilganda ownerga tegishli upload ham tozalanadi

## 6. User Tasks

Bu bo'lim worker login bo'lganda o'ziga tegishli ishlarni ko'rishi va update qilishi uchun ishlaydi.

### Endpointlar

- `GET /api/v1/user-tasks/me/{id}`
- `POST /api/v1/user-tasks/me/paging`
- `PUT /api/v1/user-tasks/me/{id}`

### UserTaskDto

```json
{
  "orderId": "uuid",
  "kind": "ALBUM",
  "categoryId": "uuid",
  "categoryName": "Premium Album",
  "orderName": "Nikoh albomi",
  "itemType": "Premium",
  "customerId": "uuid",
  "customerName": "Ali Valiyev",
  "receiverName": "Ali",
  "pageCount": 20,
  "amount": 50,
  "processedCount": 15,
  "orderProcessedCount": 0,
  "stepOrder": 1,
  "workStatus": "STARTED",
  "canWork": true,
  "acceptedDate": "2026-03-16",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "imageUrl": "/uploads-storage/file.png",
  "notes": "text"
}
```

### Worker update request

```json
{
  "processedCount": 20,
  "notes": "20 ta tayyorlandi",
  "workStatus": "COMPLETED"
}
```

### Worker update qoidalari

- faqat `order.status = IN_PROGRESS` bo'lsa update qilish mumkin
- faqat `workStatus = STARTED` bo'lgan employee o'z taskini update qila oladi
- `processedCount` kamaytirilmaydi
- `processedCount <= order.amount`
- agar oldingi step bo'lsa, current step `processedCount` oldingi step progressidan oshmasligi kerak
- `STARTED -> COMPLETED` transition ruxsat
- `PENDING -> *` worker endpoint orqali ruxsat emas
- step `COMPLETED` bo'lishi uchun `processedCount == order.amount`
- current step `COMPLETED` bo'lsa keyingi `stepOrder` employee avtomatik `STARTED`
- oxirgi employee `COMPLETED` bo'lsa order avtomatik `COMPLETED`

## 7. Order Status History

### Endpointlar

- `GET /api/v1/orders/{id}/status-history`
- qo'shimcha CRUD endpoint ham bor:
  - `POST /api/v1/order-status-histories`
  - `PUT /api/v1/order-status-histories/{id}`
  - `GET /api/v1/order-status-histories/{id}`
  - `GET /api/v1/order-status-histories`
  - `DELETE /api/v1/order-status-histories/{id}`

## 8. Uploads

### Endpointlar

- `POST /api/v1/uploads`
- `DELETE /api/v1/uploads/{key}`
- `GET /uploads-storage/{key}`

### Frontend uchun to'g'ri flow

1. Rasmni `POST /api/v1/uploads` orqali yuklang.
2. Response dan `id`, `url`, `key` ni oling.
3. Create/update requestga `uploadId` yuboring.
4. UI preview uchun `url` ni ishlating.

## 9. Dashboard

### Endpointlar

- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/orders-by-status`
- `GET /api/v1/dashboard/orders-by-kind`
- `GET /api/v1/dashboard/revenue-trend`
- `GET /api/v1/dashboard/expenses-trend`

## 10. Frontend uchun tavsiya qilingan oqimlar

### Order yaratish

1. Category, customer, employee listni oling.
2. Employee larni navbat bo'yicha `stepOrder` bilan yuboring.
3. Agar rasm bo'lsa upload qiling.
4. `POST /api/v1/orders` da `uploadId` yuboring.
5. Agar order darhol ishga tushsin desangiz `status = IN_PROGRESS` yuboring.
6. Agar navbatga qo'ymoqchi bo'lsangiz `status = PENDING` yoki `PAUSED` yuboring.

### Worker flow

1. `POST /api/v1/user-tasks/me/paging` bilan o'z tasklarini oling.
2. `canWork = true` bo'lgan taskni ishlang.
3. Jarayonda `processedCount` ni update qiling.
4. Ish to'liq tugasa `workStatus = COMPLETED` yuboring.
5. Backend keyingi workerga taskni avtomatik beradi.

## 11. Hozir muhim real qoidalar

- order workflow `stepOrder` bo'yicha yuradi
- `role` yo'q, navbat `stepOrder` bilan ifodalanadi
- admin `PAUSED` va `IN_PROGRESS` orqali ustuvor ishlarni boshqaradi
- worker faqat o'zining `STARTED` taskini update qila oladi
- final progress `processedCount` sifatida order response ichida qaytadi

## 12. Migration note

Backend va front uchun quyidagi contract o'zgargan:

- `employees[].role` olib tashlangan
- `employees[].stepOrder` majburiy bo'lgan
- `employees[].workStatus` response ichida qaytadi
- `orders.status` endi `PAUSED` qiymatini ham qabul qiladi
- worker update endpointida `status` o'rniga `workStatus` ishlatiladi

DB tarafda kerak bo'ladigan o'zgarishlar:

- `order_employees.role` ustunini olib tashlash
- `order_employees.step_order` ustuni null bo'lmasligi kerak
- `order_employees.work_status` ustuni saqlanishi kerak
- `orders.status` enumiga `PAUSED` qo'shish kerak
