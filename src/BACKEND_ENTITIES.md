# Backend uchun entitylar (2-marta tekshirilgan)

Loyiha ichida hozir ishlatilayotgan kolleksiyalar:
- `users`
- `customers`
- `materials`
- `albums`
- `vignette`
- `pictures`

`Dashboard` ichida `categories` va `expenses` tipi bor, lekin aktiv CRUD deyarli yoq.

## Tavsiya etilgan aniq model (SQL yoki NoSQL uchun)

## 1) `users` (auth user)
- `id` (uuid)
- `name` (string)
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
- `name` (string, unique) masalan: `ADMIN`, `MANAGER`, `OPERATOR`

## 3) `user_roles`
- `user_id` (fk -> users.id)
- `role_id` (fk -> roles.id)

## 4) `employees`
- `id` (uuid)
- `full_name` (string)
- `profession` (string)
- `phone_number` (string)
- `is_active` (boolean)
- `created_at`, `updated_at`

## 5) `customers`
- `id` (uuid)
- `full_name` (string)
- `phone` (string, null)
- `notes` (text, null)
- `created_at`, `updated_at`

## 6) `product_categories`
- `id` (uuid)
- `name` (string, unique)
- `kind` (enum: `ALBUM`, `VIGNETTE`, `PICTURE`)
- `default_pages` (int, null)
- `created_at`, `updated_at`

## 7) `orders` (bitta umumiy jadval)
- `id` (uuid)
- `kind` (enum: `ALBUM`, `VIGNETTE`, `PICTURE`)
- `category_id` (fk -> product_categories.id)
- `order_name` (string)
- `item_type` (string, null) `pictures` va bazi `albums` uchun
- `customer_id` (fk -> customers.id)
- `receiver_name` (string)
- `employee_id` (fk -> employees.id)
- `page_count` (int)
- `amount` (int)
- `processed_count` (int)
- `accepted_date` (date) frontend: `createdData`
- `deadline` (date) frontend: `termData`
- `status` (enum: `KUTILMOQDA`, `JARAYONDA`, `BAJARILGAN`)
- `created_at`, `updated_at`

Izoh: hozir frontendda bu 3 ta alohida kolleksiya (`albums`, `vignette`, `pictures`). Backendda xohlasangiz 1 ta `orders`da birlashtiring, yoki shu 3 tasini alohida qoldiring.

## 8) `materials`
- `id` (uuid)
- `item_name` (string)
- `item_type` (string)
- `unit_name` (string)
- `quantity` (decimal yoki int)
- `created_at`, `updated_at`

## 9) `expense_categories` (kelajak uchun)
- `id` (uuid)
- `name` (string)
- `created_at`, `updated_at`

## 10) `expenses` (kelajak uchun)
- `id` (uuid)
- `category_id` (fk -> expense_categories.id)
- `material_id` (fk -> materials.id, null)
- `name` (string)
- `price` (decimal)
- `description` (text, null)
- `payment_method` (string)
- `receipt_image_url` (string, null)
- `expense_date` (date)
- `created_at`, `updated_at`

## 11) `order_status_history` (foydali audit)
- `id` (uuid)
- `order_id` (fk -> orders.id)
- `from_status` (string)
- `to_status` (string)
- `changed_by` (fk -> users.id)
- `changed_at` (datetime)
- `created_at`, `updated_at` (umumiy standart uchun)

## 12) `refresh_tokens` (JWT refresh ishlatsa)
- `id` (uuid)
- `user_id` (fk -> users.id)
- `token_hash` (string)
- `expires_at` (datetime)
- `revoked_at` (datetime, null)

## Muhim eslatma
- Frontend hozir `customerName`, `employeeName`, `categoryName`ni string saqlaydi; backendda `*_id` FKga otkazish kerak.
- `status` va `kind`ni enum qiling.
- Har bir jadvalda `created_at`, `updated_at` bolsin.
