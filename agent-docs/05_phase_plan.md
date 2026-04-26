# 05 階段規劃

## 已完成階段

- Phase Android-UI-01：語音助手 UI / 角色互動原型
- Phase Android-UI-02：角色觸摸與感測互動原型
- Phase Android-AUTH/NAV-01：假登入 / 假註冊 / 基本導頁
- Phase Android-AUTH/NAV-01.x：UI 收斂
- Phase Android-API-01：Auth API + Text Chat API 串接驗證
- Phase Android-API-01.1：Text Chat 穩定性與互動鎖定修正
- Phase Android-API-02：Guest Token 啟動流程
- Phase Android-VOICE-01：Voice Round API 串接
- Phase Android-VOICE-01.1：支援 server `output_audio_url` 播放
- Phase Android-API-03：Token Expiry Recovery
- Phase Android-API-03.1：Login Navigation 與 Voice Busy Lock 小修
- Phase Android-API-03.2：Voice Recording Regression 修正
- Phase Android-API-03.3：Auth UI State Sync
- Phase Android-CONV-01：Registered Conversation History API 串接
- Phase Android-CONV-01.1：Conversation 時間格式與 selected session debug
- Phase Android-CONV-01.2：New Conversation Action 與 Auth Expired Emotion
- Phase Android-CONV-01.3：UI 微調（+ 按鈕位置、STT 空結果驚訝表情、底部對齊）

## 目前里程碑狀態

Android App 已具備可用的語音 AI client 基線：

- guest / registered auth
- text chat
- voice round + TTS playback
- token recovery
- registered conversation list/detail/delete
- selected conversation continue
- new conversation action
- 角色互動與錯誤情緒回饋

## 後續建議階段

### Phase Android-CONV-01.4 — Conversation UX Polish

可選小修：

- delete confirmation
- empty / loading / error polish
- detail 訊息排版更清楚
- list item title fallback / 時間顯示 polish
- new conversation 成功提示優化

### Phase Android-SETTINGS-01 — 設定與主題

可做：

- 基本設定頁
- server URL 開發設定顯示
- theme preset
- 是否顯示 debug chips / sensor debug
- registered user preference 保存

### Phase Android-MEMORY-01 — 長期記憶 / Summary（需配合 server）

若希望 AI 永遠記得姓名、偏好、長期資訊，不能只靠 conversation 最近 N 輪 context。後續可配合 server 實作：

- conversation summary
- user profile memory
- memory extract / update
- RAG / semantic memory

### Phase Android-RELEASE-01 — 發佈前整理

- 移除或關閉 debug chips
- production base URL / HTTPS
- cleartext traffic policy
- icon / app name / permissions polish
- crash reporting / analytics（若需要）
