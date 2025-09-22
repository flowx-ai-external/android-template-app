package ai.flowx.external.android.template.app

import ai.flowx.android.sdk.api.CloseModalProcessScope
import ai.flowx.android.sdk.main.Flowx
import ai.flowx.external.android.template.app.extensions.findActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class ProcessActivity : ComponentActivity() {

    private val vm: ProcessViewModel by viewModels(
        factoryProducer = { ProcessViewModel.Factory(this.application) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val workspaceId = intent.getSerializableExtra(INTENT_EXTRA_WORKSPACE_ID) as? String // used to start a new process
        val projectId = intent.getSerializableExtra(INTENT_EXTRA_PROJECT_ID) as? String // used to start a new process
        val processName = intent.getSerializableExtra(INTENT_EXTRA_PROCESS_NAME) as? String // used to start a new process
        val processUuid = intent.getSerializableExtra(INTENT_EXTRA_PROCESS_UUID) as? String // used to continue an existing process
        val accessToken = intent.getSerializableExtra(INTENT_EXTRA_ACCESS_TOKEN) as String

//        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//            Flowx.getInstance().checkRendererCompatibility {
//                when (it) {
//                    true -> Log.i(FlowxSdkApi::class.java.simpleName, "FlowX SDK Renderer is compatible with deployed platform.")
//                    false -> Log.e(FlowxSdkApi::class.java.simpleName, "FlowX SDK Renderer is NOT compatible with deployed platform.")
//                }
//            }
//        }

        // Set the access token provider which is used to authenticate inside the SDK.
        // This should be called whenever the access token refresh logic imposes it.
        Flowx.getInstance().setAccessToken(accessToken = accessToken)

        vm.updateProcessData(workspaceId, projectId, processName, processUuid)

        setContent {
            val updateProcessBroadcastReceiver = remember {
                UpdateProcessDataBroadcastReceiver(handler = { closeCurrentProcessAndContinueAnother(it) })
            }
            val context = LocalContext.current
            LifecycleStartEffect(true) {
                ContextCompat.registerReceiver(context.applicationContext, updateProcessBroadcastReceiver, IntentFilter("ai.flowx.android.demo.app.ProcessActivity.updateProcessData"), ContextCompat.RECEIVER_NOT_EXPORTED)
                onStopOrDispose {
                    runCatching {
                        context.applicationContext.unregisterReceiver(updateProcessBroadcastReceiver)
                    }
                }
            }

            var closeModalProcessScope by remember { mutableStateOf<CloseModalProcessScope?>(null) }
            val showCloseModalProcessAlert = remember { mutableStateOf(false) }
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            Box(modifier = Modifier.safeDrawingPadding()) {
                ProcessContent(
                    uiState = uiState,
                    onProcessEnded = {
                        Log.i(
                            ProcessActivity::javaClass.name,
                            "Process ${uiState.processName.takeUnless { it.isNullOrBlank() } ?: uiState.processUuid} has ended")
                    },
                    onCloseProcessModalFunc = { processName ->
                        // NOTE: possible handling could involve doing something differently based on the `processName` value
                        closeModalProcessScope = this
                        showCloseModalProcessAlert.value = true
                    },
                )
                closeModalProcessScope?.CloseModalProcessConfirmAlert(show = showCloseModalProcessAlert)
            }
        }
    }

    private fun closeCurrentProcessAndContinueAnother(processUuid: String) {
        vm.updateProcessData(null, null, null, processUuid)
    }

    @Composable
    private fun ProcessContent(
        uiState: ProcessViewModel.UiState,
        onProcessEnded: (() -> Unit)? = null,
        onCloseProcessModalFunc: (CloseModalProcessScope.(processName: String) -> Unit)? = null,
    ) {
        when {
            !uiState.workspaceId.isNullOrBlank() && !uiState.projectId.isNullOrBlank() && !uiState.processName.isNullOrBlank() -> {
                Flowx.getInstance().startProcess(
                    workspaceId = uiState.workspaceId,
                    projectId = uiState.projectId,
                    processName = uiState.processName,
                    isModal = true,
                    onProcessEnded = { onProcessEnded?.invoke() },
                    closeModalFunc = { processName -> onCloseProcessModalFunc?.invoke(this, processName) },
                ).invoke()
            }
            !uiState.processUuid.isNullOrBlank() -> {
                Flowx.getInstance().continueProcess(
                    processUuid = uiState.processUuid,
                    isModal = true,
                    onProcessEnded = { onProcessEnded?.invoke() },
                    closeModalFunc = { processName -> onCloseProcessModalFunc?.invoke(this, processName) },
                ).invoke()
            }
        }
    }

    @Composable
    private fun CloseModalProcessScope.CloseModalProcessConfirmAlert(show: MutableState<Boolean>) {
        if (show.value) {
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = {},
                title = null,
                text = {
                    Text(
                        this@CloseModalProcessConfirmAlert.replaceSubstitutionTag("@@close_message")
                                .takeUnless { it.isBlank() }
                            ?: "Are you sure you want to close the process?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            show.value = false
                            context.findActivity()?.finish()
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { show.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    companion object {
        const val INTENT_EXTRA_WORKSPACE_ID = "INTENT_EXTRA_WORKSPACE_ID"
        const val INTENT_EXTRA_PROJECT_ID = "INTENT_EXTRA_PROJECT_ID"
        const val INTENT_EXTRA_PROCESS_NAME = "INTENT_EXTRA_PROCESS_NAME"
        const val INTENT_EXTRA_PROCESS_UUID = "INTENT_EXTRA_PROCESS_UUID"
        const val INTENT_EXTRA_ACCESS_TOKEN = "INTENT_EXTRA_ACCESS_TOKEN"
        const val RESULT_CODE_RESTART_CONTINUE_PROCESS = 200
    }
}

internal class UpdateProcessDataBroadcastReceiver(
    private val handler: (String) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.extras?.getString("processInstanceUuid")
            ?.takeUnless { it.isBlank() }
            ?.let { processUuid -> handler.invoke(processUuid) }
    }
}
