# back_topwatch

REST API for the TopWatch app, which keeps track of the movies, series, books and music people want to watch, read or listen to.

Built with Java 21, Spring Boot 3.4, Spring Security (JWT), Spring Data JPA on PostgreSQL, and Amazon S3 for images.

## Run locally

1. Have Java 21 and a PostgreSQL database named `topwatch_db` running on `localhost:5432`.
2. Create your local secrets file (it is git-ignored):
   ```bash
   cp .env.example .env
   # fill in JWT_SECRET (openssl rand -base64 32) and DB_PASSWORD
   ```
3. Start the API on http://localhost:8080:
   ```bash
   ./gradlew bootRun
   ```

Tables are created by Hibernate on startup (`ddl-auto: update`) and `CategorySeeder` fills the starter categories.

## Configuration

`application.yml` reads these environment variables. Locally they can live in `.env`; real environment variables always win.

| Variable | Required | Default |
|----------|----------|---------|
| `JWT_SECRET` | yes | none (Base64 key, at least 32 bytes) |
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/topwatch_db` |
| `DB_USERNAME` | no | `postgres` |
| `DB_PASSWORD` | yes | none |
| `S3_BUCKET` | no | `topwatch-images` |
| `AWS_REGION` | no | `us-east-2` |
| `S3_PUBLIC_URL` | no | `https://topwatch-images.s3.us-east-2.amazonaws.com` |

Image uploads also need AWS credentials from the default chain (environment variables, a local profile, or the server's IAM role).

## API

All routes are under `/api/v1`. `/auth/**` and `/users/health` are public; everything else needs `Authorization: Bearer <accessToken>`. Requests without a valid token get 401. Updates always use POST, never PATCH.

The Postman collection and Swagger spec are in `src/collections/`. Swagger UI (`/swagger-ui/index.html`) is available to ADMIN users.

## Tests

```bash
./gradlew test
```

`BackTopwatchApplicationTests` starts the whole application, so it needs the database and `.env` above; the other tests are plain unit tests.

## Documentation

Full technical documentation (architecture, endpoints, database, deployment) lives in the TopWatch Notion workspace under Backend.
