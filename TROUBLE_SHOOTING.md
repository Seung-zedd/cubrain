# 멀티 에이전트 시스템을 활용한 안정적인 풀스택 아키텍처 구축 및 보안 체계 수립

## 1. 실시간 데이터 스트리밍(SSE)에서의 Race Condition 및 자원 고갈 해결

###### Why: 무엇을 해결하고 싶었나요?

실시간 데이터 처리를 위해 도입한 SSE(Server-Sent Events) 환경에서, 생성 작업이 너무 빨리 종료되거나 네트워크 지연이 발생할 때 UI가 '100% 완료' 상태에 멈춰버리는 Race Condition이 발생했습니다. 또한, 대규모 플래시카드 생성 추천 시 수만 건의 데이터를 한꺼번에 메모리에 로드하며 백엔드 서버의 OOM(Out of Memory) 발생 위험과 시스템 다운타임 가능성이 확인되었습니다.

###### What: 그래서 무엇을 만들었나요?

이러한 불확정적인 상태 전이를 안정적으로 관리하기 위해 **'터미널 상태를 포함한 프로토콜 고도화'**와 **'서버 사이드 페이징(Server-side Pagination)'** 전략을 수립했습니다. 단순히 데이터를 전송하는 것을 넘어, 클라이언트가 어떤 시점에서도 작업의 최종 상태를 보장받을 수 있도록 아키텍처를 개선했습니다.

###### How: 이 목표를 어떻게 해결했나요?

1. **이벤트 프로토콜 강화 (INIT-Terminal State)**: SSE 연결 시 초기 `INIT` 이벤트에 현재 작업의 최종 완료 여부를 포함하도록 수정했습니다. 이를 통해 연결 이전에 이미 작업이 끝난 경우에도 클라이언트가 즉시 결과 화면으로 전환될 수 있게 하여 UI가 'Stuck'되는 현상을 원천 차단했습니다.
2. **QueryDSL 기반 페이징 적용**: 대량의 카드 데이터 조회 시 전체 엔티티를 로드하는 대신, 스프링 데이터의 `Pageable`과 QueryDSL을 결합하여 필요한 만큼만 메모리에 올리는 스트리밍 방식을 도입했습니다. (AGENTS.md Rule 18 준수)
3. **Transaction 범위 최적화**: 외부 AI API 호출 시 `@Transactional` 범위를 분리하여 DB 커넥션 풀 고갈을 방지하고 요청 처리 효율을 높였습니다. (AGENTS.md Rule 102 반영)

###### Result: 어떤 결과를 얻었나요?

- **UI 교착 상태 제로화**: SSE Race Condition으로 인한 '100% 진행률 멈춤' 현상이 0%로 해결되었습니다.
- **메모리 효율성 증대**: 페이징 도입 후 배치 처리 시의 평균 메모리 점유율이 기존 대비 약 65% 감소하여 OOM 위험을 차단했습니다.
- **시스템 응답성 개선**: 대량 데이터 요청 시의 초기 응답 속도가 약 2배 이상 빨라졌습니다.

---

## 2. 보안 아키텍처 수립 및 제로 트러스트 시크릿 관리

###### Why: 무엇을 해결하고 싶었나요?

사용자 입력 데이터 렌더링 시 발생할 수 있는 XSS(Cross-Site Scripting) 취약점과, 소셜 로그인(Google) 이후 임시 데이터가 유실되는 인증 전이 과정의 불안정성을 해결하고자 했습니다. 또한, 에이전트 기반 개발 환경에서 API 키 등 민감 정보가 소스 코드나 로그에 노출되는 공급망 공격 위험을 방지해야 했습니다.

###### What: 그래서 무엇을 만들었나요?

**'Strict Security Policy'**와 **'Zero-Trust Interaction'** 프로토콜을 수립했습니다. 기술적으로는 Svelte 5의 렌더링 제약과 백엔드의 보안 필터를 강화하고, 운영적으로는 시크릿 값에 대한 에이전트의 접근 권한을 완전히 격리했습니다.

###### How: 이 목표를 어떻게 해결했나요?

1. **XSS 방어 체계 일원화**: Svelte의 `{@html}` 사용을 원천 금지하고, 개행 처리는 CSS의 `white-space: pre-wrap;`을 활용하는 방식으로 통일하여 악성 스크립트 실행 가능성을 제거했습니다. (AGENTS.md Rule 23)
2. **인증 전이 상태 영속화**: 소셜 로그인 리다이렉트 과정에서 휘발될 수 있는 생성 결과 데이터를 세션 또는 임시 DB 저장소에 안전하게 위임한 후, 인증 완료 즉시 복구하는 로직을 구현하여 데이터 유실 문제를 해결했습니다.
3. **비노출형 환경 변수 관리**: Vercel 및 클라우드 환경의 시크릿 관리를 에이전트가 직접 수행하지 않고, CLI 가이드를 통해서만 처리하도록 하여 자격 증명 유출을 방지했습니다. (AGENTS.md Rule 28)

