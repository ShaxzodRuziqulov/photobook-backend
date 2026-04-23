# API Documentation

Bu hujjat loyihadagi Spring Boot REST API bilan sinxron. Barcha REST endpointlar prefiks ostida: **`/api/v1`**. **Workflow / ruxsatlar “nimaga”:** [`BACKEND_LOGIC.md`](BACKEND_LOGIC.md). **Papka tuzilishi:** [`README.md`](README.md). **Tezkor endpoint jadvali:** **§0.1** (bu faylning boshida). **Postman:** `postman/photobook-api.postman_collection.json`.

## 0. Ishga tushirish va vositalar
 
| Parametr | Qiymat |
|----------|--------|
| Standart HTTP port | `9091` (`application.yml`: `server.port`, prod da odatda `PORT` muhit o‘zgaruvchisi) |
| Swagger UI | [`/swagger-ui/index.html`](http://localhost:9091/swagger-ui/index.html) |
| OpenAPI JSON | `/v3/api-docs` |

**Autentifikatsiya:** muhim endpointlar uchun header: `Authorization: Bearer <access_token>`.

**JWT shart emas (filter):** `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me` (Spring `permitAll`), Swagger/OpenAPI yo‘llari, statik fayllar `GET /uploads-storage/**`, Socket.IO `/socket.io/**` (handshake). **Amalda** `GET /auth/me` uchun Bearer token yubilinmasa foydalanuvchi konteksti bo‘lmaydi.

**Refresh / logout tanasi:** `{ "refresh_token": "..." }` (`RefreshTokenRequestDto`).

## 0.1. Barcha endpointlar — tezkor jadval

`SecurityConfiguration` va barcha `@RestController` lar bilan sinxron. **REST prefiks:** `/api/v1`. **Socket / statik:** prefiks yo‘q.

**Postman:** `postman/photobook-api.postman_collection.json`.

### JWT va ochiq yo‘llar

| Yo‘l / pattern | Eslatma |
|----------------|---------|
| `/api/v1/auth/**` | Filterda JWT shart emas; **`GET /auth/me`** amalda Bearer talab qiladi |
| `/socket.io/**` | Handshake (JWT socket ichida `authenticate` event bilan) |
| `/uploads-storage/**` | Statik fayllar (JWT shart emas) |
| `/swagger-ui/**`, `/v3/api-docs/**` | Swagger / OpenAPI |
| `OPTIONS /**` | CORS preflight |

Boshqa REST endpointlar uchun header: `Authorization: Bearer <access_token>`.

### Rol bo‘yicha qisqa xarita (`SecurityConfiguration`)

| Rol | Modullar (qisqa) |
|-----|------------------|
| `ROLE_ADMIN` | `roles/**`, users CRUD (qisman), manager modullari + orders to‘liq |
| `ROLE_ADMIN`, `ROLE_MANAGER` | `customers`, `materials`, `expense-categories`, `expenses`, `product-categories`, `GET /users/**`, `GET /dashboard/**`, `orders` (POST/PUT/DELETE — operatordan boshqa) |
| `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_OPERATOR` | `GET /orders/**`, `PUT /orders/*/status`, `user-tasks/**`, `notifications/**`, `uploads/**`, `GET/PUT /users/me` |

**Operator** buyurtmani faqat ko‘radi va statusni o‘zgartiradi; yaratish/o‘chirish — admin/menejer.

### Auth — `/api/v1/auth`

| Metod | Yo‘l | Tana | Javob |
|-------|------|------|--------|
| POST | `/login` | `{ "username", "password" }` | `AuthTokenResponse` (`access_token`, `refresh_token`, `user`) |
| POST | `/refresh` | `{ "refresh_token" }` | `TokenPairResponse` (`access_token`, `refresh_token`) |
| GET | `/me` | — | `AuthUserResponse` |
| POST | `/logout` | `{ "refresh_token" }` | `204 No Content` |

### Users — `/api/v1/users`

| Metod | Yo‘l | Tana | Eslatma |
|-------|------|------|---------|
| POST | `/` | `UserDto` | Yaratish |
| PUT | `/{id}` | `UserDto` | |
| GET | `/me` | — | Joriy profil |
| PUT | `/me` | `UserProfileUpdateDto` | |
| GET | `/{id}` | — | Admin/menejer: `GET /**` |
| GET | `/` | — | Ro‘yxat |
| POST | `/paging` | `UserPagingRequest` + query `page,size,sort` | `PageResponse<UserDto>` |
| DELETE | `/{id}` | — | |
| PUT | `/{id}/roles` | `UserRoleUpdateDto` | |

### Roles — `/api/v1/roles`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `RoleDto` |
| PUT | `/{id}` | `RoleDto` |
| GET | `/{id}` | — |
| GET | `/` | — |
| POST | `/paging` | `RolePageRequest` + Pageable |
| DELETE | `/{id}` | — |

**Faqat `ROLE_ADMIN`.**

### Customers — `/api/v1/customers`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `CustomerDto` |
| PUT | `/{id}` | `CustomerDto` |
| GET | `/{id}` | — |
| GET | `/` | — |
| POST | `/paging` | `CustomerPagingRequest` + Pageable |
| DELETE | `/{id}` | — |

### Product categories — `/api/v1/product-categories`

| Metod | Yo‘l | Tana / query |
|-------|------|----------------|
| POST | `/` | `ProductCategoryDto` |
| PUT | `/{id}` | `ProductCategoryDto` |
| GET | `/{id}` | — |
| GET | `/` | Query: `kind` = `ALBUM` \| `VIGNETTE` \| `PICTURE` (ixtiyoriy) |
| POST | `/paging` | `ProductCategoryPagingRequest` + Pageable |
| DELETE | `/{id}` | — |

### Orders — `/api/v1/orders`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `OrderDto` |
| PUT | `/{id}` | `OrderDto` |
| GET | `/{id}` | — |
| GET | `/` | — |
| POST | `/paging` | `OrderPagingRequest` + Pageable |
| DELETE | `/{id}` | — |
| PUT | `/{id}/status` | `OrderStatusTransitionDto` (`toStatus`) |
| GET | `/{id}/status-history` | — |

**Hodisalar (bildirishnoma):** `POST /orders` → `notifyOrderAssigned`; `PUT /orders/{id}` → `notifyOrderUpdated`; `PUT /orders/{id}/status` → `notifyOrderStatusChanged` + `notifyTaskActivated`.

### User tasks — `/api/v1/user-tasks`

| Metod | Yo‘l | Tana |
|-------|------|------|
| GET | `/me/{orderId}` | — |
| POST | `/me/paging` | `UserTaskPagingRequest` + Pageable |
| PUT | `/me/{orderId}` | `UserTaskUpdateDto` |

`{orderId}` — buyurtma UUID.

### Materials — `/api/v1/materials`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `MaterialDto` |
| PUT | `/{id}` | `MaterialDto` |
| GET | `/{id}` | — |
| GET | `/` | — |
| POST | `/paging` | `MaterialPagingRequest` + Pageable |
| POST | `/{id}/adjust` | `MaterialAdjustDto` |
| DELETE | `/{id}` | — |

### Expense categories — `/api/v1/expense-categories`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `ExpenseCategoryDto` |
| PUT | `/{id}` | `ExpenseCategoryDto` |
| GET | `/{id}` | — |
| GET | `/` | — |
| POST | `/paging` | `ExpenseCategoryPagingRequest` + Pageable |
| DELETE | `/{id}` | — |

### Expenses — `/api/v1/expenses`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `ExpenseDto` |
| PUT | `/{id}` | `ExpenseDto` |
| GET | `/{id}` | — |
| GET | `/` | — |
| POST | `/paging` | `ExpensePagingRequest` + Pageable |
| DELETE | `/{id}` | — |

### Notifications — `/api/v1/notifications`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/me/paging` | `NotificationPagingRequest` + Pageable |
| GET | `/me/unread-count` | — |
| PUT | `/{id}/read` | — |
| PUT | `/read-all` | — |

**`NotificationPagingRequest`:** `search`, `type`, `isRead`, `actionRequired` (hammasi ixtiyoriy).

**`type` qiymatlari:** `ORDER_ASSIGNED`, `TASK_ACTIVATED`, `ORDER_UPDATED`, `ORDER_STATUS_CHANGED` — repository `LOWER(n.type) = LOWER(:type)`.

### Dashboard — `/api/v1/dashboard`

| Metod | Yo‘l | Query |
|-------|------|--------|
| GET | `/orders-by-kind` | — |
| GET | `/orders-by-status` | `type` majburiy (`OrderKind`) |
| GET | `/orders-by-category` | `type` majburiy (`OrderKind`) |

### Uploads — `/api/v1/uploads`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `multipart/form-data`: `file` (majburiy), `ownerType`, `ownerId` (`UploadDto`) |
| DELETE | `/{idOrKey}` | — UUID yoki `key` |

**JWT:** admin / menejer / operator.

### Order status histories — `/api/v1/order-status-histories`

| Metod | Yo‘l | Tana |
|-------|------|------|
| POST | `/` | `OrderStatusHistoryDto` |
| PUT | `/{id}` | `OrderStatusHistoryDto` |
| GET | `/{id}` | — |
| GET | `/` | — |
| DELETE | `/{id}` | — |

*Eslatma:* buyurtma statusi `PUT /orders/{id}/status` orqali o‘zgarganda tarix avtomatik yoziladi; bu CRUD alohida vositalar uchun.

### Socket.IO va HTTP handshake

| Metod | Yo‘l | Eslatma |
|-------|------|---------|
| GET | `/socket.io`, `/socket.io/` | `EngineIoServer` |
| POST | `/socket.io`, `/socket.io/` | |
| OPTIONS | `/socket.io`, `/socket.io/` | CORS (`SocketIoHttpController`) |

**Muhit:** `application.yml` → `socket.io.path` (default `/socket.io/`).

### Xatoliklar (jadvaldan tashqari)

Ko‘p xatoliklar: `ApiExceptionHandler` — `message`, ixtiyoriy `errors`. Batafsil: [`BACKEND_LOGIC.md`](BACKEND_LOGIC.md) → «Error format».

---

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
  "refresh_token": "string"
}
```

### POST /auth/logout

```json
{
  "refresh_token": "string"
}
```

### GET /auth/me

Joriy token bo‘yicha foydalanuvchi (xavfsizlik konteksti).

### Login va refresh javabi (tana)

Maydonlar **snake_case** (`access_token`, `refresh_token`). Login javobida qo‘shimcha ravishda `user` obyekti bor.

```json
{
  "access_token": "eyJ...",
  "refresh_token": "eyJ...",
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

Logout va refresh tanada `refresh_token` yuboriladi (`RefreshTokenRequestDto`).

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

Query params:

- `page=0`
- `size=10`
- `sort=updatedAt,desc` yoki boshqa `Order` field bo'yicha sort

Request body:

```json
{
  "search": "album",
  "status": "IN_PROGRESS",
  "acceptedDate": "2026-03-01",
  "deadline": "2026-03-31"
}
```

Filterlar:

- `search` optional. `orderName`, `receiverName`, `customer.fullName`, employee full name, employee username va category name ichidan qidiradi.
- `status` optional. Qiymatlar: `PENDING`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `CANCELLED`.
- `acceptedDate` optional. Aniq qabul sanasi bo'yicha filterlaydi.
- `deadline` optional. Aniq tugash sanasi bo'yicha filterlaydi.

Bo'sh filter uchun `{}` yuboriladi:

```json
{}
```

Frontend eslatma:

- `Hammasi` holati uchun `status` yuborilmaydi yoki `null` yuboriladi.
- Sana inputi bo'sh bo'lsa `""` yuborilmaydi; field olib tashlanadi yoki `null` yuboriladi.
- `acceptedDate` va `deadline` hozir range emas, aynan teng sana bo'yicha ishlaydi.

Response:

```json
{
  "content": [],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 120,
  "totalPages": 12,
  "last": false
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
      "changedAt": "2026-04-13T11:00:00"
    }
  ]
}
```

## 7. User Tasks

Operator/menejer/admin o‘ziga biriktirilgan buyurtma zanjiridagi bosqichni ko‘radi va yangilaydi. `{id}` bu yerda **buyurtma `orderId`** (UUID).

### GET /user-tasks/me/{id}

Bitta vazifa (buyurtma + joriy foydalanuvchining `order_employees` qatori).

### POST /user-tasks/me/paging

Query parametrlar (Spring `Pageable`):

- `page` — sahifa, default `0`
- `size` — element soni
- `sort` — masalan `updatedAt,desc` yoki `deadline,asc`

Agar `sort` yuborilmasa, backend default: **`updatedAt` bo‘yicha kamayish**.

Request body (`UserTaskPagingRequest`) — barcha maydonlar ixtiyoriy:

```json
{
  "search": "album",
  "statuses": ["IN_PROGRESS"],
  "deadlineFrom": "2026-03-01",
  "deadlineTo": "2026-03-31"
}
```

Filtrlar:

- **`search`** — `orderName`, `receiverName`, mijoz `fullName`, mahsulot kategoriyasi `name` bo‘yicha `LIKE` (katta-kichik harf farqi yo‘q). **Ishchi ismi/username bo‘yicha qidiruv bu endpointda yo‘q** (admin `POST /orders/paging` da bor).
- **`statuses`** — buyurtma `OrderStatus` ro‘yxati; bo‘sh yoki `null` bo‘lsa status bo‘yicha cheklov qo‘llanmaydi.
- **`deadlineFrom` / `deadlineTo`** — `orders.deadline` oralig‘i (`>=` / `<=`).
- Joriy foydalanuvchiga biriktirilgan buyurtmalar **subquery (`EXISTS`)** bilan saralanadi (sahifalash uchun ortiqcha `JOIN`/`DISTINCT` kamaytirilgan).

Javob: boshqa paging kabi `PageResponse` (`content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`).

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
- **`type`:** bo'sh yoki `null` yuborilsa barcha turlar. Aks holda aniq tur bo'yicha filtrlash — JPQL `LOWER(n.type) = LOWER(:type)` (registr muhim emas). Frontend pastki menyudagi qiymatlar bilan backenddagi **string** lar mos kelishi kerak:
  - `ORDER_ASSIGNED` — buyurtma yaratilganda, navbat hali shu foydalanuvchida **aktiv emas** (yoki order `IN_PROGRESS` emas / assignment `STARTED` emas) bo‘lgan biriktirilganlar.
  - `TASK_ACTIVATED` — navbat foydalanuvchiga tushganda (buyurtma yaratilishi, status o‘zgarishi yoki `PUT /user-tasks/me/{orderId}` dan keyingi workflow).
  - `ORDER_UPDATED` — `PUT /orders/{id}` (buyurtma tahriri).
  - `ORDER_STATUS_CHANGED` — `PUT /orders/{id}/status`.
- `title` va `message` matnlari REST va socketda bir xil — asosan `SocketIoService` dagi literallar (til o‘zgartirish uchun shu servisni tahrirlash).
- `isRead: true` o'qilgan, `isRead: false` o'qilmagan notificationlarni qaytaradi.
- Default sort: `createdAt,desc`.

Response item:

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
  "readAt": null,
  "createdAt": "2026-04-13T11:00:00"
}
```

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
- `count` hozir orderlar soni emas, shu categorydagi orderlarning final step `processed_count` yig'indisi.
- Final processed count yo'q categorylar ham `count: 0` bilan keladi.

## 13. Upload

**Autentifikatsiya:** `POST` va `DELETE` **`/api/v1/uploads/**`** uchun JWT + rol talab qilinadi (`ROLE_ADMIN`, `ROLE_MANAGER` yoki `ROLE_OPERATOR`). Statik yuklab olish: **`GET /uploads-storage/{key}`** — ochiq.

### POST /uploads
Body: `multipart/form-data`

- `file` (majburiy) — `MultipartFile`
- `ownerType` (ixtiyoriy) — `USER` \| `ORDER` \| `EXPENSE`
- `ownerId` (ixtiyoriy) — UUID; `ownerType` bilan birga kelishi kerak

Response:

```json
{
  "id": "uuid",
  "key": "file.png",
  "url": "/uploads-storage/file.png",
  "mime": "image/png",
  "size": 12345
}
```

### DELETE /uploads/{idOrKey}

Path o‘zgaruvchisi — saqlangan yozuvning **`id` (UUID)** yoki fayl **`key`** (string).

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
- `OPTIONS /socket.io` (CORS preflight, `SocketIoHttpController`)
- `GET /socket.io/`
- `POST /socket.io/`
- `OPTIONS /socket.io/`

**Konfig:** `application.yml` → `socket.io.path` (default `/socket.io/`).

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

## 16. Eslatmalar (front / migratsiya)

- Buyurtma yaratish/yangilash: `employees[]` ichida `stepOrder` majburiy; `employees[].role` endi yo‘q.
- Worker yangilash: holat o‘rniga `workStatus` (`STARTED` → `COMPLETED`).
- Bildirishnomalar ro‘yxati: `POST /api/v1/notifications/me/paging`; badge: `GET /api/v1/notifications/me/unread-count`.
- **User task paging:** `acceptedDate` oralig‘i (`from` / `to`) olib tashlangan; faqat `deadlineFrom` / `deadlineTo` va yuqoridagi filtrlar.
- Socket `notification` eventida REST bilan mos maydonlar: `id`, `isRead`, `orderStatus`, `createdAt`, `targetType`, `targetId`, `targetKind`, `route`, `orderKind` va hokazo.

## 17. REST modullar — qisqa eslatma

Barcha yo‘llar **`/api/v1`** ostida. **Metod + yo‘l + tana** jadvali takrorlanmasligi uchun yagona manba: **§0.1** yuqorida.

## 18. Hujjatlar indeksi

| Fayl | Mazmun |
|------|--------|
| [`README.md`](README.md) | Qaysi fayl nima uchun (o‘qish tartibi) |
| **Ushbu `BACKEND_API.md` §0.1** | Barcha REST + socket handshake — tezkor jadval |
| [`BACKEND_LOGIC.md`](BACKEND_LOGIC.md) | Ruxsatlar, workflow, socket oqimi |
| [`FRONTEND_CONTRACT.md`](FRONTEND_CONTRACT.md) | Frontend uchun qisqa contract |
| [`BACKEND_ENTITIES.md`](BACKEND_ENTITIES.md) | Jadval / maydonlar |
| [`RAILWAY.md`](RAILWAY.md) | Deploy va muhit o‘zgaruvchilari |
| `postman/photobook-api.postman_collection.json` | Postman kolleksiyasi |
