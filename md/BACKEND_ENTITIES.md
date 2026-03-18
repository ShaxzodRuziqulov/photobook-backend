# Backend uchun entitylar

Loyiha ichida hozir ishlatilayotgan asosiy jadvallar:
- `users`
- `roles`
- `user_roles`
- `customers`
- `product_categories`
- `orders`
- `order_employees`
- `order_status_history`
- `materials`
- `expense_categories`
- `expenses`
- `uploads`

## 1) `users`
- `id` (uuid)
- `first_name` (string)
- `last_name` (string)
- `profession` (string, null)
- `username` (string, unique)
- `email` (string, null)
- `password_hash` (string)
- `avatar_url` (string, null)
- `phone` (string, null)
- `bio` (text, null)
- `is_active` (boolean, default true)
- `created_at`, `updated_at`

## 2) `roles`
- `id` (uuid)
- `name` (string, unique) masalan: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_OPERATOR`
- `description` (string, null)

## 3) `user_roles`
- `user_id` (fk -> users.id)
- `role_id` (fk -> roles.id)

## 4) `customers`
- `id` (uuid)
- `full_name` (string)
- `phone` (string, null)
- `notes` (text, null)
- `is_active` (boolean)
- `created_at`, `updated_at`

## 5) `product_categories`
- `id` (uuid)
- `name` (string, unique)
- `kind` (enum: `ALBUM`, `VIGNETTE`, `PICTURE`)
- `default_pages` (int, null)
- `size` (string, null)
- `created_at`, `updated_at`

## 6) `orders`
- `id` (uuid)
- `kind` (enum: `ALBUM`, `VIGNETTE`, `PICTURE`)
- `category_id` (fk -> product_categories.id)
- `order_name` (string)
- `item_type` (string, null)
- `customer_id` (fk -> customers.id)
- `receiver_name` (string)
- `page_count` (int)
- `amount` (int)
- `accepted_date` (date)
- `deadline` (date)
- `status` (enum: `PENDING`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`)
- `image_url` (string, null)
- `notes` (text, null)
- `upload_id` (fk -> uploads.id, null)
- `created_at`, `updated_at`

Izoh:
- orderning umumiy progressi response ichida hisoblab qaytariladi
- final tayyor bo'lgan son orderning oxirgi worker processed countidan olinadi

## 7) `order_employees`
- `id` (uuid)
- `order_id` (fk -> orders.id)
- `user_id` (fk -> users.id)
- `step_order` (int)
- `processed_count` (int, default 0)
- `work_status` (enum: `PENDING`, `STARTED`, `COMPLETED`)
- `created_at`, `updated_at`

Muhim:
- bir orderga bir user faqat bir marta biriktiriladi
- workflow `step_order` bo'yicha yuradi
- `step_order = 1` birinchi worker
- eng katta `step_order` oxirgi worker
- `role` endi ishlatilmaydi

## 8) `order_status_history`
- `id` (uuid)
- `order_id` (fk -> orders.id)
- `from_status` (string)
- `to_status` (string)
- `changed_by` (fk -> users.id)
- `changed_at` (datetime)
- `created_at`, `updated_at`

## 9) `materials`
- `id` (uuid)
- `item_name` (string)
- `item_type` (string, null)
- `unit_name` (string)
- `quantity` (decimal yoki int)
- `created_at`, `updated_at`

## 10) `expense_categories`
- `id` (uuid)
- `name` (string)
- `created_at`, `updated_at`

## 11) `expenses`
- `id` (uuid)
- `category_id` (fk -> expense_categories.id)
- `material_id` (fk -> materials.id, null)
- `name` (string)
- `price` (decimal)
- `description` (text, null)
- `payment_method` (string)
- `receipt_image_url` (string, null)
- `expense_date` (date)
- `upload_id` (fk -> uploads.id, null)
- `created_at`, `updated_at`

## 12) `uploads`
- `id` (uuid)
- `key` (string)
- `mime_type` (string)
- `size` (long)
- `owner_type` (enum: `USER`, `ORDER`, `EXPENSE`)
- `owner_id` (uuid, null)
- `created_at`, `updated_at`

## Muhim eslatma
- frontend order yaratishda employee larni `employees[]` orqali yuboradi
- har bir employee uchun kamida `employeeId` va `stepOrder` bo'lishi kerak
- `Order.status` admin navbatini boshqaradi
- `OrderEmployee.workStatus` esa aynan qaysi worker ishlayotganini ko'rsatadi

## Migration note
- eski `order_employees.role` ustuni ishlatilmaydi
- agar DBda saqlangan bo'lsa, migration bilan olib tashlanadi
- eski ma'lumotlarda `step_order` yo'q bo'lsa, ularni 1..N ketma-ketlik bilan to'ldirish kerak
- `orders.status` enum qiymatlari ichida `PAUSED` ham bo'lishi kerak