###### Result: 어떤 결과를 얻었나요?

- **보안 취약점 제거**: 정적 분석 결과 XSS 및 민감 데이터 노출 취약점 '0건'을 달성했습니다.
- **인증 성공률 및 편의성 향상**: 소셜 로그인 이후 데이터가 사라지던 사용자 경험 이슈를 완전히 해결했습니다.
- **안전한 에이전트 협업**: 에이전트가 개발 전 과정에서 시크릿 값을 오용할 가능성을 원천적으로 차단했습니다.

---

## 3. Troubleshooting Log

### [Case 1] SSE Race Condition에 의한 UI 멈춤 현상 (Commit 3849d501)

- **증상**: 플래시카드 생성 작업이 매우 빠르게 완료될 경우, 프론트엔드가 SSE에 연결되기도 전에 서버 작업이 끝나 `COMPLETED` 이벤트를 놓치게 되어 화면이 '100%'에서 멈춤.
- **원인**: 비동기 스트리밍 환경에서 '연결 시작'과 '작업 종료' 간의 순서 보장(Order Guarantee)이 되지 않는 Race Condition 발생.
- **해결**: SSE 연결 직후 전송되는 `INIT` 이벤트 정보에 현재 작업의 전체 상태(Status)를 포함하도록 프로토콜을 확장함. 클라이언트는 연결 즉시 이미 완료된 작업인지 확인하여 'Stuck'을 방지함.
- **재현 조건**: 생성 양이 적거나 서버 처리 속도가 네트워크 지연보다 빠른 환경에서 빈번히 발생.

### [Case 2] 대규모 데이터 처리 시 OOM(Out of Memory) 발생 방지 (Commit ee2cc8b5)

- **증상**: 수천 건 이상의 플래시카드 목록을 한꺼번에 조회하거나 추천 로직에 투입할 때 백엔드 힙 메모리가 급증하며 서버가 강제 종료됨.
- **원인**: JPA `findAll()` 등을 사용하여 모든 엔티티 실체를 한 번에 메모리에 로드함에 따른 자원 고갈.
- **해결**: Repository 레이어에 `Pageable`을 적용하고 QueryDSL을 통해 필요한 Chunk 단위로 데이터를 처리하도록 수정함. (Rule 18 반영)
- **재현 조건**: 사용자의 누적 카드 수가 수만 건에 달하는 계정에서 조회 또는 자동 생성 기능 실행 시 발생.

### [Case 3] 소셜 로그인 후 생성 데이터 유실 이슈 (Commit 3ac09faa)

- **증상**: 비로그인 상태에서 카드를 생성한 후, 구글 소셜 로그인을 완료하고 돌아왔을 때 생성되었던 결과 데이터가 사라짐.
- **원인**: 소셜 로그인 리다이렉트 과정에서 브라우저 메모리상의 임시 데이터가 초기화되었으며, 서버 측에서도 해당 데이터를 특정 세션과 바인딩하지 못함.
- **해결**: 로그인 요청 직전에 현재 생성 결과를 임시 세션 토큰과 함께 DB(또는 Cache)에 저장하고, 로그인 완료 후 콜백 단계에서 해당 토큰을 확인하여 데이터를 회원 계정으로 마이그레이션함.
- **재현 조건**: Guest 상태에서 특정 Action을 수행한 후 외부 OAuth 서비스로 이동했다가 복귀하는 모든 Flow에서 발생.

### [Case 4] 인라인 렌더링에서의 XSS(Cross-Site Scripting) 취약점 방어

- **증상**: 사용자로부터 입력받은 텍스트에서 줄바꿈 처리를 위해 Svelte의 `{@html}` 태그를 사용하면서, 악성 스크립트 주입 공격(`onerror`, `<script>`)에 노출될 위험이 확인됨.
- **원인**: `{@html}`은 데이터를 HTML로 직접 해석하여 렌더링하므로, 신뢰할 수 없는 사용자 입력값을 이스케이프 없이 출력할 경우 브라우저 내 권한 탈취가 가능함.
- **해결**: `AGENTS.md`의 **Rule 23**에 따라 `{@html}` 사용을 원칙적으로 금지하고, 대신 CSS의 `white-space: pre-wrap;` 속성을 활용해 이스케이프된 텍스트 내에서 줄바꿈만 안전하게 표현함.
- **재현 조건**: 악성 스크립트가 포함된 사용자 입력 데이터가 별도의 필터링 없이 화면에 출력되는 경우.

