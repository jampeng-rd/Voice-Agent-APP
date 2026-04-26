# 06 設計系統 — Companion Voice

## 設計定位

以語音互動為核心的 Android 助手 UI。風格是 Soft Minimalism、Organic Modernism、溫暖、安靜、有陪伴感、極簡、低認知負擔。Home 不像 Web 聊天室，也不以聊天泡泡為主。

## 色彩

```text
background: #F4FBF5
surface: #F4FBF5
surfaceContainerLow: #EFF5EF
surfaceContainer: #E9F0E9
surfaceContainerHigh: #E3EAE4
surfaceVariant: #DDE4DE
primary: #3A6758
primaryContainer: #A7D7C5
primaryFixed: #BCEDDA
secondaryContainer: #DADED9
onSurface: #161D1A
onSurfaceVariant: #404945
outline: #717975
outlineVariant: #C0C8C3
error: #BA1A1A
```

角色主色使用 `#A7D7C5`，主要按鈕使用 `#A7D7C5` 或 `#3A6758`，文字與 icon 使用深綠灰，不使用純黑。

## 角色狀態

| 狀態 | 文案 | 視覺方向 |
|---|---|---|
| 待機 | 我可以幫你什麼嗎？ | 平靜 |
| 聆聽中 | 聆聽中… | glow / pulse |
| 思考中 | 思考中… | 思考感 |
| 說話中 | 說話中… | waveform |
| 開心 | 好開心呀！ | 彎眼、大笑 |
| 不開心 | 有點難過… | 下垂 |
| 困惑 | 我有點困惑… | 不對稱 |
| 驚訝 | 哇！是這樣嗎？ | 眼睛放大 |
| 無奈 | 唉…這真是個難題呢 | 半閉眼 |

## API 串接 UI 原則

- loading 不應讓角色主畫面大幅跳動。
- 文字送出：Thinking。
- 錄音中：Listening。
- 收到回覆 / 播放 TTS：Speaking。
- 錯誤：簡潔繁中提示，不用大型錯誤頁。
- refresh 失敗切 guest：Helpless / 無奈。
- STT 空結果：Surprised / 驚訝。

## Home 底部控制

底部控制區保持三欄：

```text
左：+（registered only，新對話）
中：主按鈕（語音模式為麥克風；文字模式為送出）
右：模式切換（文字 / 語音）
```

規則：

- `+` 只在 registered 顯示，guest 隱藏。
- `+` 與右側模式切換按鈕尺寸一致。
- `+` 與右側模式切換按鈕底部要對齊中央主按鈕底部。
- Home busy 時三個控制都應依情境禁用，尤其不可開始新對話或重複錄音。

## Voice UI 原則

- 語音模式下，主麥克風是主要互動。
- 按住錄音、放開送出。
- 上傳 / 下載 / 等待期間狀態文字 `思考中…`。
- 播放後端 TTS 時狀態文字 `說話中…`，顯示 Speaking waveform。
- 若只有文字沒有可播放音訊，保留文字回覆並顯示低干擾錯誤提示。
- Voice busy 期間禁用模式切換、文字送出、主麥克風重複觸發、右上快捷選單、`+`。

## Voice 錯誤文案

- 音訊下載失敗：`語音回覆下載失敗，請稍後再試。`
- 音訊已過期：`語音回覆已過期，請再試一次。`
- 播放失敗：`播放回覆時發生問題，請稍後再試。`
- 無可播放音訊：`目前只有文字回覆，沒有可播放的語音。`
- STT 空結果：`STT 辨識結果為空，請確認錄音內容後重試。`

## Conversation UI 原則

Conversation list / detail 是輔助功能，不應取代 Home 角色語音互動。

- Chat list 使用簡潔卡片列表，不做大型 Web 聊天室。
- Conversation detail 是只讀查看頁，不放輸入框、不放麥克風。
- Detail 底部主要按鈕：`確定使用這段對話`。
- 按下後回 Home，在 Home 繼續語音或文字互動。
- Empty state：`目前還沒有歷史對話`。
- Guest 提示：`登入後可以查看歷史對話`。
- Delete 失敗：`刪除失敗，請稍後再試。`
- List / detail loading 保持低干擾。

## Debug UI

表情切換 chips 和 sensor debug 是開發用途。後續 release 前應可關閉或移除。
