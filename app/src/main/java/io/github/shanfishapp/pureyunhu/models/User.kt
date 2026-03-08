package io.github.shanfishapp.pureyunhu.models

data class GetUserInfo(
    val code : Int,
    val msg: String,
    val data: GetUserInfoData,
) {
    data class GetUserInfoData(
        val user: GetUserInfoUser,
    )
    data class GetUserInfoUser(
        val userId: String,
        val nickname: String,
        val phone: String,
        val avatarId: String,
        val avatarUrl: String,
        val goldCoinAmount: Float
    )
}

data class LogOutRequest(
    val deviceId: String
)

data class GeneralResponse(
    val code: Int,
    val msg: String
)
