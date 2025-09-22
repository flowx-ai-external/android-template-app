package ai.flowx.external.android.template.app.custom

import ai.flowx.android.sdk.api.custom.components.CustomComponent
import ai.flowx.android.sdk.api.custom.components.CustomComponentAction
import ai.flowx.android.sdk.api.custom.components.CustomComponentScope
import ai.flowx.android.sdk.api.custom.components.CustomComponentsProvider
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class CustomComponentsProviderImpl : CustomComponentsProvider {
    override fun provideCustomComponent(componentIdentifier: String): CustomComponent? =
        when (componentIdentifier) {
            "myCustomComponent" -> MyCustomComponent()
            else -> null
        }
}

private class MyCustomComponent : CustomComponent {
    private val data: MutableStateFlow<Any?> = MutableStateFlow(null)
    private var actions: MutableMap<String, CustomComponentAction> = mutableMapOf()

    private val viewModel = MyCustomComponentViewModel(data, actions)

    override val composable: @Composable (CustomComponentScope.() -> Unit)
        get() = @Composable {
            MyCustomComponent(viewModel = viewModel)
        }

    override fun populateUi(data: Any?) {
        this@MyCustomComponent.data.update { _ -> data }
    }

    override fun populateUi(actions: Map<String, CustomComponentAction>) {
        this@MyCustomComponent.actions.apply {
            clear()
            putAll(actions)
        }
    }

    override fun validate(): Boolean = true

    override fun saveData(): JSONObject? = null
}

@Composable
private fun CustomComponentScope.MyCustomComponent(
    viewModel: MyCustomComponentViewModel
) {
    viewModel.setFlowxScope(this@MyCustomComponent)

    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val dateOfBirth by viewModel.dateOfBirth.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Compose Custom Component",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .background(color = Color(0x80FFFF00))
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Client: $firstName $lastName",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Date of Birth: $dateOfBirth",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        val context = LocalContext.current
        TextButton(
            onClick = {
                // enable and adjust values to test the action (which was prior defined in the process)
//                viewModel.executeSomeRealAction()
                Toast.makeText(context, "Define action in the process and enable its execution in the code", Toast.LENGTH_LONG).show()
            }
        ) {
            Text(text = "Confirm")
        }
    }
}

class MyCustomComponentViewModel(
    val data: MutableStateFlow<Any?> = MutableStateFlow(null),
    private var actions: Map<String, CustomComponentAction> = emptyMap(),
) : ViewModel() {
    private lateinit var flowxScope: CustomComponentScope

    private val _firstName = MutableStateFlow("")
    val firstName = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName = _lastName.asStateFlow()

    private val _dateOfBirth = MutableStateFlow("")
    val dateOfBirth = _dateOfBirth.asStateFlow()

    init {
        viewModelScope.launch {
            data.collect {
                _firstName.value = (it as? JSONObject)?.optString("firstname") ?: ""
                _lastName.value = (it as? JSONObject)?.optString("lastname") ?: ""
                _dateOfBirth.value = (it as? JSONObject)?.optString("dob") ?: ""
            }
        }
    }

    fun setFlowxScope(scope: CustomComponentScope) {
        flowxScope = scope
    }

    fun executeSomeRealAction() {
        actions["someRealAction"]?.let {
            if (this@MyCustomComponentViewModel::flowxScope.isInitialized) {
                flowxScope.executeAction(
                    action = it,
                    params = JSONObject() // e.g. JSONObject("{\"someParameter\": \"someValue\"}")
                )
            }
        } ?: println("MyCustomComponent: `someRealAction` action was not found")
    }
}
