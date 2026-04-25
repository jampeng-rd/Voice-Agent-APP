# Phase Android-UI-02.1 說明

## 1. 本階段目標（UI 原型 / 不串 API）

本階段目標是維持既有 UI-01 主畫面結構，聚焦修正 UI-02 互動品質（觸摸好感 / 傾斜 / 強搖 / Idle 行為）。

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
- 待機（Idle）加入低頻率微表情：平嘴與極輕微微笑自然往返
- Idle 停留約 60 秒後會漸進進入睡著感（半閉眼/柔和變暗），互動後恢復
- 角色臉加入 UI-02 觸摸互動：摩擦可提升好感度 `affectionLevel`，停止後緩慢下降
- 好感度顏色修正為 `primaryContainer -> 柔和粉紅` 漸變（避免黃濁色），glow 同步轉為柔和粉紅光暈
- 依 `affectionLevel` 提供三階段開心：低（微笑）/ 中（開心笑）/ 高（彎眼+大笑嘴），停止觸摸後會隨好感下降漸回復
- 好感表情改為連續插值（眼睛/嘴巴/臉色/glow 同步漸變），修正中高段突變感
- 修正 affection 過渡疊影：同時間只呈現單一合理過渡結果，避免眼睛/嘴巴多套表情重疊
- affection 成長速度放慢，需持續摩擦才會進入高開心，停止後維持緩降
- UI-02 感測互動：加強傾斜偏移與旋轉可見度，並固定 portrait 避免傾斜測試時切成橫式
- 聆聽中（按住麥克風）新增多層 concentric glow / pulse，強化「正在聆聽」辨識度
- 說話中 waveform 再下移，並提升細窄圓角 bar 的起伏動態感（不推動角色臉）
- tilt 偏移與旋轉幅度再提高，保留 smoothing 且不讓角色完全跑出畫面
- 強搖判定改為高門檻 + 連續命中才觸發（含加速度與 jerk），降低一般拿起手機誤判
- 強搖流程修正為：大力搖晃中持續彈跳 -> 停止後進入 dizzy 約 3 秒 -> 明確回到 Idle
- dizzy 時長調整為約 5 秒，結束後明確恢復 Idle 並清空 bounce offset
- 強搖彈跳改為更大範圍（全畫面可視區內），並修正「未彈跳就 dizzy」與「dizzy 未回復」問題
- Idle 微表情調整為約每 3 秒一輪，平靜/微笑/小驚訝/輕微無奈以漸變切換
- Idle 超過 1 分鐘進入漸進深色睡眠狀態（臉部轉深綠灰 `#161D1A` 系），眼睛與嘴巴維持可見對比
- 進入睡著狀態時，角色上方文字顯示 `z.. Z.. z...`，互動後恢復原本 Idle 文案

### 語音主畫面

- 移除 TopBar 與底部三欄主選單，改為中央角色舞台 + 底部輸入控制區
- 預設為語音模式：底部中央較大麥克風為主操作，右側為文字模式切換
- 文字模式開啟時：中央主按鈕改為文字主操作，右側改為麥克風切回語音模式
- 語音模式按住麥克風可觸發假錄音 pulse 與「聆聽中」狀態，放開回一般語音模式
- waveform 僅在「說話中」顯示，使用細窄圓角 bars、柔和振幅動畫
- waveform 使用固定顯示區，避免切換到「說話中」時把角色往上推
- 文字輸入框僅在文字模式顯示，位置固定於底部輸入控制區上方
- 文字輸入框送出 icon 與底部文字模式主按鈕送出 icon 採用一致斜向 icon

### 右上設定快捷選單

- 右上角固定設定齒輪 icon
- 點擊後向左展開單一 pill 容器快捷選單（Home / Chat / User 入口）
- Home / Chat / User icon 共用同一個 pill 背景，不使用各自獨立底色

### AUTH / NAV（Phase Android-AUTH-01 + NAV-01）

