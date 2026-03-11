# Railway deploy

Bu loyiha Railway'da backend sifatida ishlaydi. Repository ichida alohida frontend yo'q, shuning uchun frontend alohida servis sifatida deploy qilinadi va API manzili shu backend URL'iga ulanadi.

## 1. Backend service

- Railway'da `New Project -> Deploy from GitHub Repo` tanlang.
- Shu repository'ni ulang.
- `Dockerfile` bor, Railway uni avtomatik ishlata oladi.
- Backend uchun quyidagi environment variable'larni kiriting:

```env
PORT=8080
DB_URL=jdbc:postgresql://<host>:<port>/<db>
DB_USERNAME=<username>
DB_PASSWORD=<password>
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
SECRET_KEY=<uzun-random-secret>
APP_CORS_ALLOWED_ORIGINS=https://frontend-domain.up.railway.app,https://sizning-domainingiz.uz
APP_UPLOAD_DIR=/app/uploads-storage
```

## 2. PostgreSQL

- Railway ichida `Add Service -> Database -> PostgreSQL` tanlang.
- Database ma'lumotlarini backend env variable'lariga ulang.

## 3. Frontend

- Frontend alohida repo yoki alohida papkada bo'lsa, uni ham Railway'ga alohida service qilib deploy qiling.
- Frontend'da API base URL sifatida backend Railway URL'ini ishlating.
- Backend'dagi `APP_CORS_ALLOWED_ORIGINS` ichiga frontend domenini yozing.

## 4. Muhim cheklov

`uploads-storage` lokal diskda saqlanadi. Railway container qayta ishga tushsa yoki redeploy bo'lsa, bu fayllar yo'qolishi mumkin. Doimiy saqlash kerak bo'lsa, upload'larni S3, Cloudinary yoki boshqa object storage'ga ko'chirish kerak.
