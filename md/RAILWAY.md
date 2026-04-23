# Railway deploy

Boshqa API hujjatlari: [`README.md`](README.md). Bu loyiha Railway yoki boshqa konteyner muhitida **Spring Boot** backend sifatida ishlaydi. Ma’lumotlar bazasi sxemasi **Liquibase** orqali migratsiya qilinadi; `ddl-auto=update` ishlatmaslik tavsiya etiladi — sxema changelog bilan boshqariladi.

## 1. Backend service

1. Railway’da **New Project → Deploy from GitHub Repo** (yoki Docker image).
2. Ushbu repozitoriyani ulang.
3. Loyihada `Dockerfile` bor — Railway uni build/run qilish uchun ishlatishi mumkin.
4. Quyidagi **environment variable** larni sozlang (qiymatlarni o‘zingizning muhitingizga moslang):

```env
PORT=8080
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<username>
DB_PASSWORD=<password>
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
SECRET_KEY=<uzun-tasodifiy-mahfiy-kalit>
APP_CORS_ALLOWED_ORIGINS=https://sizning-frontend-domeningiz.uz
APP_UPLOAD_DIR=/app/uploads-storage
```

**Muhim:**

- `JPA_DDL_AUTO` uchun **`validate`** (yoki `none`) — sxema o‘zgarishlari `src/main/resources/liquibase` dagi changelog orqali chiqariladi.
- `SECRET_KEY` — JWT uchun; repoda default qiymat **ishlab chiqarishda ishlatilmaydi**.
- Lokal ishlab chiqishda odatda port `9091` (`application.yml`), Railway odatda `PORT` ni o‘zi beradi (`8080` yoki boshqa).

## 2. PostgreSQL

- Railway ichida **Add Service → Database → PostgreSQL** yarating.
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` ni backend servisiga bog‘lang.

## 3. Frontend

- Frontend alohida repo yoki alohida servis bo‘lsa, uni alohida deploy qiling.
- Frontend konfiguratsiyasida API **base URL** sifatida backendning ochiq URL ini ko‘rsating.
- Backendda `APP_CORS_ALLOWED_ORIGINS` ro‘yxatiga frontend manzilini qo‘shing (vergul bilan bir nechta origin mumkin).

## 4. Upload va doimiy disk

Hozirgi konfiguratsiyada fayllar **`APP_UPLOAD_DIR`** ostidagi diskda saqlanadi. Konteyner qayta ishga tushganda yoki yangi deploy bo‘lganda bu fayllar **yo‘qolishi mumkin**. Doimiy saqlash kerak bo‘lsa, S3, Cloudinary yoki boshqa object storage integratsiyasi qilish kerak.

## 5. Health / tekshiruv

- REST: `GET /api/v1/dashboard/orders-by-kind` (JWT va rol talab qilinadi) yoki Swagger: `/swagger-ui/index.html`.
- Statik upload (public): `GET /uploads-storage/{key}`.

## 6. API hujjatlari va Postman

- **Postman kolleksiyasi:** `postman/photobook-api.postman_collection.json` (import qilib `{{baseUrl}}`, Login skripti `access_token` / `refresh_token` ni saqlaydi).
- **Endpointlar jadvali:** `md/BACKEND_API.md` → §0.1.
- **Batafsil REST + socket:** `md/BACKEND_API.md`, workflow: `md/BACKEND_LOGIC.md`, frontend: `md/FRONTEND_CONTRACT.md`.

## 7. Socket.IO path (ixtiyoriy)

`application.yml` → `socket.io.path` (`SOCKET_IO_PATH`, default `/socket.io/`). Frontend `io(BASE_URL, { path: "/socket.io/" })` bilan moslashtiring.
