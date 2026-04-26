# 03 模組規劃

## 目前主要結構

```text
app/src/main/java/com/jam/voiceagent/
├── data/
│   ├── audio/
│   ├── local/
│   ├── model/
│   ├── network/
│   ├── repository/
│   └── util/
├── ui/
│   ├── avatar/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   ├── theme/
│   ├── util/
│   └── voice/
```

## data/local

### TokenStore

負責保存 registered auth 狀態與 memory-only guest auth。

- registered access token：SharedPreferences
- registered refresh token：SharedPreferences
- registered token expires_at：SharedPreferences
- guest token / guest_id / expires_at：memory-only

### SessionStore

負責 registered / guest session_id。

- registered session_id：SharedPreferences
- guest session_id：memory-only
- `createFreshRegisteredSessionId()`：registered 開新 conversation 時使用，只換 session，不動 token。

## data/network

### ApiClient

集中建立 Retrofit / OkHttp client，不可在多處硬編 base URL。

### AuthApi

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/guest`
- `POST /api/auth/refresh`

### ChatApi

- `POST /api/chat`

### VoiceApi

- `POST /api/voice/round`
- `GET /api/audio/{filename}` 或下載工具依實作集中處理

### ConversationApi

- `GET /api/conversations`
- `GET /api/conversations/{session_id}`
- `DELETE /api/conversations/{session_id}`

## data/repository

### AuthRepository

負責 login/register/guest/recovery。

- login/register
- create guest token
- refresh registered token
- registered refresh 失敗時清 registered 並建 guest

### ChatRepository

負責文字聊天。

- 送出前取得 token + session_id
- 遇 401 做 recovery + retry once
- 回傳 `switchedToGuest` 結構化 flag
- 不印 token / 完整文字

### VoiceRepository

負責語音 round。

- 送出前取得 token + session_id
- 上傳 WAV 與 session_id
- 下載播放所需 URL 回傳給 UI
- 遇 401 做 recovery + retry once
- retry 前不可刪錄音檔
- 回傳 `switchedToGuest`
- 回傳 `isSttEmptyResult` 結構化 flag 給 UI 顯示驚訝表情

### ConversationRepository

負責 registered history。

- list / detail / delete
- 401 recovery + retry once
- 403 視為需要登入 / guest 不可看 registered history
- refresh 失敗時回傳 `switchedToGuest` / `requiresLogin`

## ui/navigation

### AppRoot

AppRoot 是最小共享狀態容器。

負責：

- route 切換：Home / Login / Register / Chat list / Conversation detail
- `isLoggedIn`
- `isChatBusy`
- `latestAssistantReply`
- auth switched to guest 同步
- detail 確認後保存 selected registered session_id 並回 Home
- Home `+` action 呼叫 SessionStore 建新 registered session

### AppRoute

維護 route enum / sealed class。避免大型 navigation 重構。

## ui/screens

### AssistantHomeScreen

核心 Home 畫面。

包含：

- Avatar / status / latest assistant reply
- voice mode / text mode
- 主麥克風按住錄音
- text input
- bottom controls：左 `+`（registered only）、中主按鈕、右模式切換
- quick menu disabled by busy
- voice phase：Idle / Recording / SendingVoice / PlayingResponse
- refresh 失敗切 guest 表情：Helpless
- STT 空結果表情：Surprised

不可重寫 Home 視覺核心。

### ChatListScreen

registered conversation list。

- loading / empty / error / content
- item click -> detail
- delete action -> API
- delete loading 防重複
- guest 不顯示長期歷史

### ConversationDetailScreen

只讀 detail。

- loading / error / content
- messages readonly
- 不提供輸入框 / 麥克風
- 底部「確定使用這段對話」

## ui/avatar

角色表情與 Canvas 繪製。

- AvatarState：Idle / Listening / Thinking / Speaking / Happy / Sad / Confused / Surprised / Helpless 等
- 不使用設計稿圖片作為 runtime 素材
- 不破壞 UI-02 感測與觸摸互動

## ui/util

### DateTimeFormatters

- parse server ISO offset timestamp
- convert to `Asia/Taipei`
- format as `yyyy-MM-dd HH:mm:ss`
- parse 失敗 fallback，不 crash

## data/util

### SessionDebug

- 安全輸出 session 短碼
- 不印完整 session / token / message
