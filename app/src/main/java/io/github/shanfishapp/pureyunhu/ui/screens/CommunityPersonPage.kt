package io.github.shanfishapp.pureyunhu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CommunityPersonPage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        StatsOverviewCard()

        Spacer(modifier = Modifier.height(16.dp))

        FunctionMenuRow(
            firstIcon = Icons.Default.AccessTime,
            firstText = "我的文章",
            secondIcon = Icons.Default.AdminPanelSettings,
            secondText = "社区管理"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FunctionMenuRow(
            firstIcon = Icons.Default.Stars,
            firstText = "我的收藏",
            secondIcon = Icons.Default.MonetizationOn,
            secondText = "投币记录"
        )
    }
}

@Composable
private fun StatsOverviewCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(top = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatsItem(icon = Icons.Default.FilePresent, text = "已发表文章", count = "5")
            StatsItem(icon = Icons.Default.Star, text = "已收藏文章", count = "1.2w")
            StatsItem(icon = Icons.Default.AddComment, text = "已关注社区", count = "1")
        }
    }
}

@Composable
private fun StatsItem(
    icon: ImageVector,
    text: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(text = count)
        Text(text = text)
    }
}

@Composable
private fun FunctionMenuRow(
    firstIcon: ImageVector,
    firstText: String,
    secondIcon: ImageVector,
    secondText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        FunctionMenuItem(
            icon = firstIcon,
            text = firstText,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 10.dp)
        )

        FunctionMenuItem(
            icon = secondIcon,
            text = secondText,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 10.dp)
        )
    }
}

@Composable
private fun FunctionMenuItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text)
        }
    }
}