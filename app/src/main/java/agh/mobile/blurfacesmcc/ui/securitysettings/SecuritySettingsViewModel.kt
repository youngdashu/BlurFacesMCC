package agh.mobile.blurfacesmcc.ui.securitysettings

import agh.mobile.blurfacesmcc.ui.util.settingsDataStore
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    val sliderValue: MutableStateFlow<Float?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            application.settingsDataStore.data.onEach { settings ->
                sliderValue.update { settings.securityLevel }
            }.onEmpty {
                updateSliderValue(0.0f)
            }.collect()
        }

    }

    fun updateSliderValue(value: Float) {
        viewModelScope.launch {
            sliderValue
                .updateAndGet { value }
                .let {
                    getApplication<Application>().settingsDataStore.updateData { settings ->
                        settings
                            .toBuilder()
                            .setSecurityLevel(it!!)
                            .build()
                    }
                }

        }
    }
}