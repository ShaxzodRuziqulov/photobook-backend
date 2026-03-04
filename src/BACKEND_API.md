# API Documentation

**Base URL:** `/api/v1`

**Auth:**
`Authorization: Bearer <access_token>` (login/refresh dan tashqari)

**Pagination standard:**
`page`, `limit`, `sort`, `order`

**Search standard:**
`q`

**Date filter:**
`from`, `to` (YYYY-MM-DD)

---

# 1. Auth

### POST /auth/login

**Maqsad:** tizimga kirish.

**Body**

```json
{
  "username": "string",
  "password": "string"
}
```

**Responses**

`200`

```json
{
  "access_token": "string",
  "refresh_token": "string",
  "user": {}
}
```

`401` – login yoki password noto‘g‘ri.

---

### POST /auth/refresh

**Maqsad:** yangi access token olish.

**Body**

```json
{
  "refresh_token": "string"
}
```

**Responses**

`200`

```json
{
  "access_token": "string",
  "refresh_token": "string"
}
```

`401` – refresh token yaroqsiz yoki revoked.

---

### POST /auth/logout

**Maqsad:** refresh tokenni bekor qilish.

**Body**

```json
{
  "refresh_token": "string"
}
```

**Response**

`204` – muvaffaqiyatli chiqish.

---

### GET /auth/me

**Maqsad:** joriy user ma’lumotini olish.

**Response**

`200`

```json
{
  "id": "uuid",
  "name": "string",
  "email": "string",
  "roles": [],
  "avatar_url": "string",
  "phone": "string",
  "bio": "string"
}
```

---

# 2. Users

### GET /users

**Maqsad:** userlar ro‘yxati.

**Query**

```
page
limit
q
is_active
role
```

**Response**

`200` – paginated list.

---

### POST /users

**Maqsad:** yangi user yaratish.

**Body**

```json
{
  "name": "string",
  "email": "string",
  "password": "string",
  "avatar_url": "string",
  "phone": "string",
  "bio": "string",
  "is_active": true
}
```

**Responses**

`201` – user yaratildi
`409` – email allaqachon mavjud

---

### GET /users/:id

**Maqsad:** bitta userni olish.

`200` – user detail
`404` – topilmadi

---

### PATCH /users/:id

**Maqsad:** userni qisman yangilash.

**Body**

```
name?
phone?
bio?
avatar_url?
is_active?
password?
```

`200` – yangilangan user

---

### DELETE /users/:id

**Maqsad:** userni o‘chirish (yoki soft delete).

`204` – o‘chirildi
`409` – bog‘liq ma’lumotlar mavjud

---

### PUT /users/:id/roles

**Maqsad:** user rolelarini to‘liq almashtirish.

**Body**

```json
{
  "role_ids": ["uuid"]
}
```

`200` – yangilangan rolelar

---

# 3. Roles

### GET /roles

**Response**

```json
[
  {
    "id": "uuid",
    "name": "string",
    "description": "string"
  }
]
```

---

### POST /roles

**Body**

```json
{
  "name": "string",
  "description": "string"
}
```

`201` – role yaratildi
`409` – name unique buzildi

---

### PATCH /roles/:id

**Body**

```
name?
description?
```

`200` – updated role

---

### DELETE /roles/:id

`204` – deleted
`409` – role userlarga biriktirilgan

---

# 4. Customers

### GET /customers

**Query**

```
page
limit
q
is_active
```

`200` – paginated list

---

### POST /customers

**Body**

```json
{
  "full_name": "string",
  "phone": "string",
  "notes": "string",
  "is_active": true
}
```

`201` – created

---

### GET /customers/:id

`200` – customer detail + last_orders

---

### PATCH /customers/:id

```
full_name?
phone?
notes?
is_active?
```

`200` – updated

---

### DELETE /customers/:id

`204` – deleted
`409` – orderlarga bog‘langan

---

# 5. Employees

### GET /employees

**Query**

