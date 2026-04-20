# API Documentation

**Base URL:** `/api/v1`

**Auth:**
`Authorization: Bearer <access_token>` login, refresh, swagger, `/uploads-storage/**` va `/socket.io/**` dan tashqari endpointlar uchun yuboriladi.

## 1. Auth

### POST /auth/login

```json
{
  "username": "string",
  "password": "string"
}
```

### POST /auth/refresh

```json
{
  "refreshToken": "string"
}
```

### POST /auth/logout

```json
{
  "refreshToken": "string"
}
```

### GET /auth/me

## 2. Users

### GET /users
### POST /users
### GET /users/{id}
### PUT /users/{id}
### DELETE /users/{id}
### PUT /users/{id}/roles
### POST /users/paging
### GET /users/me
### PUT /users/me

## 3. Roles

### GET /roles
### POST /roles
### GET /roles/{id}
### PUT /roles/{id}
### DELETE /roles/{id}
### POST /roles/paging

## 4. Customers

### GET /customers
### POST /customers
### GET /customers/{id}
### PUT /customers/{id}
### DELETE /customers/{id}
### POST /customers/paging

## 5. Product Categories

### GET /product-categories
Optional query:

- `kind=ALBUM|VIGNETTE|PICTURE`

Response:

```json
[
  {
    "id": "uuid",
    "name": "Premium Album",
    "kind": "ALBUM",
    "defaultPages": "20",
    "size": "30x40"
  }
]
```

### POST /product-categories
### GET /product-categories/{id}
### PUT /product-categories/{id}
### DELETE /product-categories/{id}
### POST /product-categories/paging

## 6. Orders

### GET /orders
### POST /orders

```json
{
  "kind": "ALBUM",
  "categoryId": "uuid",
  "orderName": "Nikoh Albomi",
  "itemType": "Premium",
  "customerId": "uuid",
  "customerName": "Ali Valiyev",
  "receiverName": "Ali",
  "employees": [
    {
      "employeeId": "uuid",
      "stepOrder": 1
    },
    {
      "employeeId": "uuid",
      "stepOrder": 2
    }
  ],
  "pageCount": 20,
  "amount": 50,
  "acceptedDate": "2026-03-12",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "notes": "Test order",
  "uploadId": "uuid"
}
```

Note:

- `customerId` yoki `customerName` yuboriladi
- `employees[].stepOrder` 1 dan boshlab ketma-ket bo'lishi kerak
- `employees[].employeeId` order ichida unique bo'lishi kerak

### GET /orders/{id}
### PUT /orders/{id}
### DELETE /orders/{id}

### PUT /orders/{id}/status

```json
{
  "toStatus": "PAUSED"
}
```

### GET /orders/{id}/status-history

### POST /orders/paging

```json
{
  "search": "album",
  "status": "IN_PROGRESS",
  "acceptedDate": "2026-03-01",
  "deadline": "2026-03-31"
}
```

### Order response

```json
{
  "id": "uuid",
  "kind": "ALBUM",
  "categoryId": "uuid",
  "categoryName": "Premium Album",
  "orderName": "Nikoh Albomi",
  "itemType": "Premium",
  "customerId": "uuid",
  "customerName": "Ali Valiyev",
  "receiverName": "Ali",
  "employees": [
    {
      "employeeId": "uuid",
      "employeeName": "Vali",
      "processedCount": 10,
      "stepOrder": 1,
      "notes": "Muqova tayyorlandi",
      "workStatus": "STARTED"
    },
    {
      "employeeId": "uuid",
      "employeeName": "Sardor",
      "processedCount": 0,
      "stepOrder": 2,
      "notes": null,
      "workStatus": "PENDING"
    }
  ],
  "processedCount": 0,
  "currentStepProcessedCount": 10,
  "activeEmployeeId": "uuid",
  "activeEmployeeName": "Vali",
  "pageCount": 20,
  "amount": 50,
  "acceptedDate": "2026-03-12",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "imageUrl": "/uploads-storage/file.png",
  "notes": "text",
  "uploadId": "uuid",
  "statusHistory": [
    {
      "id": "uuid",
      "orderId": "uuid",
      "fromStatus": "PENDING",
      "toStatus": "IN_PROGRESS",
      "changedById": "uuid",
      "changedByName": "Admin",
      "changedAt": "2026-04-13T11:00:00"
    }
  ]
}
```

## 7. User Tasks

### GET /user-tasks/me/{id}

### POST /user-tasks/me/paging

```json
{
  "search": "album",
  "statuses": ["IN_PROGRESS"],
  "from": "2026-03-01",
  "to": "2026-03-31",
  "deadlineFrom": "2026-03-01",
  "deadlineTo": "2026-03-31"
}
```

### PUT /user-tasks/me/{id}

```json
{
  "processedCount": 5,
  "notes": "5 ta tayyorlandi",
  "workStatus": "COMPLETED"
}
```

Note:

- `processedCount` increment sifatida ishlaydi, jami qiymat emas
- backend uni current `processedCount` ga qo'shib saqlaydi
- `workStatus` faqat `STARTED -> COMPLETED` transitionini qabul qiladi

### UserTask response

```json
{
  "orderId": "uuid",
  "kind": "ALBUM",
  "categoryId": "uuid",
  "categoryName": "Premium Album",
  "orderName": "Nikoh Albomi",
  "itemType": "Premium",
  "customerId": "uuid",
  "customerName": "Ali Valiyev",
  "receiverName": "Ali",
  "pageCount": 20,
  "amount": 50,
  "processedCount": 20,
  "orderProcessedCount": 0,
  "availableToProcess": 35,
  "remainingAvailable": 15,
  "remainingTotal": 30,
  "stepOrder": 1,
  "workStatus": "STARTED",
  "canWork": true,
  "acceptedDate": "2026-03-12",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "imageUrl": "/uploads-storage/file.png",
  "notes": "Shu worker bosqichi uchun izoh",
  "orderNotes": "Buyurtma bo'yicha umumiy izoh"
}
```

