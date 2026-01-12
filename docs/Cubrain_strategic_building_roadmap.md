[Cubrain Builder mode](https://linear.app/cubrain-app/project/cubrain-builder-mode-04bde422c5ae)

🟢 On track | Ambition posted an update on Nov 30, 2025


## **🧠 V2 (The "Deep Dive & Dopamine" Solution) - Updated**

> **"화려한 이펙트로 재미를(Dopamine), 소크라테스 AI로 통찰을(Deep Dive)."**

### **1. \[UX Nudge\] 🎯 Managed Choice System (Mode Selection)**

> *기존의 단순 시작 버튼을 대체하여, 사용자의 학습 목적에 맞는 3가지 넛지 레이어를 제공합니다.*

* **🐰 속독 모드 (Skimming):** 핵심 문장들을 연결하여 '5줄 요약 스토리' 생성. (전체 맥락 파악용)
* **🐢 암기 모드 (Retention):** *\[Existing MVP\]* Q&A 플래시카드 + Anki Export. (시험 대비용)
* **🦉 심층 독서 모드 (Deep Dive):** 하이라이트 기반 **소크라테스 문답** 및 **비판적 사고** 카드 생성. (통찰/논문용)

---

### **2. \[Intelligence\] 🦉 Deep Reading Engine (The Socrates)**

> *단순 요약을 넘어, AI가 사용자의 사고를 확장시킵니다.*

* **Socratic Questioning (Critical Thinking):**
  * **Logic:** 단순 Q&A 대신 "반대 가설 제시", "비유하기(Analogy)", "취약점 찾기" 등 사고 유발형 질문 생성.
  * **Tech:** `LangChain4j` 프롬프트 체인에 'Critical Thinker' 페르소나 주입.
* **Reverse Engineering (Logic Tracer):**
  * **Logic:** 저자의 결론(Conclusion)을 제시하고, 본문 속 근거(Premise)를 찾게 하는 역추적 학습.
* **Active Construction (Feedback Loop):**
  * **Logic:** 사용자가 작성한 '한 줄 요약'을 AI가 원문과 대조하여 즉시 피드백 제공.

---

### **3. \[Experience\] ✨ Golden Celebration (Gamification)**

> *학습의 지루함을 없애고 시각적, 청각적 보상을 제공합니다.*

* **4 Elements Effect (Random Reward):**
  * **🔥 불 (Fire):** 카드가 붉게 타오르며 위로 승천하는 애니메이션 + 불타는 소리.
  * **💧 물 (Water):** 화면에 물결이 치고 카드가 파도 타듯 흔들리며 사라짐 + 물소리.
  * **⛰️ 흙 (Earth/Spark):** 화면이 쿵! 하고 흔들리며(**Earthquake**) 금색 스파크 폭발 + 타격음. (타격감 강조)
  * **🌪️ 바람 (Wind):** 카드가 회오리바람에 휘말려 날아가버리는 애니메이션 + 바람 소리.
* **Rarity System (Visual Hierarchy):**
  * **R (Rare):** 기본 (Blue/Silver)
  * **SR (Super Rare):** 중요 (Purple/Pink)
  * **SSR (Specially Super Rare):** 핵심/최고 중요도 (Gold/Rainbow) - *테두리와 발광 효과 차별화.*
* **\[NEW\] 🗣️ AI Oral Test (Speak Mode):**
  * **Interaction:** 카드 우측 상단 마이크 버튼 클릭 -> 구술 답변.
  * **Engine:** `Web Speech API` (Browser Native) 활용하여 비용 절감.
  * **Grading Logic:** LLM 기반 의미 유사도(Semantic Match) 판별. (Threshold: 80% 이상).
  * **Feedback:** "Close Enough!" 피드백과 함께 4원소 이펙트 발동.

---

### **4. \[Retention\] 🔒 Lock-in & System**

> *사용자를 앱 안에 묶어두고(Lock-in) 습관을 형성합니다.*

* **\[Confirmed\] 🔄 Smart Variations (Multi-Angle Learning):**
  * **Logic:** 하나의 하이라이트에서 `Definition(정의)`, `Reasoning(이유/Why)`, `Application(사례/Example)` 3가지 유형 문제 생성.
  * **Trigger:** **SR/SSR 등급 카드에서만 활성화** (비용 절감 및 프리미엄 가치 부여).
  * **Lock-in:** "Cubrain 앱 전용" 기능으로 포지셔닝 (Anki Export 불가).
* **\[NEW\] ⏰ Daily Habit Alarm (Simple Push):**
  * **Logic:** 유저 설정 고정 시간(Cron)에 알림 발송.
  * **Message:** "연속 학습 3일째! 🔥 불꽃을 꺼뜨리지 마세요." (Streak 기반 넛지).
  * **Tech:** FCM / Web Push 인프라 구축.
* **🕸️ Neural Link (Zettelkasten):**
  * **Logic:** `pgvector` 활용, 현재 학습 내용과 유사한 과거 카드(유사도 0.85↑)를 자동 연결/추천.
* **AI Tutor Chat (RAG):**
  * **Feature:** "이게 왜 정답이야?"라고 물어볼 수 있는 채팅 인터페이스 제공.

---

## **⚡ V3 (The "Quiz & Multi-Modal" Update)**

> **"텍스트를 넘어, 모든 자료를 정복하세요."**

* **Core Feature:**
  * **Hard Mode (Quiz):** 객관식(Multiple Choice) 및 주관식 입력(Type-in) 문제 생성.
  * **Multi-Modal Ingestion:** PDF 내의 도표/다이어그램/이미지를 인식하여 문제 생성 (Gemini Vision 활용).
  * **\[Go/No-Go\] 🧠 Smart SRS Algorithm (1-3-7-14-30):** 사용자 요청(10건 이상) 시 자체 복습 알고리즘 도입 검토.
    * **Condition:** 설정 메뉴의 "스마트 복습 주기 켜기" 스위치를 눌렀을 때 -> **"Coming Soon! 10명이 요청하면 개발합니다."** 모달 띄우기.
    * **Implementation:** 요청 달성 시, V2에서 구축한 푸시 인프라에 `1-3-7...` 날짜 계산 로직만 태우면 끝.
* **Goal:** 텍스트 위주의 학습 한계를 돌파.
* **Target:** 의대생(해부학), 공대생(회로도) 등 이미지 학습 필수 유저.