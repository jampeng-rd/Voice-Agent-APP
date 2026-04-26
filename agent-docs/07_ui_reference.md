# 07 UI 設計稿參考

參考圖片位置：

```text
agent-docs/design-assets/design-system-reference.png
```

layout 與結構優先以 `AGENTS.md`、phase 需求與 `06_design_system.md` 為準。

## 目前主畫面結構

```text
AssistantHomeScreen
├── TopRightQuickMenu（Settings -> Home / Chat / User）
├── CenterStage
│   ├── StatusMessage / AI reply text
│   ├── AvatarFace
│   └── SpeakingWaveform（僅說話中）
├── EmotionButtons（debug only）
├── ChatInputBar（僅文字模式）
└── BottomInputControls
    ├── 左：New conversation +（registered only）
    ├── 中：主操作（Mic 或 Send）
    └── 右：模式切換
```

## Home / Avatar 規則

- 角色是主視覺。
- 不要讓文字輸入框遮住 Avatar。
- 聆聽中顯示多層 glow。
- 說話中 waveform 位於角色下方，不推動角色臉。
- 觸摸、傾斜、強搖、dizzy、睡著互動不得被 API 串接破壞。
- refresh 失敗切 guest 時可短暫顯示 Helpless。
- STT 空結果時可短暫顯示 Surprised。

## BottomInputControls 規則

```text
左欄：+ 新對話
中欄：中央主按鈕
右欄：模式切換
```

要求：

- 三欄左 / 中 / 右清楚排列。
- `+` 在最左側，不使用相對中央的 offset 定位。
- `+` 與右側模式切換尺寸一致。
- `+` 與模式切換按鈕底部對齊中央主按鈕底部。
- guest 隱藏 `+`。
- Home busy 時 `+` 不可點。

## Voice UI 規則

- 語音模式下，主麥克風仍是主要互動。
- 按住錄音、放開送出流程不重做。
- 上傳 / 下載 / 等待期間狀態文字 `思考中…`。
- 播放後端 TTS 時狀態文字 `說話中…`，角色 Speaking，SpeakingWaveform 顯示。
- 播放完成：回 Idle，或保留簡短 AI reply。
- 若 server 回傳 `ai_reply` 但沒有可播放音訊，保留文字回覆，不閃退。
- Voice busy 期間不可切換語音 / 文字模式、不可送文字、不可重複錄音、不可開啟右上快捷選單、不可按 `+`、不可導航離開 Home。

不要新增大型錄音頁面。錄音 / 上傳 / 下載 / 播放都在 Home 主畫面中完成。

## Chat list

```text
ChatListScreen
├── Top / title：歷史對話
├── State：loading / empty / error / content
└── ConversationItem list
    ├── title / fallback title
    ├── updated time / created time
    └── delete action（沿用刪除 UI）
```

規則：

- registered 才能查看。
- guest 導向 Login 或顯示登入提示。
- 時間使用 Asia/Taipei，格式 `yyyy-MM-dd HH:mm:ss`。
- delete 進行中不可重複刪除同一筆。

## Conversation detail

```text
ConversationDetailScreen
├── Top / title
├── Message history readonly list
└── Bottom primary button：確定使用這段對話
```

規則：

- Detail 頁不新增輸入框。
- Detail 頁不新增麥克風。
- Detail 頁不直接送 chat / voice。
- 按下確認後回 Home，Home 使用該 `session_id` 繼續對話。

## Navigation

- Home -> Chat list：從 quick menu 進入。
- Chat list -> Detail：點 conversation item。
- Detail -> Home：點「確定使用這段對話」。
- Home -> Login/Register：從 User 入口或 guest history 提示進入。
- Home busy 時不可導航離開 Home。

## 文案建議

- 待機：`我可以幫你什麼嗎？`
- 聆聽中：`聆聽中…`
- 思考中：`思考中…`
- 說話中：`說話中…`
- 開始新對話：`已開始新的對話`
- 登入過期：`登入已過期，已切換為訪客模式。`
- API 錯誤：`目前連線有點問題，請稍後再試。`
- 音訊下載失敗：`語音回覆下載失敗，請稍後再試。`
- 音訊已過期：`語音回覆已過期，請再試一次。`
- 播放失敗：`播放回覆時發生問題，請稍後再試。`
- STT 空結果：`STT 辨識結果為空，請確認錄音內容後重試。`
