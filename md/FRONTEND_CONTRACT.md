# Frontend Contract

Bu fayl frontend uchun **ixtisoslashtirilgan qisqa** contract. Transport + barcha yo‘llar jadvali: [`BACKEND_API.md`](BACKEND_API.md) (§0 va **§0.1**); batafsil misollar — shu faylning keyingi bo‘limlari; qoidalar/oqim — [`BACKEND_LOGIC.md`](BACKEND_LOGIC.md); sxema — [`BACKEND_ENTITIES.md`](BACKEND_ENTITIES.md). Papka: [`README.md`](README.md). Postman: `postman/photobook-api.postman_collection.json`.

## Base

- Base URL: `http://localhost:9091`
- API prefix: `/api/v1`
- Auth header: `Authorization: Bearer <access_token>`
- Public (Spring Security `permitAll`): `/api/v1/auth/**`, `/uploads-storage/**`, `/socket.io/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **`GET /api/v1/auth/me`** filter zanjirida ochiq bo‘lsa-da, controller joriy foydalanuvchini JWT dan oladi — **amalda doim `Authorization: Bearer`** yuboring (aks holda foydalanuvchi topilmaydi).
- **`POST/DELETE /api/v1/uploads/**` JWT + rol kerak** (operator ham chaqira oladi); faqat statik `GET /uploads-storage/...` ochiq.
- Static upload URL: `/uploads-storage/{key}`

## Enums

- `OrderKind`: `ALBUM`, `VIGNETTE`, `PICTURE`
- `OrderStatus`: `PENDING`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `CANCELLED`
- `EmployeeWorkStatus`: `PENDING`, `STARTED`, `COMPLETED`
- `OwnerType`: `USER`, `ORDER`, `EXPENSE`

## Common Paging

Paging endpointlar odatda `POST /resource/paging?page=0&size=10&sort=updatedAt,desc`.

```json
{
  "content": [],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 0,
  "totalPages": 0,
  "last": true
}
```

Bo'sh filter uchun `{}` yuboring. Sana input bo'sh bo'lsa `""` yubormang; fieldni olib tashlang yoki `null` yuboring.

## Auth

### POST `/api/v1/auth/login`

```json
{
  "username": "admin",
  "password": "password"
}
```

Response:

```json
{
  "access_token": "jwt",
  "refresh_token": "jwt",
  "user": {
    "id": "uuid",
    "name": "Admin User",
    "roles": ["ROLE_ADMIN"],
    "avatar_url": "/uploads-storage/avatar.png",
    "phone": "+998901234567",
    "bio": "text"
  }
}
```

### POST `/api/v1/auth/refresh`

Request (backend `RefreshTokenRequestDto` — **snake_case**):

```json
{
  "refresh_token": "jwt"
}
```

Response:

```json
{
  "access_token": "jwt",
  "refresh_token": "jwt"
}
```

### GET `/api/v1/auth/me`

Response `user` formati login ichidagi `user` bilan bir xil.

### POST `/api/v1/auth/logout`

Request:

```json
{
  "refresh_token": "jwt"
}
```

Response: `204 No Content`.

## Orders

Endpoints:

- `GET /api/v1/orders`
- `GET /api/v1/orders/{id}`
- `POST /api/v1/orders`
- `PUT /api/v1/orders/{id}`
- `DELETE /api/v1/orders/{id}`
- `POST /api/v1/orders/paging?page=0&size=10&sort=updatedAt,desc`
- `PUT /api/v1/orders/{id}/status`
- `GET /api/v1/orders/{id}/status-history`

Create/update request:

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
    { "employeeId": "uuid", "stepOrder": 1 },
    { "employeeId": "uuid", "stepOrder": 2 }
  ],
  "pageCount": 20,
  "amount": 50,
  "acceptedDate": "2026-03-12",
  "deadline": "2026-03-20",
  "status": "IN_PROGRESS",
  "notes": "text",
  "uploadId": "uuid"
}
```

Rules:

- `customerId` yoki `customerName` majburiy.
- `employees` bo'sh bo'lmasligi kerak.
- `employees[].employeeId` order ichida unique.
- `employees[].stepOrder` 1 dan boshlab ketma-ket.
- `amount > 0`, `pageCount >= 0`, `deadline >= acceptedDate`.
- `processedCount` response ichida final step progressidan olinadi.
- `currentStepProcessedCount` active worker progressidan olinadi.

Order paging filter:

```json
{
  "search": "album",
  "status": "IN_PROGRESS",
  "acceptedDate": "2026-03-01",
  "deadline": "2026-03-31"
}
```

Filter qoidalari:

- `acceptedDate` interval boshi, `deadline` interval oxiri sifatida ishlaydi.
- Order tanlangan oraliq bilan kesishsa chiqadi: `order.acceptedDate <= deadline` va `order.deadline >= acceptedDate`.
- Ikkalasi ham yuborilmasa backend default oxirgi 1 oylik intervalni ishlatadi.
- Faqat bittasi yuborilsa, ikkinchi tomoni ochiq interval bo'ladi.
- `search`: order name, receiver, customer full name, employee name/username, category name.
- `status` bo'lmasa hammasi qaytadi.

Status update:

```json
{
  "toStatus": "PAUSED"
}
```

Ruxsat etilgan status transitionlar:

- `PENDING -> IN_PROGRESS`, `PENDING -> PAUSED`, `PENDING -> COMPLETED`, `PENDING -> CANCELLED`
- `IN_PROGRESS -> PENDING`, `IN_PROGRESS -> PAUSED`, `IN_PROGRESS -> COMPLETED`, `IN_PROGRESS -> CANCELLED`
- `PAUSED -> PENDING`, `PAUSED -> IN_PROGRESS`, `PAUSED -> COMPLETED`, `PAUSED -> CANCELLED`
- `COMPLETED -> PENDING`, `COMPLETED -> IN_PROGRESS`, `COMPLETED -> PAUSED`
- `CANCELLED -> PENDING`, `CANCELLED -> IN_PROGRESS`

`COMPLETED` ga o'tishdan oldin hamma employee `COMPLETED` bo'lishi kerak.

## User Tasks

Endpoints:

- `GET /api/v1/user-tasks/me/{orderId}`
- `POST /api/v1/user-tasks/me/paging?page=0&size=10&sort=updatedAt,desc`
- `PUT /api/v1/user-tasks/me/{orderId}`

Paging filter:

```json
{
  "search": "album",
  "statuses": ["IN_PROGRESS"],
  "deadlineFrom": "2026-03-01",
  "deadlineTo": "2026-03-31"
}
```

Update request:

```json
{
  "processedCount": 5,
  "notes": "5 ta tayyorlandi",
  "workStatus": "COMPLETED"
}
```

Rules:

- `{orderId}` bu order id.
- Worker faqat o'zining active taskini update qila oladi.
- `canWork = true` bo'lishi uchun order `IN_PROGRESS`, assignment `STARTED`.
- `processedCount` increment, jami qiymat emas.
- `workStatus` faqat `STARTED -> COMPLETED`.
- `processedCount` order `amount` va oldingi step progressidan oshmasligi kerak.

## Notifications

Endpoints:

- `POST /api/v1/notifications/me/paging?page=0&size=10&sort=createdAt,desc`
- `GET /api/v1/notifications/me/unread-count`
- `PUT /api/v1/notifications/{id}/read`
- `PUT /api/v1/notifications/read-all`

Paging filter:

```json
{
  "search": "album",
  "type": "TASK_ACTIVATED",
  "isRead": false,
  "actionRequired": true
}
```

**`type` (ixtiyoriy):** `ORDER_ASSIGNED` \| `TASK_ACTIVATED` \| `ORDER_UPDATED` \| `ORDER_STATUS_CHANGED` — backend `LOWER` bilan solishtiradi; `Hammasi` uchun maydonni omit qiling yoki `null`.

Notification item:

```json
{
  "id": "uuid",
  "type": "TASK_ACTIVATED",
  "title": "Yangi ish navbati",
  "message": "Oldingi bosqich tugadi",
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
```

Unread count:

```json
{
  "count": 12
}
```

## Dashboard

Endpoints:

- `GET /api/v1/dashboard/orders-by-kind`
- `GET /api/v1/dashboard/orders-by-status?type=ALBUM`
- `GET /api/v1/dashboard/orders-by-category?type=ALBUM`

Enum count response:

```json
[
  { "key": "IN_PROGRESS", "count": 10 },
  { "key": "COMPLETED", "count": 5 }
]
```

Category count response:

```json
[
  {
    "categoryId": "uuid",
    "categoryName": "A3 albom",
    "count": 12
  }
]
```

Important:

- `orders-by-kind` va `orders-by-status` count orderlar soni.
- `orders-by-category` count final step `processed_count` yig'indisi, orderlar soni emas.

## Uploads

### POST `/api/v1/uploads`

**Authorization:** Bearer majburiy (`ROLE_ADMIN` \| `ROLE_MANAGER` \| `ROLE_OPERATOR`).

Body: `multipart/form-data`

- `file`: image file
- `ownerType`: optional, `USER|ORDER|EXPENSE`
- `ownerId`: optional UUID

`ownerType` va `ownerId` birga yuboriladi yoki ikkalasi ham yuborilmaydi.

Response:

```json
{
  "id": "uuid",
  "url": "/uploads-storage/file.png",
  "key": "file.png",
  "mime": "image/png",
  "size": 12345
}
```

### DELETE `/api/v1/uploads/{idOrKey}`

Path: upload **`id`** (UUID) yoki **`key`** (string).

Response: `204 No Content`.

## Reference CRUD

Customers:

- `GET|POST /api/v1/customers`
- `GET|PUT|DELETE /api/v1/customers/{id}`
- `POST /api/v1/customers/paging`

Product categories:

- `GET /api/v1/product-categories?kind=ALBUM`
- `POST /api/v1/product-categories`
- `GET|PUT|DELETE /api/v1/product-categories/{id}`
- `POST /api/v1/product-categories/paging`

Users:

- `GET|POST /api/v1/users`
- `GET|PUT|DELETE /api/v1/users/{id}`
- `GET|PUT /api/v1/users/me`
- `PUT /api/v1/users/{id}/roles`
- `POST /api/v1/users/paging`

Roles:

- `GET|POST /api/v1/roles`
- `GET|PUT|DELETE /api/v1/roles/{id}`
- `POST /api/v1/roles/paging`

Materials:

- `GET|POST /api/v1/materials`
- `GET|PUT|DELETE /api/v1/materials/{id}`
- `POST /api/v1/materials/{id}/adjust`
- `POST /api/v1/materials/paging`

Expense categories:

- `GET|POST /api/v1/expense-categories`
- `GET|PUT|DELETE /api/v1/expense-categories/{id}`
- `POST /api/v1/expense-categories/paging`

Expenses:

- `GET|POST /api/v1/expenses`
- `GET|PUT|DELETE /api/v1/expenses/{id}`
- `POST /api/v1/expenses/paging`

## Socket.IO

Path: `/socket.io/`

Client event:

- `authenticate`

Payload:

```json
{
  "token": "access_token"
}
```

Server events:

- `authenticated`
- `auth_error`
- `notification`

Recommended client:

```js
const socket = io(API_BASE_URL, {
  path: "/socket.io/",
  transports: ["websocket"],
  withCredentials: true,
});
```

## Error Shape

Ko'p xatoliklar quyidagi formatda keladi:

```json
{
  "message": "category_id is required",
  "errors": {
    "request": ["category_id is required"]
  }
}
```
