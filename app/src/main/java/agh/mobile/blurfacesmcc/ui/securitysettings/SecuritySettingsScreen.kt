package agh.mobile.blurfacesmcc.ui.securitysettings

import agh.mobile.blurfacesmcc.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SecuritySettingsScreen(
    viewModel: SecuritySettingsViewModel = hiltViewModel()
) {
    var sliderSetting = viewModel.sliderValue.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.25f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                onClick = {}
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary),
                    text = stringResource(id = R.string.confidential_data)
                )
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = {}
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondary),
                        text = stringResource(id = R.string.security_level)
                    )
                    Icon(
                        modifier = Modifier.padding(start = 10.dp),
                        imageVector = Icons.Outlined.Info,
                        contentDescription = ""
                    )
                }

            }

            sliderSetting.value?.let { value ->
                Slider(
                    steps = 10,
                    value = value,
                    onValueChange = viewModel::updateSliderValue
                )
            } ?: CircularProgressIndicator()

        }
    }
}