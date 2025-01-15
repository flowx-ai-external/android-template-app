package ai.flowx.external.android.template.app

import ai.flowx.android.sdk.FlowxSdkApi
import ai.flowx.external.android.template.app.network.Network
import ai.flowx.external.android.template.app.storage.SharedPrefsStorage
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storage by lazy { SharedPrefsStorage(context = application.applicationContext) }

    init {
        storage.getString(SharedPrefsStorage.AUTH_ACCESS_TOKEN_PREF)?.let { accessToken ->
            // Set the access token provider which is used to authenticate inside the SDK.
            // This should be called whenever the access token refresh logic imposes it.
            FlowxSdkApi.getInstance().setAccessTokenProvider(accessTokenProvider = { accessToken })
        }
    }

    private val _uiState = MutableStateFlow(
        UiState(
            loggedIn = !storage.getString(SharedPrefsStorage.AUTH_ACCESS_TOKEN_PREF).isNullOrBlank()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _startProcess = Channel<Triple<String, String, String>?>()
    val startProcess = _startProcess.receiveAsFlow()

    private val _continueProcess = Channel<Pair<String, String>?>()
    val continueProcess = _continueProcess.receiveAsFlow()

    fun login(email: String, password: String) {
        _uiState.update { currentState -> currentState.copy(loading = true) }
        viewModelScope.launch {
            val r = runCatching { Network.authService.login(username = email, password = password) }
            when {
                r.isSuccess -> {
                    r.getOrNull()?.let {
                        // save access data to persistence
                        storage.putString(SharedPrefsStorage.AUTH_ACCESS_TOKEN_PREF, it.accessToken).also { _ ->
                            // Set the access token provider which is used to authenticate inside the SDK.
                            // This should be called whenever the access token refresh logic imposes it.
                            FlowxSdkApi.getInstance().setAccessTokenProvider(accessTokenProvider = { it.accessToken })
                        }
                        storage.putString(SharedPrefsStorage.AUTH_REFRESH_TOKEN_PREF, it.refreshToken)
                    }
                }
                r.isFailure -> Log.e("Network exception:", r.exceptionOrNull().toString())
            }

            _error.update { _ -> r.exceptionOrNull()?.toString() ?: "" }
            _uiState.update { currentState -> currentState.copy(loading = false, loggedIn = r.isSuccess) }
        }
    }

    fun logout() {
        _uiState.update { currentState -> currentState.copy(loading = true) }
        viewModelScope.launch {
            CoroutineScope(currentCoroutineContext() + SupervisorJob()).launch {
                runCatching {
                    storage.getString(SharedPrefsStorage.AUTH_REFRESH_TOKEN_PREF)
                        .takeUnless { it.isNullOrBlank() }
                        ?.let { Network.authService.logout(refreshToken = it) }
                }.exceptionOrNull()?.let {
                    Log.e("Network", it.toString())
                }
            }
        }
        storage.clear() // cleanup access data from persistence, no matter what
        _uiState.update { currentState -> currentState.copy(loading = false, loggedIn = false) }
    }

    fun clearError() { _error.update { _ -> "" } }

    fun startProcess(projectId: String = START_PROCESS_PROJECT_ID, processName: String = START_PROCESS_NAME) {
        storage.getString(SharedPrefsStorage.AUTH_ACCESS_TOKEN_PREF)
            .takeUnless { it.isNullOrBlank() }
            ?.let { accessToken -> _startProcess.trySend(Triple(projectId, processName, accessToken)) }
    }

    fun continueProcess(processUuid: String = CONTINUE_PROCESS_UUID) {
        storage.getString(SharedPrefsStorage.AUTH_ACCESS_TOKEN_PREF)
            .takeUnless { it.isNullOrBlank() }
            ?.let { accessToken -> _continueProcess.trySend(processUuid to accessToken) }
    }

    fun showLoader(show: Boolean) {
        _uiState.update { currentState -> currentState.copy(loading = show) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // TODO SETUP: configure your maven repository here by setting the appropriate values
    companion object {
        const val START_PROCESS_PROJECT_ID = "your_project_id"
        const val START_PROCESS_NAME = "your_process_name"
        const val CONTINUE_PROCESS_UUID = "your_process_uuid"
    }
}

data class UiState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
)