package agh.mobile.blurfacesmcc.ui.securitysettings.confidentialData

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var selectedData: Int? by remember {
        mutableStateOf(null)
    }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        it?.let { viewModel.saveUri(it) }
    }

    Scaffold(
        floatingActionButton = {
            Row {
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
                if (selectedData != null) {
                    FloatingActionButton(
                        modifier = Modifier.padding(start = 10.dp),
                        containerColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            viewModel.deleteElement(selectedData!!)
                            selectedData = null
                        },
                    ) {
                        Icon(Icons.Filled.Delete, "Floating action button.")
                    }
                }

            }

        }
    ) { contentPadding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)) {
            confidentialData?.let {
                it.mapIndexed { index, confidentialData ->
                    context.contentResolver.takePersistableUriPermission(
                        Uri.parse(confidentialData.uri),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    val model = ImageRequest.Builder(context)
                        .data(Uri.parse(confidentialData.uri))
                        .build()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp)
                            .background(if (selectedData == index) Color(0xff74b4f7) else Color.White)
                            .clickable {
                                selectedData = if (selectedData == index) {
                                    null
                                } else {
                                    index
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(5.dp)
                                .clip(RoundedCornerShape(15.dp, 15.dp, 15.dp, 15.dp))
                                .background(Color.LightGray),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                model = model,
                                contentDescription = ""
                            )
                            Text(text = confidentialData.filename)
                        }

                    }
                }
            }


        }

    }

}