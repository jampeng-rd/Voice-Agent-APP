# Voice Agent Android

目標是建立一個以 **語音輸入 / 語音輸出為核心** 的 AI companion App；文字輸入是輔助模式。

目前 Android App 已具備：

角色語音互動 Home、Login/Register、Guest flow、文字聊天、語音 round、TTS 播放、Token recovery、registered conversation history、選擇歷史對話繼續、新對話 `+` 按鈕，以及幾個重要錯誤狀態的表情回饋。

---

## 1. 核心特色

### 語音為主

- Home 預設語音模式。
- 按住麥克風錄音，放開後送出。
- Android 上傳 WAV 到 server `/api/voice/round`。
- Server 回傳 `ai_reply` 與 `output_audio_url`。
- Android 下載 TTS 音訊並播放。

### 角色陪伴式 UI

- 角色用 Compose / Canvas 繪製，不依賴 runtime 圖片。
- 支援狀態：待機、聆聽中、思考中、說話中、開心、不開心、困惑、驚訝、無奈。
- 支援摸臉好感、傾斜、強搖、dizzy、idle 睡著。
- STT 空結果時會顯示驚訝表情。
- 登入過期切 guest 時會顯示無奈表情。

### Auth / Token

- 支援 registered login / register。
- 支援 guest token 自動啟動。
- registered access token / refresh token 可保存。
- guest token / guest session_id memory-only，不落地。
- access token 過期時自動 recovery + retry once。
- refresh token 失效時切回 guest，App 不閃退。

### Conversation History

- registered 使用者可查看 server conversation list。
- 可進入 conversation detail 只讀查看歷史訊息。
- detail 按「確定使用這段對話」後，回 Home 並使用該 `session_id` 繼續文字 / 語音對話。
- 可刪除 conversation。
- Home 左側 `+` 可建立新的 registered session_id，開始新的 conversation。

---

## 2. 已完成階段

- Phase Android-UI-01：語音助手 UI / 角色表情原型
- Phase Android-UI-02：角色觸摸與感測互動原型
- Phase Android-AUTH/NAV-01：Login / Register / Chat 基本導頁
- Phase Android-API-01：Auth API + Text Chat API 串接
- Phase Android-API-01.1：Text Chat 穩定性與互動鎖定
- Phase Android-API-02：Guest Token 啟動流程
- Phase Android-VOICE-01：Voice Round API 串接
- Phase Android-VOICE-01.1：`output_audio_url` 播放
- Phase Android-API-03：Token Expiry Recovery
- Phase Android-API-03.1：Login Navigation 與 Voice Busy Lock 小修
- Phase Android-API-03.2：Voice Recording Regression 修正
- Phase Android-API-03.3：Auth UI State Sync
- Phase Android-CONV-01：Registered Conversation History API 串接
- Phase Android-CONV-01.1：時間格式與 session debug
- Phase Android-CONV-01.2：New Conversation Action 與 Auth Expired Emotion
- Phase Android-CONV-01.3：UI 微調

---

## 3. Server 前置需求

後端專案為 `voice-agent-server`。

Android 目前依賴 server 具備以下 API：

```text
POST   /api/auth/guest
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/chat
POST   /api/voice/round
GET    /api/audio/{filename}
GET    /api/conversations
GET    /api/conversations/{session_id}
DELETE /api/conversations/{session_id}
```

server conversation API 目前實際欄位重點：

- list item：`session_id`、`title`、`created_at`、`updated_at`
- detail message：`role`、`content`、`created_at`
- delete response：`session_id`、`deleted`

---

## 4. Android API Base URL 設定

專案使用集中式 API base URL，不應在多個檔案硬編 server URL。

開發時可在 `local.properties` 設定：

```properties
VOICE_AGENT_BASE_URL=http://192.168.x.x:8000
```

真機測試請使用開發機 LAN IP。Emulator 可使用預設 fallback：

```text
http://10.0.2.2:8000
```

---

## 5. 如何執行

1. 使用 Android Studio 開啟 Android 專案根目錄。
2. 等待 Gradle Sync 完成。
3. 確認 `local.properties` 的 `VOICE_AGENT_BASE_URL` 指向可連線的 server。
4. 啟動 `voice-agent-server`。
5. 連接 Android 真機，啟用 USB 偵錯。
6. 選擇 `app` 組態並執行。

Build 驗證：

```bash
./gradlew :app:assembleDebug
```

---

## 6. 主要使用流程

### Guest 模式

