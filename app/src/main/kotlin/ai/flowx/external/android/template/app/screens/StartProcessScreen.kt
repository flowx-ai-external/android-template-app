package ai.flowx.external.android.template.app.screens

import ai.flowx.android.sdk.main.Flowx
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StartProcessScreen(
    startProcess: () -> Unit,
    continueProcess: () -> Unit,
    logout: () -> Unit
) {
    var startProcessEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // TODO SETUP: configure your theme here by adding  the `workspaceUuid`, the `themeUuid` and/or `fallbackThemeJsonFileAssetsPath`
        Flowx.getInstance().setupTheme(
            workspaceUuid = "your_workspace_id",
            themeUuid = "your_theme_id", // when empty, no theme will be downloaded
            fallbackThemeJsonFileAssetsPath = "theme/some_theme.json", // when null, no fallback will be used
        ) {
            Log.i(Flowx::class.java.simpleName, "Theme setup completed")
            startProcessEnabled = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 32.dp
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = startProcessEnabled,
                onClick = { startProcess.invoke() }
            ) {
                Text("Start process")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = { continueProcess.invoke() }
            ) {
                Text("Continue process")
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = { logout.invoke() }
            ) {
                Text("Logout")
            }
        }
    }
}