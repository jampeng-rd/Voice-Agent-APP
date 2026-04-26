package com.jam.voiceagent.data.model

data class ConversationListItemDto(
    val session_id: String?,
    val title: String?,
    val created_at: String?,
    val updated_at: String?
)

data class ConversationListResponse(
    val success: Boolean,
    val conversations: List<ConversationListItemDto>?,
    val error_message: String?
)

data class ConversationMessageDto(
    val role: String?,
    val content: String?,
    val created_at: String?
)

data class ConversationDetailResponse(
    val success: Boolean,
    val session_id: String?,
    val title: String?,
    val created_at: String?,
    val updated_at: String?,
    val messages: List<ConversationMessageDto>?,
    val error_message: String?
)

data class ConversationDeleteResponse(
    val success: Boolean,
    val session_id: String?,
    val deleted: Boolean?,
    val error_message: String?
)

data class ConversationSummary(
    val sessionId: String,
    val title: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class ConversationMessage(
    val role: String,
    val content: String,
    val createdAt: String?
)

data class ConversationDetail(
    val sessionId: String,
    val title: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val messages: List<ConversationMessage>
)
