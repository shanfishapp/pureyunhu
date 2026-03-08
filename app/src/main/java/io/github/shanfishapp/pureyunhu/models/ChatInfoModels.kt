package io.github.shanfishapp.pureyunhu.models

import com.google.gson.annotations.SerializedName

// 机器人信息响应
data class BotInfoResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: BotData,
    @SerializedName("msg") val msg: String
)

data class BotData(
    @SerializedName("bot") val bot: Bot
)

data class Bot(
    @SerializedName("id") val id: Long,
    @SerializedName("botId") val botId: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("nicknameId") val nicknameId: Long,
    @SerializedName("avatarId") val avatarId: Long,
    @SerializedName("avatarUrl") val avatarUrl: String,
    @SerializedName("token") val token: String,
    @SerializedName("link") val link: String,
    @SerializedName("introduction") val introduction: String,
    @SerializedName("createBy") val createBy: String,
    @SerializedName("createTime") val createTime: Long,
    @SerializedName("headcount") val headcount: Long,
    @SerializedName("private") val private: Long,
    @SerializedName("checkChatInfoRecord") val checkChatInfoRecord: CheckChatInfoRecord
)

// 群聊信息响应
data class GroupInfoResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: GroupData,
    @SerializedName("msg") val msg: String
)

data class GroupData(
    @SerializedName("group") val group: Group
)

data class Group(
    @SerializedName("id") val id: Long,
    @SerializedName("groupId") val groupId: String,
    @SerializedName("name") val name: String,
    @SerializedName("introduction") val introduction: String,
    @SerializedName("createBy") val createBy: String,
    @SerializedName("createTime") val createTime: Long,
    @SerializedName("avatarId") val avatarId: Long,
    @SerializedName("avatarUrl") val avatarUrl: String,
    @SerializedName("headcount") val headcount: Long,
    @SerializedName("readHistory") val readHistory: Long,
    @SerializedName("category") val category: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("checkChatInfoRecord") val checkChatInfoRecord: CheckChatInfoRecord
)

data class CheckChatInfoRecord(
    @SerializedName("id") val id: Long,
    @SerializedName("chatId") val chatId: String,
    @SerializedName("chatType") val chatType: Long,
    @SerializedName("checkWay") val checkWay: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: Long,
    @SerializedName("createTime") val createTime: Long,
    @SerializedName("updateTime") val updateTime: Long,
    @SerializedName("delFlag") val delFlag: Long
)