1. 清除 App 資料後啟動。
2. App 自動建立 guest token。
3. 可直接使用文字 / 語音。
4. guest 不可查看 registered conversation history。
5. guest token / session_id 不會落地保存。

### Registered 模式

1. 進入 Login / Register。
2. 登入成功後回 Home。
3. 可使用文字 / 語音，conversation 會寫入 registered history。
4. 從 quick menu 進 Chat list 查看歷史對話。
5. 點 detail 後可按「確定使用這段對話」回 Home 繼續該 conversation。
6. Home 左側 `+` 可開始新的 conversation。

### Token 過期

- access token 過期：自動 refresh，retry once。
- refresh token 失效：清 registered，切 guest，顯示「登入已過期，已切換為訪客模式。」，角色切無奈。
- guest token 過期：重新取得 guest token，retry once。

---

## 7. 人工測試重點

### Auth / Guest

- guest 啟動後可直接送文字 / 語音。
- Login 成功後回 Home。
- refresh 失敗切 guest 後 quick menu 顯示登入。
- refresh 失敗切 guest 時角色切無奈。

### Text / Voice

- 文字送出期間不能切模式、開 menu、重複送出。
- 語音錄音、上傳、下載、播放期間 busy lock 正常。
- 空錄音顯示 STT 空結果錯誤，角色切驚訝後回復。
- 正常語音可播放 TTS。

### Conversation

- registered 可進 Chat list。
- guest 進 Chat list 會被導向 Login 或提示登入。
- list 顯示 server conversation。
- detail 顯示歷史訊息，且沒有輸入框 / 麥克風。
- 按「確定使用這段對話」後回 Home。
- 回 Home 後文字 / 語音都接續同一 conversation。
- 刪除 conversation 後 list 更新。

### New Conversation

- guest 不顯示 `+`。
- registered 顯示 `+`。
- `+` 位於底部最左側。
- `+` 與右側模式切換同尺寸。
- `+` 和模式切換的底部對齊中央主按鈕底部。
- busy 時 `+` 不可點。
- 按 `+` 後送文字 / 語音會建立新的 conversation。

---

## 8. 專案結構概覽

```text
app/src/main/java/com/jam/voiceagent/
├── data/
│   ├── audio/          # 錄音、播放、音訊暫存
│   ├── local/          # TokenStore / SessionStore
│   ├── model/          # API models
│   ├── network/        # Retrofit APIs / ApiClient
│   ├── repository/     # Auth / Chat / Voice / Conversation repositories
│   └── util/           # SessionDebug 等安全 log 工具
├── ui/
│   ├── avatar/         # AvatarState / Canvas face
│   ├── components/     # 共用 UI 元件
│   ├── navigation/     # AppRoot / AppRoute
│   ├── screens/        # Home / Auth / Chat list / Detail
│   ├── theme/          # Material theme / colors
│   ├── util/           # DateTimeFormatters
│   └── voice/          # waveform / voice UI helpers
```

---

## 9. 重要安全 / 隱私規則

### Guest

以下資料不得落地：

- guest token
- guest_id
- guest expires_at
- guest session_id
- guest conversation history

### Logcat

不可輸出：

- access token
- refresh token
- guest token
- 完整 STT 文字
- 完整歷史對話內容

可輸出：

- error type
- HTTP status
- identity type
- retry 是否發生
- session_id 短碼

---

## 10. 已知限制

- Guest 不做長期歷史。
- Conversation detail 是只讀頁，不支援直接回覆。
- 尚未做 conversation rename / search / pagination / share / export。
- 尚未做本地 Room / SQLite 離線快取。
- 長期記憶不是 Android 端功能；若 AI 要穩定記住很早以前資訊，需要 server 端 summary / memory / RAG。
- `android:usesCleartextTraffic="true"` 僅用於開發 LAN HTTP 測試，正式環境建議改 HTTPS。
- debug chips / sensor debug 後續 release 前應可關閉或移除。

---

## 11. 後續建議

### Android-CONV-01.4 — Conversation UX Polish

- delete confirmation
- loading / empty / error polish
- detail 訊息排版改善
- new conversation 提示優化

### Android-SETTINGS-01 — 設定頁

- theme preset
- debug toggle
- server URL 顯示 / 開發設定
- 使用者偏好保存

### Server Memory / Summary

若要讓 AI 穩定記住使用者姓名、偏好與長期資訊，需要在 server 端做：

- conversation summary
- user memory profile
- semantic memory / RAG
