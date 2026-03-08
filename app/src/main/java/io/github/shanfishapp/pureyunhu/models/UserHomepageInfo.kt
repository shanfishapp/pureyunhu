package io.github.shanfishapp.pureyunhu.models

data class UserHomepageInfo(
    val code: Int,
    val msg: String,
    val data: UserHomepageData
) {
    data class UserHomepageData(
        val user: UserHomepageUser
    )

    data class UserHomepageUser(
        val userId: String,
        val nickname: String,
        val avatarUrl: String,
        val registerTime: Long,
        val registerTimeText: String,
        val onLineDay: Int,
        val continuousOnLineDay: Int,
        val medals: List<Medal>,
        val isVip: Int
    )

    data class Medal(
        val id: Int,
        val name: String,
        val desc: String,
        val imageUrl: String,
        val sort: Int
    )
}