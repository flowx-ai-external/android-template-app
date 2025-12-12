package ai.flowx.external.android.template.app

import ai.flowx.android.sdk.main.Flowx
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class UiFlowActivity : ComponentActivity() {

    private val vm: UiFlowViewModel by viewModels(
        factoryProducer = { UiFlowViewModel.Factory(this.application) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val workspaceId = intent.getSerializableExtra(INTENT_EXTRA_WORKSPACE_ID) as? String // used to start a new ui flow
        val projectId = intent.getSerializableExtra(INTENT_EXTRA_PROJECT_ID) as? String // used to start a new ui flow
        val uiFlowName = intent.getSerializableExtra(INTENT_EXTRA_UI_FLOW_NAME) as? String // used to start a new ui flow
        val accessToken = intent.getSerializableExtra(INTENT_EXTRA_ACCESS_TOKEN) as String

        // Set the access token provider which is used to authenticate inside the SDK.
        // This should be called whenever the access token refresh logic imposes it.
        Flowx.getInstance().setAccessToken(accessToken = accessToken)

        vm.updateUiFlowData(workspaceId, projectId, uiFlowName)

        setContent {
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            Box(modifier = Modifier.safeDrawingPadding()) {
                UiFlowContent(uiState = uiState)
            }
        }
    }

    @Composable
    private fun UiFlowContent(uiState: UiFlowViewModel.UiState) {
        val uiFlowView: (@Composable () -> Unit)? = when {
            !uiState.workspaceId.isNullOrBlank() && !uiState.projectId.isNullOrBlank() && !uiState.uiFlowName.isNullOrBlank() -> {
                Flowx.getInstance().startUiFlow(
                    workspaceId = uiState.workspaceId,
                    projectId = uiState.projectId,
                    uiFlowName = uiState.uiFlowName,
                )
            }

            else -> null
        }

        uiFlowView?.let {
            key(uiState.hashCode()) {
                it.invoke()
            }
        }
    }

    companion object {
        const val INTENT_EXTRA_WORKSPACE_ID = "INTENT_EXTRA_WORKSPACE_ID"
        const val INTENT_EXTRA_PROJECT_ID = "INTENT_EXTRA_PROJECT_ID"
        const val INTENT_EXTRA_UI_FLOW_NAME = "INTENT_EXTRA_UI_FLOW_NAME"
        const val INTENT_EXTRA_ACCESS_TOKEN = "INTENT_EXTRA_ACCESS_TOKEN"
    }
}