- 新增 in-memory 假登入 / 假註冊流程，不串接任何 API
- App 啟動預設進入 Home；右上 User 未登入時導向 Login
- Login / Register：輸入非空 email / password 即視為成功（僅本次 App session 有效）
- Login 成功後導向 Chat 假資料頁；Chat 提供回 Home 與登出
- 登出會清除本地記憶體登入狀態並回 Home

### AUTH / NAV（Phase Android-AUTH/NAV-01.1 收斂）

- 右上快捷選單 icon 間距與尺寸加大，降低誤按
- 快捷選單 Chat icon 改為更明確的對話 icon
- Home / Login / Register / Chat 全頁保留同一套右上快捷選單
- Login / Register 內容垂直置中並沿用 design system 背景色
- Login 移除「回到主畫面」按鈕；回 Home 改走右上 Home 快捷
- Register 主按鈕文字改為「註冊」，保留「回到登入」
- Chat 假資料頁改為歷史聊天列表樣式，移除底部回 Home/登出按鈕
- Chat 列表支援左滑露出「刪除」按鈕，點擊後從本地假列表移除

### 表情切換控制區（Debug）

- 保留開發用 chips 可切換九種狀態
- 改為 debug-only 可展開、緊湊單列捲動與更低高度，不再遮住角色主體

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
7. 切到「說話中」反覆切換，確認角色臉位置不跳動、waveform 固定在角色下方區域。
8. 於角色臉上摩擦 5~10 秒，確認臉色/光暈轉為柔和粉紅且表情依低中高三段更開心。
9. 停止摩擦後觀察約 10~30 秒，確認好感與表情漸進回落，不會瞬間跳回。
10. 輕微傾斜手機，確認角色頭在 portrait 畫面內可見偏移/旋轉，且 UI 不切橫式。
11. 按住麥克風進入聆聽中，確認角色外圍出現 2~3 層柔和呼吸光暈，明顯可辨識。
12. 大力連續搖晃手機，確認先在畫面可視範圍內明顯彈跳；停止後才進入 dizzy。
13. 大力搖晃後確認 dizzy 持續約 5 秒，結束後回 Idle 且彈跳位移歸零。
14. 靜置約 1 分鐘，確認角色臉逐漸變暗接近睡著，文字改為 `z.. Z.. z...`；任意互動後恢復正常。
15. 切到說話中，確認 waveform 位置比先前更下方、振幅更明顯，但不會把角色臉推動。
16. 點右上齒輪展開快捷選單，確認有 Home / Chat / User 三入口。
17. 未登入點 User，確認進入 Login；點前往註冊可到 Register。
18. Login/ Register 輸入非空欄位可成功；Login 成功後進入 Chat 假資料頁。
19. Chat 點登出後回 Home，重新進入 App（重開）不保留登入狀態。
20. Login / Register 內容應垂直置中，且頁面右上仍保留快捷選單。
21. Chat 頁顯示多筆單行歷史聊天紀錄，長文字會截斷顯示 `...`。
22. 對任一聊天紀錄向左滑，確認右側露出刪除按鈕；點刪除後該列移除。

## 5. 專案結構說明

主要 UI 原型集中在以下區域：

- `app/src/main/java/com/jam/voiceagent/ui/screens/AssistantHomeScreen.kt`
- `app/src/main/java/com/jam/voiceagent/ui/navigation/AppRoot.kt`
- `app/src/main/java/com/jam/voiceagent/ui/navigation/AppRoute.kt`
- `app/src/main/java/com/jam/voiceagent/ui/screens/auth/`
- `app/src/main/java/com/jam/voiceagent/ui/screens/chat/`
- `app/src/main/java/com/jam/voiceagent/ui/avatar/`
- `app/src/main/java/com/jam/voiceagent/ui/components/`
- `app/src/main/java/com/jam/voiceagent/ui/voice/`
- `app/src/main/java/com/jam/voiceagent/ui/theme/`

