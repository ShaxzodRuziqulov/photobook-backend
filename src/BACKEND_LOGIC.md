# BACKEND LOGIC

Bu fayl hozirgi backendning amaldagi logikasini tushuntiradi. Bu yerda nazariy tavsiya emas, frontend va backend integratsiyasi uchun real contract yozilgan.

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

Misol:

```json
{
  "id": "uuid",
  "name": "Sample"
}
```

### Paging endpointlar

Paging endpointlar `POST /resource/paging` ko'rinishida.

Response format:

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

Muhim:

- `pageNumber` 0-based
- `pageSize` Spring pageable qiymati
- paging filter body orqali yuboriladi

### Error format

Validation va business xatolar odatda:

```json
{
  "message": "category_id is required",
  "errors": {
    "request": ["category_id is required"]
  }
}
```

Upload size xatosi:

```json
{
  "message": "Maximum upload size exceeded",
  "errors": {
    "file": ["Maximum upload size exceeded"]
  }
}
```

## 3. Auth

### Endpointlar

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`

### Login

Request:

```json
{
  "username": "admin",
  "password": "secret"
}
```

Response:

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

Muhim:

- login `username` bilan ishlaydi
- `email` field response classda bor, lekin hozir service uni to'ldirmaydi
- refresh token DBda saqlanmaydi, JWT asosida validatsiya qilinadi

### Refresh

Request:

```json
{
  "refreshToken": "jwt"
}
```

Response:

```json
{
  "access_token": "jwt",
  "refresh_token": "jwt"
}
```

### Me

Current user profilini qaytaradi.

### Logout

Request body:

```json
{
  "refreshToken": "jwt"
}
```

Response: `204 No Content`

Muhim:

- logout refresh tokenni format bo'yicha tekshiradi
- token blacklist saqlash logikasi yo'q

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
- uploads
- dashboard

### `ROLE_OPERATOR`

Ruxsat:

- `GET /api/v1/orders/**`
- `PUT /api/v1/orders/{id}/status`
- `/api/v1/uploads/**`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`

Muhim:

- operator order create/update/delete qila olmaydi
- uploads endpoint operator uchun ochiq, lekin users/orders/expenses attach logikasi servis ichida tekshiriladi

## 5. Users

### Endpointlar

- `POST /api/v1/users`
- `PUT /api/v1/users/{id}`
- `GET /api/v1/users/{id}`
- `GET /api/v1/users`
- `POST /api/v1/users/paging`
- `DELETE /api/v1/users/{id}`
- `PUT /api/v1/users/{id}/roles`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`

### UserDto fieldlar

```json
{
  "id": "uuid",
  "firstName": "Ali",
  "lastName": "Valiyev",
  "profession": "Designer",
  "username": "ali",
  "email": null,
  "password": "secret",
  "avatarUrl": "/uploads-storage/file.png",
  "phone": "+998901234567",
  "bio": "text",
  "isActive": true,
  "uploadId": "uuid",
  "roles": []
}
```

### Yaratish logikasi

- `username` majburiy
- `firstName` majburiy
- `lastName` majburiy
- `password` majburiy
- password hash qilinadi
- default role: `ROLE_OPERATOR`
- `userStatus = ACTIVE`
- `isActive` bo'sh bo'lsa `true`
- `uploadId` berilsa avatar attach qilinadi

### Update logikasi

- `password` ixtiyoriy
- `username` o'zgarsa unique tekshiriladi
- `uploadId` berilsa avatar attach qilinadi

### `/users/me`

Current login bo'lgan user profili uchun ishlaydi.

Update body:

```json
{
  "firstName": "Ali",
  "lastName": "Valiyev",
  "profession": "Designer",
  "email": null,
  "avatarUrl": "/uploads-storage/file.png",
  "phone": "+998901234567",
  "bio": "text",
  "uploadId": "uuid"
}
```

Muhim:

- `uploadId` berilsa backend `avatarUrl` ni o'zi set qiladi
- `uploadId` berilmasa `avatarUrl` ni plain string sifatida update qilish mumkin

### Delete logikasi

- hard delete emas
- `isActive = false`
- agar userga upload bog'langan bo'lsa, u ham tozalanadi

### Role update

Request:

```json
{
  "roleIds": ["uuid"]
}
```

## 6. Roles

### Endpointlar

- `POST /api/v1/roles`
- `PUT /api/v1/roles/{id}`
- `GET /api/v1/roles/{id}`
- `GET /api/v1/roles`
- `POST /api/v1/roles/paging`
- `DELETE /api/v1/roles/{id}`

### RoleDto

```json
{
  "id": "uuid",
  "name": "ROLE_ADMIN",
  "description": "Admin"
}
```

## 7. Customers

### Endpointlar

- `POST /api/v1/customers`
- `PUT /api/v1/customers/{id}`
- `GET /api/v1/customers/{id}`
- `GET /api/v1/customers`
- `POST /api/v1/customers/paging`
- `DELETE /api/v1/customers/{id}`

### CustomerDto

```json
{
  "id": "uuid",
  "fullName": "Ali Valiyev",
  "phone": "+998901234567",
  "notes": "VIP",
  "isActive": true
}
```

### Muhim logika

- order create/update da `customerId` yoki `customerName` ishlatiladi
- agar `customerId` bo'lmasa va `customerName` kelsa, backend order ichida yangi customer yaratadi
- delete behavior servicega bog'liq, lekin controller soft/hard ni alohida ajratmaydi

## 8. Product Categories

### Endpointlar

- `POST /api/v1/product-categories`
- `PUT /api/v1/product-categories/{id}`
- `GET /api/v1/product-categories/{id}`
- `GET /api/v1/product-categories`
- `POST /api/v1/product-categories/paging`
- `DELETE /api/v1/product-categories/{id}`

### ProductCategoryDto

```json
{
  "id": "uuid",
  "name": "Premium Album",
  "kind": "ALBUM",
  "defaultPages": "20",
  "size": "30x40"
}
```

### `kind` qiymatlari

- `ALBUM`
- `VIGNETTE`
- `PICTURE`

## 9. Orders

Bu backendning asosiy moduli.

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
      "employeeIds": "uuid",
      "employeeNames": "Vali"
    }
  ],
  "pageCount": 20,
  "amount": 2,
  "processedCount": 0,
  "acceptedDate": "2026-03-16",
  "deadline": "2026-03-20",
  "status": "PENDING",
  "imageUrl": "/uploads-storage/file.png",
  "notes": "text",
  "uploadId": "uuid",
  "statusHistory": []
}
```

### Create va update validation

- `orderName` majburiy
- `categoryId` majburiy
- `customerId` yoki `customerName` majburiy
- `employees` majburiy va bo'sh bo'lmasligi kerak
- `employees` ichida `null` bo'lmasligi kerak
- har bir `employees[].employeeIds` majburiy
- `amount > 0`
- `processedCount >= 0`
- `processedCount <= amount`
- `acceptedDate` majburiy
- `deadline` majburiy
- `deadline >= acceptedDate`

### Create va update business logic

- `categoryId` bo'yicha category topiladi
- `customerId` bo'lsa mavjud customer ishlatiladi
- `customerId` bo'lmasa `customerName` dan yangi customer yaratiladi
- `employees[].employeeIds` dagi userlar resolve qilinadi
- `uploadId` bo'lsa upload `ORDER` ga attach qilinadi
- attachdan keyin `imageUrl` avtomatik set qilinadi

### Status o'zgarishi

Request:

```json
{
  "toStatus": "IN_PROGRESS"
}
```

Ruxsat etilgan transitionlar:

- `PENDING -> IN_PROGRESS`
- `IN_PROGRESS -> PENDING`
- `IN_PROGRESS -> COMPLETED`

Ruxsat etilmaydi:

- `PENDING -> COMPLETED`
- `COMPLETED -> *`

Status o'zgarsa:

- order status update qilinadi
- `order_status_history` yozuvi yaratiladi
- `changedBy` current user dan olinadi

### Paging filter

Request body:

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

## 10. Order Status History

### Endpointlar

- `GET /api/v1/orders/{id}/status-history`
- qo'shimcha CRUD endpoint ham bor:
  - `POST /api/v1/order-status-histories`
  - `PUT /api/v1/order-status-histories/{id}`
  - `GET /api/v1/order-status-histories/{id}`
  - `GET /api/v1/order-status-histories`
  - `DELETE /api/v1/order-status-histories/{id}`

Frontend uchun asosan kerakli endpoint:

- `GET /api/v1/orders/{id}/status-history`

## 11. Materials

### Endpointlar

- `POST /api/v1/materials`
- `PUT /api/v1/materials/{id}`
- `GET /api/v1/materials/{id}`
- `GET /api/v1/materials`
- `POST /api/v1/materials/paging`
- `DELETE /api/v1/materials/{id}`
- `POST /api/v1/materials/{id}/adjust`

### MaterialDto

```json
{
  "id": "uuid",
  "itemName": "Photo paper",
  "itemType": "Glossy",
  "unitName": "sheet",
  "quantity": 100
}
```

### Validation

- `itemName` majburiy
- `unitName` majburiy
- `quantity >= 0`

### Adjust

Request:

```json
{
  "delta": -2,
  "reason": "used"
}
```

Muhim:

- backend faqat `delta` ni ishlatadi
- `reason` business uchun saqlanmaydi
- yangi `quantity` manfiy bo'lsa xato qaytadi

## 12. Expense Categories

### Endpointlar

- `POST /api/v1/expense-categories`
- `PUT /api/v1/expense-categories/{id}`
- `GET /api/v1/expense-categories/{id}`
- `GET /api/v1/expense-categories`
- `POST /api/v1/expense-categories/paging`
- `DELETE /api/v1/expense-categories/{id}`

### ExpenseCategoryDto

```json
{
  "id": "uuid",
  "name": "Transport"
}
```

## 13. Expenses

### Endpointlar

- `POST /api/v1/expenses`
- `PUT /api/v1/expenses/{id}`
- `GET /api/v1/expenses/{id}`
- `GET /api/v1/expenses`
- `POST /api/v1/expenses/paging`
- `DELETE /api/v1/expenses/{id}`

### ExpenseDto

```json
{
  "id": "uuid",
  "categoryId": "uuid",
  "materialId": "uuid",
  "name": "Qog'oz xaridi",
  "price": 120000,
  "description": "text",
  "paymentMethod": "cash",
  "receiptImageUrl": "/uploads-storage/file.png",
  "expenseDate": "2026-03-16",
  "uploadId": "uuid"
}
```

### Validation

- `categoryId` majburiy
- `name` majburiy
- `price >= 0`
- `expenseDate` majburiy

### Business logic

- `categoryId` bo'yicha category resolve qilinadi
- `materialId` bo'lsa material resolve qilinadi
- `uploadId` bo'lsa upload `EXPENSE` ga attach qilinadi
- attachdan keyin `receiptImageUrl` avtomatik set qilinadi

### Delete

- expense o'chirilganda unga tegishli upload ham tozalanadi

## 14. Uploads

Bu qism hozir real ishlaydi va frontend uchun eng muhim integration pointlardan biri.

### Endpointlar

- `POST /api/v1/uploads`
- `DELETE /api/v1/uploads/{key}`
- `GET /uploads-storage/{key}`

### Upload request

`multipart/form-data`

Fieldlar:

- `file` - majburiy
- `ownerType` - ixtiyoriy
- `ownerId` - ixtiyoriy

`ownerType` qiymatlari:

- `USER`
- `ORDER`
- `EXPENSE`

### Upload response

```json
{
  "id": "uuid",
  "url": "/uploads-storage/uuid-photo.png",
  "key": "uuid-photo.png",
  "mime": "image/png",
  "size": 102400
}
```

### Validatsiya

- `file` bo'sh bo'lmasligi kerak
- faqat `image/*`
- fayl hajmi `spring.servlet.multipart.max-file-size` dan oshmasligi kerak
- `ownerType` va `ownerId` ikkalasi birga kelishi kerak
- owner mavjud bo'lishi kerak

### Saqlash logikasi

- default papka: `uploads-storage`
- env: `APP_UPLOAD_DIR`
- fayl nomi sanitizatsiya qilinadi
- DBga `key`, `mimeType`, `size`, `ownerType`, `ownerId` saqlanadi

### Frontend uchun to'g'ri flow

1. Rasmni `POST /api/v1/uploads` orqali yuklang.
2. Response dan `id`, `url`, `key` ni oling.
3. Create/update requestga `uploadId` yuboring.
4. UI preview uchun `url` ni ishlating.

Amaliy ishlatish:

- `uploadId` - create/update uchun
- `url` - preview uchun
- `key` - alohida delete qilish uchun

### Attach logikasi

`uploadId` create/update requestga berilganda:

- order uchun `imageUrl` avtomatik set qilinadi
- expense uchun `receiptImageUrl` avtomatik set qilinadi
- user uchun `avatarUrl` avtomatik set qilinadi

### Replace logikasi

Bitta ownerga yangi upload attach qilinsa:

- ownerning eski uploadi topiladi
- eski DB yozuvi o'chiriladi
- eski fayl diskdan o'chiriladi
- owner yangi uploadga o'tadi

### Delete logikasi

`DELETE /api/v1/uploads/{key}` chaqirilsa:

1. upload topiladi
2. owner reference null qilinadi
3. fizik fayl o'chiriladi
4. upload DBdan o'chiriladi

### Owner delete bo'lsa

- user delete bo'lsa upload ham tozalanadi
- order delete bo'lsa upload ham tozalanadi
- expense delete bo'lsa upload ham tozalanadi

### Muhim production cheklovi

Upload hozir lokal diskda saqlanadi. Persistent volume bo'lmasa deploy yoki restartdan keyin fayllar yo'qolishi mumkin.

## 15. Dashboard

### Endpointlar

- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/orders-by-status`
- `GET /api/v1/dashboard/orders-by-kind`
- `GET /api/v1/dashboard/revenue-trend`
- `GET /api/v1/dashboard/expenses-trend`

### Summary

Query param:

- `from` - ixtiyoriy `YYYY-MM-DD`
- `to` - ixtiyoriy `YYYY-MM-DD`

Response `DashboardSummaryDto`.

### Orders by status

Response `List<DashboardCountDto>`.

### Orders by kind

Response `List<DashboardCountDto>`.

### Revenue trend

Response `List<DashboardAmountTrendDto>`.

### Expenses trend

Response `List<DashboardAmountTrendDto>`.

## 16. Frontend uchun tavsiya qilingan oqimlar

### Order yaratish

1. Category, customer, employee listni oling.
2. Agar rasm bo'lsa upload qiling.
3. Upload response dan `id` ni oling.
4. `POST /api/v1/orders` da `uploadId` yuboring.

### Expense yaratish

1. Expense category va materiallarni oling.
2. Agar chek rasmi bo'lsa upload qiling.
3. `uploadId` bilan `POST /api/v1/expenses` qiling.

### Avatar update

1. Upload qiling.
2. `PUT /api/v1/users/me` da `uploadId` yuboring.

## 17. Hozir implement qilinmagan yoki chala joylar

- `email` user/auth response ichida to'liq ishlatilmaydi
- logout token revoke qilmaydi
- `user-tasks` controller bor, lekin endpoint yo'q
- upload storage object storage emas, lokal disk
- ayrim eski CRUD endpointlar frontend uchun kerak bo'lmasa ham mavjud

## 18. Qisqa xulosa

Frontend uchun eng muhim real qoidalar:

- hamma protected requestda bearer token yuboring
- list kerak bo'lsa `GET /resource`, filter+paging kerak bo'lsa `POST /resource/paging`
- uploadda `uploadId`, `url`, `key` ni aralashtirmang
- order/expense/user image maydonlarini upload ishlatayotganda backendning o'zi set qiladi
- status update faqat `PUT /orders/{id}/status` orqali qiling