```
page
limit
q
profession
is_active
```

---

### POST /employees

```json
{
  "full_name": "string",
  "profession": "string",
  "phone_number": "string",
  "is_active": true
}
```

---

### GET /employees/:id

`200` – employee detail

---

### PATCH /employees/:id

```
full_name?
profession?
phone_number?
is_active?
```

---

### DELETE /employees/:id

`204` – deleted

---

# 6. Product Categories

### GET /product-categories

**Query**

```
kind
q
```

---

### POST /product-categories

```json
{
  "name": "string",
  "kind": "ALBUM | VIGNETTE | PICTURE",
  "default_pages": 0
}
```

---

### PATCH /product-categories/:id

```
name?
kind?
default_pages?
```

---

### DELETE /product-categories/:id

`204` – deleted
`409` – orders ishlatayotgan bo‘lsa

---

# 7. Orders

### GET /orders

**Query**

```
page
limit
q
status
kind
customer_id
employee_id
category_id
from
to
deadline_from
deadline_to
```

---

### POST /orders

```json
{
  "kind": "string",
  "category_id": "uuid",
  "order_name": "string",
  "item_type": "string",
  "customer_id": "uuid",
  "receiver_name": "string",
  "employee_id": "uuid",
  "page_count": 0,
  "amount": 0,
  "processed_count": 0,
  "accepted_date": "date",
  "deadline": "date",
  "status": "string",
  "notes": "string"
}
```

---

### PATCH /orders/:id/status

**Body**

```json
{
  "to_status": "string",
  "comment": "string"
}
```

`422` – noto‘g‘ri status transition

---

### GET /orders/:id/status-history

```json
[
  {
    "id": "uuid",
    "from_status": "string",
    "to_status": "string",
    "changed_by": "uuid",
    "changed_at": "datetime"
  }
]
```

---

# 8. Materials

### GET /materials

**Query**

```
page
limit
q
item_type
```

---

### POST /materials

```json
{
  "item_name": "string",
  "item_type": "string",
  "unit_name": "string",
  "quantity": 0
}
```

---

### POST /materials/:id/adjust

```json
{
  "delta": -2,
  "reason": "string"
}
```

`422` – quantity manfiyga tushib qolsa

---

# 9. Expense Categories

### GET /expense-categories

`200` – list

---

### POST /expense-categories

```json
{
  "name": "string"
}
```

---

### PATCH /expense-categories/:id

```
name?
```

---

# 10. Expenses

### GET /expenses

**Query**

```
page
limit
q
category_id
material_id
payment_method
from
to
```

---

### POST /expenses

```json
{
  "category_id": "uuid",
  "material_id": "uuid",
  "name": "string",
  "price": 0,
  "description": "string",
  "payment_method": "string",
  "receipt_image_url": "string",
  "expense_date": "date"
}
```

---

# 11. Dashboard / Analytics

### GET /dashboard/summary

```
/dashboard/summary?from=&to=
```

```json
{
  "orders_total": 0,
  "orders_done": 0,
  "orders_in_progress": 0,
  "revenue_total": 0,
  "expenses_total": 0,
  "profit": 0
}
```

---

### GET /dashboard/orders-by-status

```json
[
  {
    "status": "string",
    "count": 0
  }
]
```

---

### GET /dashboard/orders-by-kind

```json
[
  {
    "kind": "string",
    "count": 0,
    "amount_sum": 0
  }
]
```

---

### GET /dashboard/revenue-trend

```
groupBy = day | month
```

```json
[
  {
    "period": "2026-01",
    "revenue": 1000
  }
]
```

---

### GET /dashboard/expenses-trend

```json
[
  {
    "period": "2026-01",
    "expenses": 800
  }
]
```

---

# 12. Upload

### POST /uploads

**Body:** `multipart/form-data`

**Response**

```json
{
  "url": "string",
  "key": "string",
  "mime": "string",
  "size": 0
}
```

---

### DELETE /uploads/:key

`204` – file removed
