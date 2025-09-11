# Places BFF 설계서 (Flutter + Spring Boot, Kakao Local 기반)

> **공통 규칙**
> 1) **공통 응답 객체(`ResponseEnvelope<T>`)**를 사용해 모든 API의 응답 포맷을 일관화한다.
> 2) **에러 코드**는 본문 §5의 **표준화 테이블**을 따른다.
> 3) **스웨거(OpenAPI) 문서화는 `docs` 인터페이스로 분리**한다. 즉, 컨트롤러 구현과 별도 패키지/인터페이스에서 OpenAPI 어노테이션을 관리한다(§7).
> 4) **페이지네이션 구현**은 기존 유틸을 따른다:  
     >    `**/Users/yunjeong/Desktop/live_backend/src/main/java/com/example/live_backend/global/page**`  
     >    (해당 모듈의 Page/Sorter 규약을 재사용하고, 본 문서의 `Page` 응답 스키마와 호환되도록 어댑터를 두어라.)

c0870e11e5693bb2fec95ba9e987d639
이 카카오 rest api 야. 
---

## 0) 목적/개요

- **클라이언트(Flutter)**: 카카오 **지도 SDK**로 지도 타일/마커/제스처/내 위치 표시
- **백엔드(Spring Boot)**: 카카오 **Local REST API**를 **OpenFeign**으로 호출 → 검색/카테고리/상세 데이터를 **정규화 + 캐시 + 레이트리밋** 후 제공
- **캐시**: **Caffeine**(인스턴스 로컬, TTL: 검색 60–90s / 상세 10m) — **Redis 없이 시작**
- **자동완성**: _옵션_ 기능. v1 기본 스코프에는 **제외**, 필요 시 “카카오 기반 자동완성” 엔드포인트를 활성화 가능(ES 불필요)

---

## 1) 외부 키/의존성

- Kakao Developers 앱 생성 후 키 발급
    - **백엔드**: **REST API Key** (서버에서만 사용; `Authorization: KakaoAK {REST_KEY}`)
    - **Flutter**: 지도 SDK용 **Native App Key / JavaScript Key**
- Tech stack: Spring Boot 3.x / Java 21 / OpenFeign / Spring Cache + Caffeine / Resilience4j / Bucket4j

**환경 변수**

KAKAO_REST_API_KEY=xxx

application.yml (예)

kakao:
  local:
    base-url: https://dapi.kakao.com
    rest-api-key: ${KAKAO_REST_API_KEY}

spring:
  cache:
    type: caffeine

feign:
  okhttp:
    enabled: true


⸻

2) 공개 API 목록 (Base: /v1)

모든 응답은 ResponseEnvelope<T> 포맷으로 반환
search / nearby / suggest 응답 헤더에 Cache-Control: public, max-age=60 권장

2.1 주변 장소 검색(키워드) — 필수

GET /places/search

Query
	•	query (string, required, 2~50)
	•	lat (double, required), lng (double, required)
	•	radius (int, required, 200~3000)  // meter
	•	page (int, ≥1, default=1)
	•	size (int, 1~15, default=15)
	•	sort (enum: distance|accuracy, default=distance)

Response

{
  "success": true,
  "data": {
    "items": [
      {
        "id": "kakao:123456789",
        "name": "더마음의원",
        "category": { "code": "PSY", "label": "정신건강의학과" },
        "address": { "road": "서울 강동구 ...", "lot": "..." },
        "location": { "lat": 37.56, "lng": 127.02 },
        "phone": "02-1234-5678",
        "thumbnailUrl": null,
        "source": "kakao"
      }
    ],
    "page": { "number": 1, "size": 15, "hasNext": true }
  },
  "error": null
}


⸻

2.2 주변 장소 검색(카테고리) — 필수

GET /places/nearby

Query
	•	lat, lng, radius (정의 동일)
	•	category (enum: LEI|PSY|WEL|CSC, required)
	•	page, size (정의 동일)

