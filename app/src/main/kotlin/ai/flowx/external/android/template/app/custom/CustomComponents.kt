package ai.flowx.external.android.template.app.custom

import ai.flowx.android.sdk.FlowxSdkApi
import ai.flowx.android.sdk.ui.components.custom.CustomComponentAction
import ai.flowx.android.sdk.ui.components.custom.CustomComponentsProvider
import ai.flowx.android.sdk.ui.components.custom.CustomComposable
import ai.flowx.android.sdk.ui.components.custom.CustomComposableComponent
import ai.flowx.android.sdk.ui.components.custom.CustomView
import ai.flowx.android.sdk.ui.components.custom.CustomViewComponent
import ai.flowx.external.android.template.app.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.json.JSONObject


class CustomComponentsProviderImpl : CustomComponentsProvider {
    override fun provideCustomComposableComponent(): CustomComposableComponent? {
        return object : CustomComposableComponent {
            override fun provideCustomComposable(componentIdentifier: String): CustomComposable =
                object : CustomComposable {

                    val data: MutableStateFlow<Any?> = MutableStateFlow(null)
                    var actions: Map<String, CustomComponentAction> = emptyMap()

                    override val isDefined: Boolean
                        get() = when (componentIdentifier) {
                            "myCustomComponent" -> true // NOTE: set this to false to use the legacy view system instead of compose, which is mainstream now
                            else -> false
                        }

                    override val composable: @Composable () -> Unit = when (componentIdentifier) {
                        "myCustomComponent" -> { {
                            val viewModel = remember { MyCustomComponentViewModel(data, actions) }
                            MyCustomComponent(viewModel = viewModel)
                        } }

                        else -> { {} }
                    }

                    override fun populateUi(data: Any?) {
                        this.data.value = data
                    }

                    override fun populateUi(actions: Map<String, CustomComponentAction>) {
                        this.actions = actions
                    }
                }
        }
    }

    override fun provideCustomViewComponent(): CustomViewComponent? {
        return object : CustomViewComponent {
            override fun provideCustomView(componentIdentifier: String) = object : CustomView {

                val data: MutableStateFlow<Any?> = MutableStateFlow(null)
                var actions: Map<String, CustomComponentAction> = emptyMap()

                override val isDefined: Boolean
                    get() = when (componentIdentifier) {
                        "myCustomComponent" -> true // NOTE: set the compose equivalent component to false to use the legacy view system instead of compose (which is mainstream now)
                        else -> false
                    }

                override fun getView(context: Context): View = when (componentIdentifier) {
                    "myCustomComponent" -> myCustomComponent(context, data, actions)
                    else -> View(context)
                }

                override fun populateUi(data: Any?) {
                    this.data.value = data
                }

                override fun populateUi(actions: Map<String, CustomComponentAction>) {
                    this.actions = actions
                }
            }
        }
    }
}

@Composable
private fun MyCustomComponent(
    viewModel: MyCustomComponentViewModel = viewModel<MyCustomComponentViewModel>()
) {
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

private fun myCustomComponent(
    context: Context,
    data: MutableStateFlow<Any?> = MutableStateFlow(null),
    actions: Map<String, CustomComponentAction> = emptyMap(),
): View {
    return CustomComponentView(context = context, data = data, actions = actions)
}

class CustomComponentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    data: MutableStateFlow<Any?> = MutableStateFlow(null),
    actions: Map<String, CustomComponentAction> = emptyMap(),
) : LinearLayout(context, attrs, defStyleAttr), ViewModelStoreOwner, LifecycleOwner {

    private val registry = LifecycleRegistry(this)

    private var job: Job? = null
    private lateinit var client: TextView
    private lateinit var dateOfBirth: TextView

    private val viewModel: MyCustomComponentViewModel by lazy {
        ViewModelProvider(
            store = viewModelStore,
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MyCustomComponentViewModel(data, actions) as T
                }
            }
        )[MyCustomComponentViewModel::class.java]
    }

    init {
        initView()
    }

    private fun initView() {
        View.inflate(context, R.layout.my_custom_component, this)
        client = findViewById<TextView>(R.id.tvClient)
        dateOfBirth = findViewById<TextView>(R.id.tvDateOfBirth)

        findViewById<Button>(R.id.btnConfirm).also {
            it.setOnClickListener {
                // enable and adjust values to test the action (which was prior defined in the process)
//                viewModel.executeSomeRealAction()
                Toast.makeText(context, "Define action in the process and enable its execution in the code", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (job?.isActive == true) {
            job?.cancel()
        }

        job = lifecycleScope.launch {
            combine(
                viewModel.firstName,
                viewModel.lastName,
                viewModel.dateOfBirth
            ) { firstName, lastName, dateOfBirth ->
                Triple(firstName, lastName, dateOfBirth)
            }.collect { (fn, ln, dob) ->
                client.text = String.format("Client: %s %s", fn, ln)
                dateOfBirth.text = String.format("Date of Birth: %s", dob)
            }
        }
    }

    override fun onDetachedFromWindow() {
        job?.cancel()
        job = null
        super.onDetachedFromWindow()
    }

    override val viewModelStore: ViewModelStore = ViewModelStore()

    override val lifecycle: Lifecycle = registry
}

class MyCustomComponentViewModel(
    private val data: MutableStateFlow<Any?> = MutableStateFlow(null),
    val actions: Map<String, CustomComponentAction> = emptyMap(),
) : ViewModel() {

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

    fun executeSomeRealAction() {
        actions["someRealAction"]?.let {
            FlowxSdkApi.getInstance().executeAction(
                action = it,
                params = JSONObject() // e.g. JSONObject("{\"someParameter\": \"someValue\"}")
            )
        } ?: println("MyCustomComponent: `someRealAction` action was not found")
    }
}
