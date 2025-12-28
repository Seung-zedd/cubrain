[Cubrain Builder mode](https://linear.app/cubrain-app/project/cubrain-builder-mode-04bde422c5ae)

🟢 On track | Ambition posted an update on Nov 30

### **🗺️ Finalized Cubrain Product Roadmap (Updated)**

> V1으로 유저들을 모으고(마케팅), V2로 팬덤을 모은다(브랜딩).

#### **🚀 V1 (MVP - The "Extractor" Solution)**

> **"공부한 흔적만 넣으세요. 정리는 우리가 합니다."**

* **Core Feature:**
  * **PDF Annotation Parser:** 사용자가 업로드한 PDF의 하이라이트/밑줄을 좌표 기반으로 자동 추출.
  * **Intent-Based Generation (Smart Prompting):**
    * 🖍️ **Highlight:** 개념/맥락 위주 (Why/How/Summarize).
    * ✍️ **Underline:** 팩트/키워드 위주 (Cloze/Definition).
  * **Context-Aware:** 추출된 문장 + 주변 문단(Paragraph)을 AI에게 전달하여 할루시네이션 방지.
  * **★ Killer Feature:** **Export to Anki (.apkg) / CSV**. (기존 생태계 침투 전략)
* **Goal:** "복습 자료 만드는 시간"을 0초로 단축.
* **Target:** 이미 굿노트/아크로뱃으로 공부하고 있는 대학생 & 전문직 수험생.
* **Why:** 사용자의 기존 습관을 바꾸지 않으면서 가치를 제공함.

* **🛡️ Infra & Security (Early Hardening):**
  * **Google Cloud Armor:** MVP 런칭 시점부터 DDoS 공격 및 웹 취약점 방어 체계 구축. (이유: 초기 서비스 중단 리스크 원천 차단 및 사용자 신뢰 확보)

---

#### **📦 V1.1 (Stability & Performance Update)**

> **"더 빠르고, 더 안정적으로."**

* **Optimization:**
  * **Batch Processing:** 하이라이트 5\~10개를 하나의 프롬프트로 묶어서 처리 (API 호출 비용 및 속도 최적화).
  * **Async Queue:** 대기열 시스템 도입으로 타임아웃 방지 및 프로그레스 바 UX 고도화.
    * **\[Add\] Visual Progress UX (The Pacer):**
      * 실시간 처리 상태와 연동된 **캐릭터 애니메이션(Running/Stop/Success)** 적용.
      * 목적: 기술적 지연 시간(Latency)을 엔터테인먼트 요소로 승화.
* **Why:** 트래픽 증가 대비 및 사용자 경험(UX) 개선.

#### **🧠 V2 (The "Deep Dive & Dopamine" Solution)**

> **"단순 생성을 넘어, 오감으로 학습하세요."**

* **Core Feature:**
  * **✨ Golden Celebration (Gamification):**
    * **4 Elements Effect:** 정답 시 불(Fire), 물(Water), 흙(Earth/Spark), 바람(Wind) 이펙트 랜덤 발동.
    * **Rarity System:** 플래시카드 중요도에 따라 R / SR / SSR 등급 부여 및 시각적 차별화.
    * **Earthquake:** 어려운 문제(Hard) 해결 시 화면 흔들림 효과로 타격감 제공.
  * 
    > ### **🛠️ 구현 전략**
    1. **4원소 이펙트 (**`ElementalEffects.svelte`):
       * **🔥 불 (Fire):** 카드가 붉게 타오르며 위로 승천하는 애니메이션 + 불타는 소리.
       * **💧 물 (Water):** 화면에 물결이 치고 카드가 파도 타듯 흔들리며 사라짐 + 물소리.
       * **⛰️ 흙 (Earth/Spark):** 화면이 쿵! 하고 흔들리며(Earthquake) 금색 스파크 폭발 + 타격음. (기존 Golden Spark 강화)
       * **🌪️ 바람 (Wind):** 카드가 회오리바람에 휘말려 날아가버리는 애니메이션 + 바람 소리.
    2. **카드 등급 시스템 (**`Rarity`):
       * **R (Rare):** 기본 (Blue/Silver)
       * **SR (Super Rare):** 중요 (Purple/Pink)
       * **SSR (Specially Super Rare):** 핵심/최고 중요도 (Gold/Rainbow)
       * *유저가 중요도를 선택하면 테두리와 빛나는 효과가 달라집니다.*
    3. **🗣️ AI Oral Test (Speak Mode) \[NEW!\]**:
       * **Interaction:** 카드 우측 상단 마이크 버튼 클릭 -> 구술 답변.
       * **Engine:** Web Speech API (Browser Native) 활용하여 비용 절감.
       * **Grading Logic (Semantic Match):** 단순 텍스트 비교가 아닌 **LLM 기반 의미 유사도 판별**. (키워드가 포함되었는지, 맥락이 맞는지 판단).
       * **Threshold:** 의미가 80% 이상 통하면 정답 처리 -> "Close Enough!" 피드백과 함께 4원소 이펙트 발동.
  * **\[Confirmed\] 🔄 Smart Variations (Multi-Angle Learning):**
    * **Logic:** 하나의 하이라이트에서 `Definition(정의)`, `Reasoning(이유/Why)`, `Application(사례/Example)` 3가지 유형의 문제 생성.
    * **Trigger:** SR/SSR 등급 카드에서만 활성화 (비용 절감 및 프리미엄 느낌 부여).
    * **Lock-in:** "Cubrain 앱 전용" 기능으로 포지셔닝 (Anki Export 불가 항목).
  * **AI Tutor Chat (RAG):** "이게 왜 정답이야?"라고 물어볼 수 있는 채팅 기능.
* **Goal:** 사용자를 앱 안에 묶어두고(Lock-in), 학습의 즐거움(Dopamine)을 제공.
* **Target:** 단순 합격이 아니라 '성취감'과 '재미'를 원하는 유저.

---

#### **⚡ V3 (The "Quiz & Multi-Modal" Update)**

> **"텍스트를 넘어, 모든 자료를 정복하세요."**

* **Core Feature:**
  * **Hard Mode (Quiz):** 객관식(Multiple Choice) 및 주관식 입력(Type-in) 문제 생성.
  * **Multi-Modal Ingestion:** PDF 내의 도표/다이어그램/이미지를 인식하여 문제 생성 (Gemini Vision 활용).
  * **SRS (Spaced Repetition) Go/No-Go:** 사용자 요청(10건 이상) 시 자체 복습 알고리즘 도입 검토.
    * **Method:** 설정 메뉴 내 '기능 요청(Feature Request)' 구글 폼/Typeform 연동.
    * **Trigger:** 사용자 요청 **10건(+10)** 달성 시 즉시 개발 착수 (Lean Approach).
* **Goal:** 텍스트 위주의 학습 한계를 돌파.
* **Target:** 의대생(해부학), 공대생(회로도) 등 이미지 학습 필수 유저.

#### **📱 V4 (The "Ecosystem" Platform)**

> **"언제 어디서나, 끊김 없는 학습."**

* **Core Feature:**
  * **Mobile App:** 이동 중에 PDF를 업로드하고 복습할 수 있는 네이티브 앱.
  * **Chrome Extension:** 웹페이지 드래그 캡처 기능 부활 ("웹 서핑 중 학습").
* **Goal:** OS와 포맷을 가리지 않는 완전한 학습 생태계 구축.
