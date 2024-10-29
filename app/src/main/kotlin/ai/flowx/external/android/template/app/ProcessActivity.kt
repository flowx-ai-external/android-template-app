package ai.flowx.external.android.template.app

import ai.flowx.android.sdk.FlowxSdkApi
import ai.flowx.external.android.template.app.extensions.findActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ProcessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val applicationUuid = intent.getSerializableExtra(INTENT_EXTRA_APPLICATION_UUID) as? String // used to start a new process
        val processName = intent.getSerializableExtra(INTENT_EXTRA_PROCESS_NAME) as? String // used to start a new process
        val processUuid = intent.getSerializableExtra(INTENT_EXTRA_PROCESS_UUID) as? String // used to continue an existing process
        val accessToken = intent.getSerializableExtra(INTENT_EXTRA_ACCESS_TOKEN) as String

//        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//            FlowxSdkApi.getInstance().checkRendererCompatibility {
//                when (it) {
//                    true -> Log.i(FlowxSdkApi::class.java.simpleName, "FlowX SDK Renderer is compatible with deployed platform.")
//                    false -> Log.e(FlowxSdkApi::class.java.simpleName, "FlowX SDK Renderer is NOT compatible with deployed platform.")
//                }
//            }
//        }

        // Set the access token provider which is used to authenticate inside the SDK.
        // This should be called whenever the access token refresh logic imposes it.
        FlowxSdkApi.getInstance().setAccessTokenProvider(accessTokenProvider = { accessToken })

        setContent {
            val showCloseModalProcessAlert = remember { mutableStateOf(false) }
            when {
                !applicationUuid.isNullOrBlank() && !processName.isNullOrBlank() -> {
                    FlowxSdkApi.getInstance().startProcess(
                        applicationUuid = applicationUuid,
                        processName = processName,
                        isModal = true,
                        onProcessEnded = {
                            Log.i(ProcessActivity::javaClass.name, "Process $processName has ended")
                        },
                        closeModalFunc = { processName ->
                            // NOTE: possible handling could involve doing something differently based on the `processName` value
                            showCloseModalProcessAlert.value = true
                        },
                    ).invoke()
                }
                !processUuid.isNullOrBlank() -> {
                    FlowxSdkApi.getInstance().continueProcess(
                        processUuid = processUuid,
                        isModal = true,
                        onProcessEnded = {
                            Log.i(ProcessActivity::javaClass.name, "Process $processUuid has ended")
                        },
                        closeModalFunc = { processName ->
                            // NOTE: possible handling could involve doing something differently based on the `processName` value
                            showCloseModalProcessAlert.value = true
                        },
                    ).invoke()
                }
            }

            CloseModalProcessConfirmAlert(show = showCloseModalProcessAlert)
        }
    }

    @Composable
    private fun CloseModalProcessConfirmAlert(show: MutableState<Boolean>) {
        if (show.value) {
            val context = LocalContext.current
            AlertDialog(
                onDismissRequest = {},
                title = null,
                text = {
                    Text("Are you sure you want to close the process?")
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
        const val INTENT_EXTRA_APPLICATION_UUID = "INTENT_EXTRA_APPLICATION_UUID"
        const val INTENT_EXTRA_PROCESS_NAME = "INTENT_EXTRA_PROCESS_NAME"
        const val INTENT_EXTRA_PROCESS_UUID = "INTENT_EXTRA_PROCESS_UUID"
        const val INTENT_EXTRA_ACCESS_TOKEN = "INTENT_EXTRA_ACCESS_TOKEN"
        const val RESULT_CODE_RESTART_CONTINUE_PROCESS = 200
    }
}