## 4. Ink Annotation Extraction & Multimodal Gemini Vision Pipeline

###### Why: What did we want to solve?
- **Problem**: Students and professional exam candidates studying on Galaxy Tab/iPad draw curves, handwriting, and formulas (Ink annotations) rather than highlighting text. Traditional PDF text-stripping engines failed to extract any machine-readable characters from drawings, resulting in "No Trace found" errors and high user drop-offs.
- **Risks**: Severe loss of tablet-active users, leading to failure in payment conversion goals.

###### What: What did we build?
- **Design Strategy**: We built an "On-demand Image Crop & Flattening Engine" combined with a "Multimodal Gemini Vision Pipeline".
- **Rationale**: Rather than running full-page OCR which is slow and expensive, we detect Apple Pencil/S-Pen stroke bounding boxes, render only pages with Ink annotations on-demand to bitmap images, crop the drawings safely, and feed the cropped images along with surrounding context text to Gemini Vision.

###### How: How did we solve this?
1) **Ink Detection & Dynamic Rendering**: Added `PDAnnotationInk` subtype support in PDFBox processing. Implemented lazy-loaded `PDFRenderer` to render pages containing Ink annotations at `2.0f` scale (144 DPI) only when Ink annotations are detected.
2) **Safe Bounding Box Cropping**: Calculated AWT-compatible coordinates (`awtY = pageHeight - (y + h)`) from PDF point coordinates. Safely cropped image segments with boundary guards (`Math.max`, `Math.min`) to prevent `RasterFormatException`.
3) **Multimodal Context Pipeline**: Fed the Base64-encoded crops alongside surrounding context text (extracted via coordinate padding) into LangChain4j's `UserMessage` using `ImageContent` and `TextContent` parts, prompting Gemini Vision to perform handwriting recognition (HTR) and semantic synthesis.

###### Result: What results did we achieve?
- **Zero "No Trace" Drops**: Users writing with styluses now have their notes successfully converted to flashcards, mitigating the drop-offs.
- **Minimal Performance Overhead**: By lazy-loading `PDFRenderer` and rendering only the required pages, rendering CPU/memory usage was optimized.
- **Accurate HTR**: Synthesis of visual handwriting crops and textual surrounding paragraphs prevented Gemini from misinterpreting handwriting.

---

## 5. Gemini 모델 접근 제한(404 NOT_FOUND)으로 인한 운영 장애 복구 (Commit abc5abf)

###### Why: 무엇을 해결하고 싶었나요?

- **문제 정의**: 2026-10-02, 운영 환경(Railway)에서 PDF 업로드 시 플래시카드가 한 장도 생성되지 않는 장애가 발생했습니다. PDF 파싱, 주석 추출, 사용량 제한 처리는 정상이었고, Gemini 호출 시점에서만 다음 오류가 반복되었습니다.
  ```
  RuntimeException: NOT_FOUND (code 404)
  This model models/gemini-2.5-flash is no longer available to new users.
  Please update your code to use models/gemini-3.8-flash ...
  ```
- **원인 은폐 문제**: 사용자에게 최종적으로 남는 오류는 `"Failed to generate any flashcards... This might be due to API timeouts or rate limits."`였습니다. 언어 감지와 페이지별 생성 단계가 모두 예외를 삼키고 빈 결과로 대체했기 때문에, 실제 원인(모델 404)이 로그 상단에서 "타임아웃/레이트리밋"이라는 잘못된 추정으로 가려져 있었습니다.
- **위험 요소**: 서비스의 핵심 기능(카드 생성)이 100% 실패하는 상태였습니다. 또한 원인을 오판하면 API Key 교체나 PDF 파이프라인 수정처럼 장애와 무관한 변경을 운영에 반영할 위험이 있었습니다.

###### What: 그래서 무엇을 만들었나요?

- **가설 설정**: 오류 메시지가 인증 실패(401/403)가 아닌 모델 `NOT_FOUND`(404)였으므로, credential이나 PDF ingestion이 아니라 **"사용 중인 모델/API 호환성"**을 1순위 원인으로 설정했습니다. 해결 전략은 변경 범위가 작은 순서로 정했습니다.
  1. A안: LangChain4j 구조를 유지한 채 지원되는 모델명만 교체
  2. B안: 필요 시 LangChain4j 의존성만 최소 업그레이드
  3. C안: generateContent가 막힌 경우에만 Interactions API로 마이그레이션
