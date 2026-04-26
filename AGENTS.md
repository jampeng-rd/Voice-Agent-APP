# voice-agent-android — AGENTS.md

## 專案定位

`voice-agent-android` 是 `voice-agent-server` 的 Android client。App 以 **語音輸入 / 語音輸出為核心**，文字輸入是輔助模式；Home 不做 Web 聊天室，而是角色陪伴式語音互動畫面。

## 目前完成狀態

已完成至 **Android-CONV-01.3 UI 微調**。

### 已完成階段

1. Phase Android-UI-01：語音助手主畫面 / 角色表情原型
2. Phase Android-UI-02：觸摸好感度、傾斜、強搖、dizzy、idle 睡著等角色互動
3. Phase Android-AUTH/NAV-01：Login / Register / Chat 基本導頁
4. Phase Android-API-01：Auth API + Text Chat API 串接
5. Phase Android-API-01.1：Text Chat 穩定性與互動鎖定
6. Phase Android-API-02：Guest Token 啟動流程
7. Phase Android-VOICE-01：Voice Round API 串接，完成錄音、上傳 `/api/voice/round`、顯示文字回覆、busy 鎖定與暫存檔清理基礎
8. Phase Android-VOICE-01.1：支援 server `output_audio_url` 下載播放，guest / registered 語音皆可播放 TTS 回覆
9. Phase Android-API-03：Token Expiry Recovery，完成 registered refresh token、guest token recovery、chat/voice 401 retry once
10. Phase Android-API-03.1：Login 成功回 Home，語音流程 busy lock 補強
11. Phase Android-API-03.2：修正 voice recording regression，避免 pointerInput 因 busy state 重建造成錄音過短 / STT 空結果
12. Phase Android-API-03.3：refresh 失敗切 guest 後同步 UI 登入狀態，quick menu 立即切回登入入口
13. Phase Android-CONV-01：Registered conversation list / detail / delete API 串接，detail 確認後可回 Home 沿用選定 `session_id`
14. Phase Android-CONV-01.1：時間轉 Asia/Taipei、selected/request/response session 短碼 debug log
15. Phase Android-CONV-01.2：Home 新增 registered 新對話 `+` action；refresh 失敗切 guest 時角色切「無奈」
16. Phase Android-CONV-01.3：`+` 移到最左側並與模式切換底部對齊；STT 空結果時角色切「驚訝」

## 目前 App 已有能力

- Home：角色語音互動主畫面
- Login / Register：真實 API 流程
- Chat list：registered conversation list 真實 API
- Conversation detail：只讀查看歷史訊息，按「確定使用這段對話」後回 Home 繼續該 session
- Delete conversation：呼叫後端 DELETE API，成功後更新 list
- New conversation：registered Home 底部左側 `+`，建立新的 registered session，不登出、不刪歷史
- 文字模式：`POST /api/chat`
- 語音模式：錄音、WAV 上傳 `/api/voice/round`、下載 `output_audio_url`、播放 TTS
- registered access token / refresh token 持久保存
- guest token / guest session_id memory-only，不落地保存
- `session_id` 用於文字與語音對話連續性
- chat / voice busy 期間鎖住模式切換、麥克風、文字送出、新對話 `+`、重複送出、右上快捷選單
- token 過期時自動 recovery + retry once
- refresh 失敗時切回 guest、同步 UI 登入狀態、角色切「無奈」
- STT 空結果時保留錯誤訊息並切「驚訝」

## Server 已完成能力

`voice-agent-server` 已完成：

- `/api/auth/guest`
- `/api/auth/register`
- `/api/auth/login`
- `/api/auth/refresh`
- `/api/chat`
- `/api/voice/round`
- `/api/audio/{filename}`
- `/api/conversations`
- `/api/conversations/{session_id}` GET / DELETE
- registered refresh token flow
- registered conversation history in PostgreSQL
- voice output_audio_url
- voice 音檔生命週期清理

## 重要不可破壞規則

### Guest 隱私

Guest token、guest_id、guest expires_at、guest session_id 只能存在 memory，不可寫入 SharedPreferences / DataStore / Room / SQLite / cache file。Guest 不做長期 conversation history。

### Token recovery

- guest token 過期：重新呼叫 `/api/auth/guest`，保留 guest session_id，retry 原 request 一次。
- registered access token 過期：呼叫 `/api/auth/refresh`，保存新 access token，retry 原 request 一次。
- refresh token invalid / expired：清 registered token/session，建立 guest token，同步 `isLoggedIn=false`。
- retry 最多一次，避免無限迴圈。

### 語音流程

不要破壞 API-03.2 修正後的 voice pointerInput / 錄音生命週期。錄音、上傳、下載、播放期間維持 busy lock。Voice retry 前不可刪錄音暫存檔。

### 角色互動

不要重寫或破壞 UI-02 已完成的角色互動：摸臉 affectionLevel、三階段開心、傾斜、強搖 dizzy、Idle 微表情 / 睡著、聆聽中 glow、說話中 waveform。

### Logcat

不可印出 access token、refresh token、guest token、完整 STT 文字、完整歷史訊息。可以印 error type、HTTP status、identity type、retry 是否發生、session_id 短碼。

## 目前後續建議階段

### Phase Android-CONV-01.4 — Conversation UX Polish

可選補強：delete confirmation、empty/error/loading polish、時間格式細節、detail 訊息排序/排版、new conversation 提示優化。

### Phase Android-SETTINGS-01 — 設定與主題

registered user theme preset、基本設定頁、使用者偏好保存。

## Codex 執行方式

每次開始新 phase 前，請先閱讀：

- `AGENTS.md`
- `agent-docs/01_scope_and_goals.md`
- `agent-docs/02_runtime_flow.md`
- `agent-docs/03_module_plan.md`
- `agent-docs/04_testing_strategy.md`
- `agent-docs/05_phase_plan.md`
- `agent-docs/06_design_system.md`
- `agent-docs/07_ui_reference.md`
- `agent-docs/08_api_contract.md`

每次只做一個明確階段，完成後即停止，並回報：修改檔案、做了什麼、如何執行、如何人工測試、已知限制、README.md / agent-docs 更新內容。
