# auth-service

Kotlin + Spring Boot 기반의 경량 인증 서버입니다. MSA 환경에서 공통으로 사용할 회원가입, 로그인, JWT Access Token 발급, Refresh Token Rotation, 로그아웃, 사용자 상태와 전역 Role 관리를 담당합니다.

## Stack

- Kotlin
- Spring Boot 3.x
- Gradle Kotlin DSL
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security Crypto
- MySQL
- Actuator
- DevTools

## Package

Base package는 `com.dbwp031.authservice`입니다.

```text
com.dbwp031.authservice
├─ auth
│  ├─ controller
│  ├─ service
│  ├─ dto
│  ├─ token
│  └─ support
├─ user
│  ├─ domain
│  ├─ repository
│  └─ service
├─ role
│  ├─ domain
│  ├─ repository
│  └─ service
└─ common
   ├─ exception
   └─ security
```

## Run

MySQL 실행:

```bash
docker compose up -d
```

애플리케이션 실행:

```bash
./gradlew bootRun
```

로컬 기본 포트는 `8084`입니다.

## Configuration

`src/main/resources/application.yml`은 환경변수 주입을 지원합니다.

| Name | Default |
| --- | --- |
| `SERVER_PORT` | `8084` |
| `DB_URL` | `jdbc:mysql://localhost:3306/auth_service...` |
| `DB_USERNAME` | `auth` |
| `DB_PASSWORD` | `auth1234` |
| `JWT_ISSUER` | `auth-service` |
| `JWT_SECRET` | `local-dev-secret-change-me-local-dev-secret-change-me` |
| `JWT_ACCESS_TOKEN_VALIDITY_SECONDS` | `1800` |
| `JWT_REFRESH_TOKEN_VALIDITY_SECONDS` | `1209600` |

운영 환경에서는 반드시 `JWT_SECRET`을 충분히 긴 랜덤 값으로 교체해야 합니다.

## APIs

### POST `/auth/signup`

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "nickname": "user"
}
```

응답:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "user"
}
```

### POST `/auth/login`

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

응답:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

### POST `/auth/reissue`

```json
{
  "refreshToken": "..."
}
```

Refresh Token Rotation 방식으로 기존 토큰을 revoked 처리하고 새 Access Token과 Refresh Token을 발급합니다.

### POST `/auth/logout`

```json
{
  "refreshToken": "..."
}
```

응답은 `204 No Content`입니다. 토큰이 없거나 이미 revoked된 경우에도 204로 처리합니다.

### GET `/auth/me`

```http
Authorization: Bearer <access-token>
```

응답:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "user",
  "status": "ACTIVE",
  "roles": ["USER"]
}
```

## Security Notes

- 비밀번호는 BCrypt hash로 저장합니다.
- Refresh Token은 원문을 저장하지 않고 SHA-256 hash만 저장합니다.
- Access Token 만료 시간은 기본 30분입니다.
- Refresh Token 만료 시간은 기본 14일입니다.
- Access Token claim에는 `sub`, `email`, `roles`, `status`, `iat`, `exp`가 포함됩니다.
- 전역 Role은 초기 구동 시 `USER`, `ADMIN`을 생성합니다.