## 8. Materials

### GET /materials
### POST /materials
### GET /materials/{id}
### PUT /materials/{id}
### DELETE /materials/{id}
### POST /materials/{id}/adjust
### POST /materials/paging

## 9. Expense Categories

### GET /expense-categories
### POST /expense-categories
### GET /expense-categories/{id}
### PUT /expense-categories/{id}
### DELETE /expense-categories/{id}
### POST /expense-categories/paging

## 10. Expenses

### GET /expenses
### POST /expenses
### GET /expenses/{id}
### PUT /expenses/{id}
### DELETE /expenses/{id}
### POST /expenses/paging

## 11. Notifications

### GET /notifications/me

Response:

```json
[
  {
    "id": "uuid",
    "type": "TASK_ACTIVATED",
    "title": "Yangi ish navbati",
    "message": "Oldingi bosqich tugadi. Buyurtma endi sizning navbatingizda",
    "orderId": "uuid",
    "orderName": "Nikoh Albomi",
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
    "readAt": null,
    "createdAt": "2026-04-13T11:00:00"
  }
]
```

### POST /notifications/me/paging

```json
{
  "search": "album",
  "type": "TASK_ACTIVATED",
  "isRead": false,
  "actionRequired": true
}
```

Note:

- Barcha filterlar optional.
- `search` title, message, orderName va employeeName bo'yicha qidiradi.
- `isRead: true` o'qilgan, `isRead: false` o'qilmagan notificationlarni qaytaradi.
- Default sort: `createdAt,desc`.

### GET /notifications/me/unread-count

Response:

```json
{
  "count": 12
}
```

### PUT /notifications/{id}/read

### PUT /notifications/read-all

Response:

- `204 No Content`

## 12. Dashboard

Dashboard endpointlari SQL/JPQL aggregation bilan ishlaydi. Paging yoki `findAll()` ishlatilmaydi.

### GET /dashboard/orders-by-kind

Response:

```json
[
  {
    "key": "ALBUM",
    "count": 56
  },
  {
    "key": "VIGNETTE",
    "count": 10
  },
  {
    "key": "PICTURE",
    "count": 7
  }
]
```

Izoh:

- Barcha `OrderKind` enum qiymatlari qaytadi.
- Bazada order yo'q kindlar `count: 0` bilan qaytadi.
- `count` orderlar soni, `orders.amount` yig'indisi emas.

### GET /dashboard/orders-by-status?type=ALBUM|VIGNETTE|PICTURE

Response:

```json
[
  {
    "key": "PENDING",
    "count": 3
  },
  {
    "key": "IN_PROGRESS",
    "count": 40
  },
  {
    "key": "PAUSED",
    "count": 1
  },
  {
    "key": "COMPLETED",
    "count": 12
  },
  {
    "key": "CANCELLED",
    "count": 0
  }
]
```

Izoh:

- `type` required query param.
- Barcha `OrderStatus` enum qiymatlari qaytadi.
- Tanlangan type uchun order yo'q statuslar `count: 0` bilan qaytadi.
- `count` orderlar soni.

### GET /dashboard/orders-by-category?type=ALBUM|VIGNETTE|PICTURE

Response:

```json
[
  {
    "categoryId": "uuid",
    "categoryName": "A3 albom",
    "count": 2
  }
]
```

Izoh:

- `type` required query param.
- Rasmda ko'ringan `Mahsulot turi bo'yicha hisobot` bloki uchun mos.
- Backend tanlangan `type` dagi barcha product categorylarni qaytaradi.
- Order yo'q categorylar ham `count: 0` bilan keladi.

## 13. Upload

### POST /uploads
Body: `multipart/form-data`

Response:

```json
{
  "id": "uuid",
  "key": "file.png",
  "url": "/uploads-storage/file.png",
  "mimeType": "image/png",
  "size": 12345
}
```

### DELETE /uploads/{key}

## 14. Order Status Histories

### GET /order-status-histories
### POST /order-status-histories
### GET /order-status-histories/{id}
### PUT /order-status-histories/{id}
### DELETE /order-status-histories/{id}

## 15. Socket.IO

### Handshake path

- `GET /socket.io`
- `POST /socket.io`
- `GET /socket.io/`
- `POST /socket.io/`

### Client events

- `authenticate`

Payload:

```json
{
  "token": "access_token"
}
```

`"Bearer <token>"` format ham qabul qilinadi.

### Server events

- `authenticated`
- `auth_error`
- `notification`

`notification` payload:

```json
{
  "id": "uuid",
  "type": "TASK_ACTIVATED",
  "title": "Yangi ish navbati",
  "message": "Oldingi bosqich tugadi. Buyurtma endi sizning navbatingizda",
  "orderId": "uuid",
  "orderName": "Nikoh Albomi",
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

## 16. Migration note

- order create/update requestida `employees[].stepOrder` yuborish
- `employees[].role` yubormaslik
- worker update requestida `status` o'rniga `workStatus` yuborish
- notification preview uchun `POST /api/v1/notifications/me/paging` yoki `GET /api/v1/notifications/me` ishlatish
- notification badge uchun `GET /api/v1/notifications/me/unread-count` ishlatish
- socket notification payloadida `id`, `isRead`, `orderStatus`, `createdAt`, `targetType`, `targetId`, `targetKind`, `route`, `orderKind` maydonlari borligini hisobga olish
