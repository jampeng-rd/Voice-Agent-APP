# 01 範圍與目標

## 專案目標

`voice-agent-android` 是 `voice-agent-server` 的 Android client。短期目標是完成 Android 端以語音為核心的 client 基線：角色互動 Home、真實 auth、文字聊天、語音聊天、token recovery、registered conversation history。

## 目前完成

- UI / 角色互動：Avatar、九種狀態表情、聆聽 glow、說話 waveform、摸臉好感、傾斜、強搖、dizzy、睡著。
- Auth / Nav：Login / Register 真實 API；右上 quick menu；登入成功回 Home；refresh 失敗切 guest 後 UI 同步。
- Guest flow：App 無 registered token 時自動建立 memory-only guest token；guest session_id 不落地。
- Text Chat：`POST /api/chat`，帶 token 與 `session_id`。
- Voice Round：按住錄音、放開送出 WAV 到 `/api/voice/round`，顯示 `ai_reply`，下載並播放 `output_audio_url`。
- Token Expiry Recovery：Chat / Voice / Conversation API 遇 401 時 refresh or recreate guest token，retry once。
- Conversation History：registered conversation list/detail/delete；detail 確認後回 Home 使用選定 `session_id` 繼續文字/語音。
- New Conversation：registered Home 底部左側 `+` 可建立新的 registered session_id。
- UI feedback：refresh 失敗切 guest 表情切「無奈」；STT 空結果表情切「驚訝」。

## 重要隱私要求

Guest token、guest_id、guest expires_at、guest session_id 只能存在 memory，不可寫入 SharedPreferences / DataStore / Room / SQLite / cache file。Guest 語音資料只可暫存在 memory 或 `cacheDir`，流程結束應清理。

Registered access token、refresh token、registered session_id 可以保存到 SharedPreferences。

## 目前不做

- 不改 server API contract。
- 不做 guest 長期歷史。
- 不把 guest token / guest session_id 落地。
- 不做 Room / SQLite 本地歷史資料庫。
- 不做離線快取。
- 不做聊天泡泡大型重設計。
- 不做 conversation rename / search / share / export。
- 不改 token refresh / retry once 主流程。
- 不重寫 UI-02 角色互動核心。

## 成功標準

- guest / registered 文字聊天可用。
- guest / registered 語音聊天可錄音、送出、播放 TTS 回覆。
- token 過期可 recovery + retry once；refresh 失敗切 guest 並同步 UI。
- registered 能查看、選擇、刪除歷史 conversation。
- detail 選定 conversation 後，Home 後續文字與語音使用該 `session_id`。
- registered 可按 Home `+` 開始新 conversation，不需登出。
- guest token / guest session_id 不落地。
- Logcat 不印 token / 完整 STT / 完整歷史內容。
- `./gradlew :app:assembleDebug` 成功。
