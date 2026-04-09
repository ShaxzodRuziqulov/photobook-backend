# API Documentation

**Base URL:** `/api/v1`

**Auth:**
`Authorization: Bearer <access_token>` login, refresh va swagger'dan tashqari endpointlar uchun yuboriladi.

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
  "kind": "ALBUM",
  "status": "IN_PROGRESS",
  "customerId": "uuid",
  "employeeId": "uuid",
  "categoryId": "uuid",
  "from": "2026-03-01",
  "to": "2026-03-31",
  "deadlineFrom": "2026-03-01",
  "deadlineTo": "2026-03-31"
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
  "statusHistory": []
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
  "notes": "20 ta tayyorlandi",
  "workStatus": "COMPLETED"
}
```

Note:

- `processedCount` bu jami emas, aynan shu submitda yangi bajarilgan son
- backend uni oldingi progressga qo'shib saqlaydi
- agar jami progress `amount` ga yetsa, step avtomatik `COMPLETED` bo'ladi va keyingi worker `STARTED` bo'ladi
- `availableToProcess` oldingi bosqich nechta tayyorlab berganini bildiradi
- `remainingAvailable` worker hozir yana nechta qila olishini bildiradi
- `remainingTotal` workerning umumiy tugatishi uchun qolgan sonni bildiradi
- `notes` workerning aynan shu bosqichdagi izohi
- `orderNotes` buyurtmaning umumiy izohi

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

## 11. Dashboard

### GET /dashboard/summary
Optional query:

- `from=2026-03-01`
- `to=2026-03-31`

Response:

```json
{
  "ordersTotal": 24,
  "ordersDone": 10,
  "ordersInProgress": 8,
  "revenueTotal": 1500000,
  "expensesTotal": 400000,
  "profit": 1100000
}
```

### GET /dashboard/orders-by-status?type=ALBUM|VIGNETTE|PICTURE
Note:

- `type` majburiy
- response har doim barcha statuslarni qaytaradi: `PENDING`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`

Response:

```json
[
  { "key": "PENDING", "count": 0 },
  { "key": "IN_PROGRESS", "count": 4 },
  { "key": "PAUSED", "count": 0 },
  { "key": "COMPLETED", "count": 6 }
]
```

### GET /dashboard/orders-by-kind
Note:

- response har doim barcha kindlarni qaytaradi: `ALBUM`, `VIGNETTE`, `PICTURE`

Response:

```json
[
  { "key": "ALBUM", "count": 10 },
  { "key": "VIGNETTE", "count": 20 },
  { "key": "PICTURE", "count": 5 }
]
```

### GET /dashboard/orders-by-category?type=ALBUM|VIGNETTE|PICTURE
Note:

- `type` majburiy
- `key` bu `product_categories.name`
- tanlangan `kind` ichidagi categorylarda order bo'lmasa ham `count: 0` bilan qaytadi

Response:

```json
[
  { "key": "Premium Album", "count": 7 },
  { "key": "Mini Album", "count": 3 }
]
```

### GET /dashboard/revenue-trend
Response:

```json
[
  { "period": "2026-01", "amount": 500000 },
  { "period": "2026-02", "amount": 750000 },
  { "period": "2026-03", "amount": 250000 }
]
```

### GET /dashboard/expenses-trend
Response:

```json
[
  { "period": "2026-01", "amount": 100000 },
  { "period": "2026-02", "amount": 150000 },
  { "period": "2026-03", "amount": 50000 }
]
```

## 12. Upload

### POST /uploads
Body: `multipart/form-data`

### DELETE /uploads/{key}

## 13. Order Status Histories

### GET /order-status-histories
### POST /order-status-histories
### GET /order-status-histories/{id}
### PUT /order-status-histories/{id}
### DELETE /order-status-histories/{id}

## 14. Migration note

Front quyidagicha yangilanishi kerak:

- order create/update requestida `employees[].stepOrder` yuborish
- `employees[].role` yubormaslik
- worker update requestida `status` o'rniga `workStatus` yuborish
- order response ichidagi `activeEmployeeId`, `activeEmployeeName`, `currentStepProcessedCount` maydonlarini ishlatish mumkin