## 6. 已知限制

- 尚未串接 `voice-agent-server` API（含 guest/chat/voice round）
- 尚未實作真正語音錄音與播放
- AUTH/NAV 目前為 in-memory 假流程，未做 token、SharedPreferences、資料庫或正式驗證
- Chat 頁目前為假資料頁，未串接 conversations API
- Chat 左滑刪除目前僅本地 UI 原型，未串接刪除 API
- 表情切換目前由開發用 chips 手動控制，未與語音流程綁定
- 強搖門檻目前以真機體感做保守值，仍需依不同裝置型號微調
- portrait 為現階段測試穩定性設定，後續若支援橫式需重新設計感測與版面策略
- 強搖彈跳與觸發參數仍需依不同更新率感測器（60/120Hz）校正

## 7. 下一階段預計（Phase Android-UI-02.2 / API 前）

- 進一步校正不同手機的 strong shake threshold 與 cooldown 參數
- 評估加入可關閉的 sensor toggle（debug 或設定）
- 規劃 UI 狀態與後端語音流程接軌前的狀態機收斂
- 準備後續 API 串接所需的最小錯誤回饋與 loading 互動

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

## 11. Phase Android-UI-01.8 細節修正摘要

- 說話中 waveform 改為固定保留區顯示，避免出現時推動角色位置
- debug 面板進一步降低高度與 padding，釋放角色區空間
- 底部輸入控制改為「中央主按鈕絕對置中 + 右側輔助按鈕靠邊」並做底部對齊
- 右側輔助按鈕略縮小，中央主按鈕大小維持
- 角色漂浮幅度小幅提升，維持柔和不跳動
- 角色上方文字字級略增並拉開與角色距離，保留作為未來 AI 回覆/狀態顯示區
- 右上快捷選單圖示整合在同一個 pill 容器內，向左展開，齒輪維持右側

## 12. Phase Android-UI-01.9 細節修正摘要

- 右上快捷選單向左展開時改為更明確的單一 pill 容器，Home 與帳號 icon 共用同一背景
- Idle 新增低頻率自然微表情（平嘴 → 輕微微笑 → 平嘴），且不影響其他狀態
- 文字輸入框與底部文字模式主按鈕的送出 icon 統一為較自然斜向風格

## 13. Phase Android-UI-01.10 polish 修正摘要

- Idle 微表情改為低頻隨機與漸變（平靜/微笑/小驚訝/輕微無奈），避免固定節奏切換
- Idle 停留約 60 秒後漸進進入睡著感（半閉眼與臉色微暗），任一互動會重置並恢復
- 右上快捷展開維持從齒輪左側展開，Home/帳號保持同一個 pill 容器且無獨立 icon 背景
- 說話中 waveform 下移並維持固定高度區，不推動角色位置
- Debug 面板略上移，降低與文字模式輸入框重疊機率

## 14. Phase Android-UI-02 角色觸摸與感測互動摘要

- 新增本地觸摸好感狀態 `affectionLevel`（0.0～1.0），摩擦角色臉時上升、停止後緩降
- 好感度驅動角色情緒：臉色與 glow 逐步變暖、嘴型更偏向開心
- 以 accelerometer 驅動角色互動：輕微傾斜柔和偏移/旋轉，大力搖晃觸發短暫彈跳
- 大力搖晃停止後進入 dizzy 表情約 3 秒，之後自動恢復
- 感測器採 lifecycle-safe 註冊/解除；無 accelerometer 時安全退化，不影響主畫面使用
- debug 區可顯示 affection / shake / dizzy 狀態，方便真機人工測試

## 15. Phase Android-UI-02.1 修正摘要

