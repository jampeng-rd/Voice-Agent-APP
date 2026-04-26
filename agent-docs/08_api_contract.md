# 08 API Contract — voice-agent-server

本文件是 Android 專案內保存的 `voice-agent-server` API 串接摘要。實際欄位以 server 目前實作為準；若不一致，Codex 應先回報差異並做最小調整。

## Base URL

後端 FastAPI router prefix 為 `/api`。Android 端不可把 server URL 硬編死在多個檔案中。真機測試可使用開發機 LAN IP，例如：

```text
http://192.168.x.x:8000
```

## Auth Header

需要 token 的 API 使用：

```text
Authorization: Bearer <token>
```

Logcat 不可印出 token。

## Auth API

### Register

```text
POST /api/auth/register
Content-Type: application/json
```

### Login

```text
POST /api/auth/login
Content-Type: application/json
```

成功 response：

```json
{
  "success": true,
  "token": "...",
  "refresh_token": "...",
  "user_id": 1,
  "email": "user@example.com",
  "expires_at": "...",
  "refresh_expires_at": "...",
  "error_message": null
}
```

### Refresh

```text
POST /api/auth/refresh
Content-Type: application/json
```

Request：

```json
{
  "refresh_token": "..."
}
```

成功 response：

```json
{
  "success": true,
  "token": "new-access-token",
  "refresh_token": "...",
  "expires_at": "...",
  "refresh_expires_at": "...",
  "user_id": 1,
  "email": "user@example.com",
  "error_message": null
}
```

規則：

- refresh token 只給 registered user 使用。
- guest token 不可 refresh。
- refresh token 只可用於 `/api/auth/refresh`。
- refresh token invalid / expired 時，Android 應清除 registered token/session 並切回 guest。

### Guest Token

```text
POST /api/auth/guest
```

Response：

```json
{
  "success": true,
  "token": "...",
  "guest_id": "...",
  "expires_at": "...",
  "error_message": null
}
```

guest token / guest_id / expires_at 只存在 memory，不寫入 SharedPreferences / DataStore / Room / SQLite / cache file。

## Text Chat API

```text
POST /api/chat
Authorization: Bearer <token>
Content-Type: application/json
```

Request：

```json
{
  "session_id": "android-session-uuid",
  "text": "你好"
}
```

Response 概念欄位：

```json
{
  "success": true,
  "session_id": "android-session-uuid",
  "identity_type": "registered",
  "user_id": 1,
  "guest_id": null,
  "user_text": "你好",
  "ai_reply": "...",
  "error_message": null
}
```

## Voice Round API

```text
POST /api/voice/round
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

欄位：

```text
session_id
audio_file
```

Response：

```json
{
  "success": true,
  "session_id": "android-session-uuid",
  "identity_type": "guest",
  "user_id": null,
  "guest_id": "...",
  "user_text": "使用者 STT 文字",
  "ai_reply": "AI 回覆文字",
  "input_wav": "...",
  "output_wav": "/home/jam/voice-agent-server/outputs/generated_audio/xxx.wav",
  "output_audio_url": "/api/audio/xxx.wav",
  "error_message": null
}
```

重點：

- `output_wav` 可能是 server 本機路徑，Android 不可直接讀取。
- Android 應優先使用 `output_audio_url`。
- `output_audio_url` 通常是相對 URL，例如 `/api/audio/xxx.wav`。
- Android 應用 `ApiConfig.baseUrl + output_audio_url` 組成完整 URL。
- 若 `output_audio_url` 是 null，Android 仍應顯示 `ai_reply`，不應閃退。

## Audio Download API

```text
GET /api/audio/{filename}
```

用途：下載 server 產生的 TTS wav。

- Android 用 `/api/voice/round` 回傳的 `output_audio_url` 下載。
- server 只允許安全檔名。
- server 有 generated audio TTL，預設 600 秒。
- Android 收到 `output_audio_url` 後應盡快下載播放。
- 若檔案過期或被清理，download 會回 404。

## Conversation API

### List conversations

```text
GET /api/conversations
Authorization: Bearer <registered access token>
```

用途：取得 registered user 的歷史 conversation list。Guest 不應使用長期 conversation history。

Server 實際 list item 欄位：

```json
{
  "success": true,
  "conversations": [
    {
      "session_id": "android-session-uuid",
      "title": "...",
      "created_at": "...",
      "updated_at": "..."
    }
  ],
  "error_message": null
}
```

注意：目前 server list item 不一定有 `last_message`，Android 使用 title / updated_at / created_at fallback。

### Get conversation detail

```text
GET /api/conversations/{session_id}
Authorization: Bearer <registered access token>
```

用途：取得單一 conversation 的歷史訊息，只讀顯示。

Server 實際 message 欄位使用 `content`：

```json
{
  "success": true,
  "session_id": "android-session-uuid",
  "title": "...",
  "created_at": "...",
  "updated_at": "...",
  "messages": [
    {
      "role": "user",
      "content": "...",
      "created_at": "..."
    },
    {
      "role": "assistant",
      "content": "...",
      "created_at": "..."
    }
  ],
  "error_message": null
}
```

### Delete conversation

```text
DELETE /api/conversations/{session_id}
Authorization: Bearer <registered access token>
```

Response：

```json
{
  "success": true,
  "session_id": "android-session-uuid",
  "deleted": true,
  "error_message": null
}
```

## Token Recovery 原則

所有需要 auth 的 chat / voice / conversation API：

- guest token 過期：Android 重新呼叫 `/api/auth/guest`，更新 memory-only guest token，retry 原 request 一次。
- registered access token 過期：Android 呼叫 `/api/auth/refresh`，保存新的 access token，retry 原 request 一次。
- refresh token invalid / expired：Android 清除 registered token / refresh token / registered session，建立 guest token，顯示「登入已過期，已切換為訪客模式」。
- retry 最多一次，避免無限迴圈。

## 403 處理

Conversation API 若以 guest token 呼叫，server 可能回 403。Android 應視為需要登入，顯示 `登入後可以查看歷史對話` 或導向 Login。

## session_id 原則

Android 必須帶 `session_id` 呼叫 `/api/chat` 與 `/api/voice/round`。

- registered user：可使用持久 registered session_id。
- guest user：使用 memory-only session_id。
- conversation detail 按「確定使用這段對話」後：Home current `session_id` 切換為該 conversation 的 `session_id`。
- Home `+` 新對話：建立新的 registered session_id，下一次 chat / voice 建立新 conversation。

## Guest Voice Privacy

Guest 模式下以下資料只能暫時存在 memory 或 cache：錄音檔、回覆音訊檔、STT transcript、AI reply、voice round 狀態。不可寫入 SharedPreferences / DataStore / Room / SQLite / 自訂持久檔案。voice round 完成 / 失敗 / 取消後，應盡量刪除暫存錄音與回覆音訊檔。
