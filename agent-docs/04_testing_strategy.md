# 04 測試策略

## Build

每個 phase 至少執行：

```bash
./gradlew :app:assembleDebug
```

預期：BUILD SUCCESSFUL。

## 1. App 啟動 / Guest 測試

1. 清除 App 資料。
2. 啟動 App。
3. 預期自動建立 guest token。
4. Home 可使用文字 / 語音。
5. 檢查 SharedPreferences 不應有 guest token / guest_id / guest session_id。

## 2. Registered Auth 測試

1. Register 新帳號。
2. Login。
3. Login 成功後回 Home。
4. quick menu 顯示登出狀態。
5. SharedPreferences 可有 registered access token / refresh token / registered session_id。

## 3. Text Chat 測試

1. guest 模式送文字，預期收到 `ai_reply`。
2. registered 模式送文字，預期收到 `ai_reply`。
3. 送出期間不能切模式、不能重複送出、不能開 quick menu。
4. 錯誤時顯示繁中錯誤且不閃退。

## 4. Voice Round 測試

1. guest 模式按住錄音 2–3 秒，放開送出。
2. 預期 STT 有文字、AI 有回覆、TTS 可播放。
3. registered 模式同樣測試。
4. 錄音、上傳、下載、播放期間 busy lock 正常。
5. 空錄音 / 太短錄音時顯示 `STT 辨識結果為空，請確認錄音內容後重試`，角色切驚訝後回復。
6. 檢查暫存音訊檔在流程完成 / 失敗後清理。

## 5. Token Recovery 測試

### Registered access token 過期

1. 登入 registered。
2. 將 `REGISTERED_TOKEN_TTL_SECONDS` 設短或等待 access token 過期。
3. 不重啟 App，送文字。
4. 預期自動呼叫 `/api/auth/refresh`，成功後 retry once。
5. 語音也需同樣可 recovery。

### Refresh token 失效

1. 讓 refresh token invalid / expired。
2. 送文字或語音。
3. 預期 App 不閃退。
4. 清 registered token/session，建立 guest token。
5. UI `isLoggedIn=false`，quick menu 顯示登入。
6. Home 角色切無奈。

### Guest token 過期

1. guest 模式讓 token 過期。
2. 送文字 / 語音。
3. 預期自動重新取得 guest token，保留 guest session_id，retry once。

## 6. Conversation List 測試

1. registered 登入。
2. 建立幾筆不同 session 的對話。
3. 從 Home quick menu 進入 Chat list。
4. 預期顯示 server conversations。
5. 時間顯示為 Asia/Taipei，格式 `yyyy-MM-dd HH:mm:ss`。
6. guest 進入 Chat list 應導向 Login 或顯示登入提示，不載入長期歷史。

## 7. Conversation Detail / Continue 測試

1. registered 進入 Chat list。
2. 點任一 conversation。
3. 預期進入只讀 detail。
4. detail 沒有輸入框、沒有麥克風。
5. 按「確定使用這段對話」。
6. 回 Home。
7. 送文字，確認新訊息接在同一 conversation。
8. 送語音，確認也接在同一 conversation。
9. 若 Android selected/request/response session 短碼一致，但 AI 不知道很早以前內容，應檢查 server context window，不在 Android 硬改。

## 8. New Conversation 測試

1. guest 模式：Home 不顯示 `+`。
2. registered 模式：Home 底部左側顯示 `+`。
3. `+` 與右側模式切換同尺寸，且底部對齊中央主按鈕底部。
4. Home busy / 錄音 / 播放中 `+` 不可點。
5. 按 `+` 後送文字，Chat list 出現新 conversation。
6. 按 `+` 後送語音，也應建立 / 使用新 conversation。
7. 原本歷史 conversation 不被刪除或覆蓋。

## 9. Delete Conversation 測試

1. registered 進入 Chat list。
2. 對某筆 conversation 按刪除。
3. 成功後該 item 從 list 消失或重新載入後不再出現。
4. 刪除失敗時 item 不消失，顯示錯誤。
5. 刪除中不可重複點擊同一筆。

## 10. UI 回歸測試

確認以下功能未破壞：

- Home 角色置中
- 摸臉 affection
- 傾斜
- 強搖 dizzy
- 睡著
- debug chips
- Listening glow
- Speaking waveform
- 文字模式切換
- 語音錄音按住/放開
- quick menu disabled by busy
- Login / Register
- Chat list / detail

## 11. Logcat 檢查

不可出現：

- access token
- refresh token
- guest token
- 完整 STT 文字
- 完整歷史訊息

可接受：

- HTTP status
- error type
- identity type
- retry 是否發生
- session 短碼
