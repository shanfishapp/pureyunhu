package io.github.shanfishapp.pureyunhu.models

import com.google.gson.annotations.SerializedName

data class ConversationListResponse(
    @SerializedName("status") val status: Status,
    @SerializedName("data") val data: List<ConversationData>,
    @SerializedName("total") val total: Long,
    @SerializedName("request_id") val requestId: String
)




data class ConversationData(
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("chat_type") val chatType: Long,
    @SerializedName("name") val name: String,
    @SerializedName("chat_content") val chatContent: String,
    @SerializedName("timestamp_ms") val timestampMs: Long,
    @SerializedName("unread_message") val unreadMessage: Long,
    @SerializedName("at") val at: Long,
    @SerializedName("avatar_id") val avatarId: Long,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("do_not_disturb") val doNotDisturb: Long,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("at_data") val atData: AtData?,
    @SerializedName("certification_level") val certificationLevel: Long
)

data class AtData(
    @SerializedName("unknown") val unknown: Long,
    @SerializedName("mentioned_id") val mentionedId: String,
    @SerializedName("mentioned_name") val mentionedName: String,
    @SerializedName("mentioned_in") val mentionedIn: String,
    @SerializedName("mentioner_id") val mentionerId: String,
    @SerializedName("mentioner_name") val mentionerName: String,
    @SerializedName("msg_seq") val msgSeq: Long
)