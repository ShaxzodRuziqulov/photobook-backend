# Frontend uchun kerak bo'ladigan backend logikasi

Bu hujjat frontend normal ishlashi uchun backendda qanday logika bo'lishi kerakligini tushuntiradi. Siz entity CRUDlarni qilib bo'lgansiz, endi frontend bilan to'g'ri integratsiya bo'lishi uchun qo'shimcha business logic, auth, filtering va analytics qismlari kerak bo'ladi.

## 1. Asosiy modullar

Loyihadagi frontend quyidagi backend modullarga tayanadi:

- `auth`
- `users`
- `roles`
- `employees`
- `customers`
- `product_categories`
- `orders`
- `materials`
- `uploads`
- `dashboard`

Asosiy ish oqimi `orders` modulida bo'ladi. Qolgan modullar esa order yaratish, update qilish va dashboardni to'ldirish uchun xizmat qiladi.

## 2. Auth logikasi

Frontend ishlashi uchun quyidagi endpointlar kerak:

- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`

### Login oqimi

1. User `username/email` va `password` yuboradi.
2. Backend passwordni tekshiradi.
3. To'g'ri bo'lsa `access_token` va `refresh_token` qaytaradi.
4. Frontend `access_token`ni saqlaydi va keyingi requestlarda `Bearer token` yuboradi.
5. `401` qaytsa frontend refresh token orqali yangi access token oladi.
6. Refresh ham xato bo'lsa user login pagega qaytariladi.

### Auth ichidagi majburiy logika

- Password faqat `hash` ko'rinishida saqlanishi kerak.
- Refresh token DBda yoki xavfsiz storage'da saqlanishi kerak.
- Revoked tokenlar qayta ishlatilmasligi kerak.
- `GET /auth/me` endpoint current user va rolelarini qaytarishi kerak.

### Tavsiya etilgan response

```json
{
  "access_token": "string",
  "refresh_token": "string",
  "user": {
    "id": "uuid",
    "name": "Ali Valiyev",
    "email": "ali@gmail.com",
    "roles": ["ADMIN"],
    "avatar_url": "https://cdn.site/avatar.jpg",
    "phone": "+998901234567",
    "bio": "..."
  }
}
```

## 3. Users va roles logikasi

Bu qism admin panel va ruxsatlar uchun kerak.

### Users bo'yicha kerakli amallar

- user yaratish
- userni edit qilish
- userni o'chirish
- userni aktiv/deaktiv qilish
- userga role biriktirish

### Endpointlar

- `GET /users`
- `POST /users`
- `GET /users/:id`
- `PATCH /users/:id`
- `DELETE /users/:id`
- `PUT /users/:id/roles`
- `GET /roles`

### Tavsiya etilgan schema

#### `users`

- `id`
- `name`
- `email`
- `password_hash`
- `avatar_url`
- `phone`
- `bio`
- `is_active`
- `created_at`
- `updated_at`

#### `roles`

- `id`
- `name`
- `description`

#### `user_roles`

- `user_id`
- `role_id`

## 4. Employees logikasi

Frontenddagi `Employees` sahifasi uchun alohida employee moduli kerak bo'lishi mumkin.

### Employees uchun fieldlar

- `full_name`
- `profession`
- `phone_number`
- `is_active`

### Endpointlar

- `GET /employees`
- `POST /employees`
- `GET /employees/:id`
- `PATCH /employees/:id`
- `DELETE /employees/:id`

### Eslatma

Agar tizimga kiradigan xodim va oddiy ishlab chiqarish xodimi bitta obyekt bo'lsa, `users` ichida professionni ham saqlash mumkin. Agar ular alohida bo'lsa, `users` va `employees`ni ajratgan yaxshi.

## 5. Customers logikasi

Buyurtma yaratishda mijoz albatta kerak bo'ladi.

### Kerakli imkoniyatlar

- customer yaratish
- customer qidirish
- customer detail ko'rish
- customer order history ko'rish

### Endpointlar

- `GET /customers`
- `POST /customers`
- `GET /customers/:id`
- `PATCH /customers/:id`
- `DELETE /customers/:id`

### Tavsiya etilgan schema

- `id`
- `full_name`
- `phone`
- `notes`
- `is_active`
- `created_at`
- `updated_at`

### Muhim nuqta

Frontend hozir `customerName` bilan ishlayapti. Backendda esa `customer_id` saqlash kerak. Response ichida qulaylik uchun `customer_name` qaytarib berish mumkin.

## 6. Product categories logikasi

Frontend categorylarni 3 turga bo'ladi:

- `ALBUM`
- `VIGNETTE`
- `PICTURE`

### Tavsiya etilgan schema

- `id`
- `name`
- `kind`
- `default_pages`
- `created_at`
- `updated_at`

### Endpointlar

- `GET /product-categories`
- `POST /product-categories`
- `PATCH /product-categories/:id`
- `DELETE /product-categories/:id`

### Filter

Categorylarni tur bo'yicha olish kerak:

- `GET /product-categories?kind=ALBUM`
- `GET /product-categories?kind=VIGNETTE`
- `GET /product-categories?kind=PICTURE`

## 7. Orders logikasi

Bu frontend uchun eng muhim modul.

Frontenddagi sahifalarda order uchun quyidagi fieldlar ishlatilmoqda:

- `categoryName`
- `orderName`
- `itemType`
- `customerName`
- `receiverName`
- `employeeName`
- `pageNumber`
- `amountNumber`
- `processNumber`
- `createdData`
- `termData`
- `status`
- `imageUrl`

Backendda esa buni normal schema ko'rinishida saqlash kerak.

### Tavsiya etilgan orders schema

- `id`
- `kind`
- `category_id`
- `order_name`
- `item_type`
- `customer_id`
- `receiver_name`
- `employee_id`
- `page_count`
- `amount`
- `processed_count`
- `accepted_date`
- `deadline`
- `status`
- `image_url`
- `notes`
- `created_at`
- `updated_at`

### `kind` enum

- `ALBUM`
- `VIGNETTE`
- `PICTURE`

### `status` enum

- `KUTILMOQDA`
- `JARAYONDA`
- `BAJARILGAN`

## 8. Frontend va backend field mapping

Frontenddagi fieldlar bilan backenddagi fieldlar bir xil emas. Shu sabab mapping qatlam bo'lishi kerak.

| Frontend field | Backend field |
|---|---|
| `categoryName` | `category_id` |
| `orderName` | `order_name` |
| `itemType` | `item_type` |
| `customerName` | `customer_id` |
| `receiverName` | `receiver_name` |
| `employeeName` | `employee_id` |
| `pageNumber` | `page_count` |
| `amountNumber` | `amount` |
| `processNumber` | `processed_count` |
| `createdData` | `accepted_date` |
| `termData` | `deadline` |
| `imageUrl` | `image_url` |

### Amaliy tavsiya

DBga faqat `id`lar saqlansin:

- `customer_id`
- `employee_id`
- `category_id`

Lekin response ichida frontend uchun quyidagilar ham qaytsin:

- `customer_name`
- `employee_name`
- `category_name`

## 9. Orders endpointlari

### Asosiy endpointlar

- `GET /orders`
- `POST /orders`
- `GET /orders/:id`
- `PATCH /orders/:id`
- `DELETE /orders/:id`
- `PATCH /orders/:id/status`
- `GET /orders/:id/status-history`

### `GET /orders` filterlari

Quyidagi query parametrlar kerak bo'ladi:

- `page`
- `limit`
- `q`
- `kind`
- `status`
- `customer_id`
- `employee_id`
- `category_id`
- `from`
- `to`
- `deadline_from`
- `deadline_to`

### Misol

```http
GET /orders?kind=ALBUM&status=JARAYONDA&q=maktab&from=2026-03-01&to=2026-03-31
```

## 10. Orders business logic

CRUDdan tashqari quyidagi logikalar majburiy:

- `processed_count` hech qachon `amount`dan katta bo'lmasin
- `deadline` `accepted_date`dan kichik bo'lmasin
- `category_id`, `customer_id`, `employee_id` mavjudligi tekshirilsin
- status transition nazorat qilinsin
- delete paytida bog'liqlik yoki soft delete ko'rilsin
- order list qaytganda relationlar join qilinsin

### Status transition qoidalari

Ruxsat beriladigan o'tishlar:

- `KUTILMOQDA -> JARAYONDA`
- `JARAYONDA -> BAJARILGAN`
- kerak bo'lsa `JARAYONDA -> KUTILMOQDA`

Ko'pincha `BAJARILGAN -> ortga` o'tishni cheklagan yaxshi.

## 11. Order status history

Status almashganini audit qilish uchun alohida jadval foydali bo'ladi.

### Tavsiya etilgan schema

- `id`
- `order_id`
- `from_status`
- `to_status`
- `changed_by`
- `changed_at`

### Foydasi

- kim statusni o'zgartirganini bilish
- qachon o'zgarganini ko'rish
- xatoni tekshirish
- dashboard yoki audit uchun ishlatish

## 12. Materials logikasi

Frontenddagi `Materials.vue` uchun quyidagi backend logika kerak:

- material list
- create
- update
- delete
- quantity adjust

### Schema

- `id`
- `item_name`
- `item_type`
- `unit_name`
- `quantity`
- `created_at`
- `updated_at`

### Endpointlar

- `GET /materials`
- `POST /materials`
- `PATCH /materials/:id`
- `DELETE /materials/:id`
- `POST /materials/:id/adjust`

### `adjust` endpoint misoli

```json
{
  "delta": -10,
  "reason": "Ishlab chiqarishga berildi"
}
```

### Business rule

- quantity manfiy bo'lib ketmasligi kerak
- har adjust operation log qilinsa yaxshi bo'ladi

## 13. Upload logikasi

Frontend orderga rasm biriktiryapti, shuning uchun upload endpoint kerak.

### Endpointlar

- `POST /uploads`
- `DELETE /uploads/:key`

### `POST /uploads`

Request:

- `multipart/form-data`

Response:

```json
{
  "url": "https://cdn.example.com/order/file.jpg",
  "key": "orders/file.jpg",
  "mime": "image/jpeg",
  "size": 102400
}
```

### Oqim

1. Frontend rasmni upload qiladi.
2. Backend file storagega saqlaydi.
3. `url` qaytaradi.
4. Frontend shu `url`ni order create yoki update requestiga qo'shadi.

## 14. Dashboard logikasi

Dashboard uchun oddiy CRUD emas, agregatsiya kerak bo'ladi.

### Endpointlar

- `GET /dashboard/summary`
- `GET /dashboard/orders-by-status`
- `GET /dashboard/orders-by-kind`
- `GET /dashboard/revenue-trend`
- `GET /dashboard/expenses-trend`

### `summary` response misoli

```json
{
  "orders_total": 120,
  "orders_done": 45,
  "orders_in_progress": 50,
  "revenue_total": 15000000,
  "expenses_total": 7000000,
  "profit": 8000000
}
```

### Dashboard ichida hisoblanadigan narsalar

- jami orderlar soni
- bajarilgan orderlar
- jarayondagi orderlar
- orderlar tur bo'yicha soni
- revenue
- expenses
- profit
- vaqt bo'yicha trend

## 15. Response format bir xil bo'lishi kerak

Frontendni soddalashtirish uchun barcha list endpointlar bir xil formatda qaytishi kerak.

### List response

```json
{
  "items": [],
  "meta": {
    "page": 1,
    "limit": 10,
    "total": 120,
    "total_pages": 12
  }
}
```

### Error response

```json
{
  "message": "Validation error",
  "errors": {
    "customer_id": ["Customer topilmadi"],
    "deadline": ["Deadline accepted_date dan kichik bo'lmasligi kerak"]
  }
}
```

## 16. Validation qoidalari

Har requestda validation qatlam bo'lishi kerak.

### Orders uchun

- `order_name` bo'sh bo'lmasin
- `category_id` majburiy
- `customer_id` majburiy
- `employee_id` majburiy
- `amount > 0`
- `processed_count >= 0`
- `processed_count <= amount`
- `accepted_date` valid date bo'lsin
- `deadline` valid date bo'lsin
- `deadline >= accepted_date`

### Users uchun

- `email` unique bo'lsin
- `password` minimal talabga mos bo'lsin
- `role_ids` valid bo'lsin

### Materials uchun

- `item_name` bo'sh bo'lmasin
- `quantity >= 0`
- `unit_name` bo'sh bo'lmasin

## 17. Permission va role logic

Kamida quyidagicha ruxsat tizimi bo'lishi kerak:

- `ADMIN`
- `MANAGER`
- `OPERATOR`

### Tavsiya etilgan permissionlar

#### `ADMIN`

- users va roles boshqaradi
- ordersni to'liq boshqaradi
- materialsni boshqaradi
- dashboard ko'radi

#### `MANAGER`

- orders yaratadi va update qiladi
- customers bilan ishlaydi
- materials bilan ishlaydi
- dashboard ko'radi

#### `OPERATOR`

- orderlarni ko'radi
- status update qiladi
- lekin users va rolesni boshqarmaydi

## 18. Service qatlamida bo'lishi kerak bo'lgan metodlar

Har modulda controllerdan tashqari service bo'lsin.

### `orders.service`

- `createOrder(dto, currentUser)`
- `updateOrder(id, dto, currentUser)`
- `deleteOrder(id, currentUser)`
- `listOrders(filters)`
- `getOrderById(id)`
- `changeStatus(id, dto, currentUser)`
- `getStatusHistory(id)`

### `auth.service`

- `login(dto)`
- `refresh(dto)`
- `logout(dto)`
- `getCurrentUser(userId)`

### `materials.service`

- `createMaterial(dto)`
- `updateMaterial(id, dto)`
- `deleteMaterial(id)`
- `adjustQuantity(id, dto)`
- `listMaterials(filters)`

## 19. Controller ichidagi emas, service ichidagi business logic

Masalan `createOrder` ichida quyidagi ketma-ketlik bo'lishi kerak:

1. `category_id` mavjudligini tekshiradi
2. `customer_id` mavjudligini tekshiradi
3. `employee_id` mavjudligini tekshiradi
4. `amount` va `processed_count` ni validatsiya qiladi
5. `deadline` va `accepted_date` ni validatsiya qiladi
6. orderni yaratadi
7. response uchun category, customer va employee nomlarini join qilib qaytaradi

Bu logika controller ichiga yozilmasligi kerak. Controller faqat request qabul qiladi va service'ga uzatadi.

## 20. Minimum production-ready checklist

Agar frontend muammosiz ishlasin desangiz, CRUDdan tashqari quyidagilar bo'lishi kerak:

- auth
- access token
- refresh token
- auth middleware
- role middleware
- request validation
- global error handler
- pagination
- filtering
- sorting
- relation join
- upload
- status history
- consistent response format
- `created_at` va `updated_at`
- soft delete yoki referential check

## 21. Yakuniy xulosa

Siz entity CRUDlarni yozib bo'lgan bo'lsangiz, frontendni ishlatish uchun endi eng kerakli backend logikalar quyidagilar:

- `auth + refresh token`
- `users + roles`
- `employees`
- `customers`
- `product categories`
- `orders` uchun to'liq business logic
- `materials`
- `uploads`
- `dashboard analytics`
- `validation`
- `permissions`
- `joined response fields`

Eng muhim qism `orders` bo'lib, frontend asosan shu modulga tayanadi. Aynan shu yerda relationlar, filterlar, status logikasi va response mapping to'g'ri yozilsa, frontend backend bilan silliq ishlaydi.
