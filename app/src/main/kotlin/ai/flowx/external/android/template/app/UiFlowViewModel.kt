package ai.flowx.external.android.template.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UiFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun updateUiFlowData(workspaceId: String?, projectId: String?, uiFlowName: String?) {
        _uiState.update { currentState -> currentState.copy(workspaceId = workspaceId, projectId = projectId, uiFlowName = uiFlowName) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UiFlowViewModel::class.java)) {
                return UiFlowViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    data class UiState(
        val workspaceId: String? = null,
        val projectId: String? = null,
        val uiFlowName: String? = null,
    )
}