- 觸摸好感色彩改為柔和紅粉系，不再偏黃或混濁
- 新增三階段開心表情（微笑 -> 開心笑 -> 彎眼大笑）
- 提升傾斜視覺強度並固定 portrait，避免橫式重排
- 強搖改為連續高強度判定，避免拿起手機即觸發 dizzy
- 強搖流程改為「搖晃中彈跳、停止後 dizzy、3 秒回 Idle」
- Idle 微表情頻率提高至約 3 秒一輪，並維持柔和漸變
- Idle 1 分鐘後臉部漸暗接近睡眠，五官保持可視
- debug 文案新增 shake magnitude / jerk / count / cooldown，方便真機調參

## 16. Phase Android-UI-02.2 互動手感修正摘要

- 摸臉開心三階段改為連續參數插值，移除中高段硬切「跳臉」感
- 傾斜偏移與旋轉幅度再上調，保留平滑濾波與 portrait 固定
- 強搖流程修正為「先進入 strongShakeActive 彈跳 -> 停止後 dizzy -> 3 秒回 Idle」
- 彈跳改為較大可視範圍移動，並限制在螢幕內不完全消失
- 修正 dizzy 恢復流程，結束後強制回 Idle、清除彈跳 offset
- 睡著深色改用 design system 深綠灰（接近 `#161D1A`），不使用純黑
- debug 增加 threshold 參數顯示（magnitude / jerk / required hits）以利真機調參

## 17. Phase Android-UI-02.3 互動小修摘要

- 修正 affection 過渡時的眼睛/嘴巴疊影，避免同時繪製多套完整表情
- 放慢 affection 成長速度並拉長摩擦區間，避免快速直達最高開心
- 大力搖晃後 dizzy 時長改為約 5 秒，結束後強制回 Idle 並清空 dizzy/bounce 狀態
- 睡著狀態文字改為 `z.. Z.. z...`，互動後恢復原本 Idle 文字

## 18. Phase Android-UI-02.4 最後手感調校摘要

- 按住麥克風進入聆聽中時，角色外圍改為多層 concentric glow / pulse，聆聽辨識更明確
- 說話中 waveform 再下移且振幅動態更清楚，維持細窄圓角 bar，不推動角色臉位置
- tilt 偏移與旋轉幅度再提高，保留平滑過渡與 portrait 固定

## 19. Phase Android-AUTH-01 + NAV-01 摘要

- 新增最小路由容器（in-memory）：Home / Login / Register / Chat
- App 啟動預設進入 Home
- 右上快捷選單改為 Home / Chat / User 入口
- 未登入點 User 進 Login；已登入點 User 執行登出並回 Home
- Login / Register 採假流程：非空 email/password 視為成功，不串 API
- Login 成功後僅在當前 App session 設為 `isLoggedIn=true`，導到 Chat 假資料頁
- Chat 頁提供假資料列表、回 Home 與登出，登出會清除 session 內登入狀態

## 20. Phase Android-AUTH/NAV-01.1 UI 收斂摘要

- 右上快捷選單（Home / Chat / User）在所有頁面統一可用，icon 間距與尺寸加大
- Chat icon 改為更清楚的對話圖示，提升辨識度
- Login / Register 內容垂直置中並沿用主背景色
- Login 移除回主畫面按鈕；Register 主按鈕文案改為「註冊」
- Chat 頁改為歷史聊天列表樣式，移除底部回 Home/登出按鈕
- 每筆歷史聊天支援左滑露出刪除按鈕，刪除後直接從本地假列表移除

## 21. Phase Android-AUTH/NAV-01.2 Chat list 左滑刪除修正摘要

- Chat 列表左滑改為「達門檻即停留展開」，不再放手就自動彈回
- 右側刪除區改為固定寬度，只有展開中的單列顯示
- 同一時間最多僅一列展開；展開新列時，前一列自動收合
- 修正紅色背景外露問題：未展開時不顯示紅色區塊
- Chat 假資料增加到 20 筆以上，列表可穩定垂直滾動
- 右上快捷選單維持固定位置，不隨聊天列表滾動
