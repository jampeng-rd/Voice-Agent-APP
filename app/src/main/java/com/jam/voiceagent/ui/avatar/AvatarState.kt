package com.jam.voiceagent.ui.avatar

enum class AvatarState(
    val displayName: String,
    val statusText: String
) {
    Idle(displayName = "待機", statusText = "我可以幫你什麼嗎？"),
    Listening(displayName = "聆聽中", statusText = "聆聽中…"),
    Thinking(displayName = "思考中", statusText = "思考中…"),
    Speaking(displayName = "說話中", statusText = "說話中…"),
    Happy(displayName = "開心", statusText = "好開心呀！"),
    Sad(displayName = "不開心", statusText = "有點難過…"),
    Confused(displayName = "困惑", statusText = "我有點困惑…"),
    Surprised(displayName = "驚訝", statusText = "哇！是這樣嗎？"),
    Helpless(displayName = "無奈", statusText = "唉…這真是個難題呢")
}
