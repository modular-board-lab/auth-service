Kotlin + Spring Boot로 MSA 환경을 가정한 auth-service 프로젝트를 구축해줘.

현재 상황:
- board-service는 이미 구현되어 있다.
- api-gateway도 구현되어 있고, 현재는 board-service로 단순 라우팅만 한다.
- 아직 인증/인가 기능은 없다.
- 이번 단계에서는 auth-service를 만들어 회원가입, 로그인, 토큰 발급, 토큰 재발급 기능을 구현한다.

목표:
- auth-service는 여러 MSA 서비스에서 공통으로 사용할 수 있는 경량 인증 서버다.
- 특정 도메인 서비스에 종속되지 않도록 설계한다.
- 인증은 auth-service가 담당한다.
- api-gateway는 추후 auth-service가 발급한 JWT를 검증하고, 내부 서비스로 사용자 컨텍스트를 전달할 예정이다.
- auth-service에는 게시판, 댓글, 파일 같은 도메인 로직을 넣지 않는다.

기술 스택:
- Kotlin
- Spring Boot 3.x
- Gradle Kotlin DSL
- Spring Web
- Spring Data JPA
- Validation
- Spring Security Crypto 또는 Spring Security
- MySQL
- Actuator
- DevTools

구현 범위:
1. 회원가입
2. 로그인
3. Access Token 발급
4. Refresh Token 발급
5. Refresh Token 재발급
6. 로그아웃
7. 사용자 계정 상태 관리
8. 전역 Role 관리
9. 공통 예외 처리
10. README.md 작성
11. docker-compose.yml로 MySQL 실행 구성

초기 API:
- POST /auth/signup
- POST /auth/login
- POST /auth/reissue
- POST /auth/logout
- GET /auth/me

아직 구현하지 말 것:
- OAuth2 소셜 로그인
- MFA
- Passkey
- SAML
- 복잡한 Permission 관리
- 게시판/댓글/파일 도메인 권한
- 관리자 콘솔
- Kubernetes
- Kafka
- Service Mesh

중요한 설계 원칙:
- auth-service는 인증과 전역 사용자 상태만 담당한다.
- auth-service는 board-service의 게시글 권한을 판단하지 않는다.
- 전역 Role은 USER, ADMIN 정도만 둔다.
- 리소스별 인가는 각 도메인 서비스에서 처리한다.
- Refresh Token은 원문 그대로 DB에 저장하지 말고 hash로 저장한다.
- 비밀번호도 반드시 hash로 저장한다.
- Access Token은 짧은 만료 시간을 가진다.
- Refresh Token은 Access Token보다 긴 만료 시간을 가진다.

패키지 구조:
com.dbwp031.authservice
├─ AuthServiceApplication.kt
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
   │  ├─ ErrorCode.kt
   │  ├─ BusinessException.kt
   │  └─ GlobalExceptionHandler.kt
   └─ security
      ├─ PasswordEncoderConfig.kt
      └─ JwtProperties.kt

Entity:
1. User
- id: Long
- email: String
- passwordHash: String
- nickname: String
- status: UserStatus
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

UserStatus:
- ACTIVE
- LOCKED
- WITHDRAWN

2. Role
- id: Long
- code: String
- name: String

Role 초기 데이터:
- USER
- ADMIN

3. UserRole
- id: Long
- userId: Long
- roleId: Long

4. RefreshToken
- id: Long
- userId: Long
- tokenHash: String
- revoked: Boolean
- expiresAt: LocalDateTime
- createdAt: LocalDateTime

JWT 요구사항:
- Access Token에는 다음 claim을 포함한다.
  - sub: userId
  - email
  - roles
  - status
  - iat
  - exp

예시:
{
  "sub": "1",
  "email": "user@example.com",
  "roles": ["USER"],
  "status": "ACTIVE",
  "iat": 1710000000,
  "exp": 1710001800
}

토큰 만료 시간:
- Access Token: 30분
- Refresh Token: 14일

JWT 설정:
- secret은 application.yml에 직접 박지 말고 환경변수로 주입 가능하게 구성한다.
- local 개발용 기본값은 제공해도 된다.
- issuer는 auth-service로 둔다.

API 상세:

1. POST /auth/signup
Request:
{
  "email": "user@example.com",
  "password": "password1234",
  "nickname": "user"
}

Response:
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "user"
}

요구사항:
- email은 unique
- password는 hash 저장
- 기본 Role은 USER 부여

