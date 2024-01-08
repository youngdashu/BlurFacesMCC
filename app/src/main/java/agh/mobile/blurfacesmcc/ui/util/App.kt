package agh.mobile.blurfacesmcc.ui.util

import agh.mobile.blurfacesmcc.ui.navigation.BlurFacesNavHost
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun App() {

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { data ->
                Snackbar(
                    snackbarData = data
                )
            }
        }
    ) {
        BlurFacesNavHost(
            modifier = Modifier.padding(it),
            showSnackbar = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = it,
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = true
                    )
                }
            }
        )
    }


}