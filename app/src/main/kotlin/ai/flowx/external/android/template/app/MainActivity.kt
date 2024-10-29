package ai.flowx.external.android.template.app

import ai.flowx.external.android.template.app.extensions.collectAsEffectWithLifecycle
import ai.flowx.external.android.template.app.screens.LoginScreen
import ai.flowx.external.android.template.app.screens.StartProcessScreen
import ai.flowx.external.android.template.app.ui.theme.AndroidTemplateAppTheme
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels(
        factoryProducer = { MainViewModel.Factory(this.application) }
    )

    private lateinit var processActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        processActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            this::onActivityResult
        )

        setContent {
            val context = LocalContext.current

            val navController = rememberNavController()
            val uiState = vm.uiState.collectAsStateWithLifecycle()
            val error = vm.error.collectAsStateWithLifecycle()

//            vm.startProcess.collectAsEffect(
            vm.startProcess.collectAsEffectWithLifecycle(
                block = {
                    it?.let {
                        if (this::processActivityLauncher.isInitialized) {
                            processActivityLauncher.launch(
                                // This activity belongs to the container app.
                                // The SDK will return only a @Composable function, which, for now,
                                //   must be used only inside a container activity.
                                Intent(context, ProcessActivity::class.java).apply {
                                    // start a new process
                                    putExtra(ProcessActivity.INTENT_EXTRA_APPLICATION_UUID, it.first)
                                    putExtra(ProcessActivity.INTENT_EXTRA_PROCESS_NAME, it.second)
                                    putExtra(ProcessActivity.INTENT_EXTRA_ACCESS_TOKEN, it.third)
                                }
                            )
                        }
                    }
                }
            )

            vm.continueProcess.collectAsEffectWithLifecycle(
                block = {
                    it?.let {
                        if (this::processActivityLauncher.isInitialized) {
                            processActivityLauncher.launch(
                                // This activity belongs to the container app.
                                // The SDK will return only a @Composable function, which, for now,
                                //   must be used only inside a container activity.
                                Intent(context, ProcessActivity::class.java).apply {
                                    // continue an existing process
                                    putExtra(ProcessActivity.INTENT_EXTRA_PROCESS_UUID, it.first)
                                    putExtra(ProcessActivity.INTENT_EXTRA_ACCESS_TOKEN, it.second)
                                }
                            )
                        }
                    }
                }
            )

            AndroidTemplateAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        route = "root"
                    ) {
                        composable("login") {
                            LoginScreen(
                                login = { email, password -> vm.login(email, password) }
                            )
                        }
                        composable("start-process") {
                            StartProcessScreen(
                                startProcess = { vm.startProcess() },
                                continueProcess = { vm.continueProcess() },
                                logout = { vm.logout() }
                            )
                        }
                    }

                    when {
                        uiState.value.loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.value.loggedIn -> navController.currentBackStackEntry?.destination?.route?.takeUnless { it == "start-process" }?.let {
                            navController.navigate("start-process") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        }
                        !uiState.value.loggedIn -> navController.currentBackStackEntry?.destination?.route?.takeUnless { it == "login" }?.let {
                            navController.navigate("login") {
                                popUpTo("start-process") {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(error.value) {
                    error.value.takeUnless { it.isBlank() }?.let {
                        vm.clearError()
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun onActivityResult(result: ActivityResult) {
        when (result.resultCode) {
            ProcessActivity.RESULT_CODE_RESTART_CONTINUE_PROCESS -> {
                result.data?.extras?.getString(ProcessActivity.INTENT_EXTRA_PROCESS_UUID).takeUnless { it.isNullOrBlank() }?.let {
                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                        vm.showLoader(true) // show loader while waiting for the activity to be destroyed
                        delay(1000L) // delay to make sure the activity has been destroyed (i.e. its `onDestroy()` has been called) // NOTE: this is VERY IMPORTANT
                        vm.showLoader(false)

                        vm.continueProcess(processUuid = it)
                    }
                }
            }
            else -> {}
        }
    }
}
