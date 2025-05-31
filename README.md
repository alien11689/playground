# Playground task application

## Run task provider (backend + frontend)

```bash
docker compose up -d
mvn -f task-provider clean spring-boot:run -Dspring-boot.run.profiles=dev
```

## Run frontend in development mode

**Backend app must run as described above**

```bash
cd task-app
npm run dev
```