Response
	•	2.1과 동일 스키마

비즈 규칙
	•	내부 카테고리 → 카카오 category_group_code(+ 필요 시 키워드 후필터) 매핑

⸻

2.3 장소 상세(정보 패널) — 필수

GET /places/{placeId}   // ex) kakao:123456789

Response

{
  "success": true,
  "data": {
    "id": "kakao:123456789",
    "name": "더마음의원",
    "category": { "code": "PSY", "label": "정신건강의학과" },
    "address": { "road": "...", "lot": "..." },
    "location": { "lat": 37.56, "lng": 127.02 },
    "phone": "02-3242-3242",
    "hours": [{ "day": "월", "open": "10:00", "close": "19:00" }],
    "intro": "정신건강 전문의 진료...",
    "photos": ["https://.../p1.jpg","https://.../p2.jpg"],
    "source": "kakao"
  },
  "error": null
}

비즈 규칙
	•	카카오 기본 필드 + 우리 DB 보정치(운영시간/소개/사진) 병합

캐시
	•	key = placeId, TTL = 10m

⸻

2.4 활성 미션 장소(마커 강조) — 필수 / JWT

GET /me/missions/active

Response 200

{
  "success": true,
  "data": {
    "placeId": "kakao:123456789",
    "location": { "lat": 37.56, "lng": 127.02 },
    "name": "더마음의원"
  },
  "error": null
}

Response 204
	•	본문 없음(Envelope 미반환 권장)

⸻

2.5 자동완성(카카오 기반) — 옵션

GET /places/suggest

Query
	•	query (string, required, 2~50)
	•	lat, lng, radius (정의 동일)
	•	size (int, 1~15, default=10)

Response

{
  "success": true,
  "data": {
    "suggestions": [
      { "type": "place", "label": "더마음의원", "placeId": "kakao:123", "highlight": [0,2] },
      { "type": "category", "label": "정신건강의학과" }
    ],
    "degraded": false
  },
  "error": null
}

비즈 규칙
	•	Feign으로 카카오 키워드 1페이지 호출 → 중복 제거/간단 랭킹 → 상위 N
	•	카카오 오류/429/타임아웃 시 degraded=true + 카테고리 제안만 반환

캐시
	•	key = query|tile(lat,lng)|radius|size, TTL = 60–90s

⸻

3) 요청 검증(서버)
	•	query: 2~50 chars
	•	lat: -90 ~ 90, lng: -180 ~ 180
	•	radius: 200~3000 (m)
	•	page: ≥1 / size: 1~15
	•	category: LEI|PSY|WEL|CSC

검증 실패 시 400 + VALIDATION_ERROR (공통 에러 포맷)

⸻

4) 카카오 Local 연동(OpenFeign)
	•	헤더: Authorization: KakaoAK {REST_API_KEY}
	•	타임아웃: connect 1s / read 2.5s (조정 가능)
	•	Resilience4j: @CircuitBreaker(name="kakaoLocal"), Retry 0~1회

@FeignClient(
  name="kakaoLocal",
  url="${kakao.local.base-url:https://dapi.kakao.com}",
  configuration=KakaoFeignConfig.class
)
public interface KakaoLocalFeign {

  @GetMapping("/v2/local/search/keyword.json")
  KakaoKeywordResponse searchByKeyword(
    @RequestParam("query") String query,
    @RequestParam("x") double lng, @RequestParam("y") double lat,
    @RequestParam("radius") int radius,
    @RequestParam("page") int page, @RequestParam("size") int size,
    @RequestParam(value="sort", required=false) String sort
  );

  @GetMapping("/v2/local/category/search.json")
  KakaoCategoryResponse searchByCategory(
    @RequestParam("category_group_code") String group,
    @RequestParam("x") double lng, @RequestParam("y") double lat,
    @RequestParam("radius") int radius,
    @RequestParam("page") int page, @RequestParam("size") int size
  );
}


