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
- `notifications`
- `materials`
- `expense_categories`
- `expenses`
- `upload`

`BaseEntity` orqali deyarli barcha jadvallarda quyidagilar bor:

- `id` (uuid)
- `created_at`
- `updated_at`

## 1) `users`

- `id` (uuid)
- `first_name` (string, required)
- `last_name` (string, required)
- `username` (string, unique, required)
- `password` (string, required)
- `avatar_url` (string, null)
- `phone` (string, null)
- `profession` (string, null)
- `bio` (text, null)
- `user_status` (enum, masalan `ACTIVE`)
- `is_active` (boolean, default true)
- `upload_id` (fk -> upload.id, null)
- `created_at`, `updated_at`

## 2) `roles`

- `id` (uuid)
- `name` (string, unique)
- `description` (string, null)
- `created_at`, `updated_at`

## 3) `user_roles`

- `user_id` (fk -> users.id)
- `role_id` (fk -> roles.id)

## 4) `customers`

- `id` (uuid)
- `full_name` (string, required)
- `phone` (string, null)
- `notes` (string, null)
- `is_active` (boolean, default true)
- `created_at`, `updated_at`

## 5) `product_categories`

- `id` (uuid)
- `name` (string, required)
- `kind` (enum: `ALBUM`, `VIGNETTE`, `PICTURE`)
- `default_pages` (string, null)
- `size` (string, null)
- `created_at`, `updated_at`

## 6) `orders`

- `id` (uuid)
- `kind` (enum: `ALBUM`, `VIGNETTE`, `PICTURE`)
- `category_id` (fk -> product_categories.id, required)
- `order_name` (string, required)
- `item_type` (string, null)
- `customer_id` (fk -> customers.id, required)
- `upload_id` (fk -> upload.id, null)
- `receiver_name` (string, required)
- `page_count` (int, required, default 0)
- `amount` (int, required, default 0)
- `accepted_date` (date, required)
- `deadline` (date, required)
- `status` (enum: `PENDING`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `CANCELLED`)
- `image_url` (string, null)
- `notes` (text, null)
- `created_at`, `updated_at`

Izoh:

- orderning workflow holati `status` bilan boshqariladi
- umumiy progress DB ustuni emas, response ichida `order_employees` asosida hisoblanadi

## 7) `order_employees`

- `id` (uuid)
- `order_id` (fk -> orders.id, required)
- `user_id` (fk -> users.id, required)
- `processed_count` (int, required, default 0)
- `step_order` (int, required)
- `notes` (text, null)
- `work_status` (enum: `PENDING`, `STARTED`, `COMPLETED`)
- `created_at`, `updated_at`

Constraint va indexlar:

- unique: `(order_id, user_id)`
- index: `order_id`
- index: `user_id`

## 8) `order_status_history`

- `id` (uuid)
- `order_id` (fk -> orders.id, required)
- `from_status` (enum, null bo'lishi mumkin)
- `to_status` (enum, required)
- `changed_by` (fk -> users.id, required)
- `changed_at` (datetime, required)
- `created_at`, `updated_at`

## 9) `notifications`

- `id` (uuid)
- `user_id` (fk -> users.id, required)
- `type` (string, required)
- `title` (string, required)
- `message` (text, required)
- `order_id` (uuid, null)
- `order_name` (string, null)
- `order_kind` (string, null; masalan `ALBUM`, `VIGNETTE`, `PICTURE`)
- `employee_id` (uuid, null)
- `employee_name` (string, null)
- `step_order` (int, null)
- `work_status` (string, null)
- `target_type` (string, null; hozir order notificationlarda `ORDER`)
- `target_id` (uuid, null; hozir order notificationlarda `order_id`)
- `target_kind` (string, null; hozir order notificationlarda order kind)
- `route` (string, null; masalan `/album`, `/vignette`, `/picture`)
- `action_required` (boolean, required, default false)
- `read_at` (datetime, null)
- `created_at`, `updated_at`

REST `NotificationDto` da qo‘shimcha ravishda `isRead` (boolean) qaytadi — `read_at != null` bo‘lsa `true`, `read_at == null` bo‘lsa `false`.

Indexlar:

- `user_id`
- `read_at`
- `(target_type, target_id)`

Izoh:

- notification payload frontend route qilishi uchun `target_type`, `target_id`, `target_kind`, `route` maydonlarini saqlaydi
- order notificationlarda `order_kind` va `target_kind` bir xil bo'ladi
- eski notificationlar migration orqali `orders` jadvalidan backfill qilinadi

## 10) `materials`

- `id` (uuid)
- `item_name` (string, required)
- `item_type` (string, null)
- `unit_name` (string, null)
- `quantity` (decimal(19,3), required, default 0)
- `created_at`, `updated_at`

## 11) `expense_categories`

- `id` (uuid)
- `name` (string, required)
- `created_at`, `updated_at`

## 12) `expenses`

- `id` (uuid)
- `category_id` (fk -> expense_categories.id, required)
- `material_id` (fk -> materials.id, null)
- `name` (string, required)
- `price` (decimal(19,2), required)
- `description` (text, null)
- `payment_method` (string, null)
- `receipt_image_url` (string, null)
- `expense_date` (date, required)
- `upload_id` (fk -> upload.id, null)
- `created_at`, `updated_at`

## 13) `upload`

- `id` (uuid)
- `key` (string)
- `mime_type` (string)
- `size` (long)
- `owner_type` (enum: `USER`, `ORDER`, `EXPENSE`)
- `owner_id` (uuid, null)
- `created_at`, `updated_at`

## Muhim eslatma

- frontend order yaratishda `employees[]` orqali worker zanjirini yuboradi
- har bir employee uchun kamida `employeeId` va `stepOrder` bo'lishi kerak
- `orders.status` admin darajadagi workflow holatini ko'rsatadi
- `order_employees.work_status` esa aynan qaysi worker navbatda ekanini bildiradi
- `notifications` jadvali realtime va offline notificationlarni bir xil manbada saqlaydi
- notification route payloadi frontendga qo'shimcha `GET /orders/{id}` qilmasdan to'g'ridan-to'g'ri route qilish imkonini beradi

## Migration note

- eski `order_employees.role` ustuni ishlatilmaydi
- `order_employees.step_order` null bo'lmasligi kerak
- `order_employees.work_status` saqlanishi kerak
- `orders.status` enumida `PAUSED` va `CANCELLED` bo'lishi kerak
- `notifications` jadvali realtime/offline notification oqimi uchun kerak
- notification route ustunlari: `order_kind`, `target_type`, `target_id`, `target_kind`, `route`
