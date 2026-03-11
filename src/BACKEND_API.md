# API Documentation

**Base URL:** `/api/v1`

**Auth:**
`Authorization: Bearer <access_token>` login, refresh va swagger'dan tashqari endpointlar uchun yuboriladi.

**Muhim:**
- Oddiy list endpointlar `GET /resource`
- Paging endpointlar `POST /resource/paging`
- Update endpointlar asosan `PUT`

---

# 1. Auth

### POST /auth/login

```json
{
  "username": "string",
  "password": "string"
}
```

Response:

```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "user": {
    "id": "uuid",
    "name": "string",
    "email": "string",
    "roles": ["ROLE_ADMIN"],
    "avatarUrl": "string",
    "phone": "string",
    "bio": "string"
  }
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

Response: `204`

### GET /auth/me

Response:

```json
{
  "id": "uuid",
  "name": "string",
  "email": "string",
  "roles": ["ROLE_ADMIN"],
  "avatarUrl": "string",
  "phone": "string",
  "bio": "string"
}
```

---

# 2. Users

### GET /users

Response: `List<UserDto>`

### POST /users

```json
{
  "firstName": "string",
  "lastName": "string",
  "middleName": "string",
  "username": "string",
  "email": "string",
  "password": "string",
  "avatarUrl": "string",
  "phone": "string",
  "bio": "string",
  "isActive": true
}
```

### GET /users/{id}

### PUT /users/{id}

Body `POST /users` bilan bir xil, `password` optional.

### DELETE /users/{id}

Soft delete ko‘rinishida `isActive=false`.

### PUT /users/{id}/roles

```json
{
  "roleIds": ["uuid"]
}
```

### POST /users/paging

Body:

```json
{
  "search": "ali",
  "isActive": true,
  "role": "ROLE_ADMIN"
}
```

Response:

```json
{
  "items": [],
  "page": 1,
  "limit": 10,
  "total": 0,
  "totalPages": 0
}
```

---

# 3. Roles

### GET /roles

### POST /roles

```json
{
  "name": "ROLE_ADMIN",
  "description": "string"
}
```

### GET /roles/{id}

### PUT /roles/{id}

### DELETE /roles/{id}

### POST /roles/paging

```json
{
  "search": "admin"
}
```

---

# 4. Customers

### GET /customers

### POST /customers

```json
{
  "fullName": "string",
  "phone": "string",
  "notes": "string",
  "isActive": true
}
```

### GET /customers/{id}

### PUT /customers/{id}

### DELETE /customers/{id}

Soft delete ko‘rinishida `isActive=false`.

### POST /customers/paging

```json
{
  "search": "ali",
  "isActive": true
}
```

---

# 5. Employees

### GET /employees

### POST /employees

```json
{
  "fullName": "string",
  "profession": "string",
  "phoneNumber": "string",
  "isActive": true
}
```

### GET /employees/{id}

### PUT /employees/{id}

### DELETE /employees/{id}

Soft delete ko‘rinishida `isActive=false`.

### POST /employees/paging

```json
{
  "search": "ali",
  "profession": "Albomchi",
  "isActive": true
}
```

---

# 6. Product Categories

### GET /product-categories

### POST /product-categories

```json
{
  "name": "string",
  "kind": "ALBUM",
  "defaultPages": 12
}
```

### GET /product-categories/{id}

### PUT /product-categories/{id}

### DELETE /product-categories/{id}

### POST /product-categories/paging

```json
{
  "search": "maktab",
  "kind": "ALBUM"
}
```

---

# 7. Orders

### GET /orders

### POST /orders

```json
{
  "kind": "ALBUM",
  "categoryId": "uuid",
  "orderName": "string",
  "itemType": "string",
  "customerId": "uuid",
  "receiverName": "string",
  "employeeId": "uuid",
  "pageCount": 10,
  "amount": 100,
  "processedCount": 0,
  "acceptedDate": "2026-03-11",
  "deadline": "2026-03-20",
  "status": "PENDING",
  "imageUrl": "string",
  "notes": "string"
}
```

### GET /orders/{id}

### PUT /orders/{id}

### DELETE /orders/{id}

### PUT /orders/{id}/status

```json
{
  "toStatus": "IN_PROGRESS"
}
```

### GET /orders/{id}/status-history

### POST /orders/paging

```json
{
  "search": "maktab",
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

Order response ichida odatda quyidagilar ham qaytadi:

```json
{
  "id": "uuid",
  "kind": "ALBUM",
  "categoryId": "uuid",
  "categoryName": "string",
  "orderName": "string",
  "itemType": "string",
  "customerId": "uuid",
  "customerName": "string",
  "receiverName": "string",
  "employeeId": "uuid",
  "employeeName": "string",
  "pageCount": 0,
  "amount": 0,
  "processedCount": 0,
  "acceptedDate": "2026-03-11",
  "deadline": "2026-03-20",
  "status": "PENDING",
  "imageUrl": "string",
  "notes": "string"
}
```

---

# 8. Materials

### GET /materials

### POST /materials

```json
{
  "itemName": "string",
  "itemType": "string",
  "unitName": "string",
  "quantity": 0
}
```

### GET /materials/{id}

### PUT /materials/{id}

### DELETE /materials/{id}

### POST /materials/{id}/adjust

```json
{
  "delta": -2,
  "reason": "string"
}
```

### POST /materials/paging

```json
{
  "search": "paper",
  "itemType": "A4"
}
```

---

# 9. Expense Categories

### GET /expense-categories

### POST /expense-categories

```json
{
  "name": "string"
}
```

### GET /expense-categories/{id}

### PUT /expense-categories/{id}

### DELETE /expense-categories/{id}

### POST /expense-categories/paging

```json
{
  "search": "transport"
}
```

---

# 10. Expenses

### GET /expenses

### POST /expenses

```json
{
  "categoryId": "uuid",
  "materialId": "uuid",
  "name": "string",
  "price": 0,
  "description": "string",
  "paymentMethod": "string",
  "receiptImageUrl": "string",
  "expenseDate": "2026-03-11"
}
```

### GET /expenses/{id}

### PUT /expenses/{id}

### DELETE /expenses/{id}

### POST /expenses/paging

```json
{
  "search": "yoqilgi",
  "categoryId": "uuid",
  "materialId": "uuid",
  "paymentMethod": "cash"
}
```

---

# 11. Dashboard

### GET /dashboard/summary

Query:

```text
from=2026-03-01&to=2026-03-31
```

### GET /dashboard/orders-by-status

### GET /dashboard/orders-by-kind

### GET /dashboard/revenue-trend

### GET /dashboard/expenses-trend

---

# 12. Upload

### POST /uploads

Body: `multipart/form-data`

Response:

```json
{
  "url": "/uploads-storage/file.jpg",
  "key": "file.jpg",
  "mime": "image/jpeg",
  "size": 102400
}
```

### DELETE /uploads/{key}

Response: `204`

---

# 13. Order Status Histories

### GET /order-status-histories

### POST /order-status-histories

### GET /order-status-histories/{id}

### PUT /order-status-histories/{id}

### DELETE /order-status-histories/{id}