⸻

5) 공통 응답/에러 규약

5.1 공통 응답 객체

public record ResponseEnvelope<T>(boolean success, T data, ErrorBody error) {
  public static <T> ResponseEnvelope<T> ok(T data) { return new ResponseEnvelope<>(true, data, null); }
  public static <T> ResponseEnvelope<T> error(String code, String message, Map<String, Object> details) {
    return new ResponseEnvelope<>(false, null, new ErrorBody(code, message, details));
  }
}

public record ErrorBody(String code, String message, Map<String, Object> details) {}

5.2 에러 코드 표

HTTP	code	설명
400	VALIDATION_ERROR	파라미터 검증 실패(범위/형식/필수)
401	UNAUTHORIZED	인증 필요(JWT 만료/누락)
403	FORBIDDEN	권한 부족
404	NOT_FOUND	리소스 없음(placeId 등)
409	CONFLICT	상태 충돌(미션 등)
429	RATE_LIMITED	레이트리밋 초과(Bucket4j/WAF)
502	UPSTREAM_ERROR	카카오 5xx 등 상위 장애
504	UPSTREAM_TIMEOUT	카카오 타임아웃
500	INTERNAL_ERROR	서버 내부 오류

예시(400)

{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "query must be at least 2 characters",
    "details": { "field": "query", "min": 2 }
  }
}


⸻

6) 비즈니스 규칙/정규화
	•	파라미터 변환: 카카오 호출 시 x=lng, y=lat
	•	카테고리 매핑(운영 가능)
	•	LEI/PSY/WEL/CSC → category_group_code (+ 필요 시 keyword filter)
	•	설정은 application.yml 또는 DB 테이블로 관리
	•	응답 정규화 공통 필드
	•	id = kakao:{place_id}
	•	category.code = 내부 코드(PSY 등), label = 한글 표시명
	•	address.road/lot, location.lat/lng, source = "kakao"

⸻

7) 문서화(스웨거) — docs 인터페이스 분리
	•	원칙: 비즈니스 컨트롤러와 OpenAPI 어노테이션 분리
	•	com.example.docs.api 패키지에 인터페이스를 두고
@Operation, @Parameter, @ApiResponses 등 문서 어노테이션은 인터페이스에만 부여
	•	실제 컨트롤러는 해당 인터페이스를 implements 하며 문서 의존을 갖지 않음
	•	springdoc OpenAPI 그룹 분리 권장

