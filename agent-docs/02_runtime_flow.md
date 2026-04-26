# 02 執行流程

## App 啟動

1. App 進入 Home。
2. 若存在 registered token，沿用 registered flow。
3. 若沒有 registered token，呼叫 `POST /api/auth/guest` 建立 memory-only guest token。
4. Home 預設語音模式；文字模式為輔助。

## Text Chat Flow

1. 使用者切到文字模式並送出文字。
2. App 取得目前身份對應 token 與 `session_id`。
3. 呼叫 `POST /api/chat`。
4. 送出期間：角色 `Thinking`，busy lock 啟動。
5. 成功後：顯示 `ai_reply`，短暫 `Speaking`，再回 Idle。
6. 失敗時：顯示繁中錯誤，依錯誤類型切表情。

## Voice Round Flow

1. 使用者按住主麥克風開始錄音，角色 `Listening`。
2. 放開後停止錄音，WAV 檔存於 `cacheDir`。
3. 呼叫 `POST /api/voice/round`，multipart 欄位包含 `session_id` 與 `audio_file`。
4. 等待 / 上傳 / 下載期間角色 `Thinking`。
5. 成功後顯示 `ai_reply`，下載 `output_audio_url`，播放本地暫存音訊，角色 `Speaking`。
6. 播放完成後回 Idle。
7. STT 空結果時，保留錯誤文案 `STT 辨識結果為空，請確認錄音內容後重試`，角色切 `Surprised` 後自動回復。
8. 錄音、上傳、下載、播放完成或失敗後清理暫存檔。

## Token Recovery Flow

### Guest token recovery

1. Chat / Voice / Conversation request 遇 401。
2. 若目前身份是 guest，呼叫 `/api/auth/guest` 建立新的 memory-only guest token。
3. 保留 guest session_id。
4. retry 原 request 一次。
5. retry 後仍失敗則不再重試。

### Registered token recovery

1. Chat / Voice / Conversation request 遇 401。
2. 若目前身份是 registered，用 refresh token 呼叫 `/api/auth/refresh`。
3. refresh 成功：保存新 access token / expires_at，retry 原 request 一次。
4. refresh 失敗：清除 registered token / refresh token / registered session_id，建立 guest token，UI `isLoggedIn=false`，quick menu 切回登入，角色切 `Helpless`。

## Conversation Flow

### Chat list

1. Home 右上 quick menu 進入 Chat list。
2. guest：不載入 registered history，導向 Login 或顯示登入提示。
3. registered：呼叫 `GET /api/conversations`。
4. 顯示 loading / empty / error / content。
5. 每筆 item 顯示 title、created_at / updated_at（Asia/Taipei，`yyyy-MM-dd HH:mm:ss`）。
6. 刪除：呼叫 `DELETE /api/conversations/{session_id}`，成功後更新 list。

### Conversation detail

1. 點 Chat list item。
2. 呼叫 `GET /api/conversations/{session_id}`。
3. 顯示只讀 messages，不提供輸入框或麥克風。
4. 底部按「確定使用這段對話」。
5. App 保存該 registered `session_id`，回 Home。
6. Home 後續 `/api/chat` 與 `/api/voice/round` 使用選定 `session_id`。

### New conversation

1. registered Home 底部左側顯示 `+`。
2. 按 `+` 建立新的 registered session_id。
3. 不清 token、不登出、不刪歷史。
4. 下一次文字或語音送出後，server 建立新的 conversation。
5. guest 隱藏 `+`。

## Busy / Loading 鎖定

- Home busy 時不可切換模式、不可重複錄音、不可送文字、不可按 `+`、不可開 quick menu、不可離開 Home。
- Conversation list loading 時避免重複 request。
- Delete 進行中不可重複刪同一筆。
- Detail loading 時避免重複進入同一筆造成狀態錯亂。

## Logcat 原則

不可印 access token、refresh token、guest token、完整 STT 文字、完整歷史訊息。可印 error type、HTTP status、identity type、retry 是否發生、session 短碼。
