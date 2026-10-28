package com.bresler.parkingbro.ui.fullscreen

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bresler.parkingbro.R
import com.bresler.parkingbro.ui.theme.AppWhite
import com.bresler.parkingbro.ui.theme.DarkBackground
import com.bresler.parkingbro.ui.theme.Green

@Composable
fun FullScreenImageScreen(uri: Uri, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            FloatingActionButton(
                onClick = onBack,
                containerColor = Green,
                contentColor = AppWhite,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(20.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_arrow_back_24),
                    contentDescription = null,
                    tint = AppWhite,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
