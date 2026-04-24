# Phase Android-UI-01 說明

## 1. 本階段目標（UI 原型 / 不串 API）

本階段目標是建立 Android 語音助手主畫面的可執行 UI 原型，重點是角色互動與語音優先體驗。

- 使用 Kotlin + Jetpack Compose
- 不串接任何後端 API
- 不實作真正錄音與播放
- 先提供可人工測試的狀態切換與視覺回饋

## 2. 目前完成的功能

### 角色表情系統

- 建立 `AvatarState` 九種狀態：待機、聆聽中、思考中、說話中、開心、不開心、困惑、驚訝、無奈
- 角色臉部使用 Compose/Canvas 繪製，不使用設計稿圖片作為 runtime 素材
- 每個狀態皆有對應文案、眼睛樣式、嘴巴樣式
- 加入輕量動態：較慢眨眼、微浮動、聆聽/說話 glow、說話嘴型變化

### 語音主畫面

- 移除 TopBar 與底部三欄主選單，改為中央角色舞台 + 底部輸入控制區
- 預設為語音模式：底部中央較大麥克風為主操作，右側為文字模式切換
- 文字模式開啟時：中央主按鈕改為文字主操作，右側改為麥克風切回語音模式
- 語音模式按住麥克風可觸發假錄音 pulse 與「聆聽中」狀態，放開回一般語音模式
- waveform 僅在「說話中」顯示，使用細窄圓角 bars、柔和振幅動畫
- 文字輸入框僅在文字模式顯示，位置固定於底部輸入控制區上方

### 右上設定快捷選單

- 右上角固定設定齒輪 icon
- 點擊後向左展開小型快捷選單（Home / 帳號入口）
- 帳號入口目前為假狀態：未登入顯示使用者圖示，切換後可顯示登出圖示

### 表情切換控制區（Debug）

- 保留開發用 chips 可切換九種狀態
- 改為 debug-only 可展開、緊湊單列捲動，不再遮住角色主體

## 3. 專案如何執行（Android Studio / 真機）

1. 使用 Android Studio 開啟專案根目錄 `VoiceAgent`。
2. 等待 Gradle sync 完成。
3. 連接 Android 真機（建議）並啟用 USB 偵錯。
4. 選擇 `app` 組態後執行 Run。

## 4. 如何人工測試（如何切換表情、確認 UI 狀態）

1. 啟動 App，確認角色主體維持中央顯示，debug 區不遮住角色臉。
2. 按住底部中央麥克風，確認出現錄音 pulse 並切到「聆聽中」；放開後回一般語音模式。
3. 點右側文字切換 icon，確認進入文字模式並顯示底部文字輸入框。
4. 文字模式下確認中央按鈕為文字主操作，右側按鈕變為麥克風切回語音模式。
5. 點右上齒輪，確認向左展開 Home / 帳號快捷 icon；帳號 icon 可做假登入狀態切換。
6. 使用 debug chips 依序切換九種狀態，確認文案、眼睛、嘴巴變化與說話中 waveform 顯示。

## 5. 專案結構說明

主要 UI 原型集中在以下區域：

- `app/src/main/java/com/jam/voiceagent/ui/screens/AssistantHomeScreen.kt`
- `app/src/main/java/com/jam/voiceagent/ui/avatar/`
- `app/src/main/java/com/jam/voiceagent/ui/components/`
- `app/src/main/java/com/jam/voiceagent/ui/voice/`
- `app/src/main/java/com/jam/voiceagent/ui/theme/`

## 6. 已知限制

- 尚未串接 `voice-agent-server` API（含 guest/chat/voice round）
- 尚未實作真正語音錄音與播放
- 右上快捷選單目前為 UI 假互動，未做正式頁面切換
- 帳號入口目前為假登入狀態，未做真正登入流程
- 表情切換目前由開發用 chips 手動控制，未與語音流程綁定

## 7. 下一階段預計（Phase Android-API-01）

- 串接 `/api/auth/guest`
- 串接 `/api/chat`
- 串接 `/api/voice/round`
- 將 UI 狀態由 API/語音流程驅動，而非手動切換
- 規劃最小可用的錯誤提示與流程回饋

## 8. Phase Android-UI-01.5 收斂修正摘要

- 移除 TopBar（`Mochi AI` 與右上 icon）
- 移除角色下方獨立麥克風，只保留底部主導航中間麥克風
- waveform 改為僅在「說話中」顯示，並縮窄與降噪
- 眨眼動畫改為較慢節奏，避免快速閃爍
- 移除角色旁裝飾線條提示
- 輸入框 placeholder 由 `輸入文字（輔助）` 改為 `輸入文字`
- chips 改為 FlowRow 排版，降低擠壓變形
- 狀態文字弱化（字級與顏色權重下降）

## 9. Phase Android-UI-01.6 收斂修正摘要

- 主畫面改為中央角色區置中，底部導覽固定於底部；debug 區不再擠壓角色主區
- 語音/文字輸入改為模式切換：預設語音模式；文字框僅在文字模式顯示
- 文字輸入框位置調整為底部導航上方，不再貼在角色下方
- 底部導覽 icon 調整為 Home / Mic / Settings，並做 floating pill 間距優化
- 狀態文字保留但縮小權重，降低垂直佔位
- 說話中 waveform 改為細窄圓角 bar + 兩側小振幅，寬度約角色臉寬一半

## 10. Phase Android-UI-01.7 收斂修正摘要

- 移除底部 Home/Mic/Settings 導覽，改為底部輸入控制區（中央主操作 + 右側輔助切換）
- 新增右上角設定快捷選單，向左展開 Home / 帳號入口（假狀態）
- 修正 debug 表情區為可展開緊湊面板，避免遮住角色臉
- 狀態文字上移並增加與角色距離，避免角色浮動時貼頭
- 語音模式支援按住麥克風的假錄音 pulse 與聆聽狀態切換
- 文字模式下交換中央/右側按鈕語意，並將文字輸入框固定在底部控制區上方
