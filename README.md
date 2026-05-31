# Spring Boot 게시판

Spring Boot 3 + Java 21 기반의 REST API + Thymeleaf 웹 UI 게시판 프로젝트입니다.  
JWT 인증(Access Token + Refresh Token), 게시글 CRUD, 댓글, 좋아요 기능을 포함합니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| DB | H2 (인메모리) |
| ORM | Spring Data JPA / Hibernate |
| 인증 | Spring Security + JWT (jjwt 0.12) |
| 웹 UI | Thymeleaf + Bootstrap 5 |
| API 문서 | SpringDoc OpenAPI (Swagger UI) |
| 검증 | Jakarta Bean Validation |
| 빌드 | Gradle |

---

## 빠른 시작

```bash
# 빌드
./gradlew.bat build

# 서버 실행 (기본 포트 8080)
./gradlew.bat bootRun

# 테스트
./gradlew.bat test
```

### 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `JWT_SECRET` | JWT 서명 키 (Base64) | 내장 기본값 (개발용) |

운영 환경에서는 반드시 `JWT_SECRET` 환경 변수를 직접 지정해야 합니다.

---

## 접속 URL

| 경로 | 설명 |
|---|---|
| `http://localhost:8080` | 웹 UI (게시판) |
| `http://localhost:8080/swagger-ui/index.html` | Swagger API 문서 |
| `http://localhost:8080/h2-console` | H2 DB 콘솔 (JDBC URL: `jdbc:h2:mem:testdb`) |

> H2는 인메모리 DB이므로 서버 재시작 시 모든 데이터가 초기화됩니다.

---

## 프로젝트 구조

```
src/main/java/com/example/demo/
├── config/          # Security, Swagger, CORS 설정
├── controller/      # REST API 컨트롤러 + Thymeleaf 웹 컨트롤러
│   └── BaseController.java   # 공통 응답 처리 추상 클래스
├── service/         # 비즈니스 로직
├── repository/      # Spring Data JPA 인터페이스
├── entity/          # JPA 엔티티
├── dto/             # 요청/응답 DTO
│   └── CommonResponse.java   # 통일된 API 응답 봉투 {status, message, data}
├── security/        # JWT 필터, 토큰 생성/검증
└── exception/       # 통합 예외 처리
```

### 레이어 흐름

```
요청
 └─> JwtAuthenticationFilter (JWT 파싱 → SecurityContext 저장)
      └─> Controller (요청 수신 · @Valid 검증)
           └─> Service (@Transactional · 비즈니스 로직)
                └─> Repository (JPA · DB 쿼리)
                     └─> Entity
```

---

## API 엔드포인트

> 인증이 필요한 API는 요청 헤더에 `Authorization: Bearer {accessToken}` 을 포함해야 합니다.

### 인증 (`/api/auth`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/auth/register` | ✗ | 회원가입 |
| POST | `/api/auth/login` | ✗ | 로그인 → 토큰 발급 |
| POST | `/api/auth/refresh` | ✗ | Refresh Token으로 Access Token 갱신 |

### 게시글 (`/api/posts`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/posts` | ✗ | 게시글 목록 (페이징) |
| GET | `/api/posts/{id}` | ✗ | 게시글 단건 조회 (조회수 증가) |
| POST | `/api/posts` | ✓ | 게시글 작성 |
| PUT | `/api/posts/{id}` | ✓ | 게시글 수정 (작성자 본인) |
| DELETE | `/api/posts/{id}` | ✓ | 게시글 삭제 (작성자 본인) |
| GET | `/api/posts/{id}/like` | ✗ | 좋아요 수 / 내 좋아요 여부 조회 |
| POST | `/api/posts/{id}/like` | ✓ | 좋아요 토글 |

### 댓글 (`/api/posts/{postId}/comments`)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/posts/{postId}/comments` | ✗ | 댓글 목록 |
| POST | `/api/posts/{postId}/comments` | ✓ | 댓글 작성 |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | ✓ | 댓글 삭제 (작성자 본인) |

### 공통 응답 형식

```json
{
  "status": 200,
  "message": "OK",
  "data": { ... }
}
```

에러 응답도 동일한 형식이며 `data` 는 `null` 입니다.

```json
{
  "status": 404,
  "message": "게시글을 찾을 수 없습니다: 99",
  "data": null
}
```

---

## 인증 방식

```
로그인 → accessToken (30분) + refreshToken (7일) 발급
           │
           ├─ API 요청 시: Authorization: Bearer {accessToken}
           │
           └─ accessToken 만료 시: POST /api/auth/refresh
                                    body: { "refreshToken": "..." }
                                    → 새 accessToken 발급
```

- **API**: JWT (Stateless) — `Authorization: Bearer` 헤더
- **웹 UI**: Spring Security 폼 로그인 (세션 쿠키)
- 두 방식은 `SecurityConfig`에서 별도 필터체인으로 완전히 분리되어 있습니다.

---

## 주요 설계 결정

### N+1 쿼리 방지
`PostRepository`와 `CommentRepository`에 `@EntityGraph(attributePaths = "author")`를 적용해 목록 조회 시 author를 JOIN FETCH로 한 번에 로딩합니다.

### FK 삭제 순서
게시글 삭제 시 자식 엔티티(좋아요 → 댓글)를 먼저 삭제한 후 게시글을 삭제해 FK 제약 위반을 방지합니다.

### 사용자 열거 공격 방지
로그인 시 "존재하지 않는 사용자"와 "비밀번호 불일치" 모두 동일한 메시지(`아이디 또는 비밀번호가 올바르지 않습니다.`)를 반환합니다.