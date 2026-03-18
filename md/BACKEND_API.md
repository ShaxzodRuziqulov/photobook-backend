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
  "processedCount": 20,
  "notes": "20 ta tayyorlandi",
  "workStatus": "COMPLETED"
}
```

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
  "stepOrder": 1,
  "workStatus": "STARTED",
  "canWork": true,
  "acceptedDate": "2026-03-12",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "imageUrl": "/uploads-storage/file.png",
  "notes": "text"
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
### GET /dashboard/orders-by-status
### GET /dashboard/orders-by-kind
### GET /dashboard/revenue-trend
### GET /dashboard/expenses-trend

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
