package agh.mobile.blurfacesmcc.ui.securitysettings.confidentialData

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun ConfidentialDataScreen(
    viewModel: ConfidentialDataViewModel = hiltViewModel()
) {
    val confidentialData by viewModel.confidentialData.collectAsState()
    val context = LocalContext.current

    var result by remember {
        mutableStateOf<Uri?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        it?.let { viewModel.saveUri(it) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    launcher.launch(
                        PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    ) { contentPadding ->
        confidentialData?.let {
            it.map {
                context.contentResolver.takePersistableUriPermission(
                    Uri.parse(it.uri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val model = ImageRequest.Builder(context)
                    .data(Uri.parse(it.uri))
                    .build()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        modifier = Modifier
                            .width(200.dp)
                            .height(200.dp), model = model, contentDescription = ""
                    )
                    Text(text = it.filename)
                }
            }
        }
    }

}