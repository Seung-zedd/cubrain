# 🧠 Cubrain (큐브레인) - AI 기반 PDF 학습 자료(플래시카드/문제) 자동 생성 SaaS 서비스

> **"공부한 흔적만 넣으세요. 정리는 AI가 합니다."**
> <br/>

[🌐 서비스 바로가기 (cubrain.app)](https://cubrain.app)

  <p align="center">
    <img src="https://img.shields.io/badge/Java 21-007396?style=flat-square&logo=Java&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring Boot 3.5-6DB33F?style=flat-square&logo=Spring-Boot&logoColor=white"/>
    <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=PostgreSQL&logoColor=white"/>
    <img src="https://img.shields.io/badge/Svelte 5-FF3E00?style=flat-square&logo=Svelte&logoColor=white"/>
  </p>

<br/>

## 🚀 프로젝트 소개

대학 전공 공부 시 수백 페이지의 PDF 강의록을 보며 수동으로 플래시카드를 만드는 비효율을 해결하기 위해 기획된 1인 풀스택 SaaS 서비스입니다. 사용자가 형광펜(Highlight) 또는 밑줄(Underline)을 친 내용을 AI가 분석하여 자동으로 복습용 문제와 요약 카드를 생성합니다.

- **개발 및 운영 기간:** 2025.12 ~ 현재 (실서비스 운영 중)
- **담당 역할:** 1인 풀스택 기획/개발/배포 (Backend 중심)

<br/>

## 🔒 열람 안내 (For Interviewers)

본 레포지토리는 비즈니스 로직 보호를 위해 프라이빗으로 관리되고 있으며, 면접관님의 리뷰를 목적으로 접근 권한이 부여되었습니다. **소스 코드 열람 시 `backend/src/main/java/` 내의 주요 비즈니스 로직(특히 PDF 처리 및 AI 프롬프트 엔진)을 중점적으로 검토해 주시면 감사하겠습니다.**

<br/>

## 🔗 API Documentation

[![API Docs](https://img.shields.io/badge/Apidog-API_Reference-7C3AED?style=for-the-badge&logo=apidog)](https://86o7xvdzj4.apidog.io/)
_👈Cubrain의 모든 API 엔드포인트와 데이터 구조는 위 링크에서 확인하실 수 있습니다._

<br/>

## 🛠 기술 스택

- **Backend:** Java 21, Spring Boot 3.5, Spring Data JPA, QueryDSL
- **AI & Data:** LangChain4j, Google Gemini 2.5 API, Apache PDFBox, pgvector
- **Database:** PostgreSQL, Flyway
- **Infra & DevOps:** Docker, Railway, GitHub Actions
- **Auth & Architecture:** Firebase Auth (Google OAuth 2.0 / JWT), SSE (Server-Sent Events) 비동기 스트리밍
- **Frontend (MVP용):** Svelte 5, TypeScript, Tailwind CSS

<br/>

## 🏗 시스템 아키텍처

<img width="1791" height="880" alt="system architecture diagram" src="https://github.com/user-attachments/assets/b2583e94-2184-4db2-8486-0d3845fa24d2" />

<br/>

## 💾 데이터베이스 스키마 (ERD)

서비스의 전체 데이터베이스 구조는 다음과 같습니다.

<br/>

<img width="841" height="990" alt="ERD diagram" src="https://github.com/user-attachments/assets/969c3e53-e90d-4896-a6a5-b2141a688db7" />

## 🔥 핵심 트러블슈팅 및 성능 최적화 (Key Engineering Decisions)

> 💡 **단순한 기능 구현을 넘어, 인프라 비용과 서비스 안정성 향상에 집중했습니다.**

### 1. 배치 프로세싱(Batch Processing)을 통한 AI 비용 90% 절감

- **문제:** 초기 모델에서는 사용자가 칠한 하이라이트 영역마다 개별적으로 LLM API를 호출하여 **과도한 트래픽 비용과 응답 지연** 발생.
- **해결:** 여러 개의 하이라이트 컨텍스트를 묶어 '페이지 단위의 합성 배치(Batch) 처리' 방식으로 백엔드 로직 전면 개편.
- **결과:** **LLM API 호출 횟수를 획기적으로 줄여 트래픽 비용 약 90% 절감 달성.**

### 2. PDFBox 좌표 기반 정밀 추출로 할루시네이션(Hallucination) 제어

- **문제:** 단순 텍스트 추출 시 문장 잘림이나 줄바꿈으로 인해 문맥이 유실되어 AI가 엉뚱한 문제를 생성.
- **해결:** `Apache PDFBox`를 활용해 하이라이트 객체의 물리적 좌표(Coordinate)와 주변 패딩 영역을 수학적으로 계산하여 텍스트를 정밀하게 추출하는 전처리 엔진 직접 구현.
- **결과:** LangChain4j와 연동하여 정확한 문맥(Context)을 프롬프트에 동적으로 주입, **AI 문맥 인식률 대폭 향상 및 할루시네이션 억제.**

### 3. 지수 백오프(Exponential Backoff) 기반의 회복 탄력적 장애 격리

- **문제:** 외부 AI API(Gemini)의 할당량 초과나 간헐적 네트워크 지연 시 서버 스레드가 블로킹되어 서비스 전체의 장애로 전파될 위험.
- **해결:** 메인 스레드와 AI 생성 스레드를 분리(Async Executor Queue)하고, 외부 장애 발생 시 **지수 백오프 기반의 재시도(Retry) 로직**을 아키텍처에 내재화.
- **결과:** 단일 장애점(SPOF)을 회피하고 서비스 가용성 및 안정성 극대화.

### 4. SSE(Server-Sent Events)를 활용한 비동기 실시간 UX 구축

- **문제:** 생성형 AI 특성상 응답 대기 시간이 길어, 일반적인 HTTP 요청-응답 모델에서는 브라우저 타임아웃 또는 사용자 이탈 발생.
- **해결:** Svelte 5의 Runes 기반 반응성 모델과 백엔드의 SSE 스트리밍을 연동하여, **AI의 생성 진행 상태를 프론트엔드에 실시간으로 시각화**.
- **결과:** 기존 Polling 방식 대비 서버 부하를 최소화하고, 사용자의 체감 대기 시간을 대폭 단축하여 UX 대폭 향상.

<br/>

## 🚀 로컬 실행 및 테스트 안내 (Notice)

본 프로젝트는 보안 및 데이터 무결성을 위해 **Firebase Auth**와 **Google Gemini API**, **pgvector**가 강제로 연동되어 구동됩니다.

환경 변수(`.env`) 세팅 및 외부 API 키 연동 없이 로컬 환경에서 즉시 빌드 및 실행하는 것은 제한되어 있습니다. 서비스의 전체 기능과 UX 테스트는 아래의 라이브 서버 환경에서 확인해 주시면 감사하겠습니다.

<br/>

👉 **[Cubrain 라이브 서비스에서 테스트하기](https://cubrain.app)**
