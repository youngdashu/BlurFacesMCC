package agh.mobile.blurfacesmcc.ui.homePage

import agh.mobile.blurfacesmcc.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun HomePage(
    navigateToUploadVideo: () -> Unit,
    navigateToVideos: () -> Unit,
    navigateToSecuritySettings: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = navigateToUploadVideo) {
                Text(text = stringResource(id = R.string.uploadVideo))
            }

            Button(onClick = navigateToUploadVideo) {
                Text(text = stringResource(id = R.string.myVideos))
            }

            Button(onClick = navigateToUploadVideo) {
                Text(text = stringResource(id = R.string.securitySettings))
            }
        }

    }
}