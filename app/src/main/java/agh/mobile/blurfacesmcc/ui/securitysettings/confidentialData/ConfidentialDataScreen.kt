package agh.mobile.blurfacesmcc.ui.securitysettings.confidentialData

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun ConfidentialDataScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = { /*TODO*/ },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    ) { contentPadding ->
        // TODO
    }

}