2. POST /auth/login
Request:
{
  "email": "user@example.com",
  "password": "password1234"
}

Response:
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}

요구사항:
- email/password 검증
- ACTIVE 상태인 사용자만 로그인 가능
- Access Token 발급
- Refresh Token 발급
- Refresh Token은 hash 저장

3. POST /auth/reissue
Request:
{
  "refreshToken": "..."
}

Response:
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 1800
}

요구사항:
- Refresh Token 유효성 검증
- DB에 저장된 tokenHash와 비교
- revoked=false 확인
- expiresAt 확인
- 기존 Refresh Token을 revoked 처리하고 새 Refresh Token 발급
- 새 Refresh Token hash 저장
- Refresh Token Rotation 방식 적용

4. POST /auth/logout
Request:
{
  "refreshToken": "..."
}

Response:
204 No Content

요구사항:
- Refresh Token을 찾아 revoked=true 처리
- 이미 revoked거나 없으면 204로 처리해도 된다.

5. GET /auth/me
Header:
Authorization: Bearer accessToken

Response:
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "user",
  "roles": ["USER"],
  "status": "ACTIVE"
}

요구사항:
- Access Token을 검증하고 사용자 정보를 반환한다.
- 추후 api-gateway가 토큰 검증 로직을 가져갈 예정이지만, auth-service 자체 검증 API로도 사용할 수 있게 한다.

예외 처리:
- 중복 이메일: 409
- 로그인 실패: 401
- 토큰 만료: 401
- 토큰 위조/유효하지 않음: 401
- 사용자를 찾을 수 없음: 404
- 잠긴 계정/탈퇴 계정: 403
- validation 실패: 400

공통 응답 예시:
{
  "code": "DUPLICATED_EMAIL",
  "message": "이미 사용 중인 이메일입니다."
}

보안 관련:
- password는 BCrypt로 hash한다.
- refreshToken은 SHA-256 등으로 hash해서 저장한다.
- Access Token은 JWT로 발급한다.
- Refresh Token은 JWT여도 되고 랜덤 문자열이어도 된다. 단, DB에는 hash만 저장한다.
- 가능하면 Refresh Token은 충분히 긴 랜덤 문자열로 만들어줘.
- JWT secret은 최소 256bit 이상을 가정한다.

application.yml:
server:
  port: 8082

spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://localhost:3307/auth_db
    username: auth
    password: auth
  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true

auth:
  jwt:
    issuer: auth-service
    secret: ${JWT_SECRET:local-dev-secret-local-dev-secret-local-dev-secret}
    access-token-expiration-seconds: 1800
  refresh-token:
    expiration-days: 14

management:
  endpoints:
    web:
      exposure:
        include: health,info

docker-compose.yml:
- MySQL 8
- port 3307:3306
- database: auth_db
- username: auth
- password: auth

테스트 데이터:
- data.sql 또는 애플리케이션 초기화 코드로 USER, ADMIN Role을 생성해줘.
- 필요하다면 기본 관리자 계정도 local profile에서만 생성해줘.
  - admin@example.com
  - password: admin1234

주의사항:
- Spring Security를 사용하더라도 기본 form login은 비활성화해줘.
- REST API 방식으로 동작하게 해줘.
- CSRF는 비활성화해도 된다.
- CORS는 추후 gateway에서 처리할 예정이므로 auth-service에서는 최소 설정만 해도 된다.
- Entity를 API 응답으로 직접 반환하지 말고 DTO를 사용해줘.
- Service 계층에 비즈니스 로직을 작성해줘.
- Controller는 요청/응답 처리만 담당하게 해줘.
- Repository는 Spring Data JPA 인터페이스로 작성해줘.
- 테스트하기 쉽게 README.md에 curl 예시를 작성해줘.

검증 방법:
1. docker compose up -d 로 auth DB 실행
2. auth-service 실행
3. GET /actuator/health 호출 시 UP 확인
4. POST /auth/signup 으로 회원가입
5. POST /auth/login 으로 accessToken, refreshToken 발급 확인
6. GET /auth/me 에 Authorization 헤더를 넣고 사용자 정보 조회 확인
7. POST /auth/reissue 로 토큰 재발급 확인
8. POST /auth/logout 으로 refreshToken 폐기 확인

구현 전에 먼저 간단한 구현 계획을 설명하고, 그다음 파일을 생성/수정해줘.
구현 완료 후에는 생성된 주요 파일, 실행 방법, 테스트 방법을 요약해줘.