springdoc:
  group-configs:
    - group: public
      paths-to-match: /v1/**
    - group: internal
      paths-to-match: /internal/**


	•	Swagger UI: /swagger-ui.html, OpenAPI JSON: /v3/api-docs/public

예시

// docs 인터페이스
@RequestMapping("/v1/places")
public interface PlacesApiDocs {
  @Operation(summary="키워드 주변 검색", description="카카오 Local 프록시")
  @ApiResponse(responseCode="200", description="OK")
  @GetMapping("/search")
  ResponseEnvelope<PlaceSearchResult> search(@Parameter(description="검색 요청") @Valid SearchReq req);
}

// 실제 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/places")
public class PlaceController implements PlacesApiDocs {
  private final PlaceService service;
  @Override public ResponseEnvelope<PlaceSearchResult> search(@Valid SearchReq req) {
    return ResponseEnvelope.ok(service.search(req));
  }
}


⸻

8) 캐시/레이트리밋/회복력/헤더
	•	Caffeine 캐시
	•	placeSearch, placeNearby, suggest: TTL 60–90s, sync=true
	•	키 폭발 방지: 타일링(lat/lng 소수점 3자리 반올림)
	•	placeDetail: TTL 10m
	•	레이트리밋
	•	Bucket4j (IP/토큰별 5 rps, burst 10) → /v1/places/*
	•	(가능하면) L7 WAF rate-based rule 추가
	•	Resilience4j
	•	CircuitBreaker + (Retry 0~1회)
	•	HTTP 캐시 헤더
	•	Cache-Control: public, max-age=60 (검색류)

⸻

9) DTO 스케치

// 요청
public record SearchReq(
  @NotBlank String query, double lat, double lng,
  @Min(200) @Max(3000) int radius,
  @Min(1) int page, @Min(1) @Max(15) int size,
  String sort
) {}

public record NearbyReq(
  @NotBlank String category, double lat, double lng,
  @Min(200) @Max(3000) int radius,
  @Min(1) int page, @Min(1) @Max(15) int size
) {}

public record SuggestReq(
  @NotBlank String query, double lat, double lng,
  @Min(200) @Max(3000) int radius,
  @Min(1) @Max(15) int size
) {}

// 응답
public record Page(int number, int size, boolean hasNext) {}
public record Category(String code, String label) {}
public record Address(String road, String lot) {}
public record Location(double lat, double lng) {}
public record PlaceItem(String id, String name, Category category, Address address,
                        Location location, String phone, String thumbnailUrl, String source) {}
public record PlaceSearchResult(List<PlaceItem> items, Page page) {}
public record Hour(String day, String open, String close) {}
public record PlaceDetail(String id, String name, Category category, Address address,
                          Location location, String phone, List<Hour> hours,
                          String intro, List<String> photos, String source) {}
public record SuggestItem(String type, String label, String placeId, int[] highlight) {}
public record SuggestPayload(List<SuggestItem> suggestions, boolean degraded) {}

페이지네이션 주의: 서버 내부에서는
com/example/live_backend/global/page 모듈의 규약을 우선 적용하고,
외부 응답은 본 문서의 Page(number,size,hasNext) 스키마로 맵핑하여 반환한다.

⸻

10) 패키지/레이어 구조(예시)

com.example.places
 ├─ api           // 컨트롤러(비즈 구현)
 ├─ docs.api      // Swagger/OpenAPI 인터페이스(문서 전용)
 ├─ application   // 서비스 계층
 ├─ infra         // Feign 클라이언트, 매퍼
 ├─ domain        // DTO/엔티티/도메인 로직
 └─ config        // Cache/Feign/Resilience/RateLimit 설정


⸻

11) DB (선택: 상세 보정치/운영)

CREATE TABLE place_overrides (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  kakao_place_id VARCHAR(32) UNIQUE NOT NULL,
  intro VARCHAR(1000),
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE place_hours (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  place_override_id BIGINT NOT NULL,
  day_of_week TINYINT NOT NULL, -- 1=월 ... 7=일
  open_time TIME NULL,
  close_time TIME NULL,
  FOREIGN KEY(place_override_id) REFERENCES place_overrides(id)
);

CREATE TABLE place_photos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  place_override_id BIGINT NOT NULL,
  url VARCHAR(500) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  FOREIGN KEY(place_override_id) REFERENCES place_overrides(id)
);


⸻

12) 테스트/운영 체크리스트
	•	WireMock으로 카카오 Local 응답 녹화/회귀
	•	캐시 동시성: @Cacheable(sync=true)로 외부콜 1회 보장 확인
	•	에러/타임아웃/429 플로우 및 에러코드 매핑 검증
	•	메트릭: 카카오 호출 p95, 캐시 히트율, 429 비율, 트래픽
	•	키 보안: REST 키는 서버 시크릿으로만 주입(클라이언트 절대 금지)

⸻

결론
	•	본 설계서 기준으로 컨트롤러/서비스/Feign/캐시/문서화 분리까지 바로 개발 가능
	•	공통 응답 포맷과 에러 코드 표준을 준수하고, Swagger는 docs 인터페이스로 분리하라.
	•	페이지네이션은 기존 전사 유틸 경로(global/page)를 재사용해 일관성을 유지한다.