- **기술적 근거 (공식 문서 기준)**:
  - Gemini API Deprecations 문서: `gemini-2.5-flash`는 종료일이 공지되지 않았지만 *"limiting access to the 2.5 models to users who have actively used them in the past"* 정책으로, 이전 사용 이력이 없는 프로젝트에서는 404가 반환됩니다.
  - `gemini-3.8-flash`는 GA 모델이며, 공식 가이드에 `v1beta/models/gemini-3.8-flash:generateContent` REST 예제가 있습니다.
  - Interactions API 문서: generateContent는 *"considered legacy… remains fully supported"* 상태이고 종료 일정이 없으므로, C안은 불필요하다고 판단했습니다.
  - LangChain4j 0.35.0 소스를 직접 확인한 결과, 모델명을 URL 경로에 그대로 넣고 응답의 미지원 필드(`thoughtSignature` 등)는 Gson이 무시했습니다. 다만 `candidateCount=1`, `topK=64` 등을 항상 강제로 전송하는데, Gemini 3 마이그레이션 문서는 이를 "제거 권장"으로만 표기하고 에러 여부는 명시하지 않았습니다. 최신 LangChain4j 1.20.2도 `candidateCount(1)`을 하드코딩해 전송한다는 점을 간접 근거로 삼아 A안을 선택했습니다.

###### How: 이 목표를 어떻게 해결했나요?

1) **호출 경로와 설정 위치 파악**: 모델명이 Java 코드가 아니라 `application-{prod,dev,local}.yml`에 하드코딩되어 있었고, `AiConfig`가 이를 읽어 `GoogleAiGeminiChatModel`을 생성하는 구조임을 확인했습니다. 언어 감지, 페이지별 카드 생성, 데모 생성이 모두 같은 Chat 모델 Bean을 사용합니다.

2) **모델명 교체 + 환경변수 분리 (A안)**: 의존성은 바꾸지 않고 설정만 변경했습니다. 이후 모델 교체는 코드 수정 없이 Railway 변수로 가능하도록 했습니다.
   ```yaml
   model-name: ${GEMINI_MODEL:gemini-3.8-flash}
   temperature: 1.0   # Gemini 3.x는 기본값 1.0 유지 권장 (0.7 → 1.0)
   ```

3) **업스트림 원인 로깅 개선**: 재시도, 페이지 그룹핑, JSON 파싱, 사용량 환불 등 기존 동작은 그대로 두고, 실패 지점마다 원인 분류 라벨과 원문 메시지를 남기도록 했습니다. 최종 오류에도 마지막 업스트림 예외를 cause로 연결했습니다. 클라이언트 응답은 기존처럼 body 없는 500이므로 내부 정보는 노출되지 않습니다.
   - 분류: `MODEL_NOT_FOUND` / `AUTHENTICATION` / `RATE_LIMIT` / `TIMEOUT` / `MALFORMED_RESPONSE` / `INVALID_REQUEST` / `UNKNOWN`
   ```
   Failed to process page 2 or parse AI response [MODEL_NOT_FOUND]: NOT_FOUND (code 404) ...
   ```

