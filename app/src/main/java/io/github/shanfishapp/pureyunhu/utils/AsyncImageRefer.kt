package io.github.shanfishapp.pureyunhu.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun AsyncImageRefer(
    imageUrl: String,
    contentDescription: String,
    modifier : Modifier = Modifier
) {
    val request = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .addHeader("Referer", "https://www.yhchat.com")
        .build()

    AsyncImage(
        model=request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}