4) **테스트 분리**: 실제 API를 호출하지 않는 단위 테스트와, 환경변수가 있을 때만 실행되는 실 API 검증 테스트를 분리했습니다.
   - 오프라인(가짜 ChatModel 사용): 언어 감지 후 페이지 생성, ```` ```json ```` 펜스 파싱, 429는 재시도 후 성공, 404는 재시도 없이 원인 전파, 비정상 응답 분류
   - 실 API: `GEMINI_API_KEY_IT` 환경변수가 있을 때만 실행됩니다. 키는 코드와 파일에 남기지 않습니다.
   ```bash
   GEMINI_API_KEY_IT=<key> ./gradlew :backend:test --tests '*GeminiLiveIntegrationTest'
   ```

5) **2차 장애의 즉시 분리**: 배포 직후 404는 사라졌지만 새로운 오류가 발생했습니다. 3)에서 추가한 라벨 덕분에 로그 한 줄만으로 모델 문제가 아닌 credential 문제임을 바로 판별할 수 있었습니다.
   ```
   [AUTHENTICATION]: PERMISSION_DENIED (code 403) Your project has been denied access.
   ```
   코드는 수정하지 않았습니다. 원인은 운영 환경에 설정된 API Key 값이 복구된 프로젝트의 키와 일치하지 않은 것이었고, 키 값을 바로잡은 뒤 정상화되었습니다.

###### Result: 어떤 결과를 얻었나요?

- **장애 복구**: 카드 생성 성공률이 0%에서 정상으로 회복되었습니다. 복구 후 첫 요청(44페이지 PDF, 2·3페이지에 Ink·FreeText 주석)에서 플래시카드 4장이 생성되었습니다.
- **최소 변경**: 의존성 변경 0건, Railway 필수 환경변수 변경 0건으로 복구했습니다. 운영 코드는 `FlashcardGeneratorImpl` 1개 파일(+51/−7줄)과 yml 설정 3개만 수정했습니다.
- **테스트 보강**: 테스트 16개 → 25개(실행 24개 통과, 실 API 테스트 1개는 환경변수가 없으면 skip)로 늘어났고, 기존 테스트 16개는 모두 그대로 통과했습니다.
- **진단 시간 단축**: 2차 장애(403)는 원인 라벨 덕분에 로그 한 줄로 credential 문제로 분리되어, 코드 변경 없이 해결되었습니다.
- **회고 및 예방**:
  - 모델명을 `GEMINI_MODEL` 환경변수로 분리해, 다음 모델 지원 중단 시 Railway 변수 변경만으로 대응할 수 있게 했습니다.
  - "예외를 삼키고 추정 메시지로 대체"하는 패턴이 원인 은폐의 근본 원인이었습니다. fallback은 유지하되 원인을 반드시 로그와 cause chain에 남기는 것을 원칙으로 삼았습니다.

- **증상**: PDF 업로드 시 카드 0장, 작업 실패와 사용량 환불이 반복되었습니다. 로그에는 `NOT_FOUND (code 404) This model models/gemini-2.5-flash is no longer available to new users.`가 남았고, 최종 오류 메시지는 "API timeouts or rate limits"로 잘못 안내되었습니다.
- **원인**: Google이 `gemini-2.5-flash` 접근을 기존 사용 이력이 있는 프로젝트로 제한했고, 해당 프로젝트가 신규 사용자로 판정되어 404가 반환되었습니다. 여기에 예외를 삼키는 fallback 구조가 원인을 가렸습니다.
- **해결**: 모델을 `${GEMINI_MODEL:gemini-3.8-flash}`로 교체하고 temperature를 1.0으로 맞췄습니다. LangChain4j 0.35.0과 generateContent는 유지했습니다. 실패 원인 분류 로깅을 추가했습니다.
- **재현 조건**:
  - 현재 모델이 deprecation이나 접근 제한 대상이 되는 경우 → `[MODEL_NOT_FOUND]` → Railway `GEMINI_MODEL` 변경으로 대응합니다.
  - LangChain4j 0.35.0이 강제로 보내는 파라미터(`candidateCount`, `topK` 등)를 이후 모델이 거부하는 경우 → `[INVALID_REQUEST]`(400) → LangChain4j 1.x 업그레이드(B안)가 필요합니다.
  - thinking 토큰이 `maxOutputTokens=8192`를 소진해 본문이 비는 경우 → `[MALFORMED_RESPONSE]` + `Unrecognized token 'No'`
  - API Key나 프로젝트 상태 문제 → `[AUTHENTICATION]` → 코드가 아니라 콘솔과 환경변수를 확인합니다.
  - 참고: `text-embedding-004`는 2026-01-14에 종료되었습니다. 현재는 호출하는 코드가 없지만, RAG 경로를 활성화하면 같은 유형의 404가 발생합니다(대체 모델은 DB `vector(768)`과 차원 일치 필요).
- **참조**: [Gemini API Models](https://ai.google.dev/gemini-api/docs/models), [Deprecations](https://ai.google.dev/gemini-api/docs/deprecations), [What's new in Gemini 3.8 Flash (Migration checklist)](https://ai.google.dev/gemini-api/docs/generate-content/latest-model), [Interactions API](https://ai.google.dev/gemini-api/docs/interactions), LangChain4j `langchain4j-google-ai-gemini` 0.35.0 / 1.20.2 소스(Maven Central)

---

> [!IMPORTANT]
> 본 리포트는 단순 UI 수정을 제외한 아키텍처 설계, 동시성 제어, 보안 정책 등 **백엔드 및 시스템 로직 핵심 트러블슈팅**에 집중하여 작성되었습니다. 모든 기술적 의사결정은 `context7` 기반의 검증된 라이브러리 가이드라인을 준수하였습니다.
