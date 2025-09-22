package ai.flowx.external.android.template.app.custom

import ai.flowx.android.sdk.api.custom.loader.CustomLoader
import ai.flowx.android.sdk.api.custom.loader.CustomLoaderProvider
import ai.flowx.android.sdk.api.custom.loader.CustomLoaderScope
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.compose.AsyncImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import org.json.JSONObject

class CustomLoadersProviderImpl : CustomLoaderProvider {
    override fun provideCustomLoader(actionName: String?): CustomLoader? =
        when (actionName) {
            "startProcess" -> MyCustomLoader(backgroundColor = Color.Black.copy(alpha = 0.38f), indicatorColor = Color.Red)
            "reloadProcess" -> MyCustomLoader(backgroundColor = Color.Yellow.copy(alpha = 0.38f), indicatorColor = Color.Green)
            "saveData" -> MyCustomLoader(backgroundColor = Color.Cyan.copy(alpha = 0.38f), indicatorColor = Color.Magenta)
            "action2" -> ComplexCustomLoader()
            else -> null
        }
}

private class MyCustomLoader(val backgroundColor: Color, val indicatorColor: Color) : CustomLoader {
    override val composable: @Composable (CustomLoaderScope.() -> Unit)
        get() = @Composable {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = indicatorColor)
                BackHandler(enabled = true) {} // block back navigation
            }
        }
}

private class ComplexCustomLoader() : CustomLoader {
    override val composable: @Composable (CustomLoaderScope.() -> Unit)
        get() = @Composable {
            AnimatedLoader(
                viewModel = viewModel(
                    factory = AnimatedLoaderViewModel.provideViewModelFactory(),
                    key = AnimatedLoaderViewModel::class.java.simpleName
                )
            )
        }
}

@Composable
private fun CustomLoaderScope.AnimatedLoader(viewModel: AnimatedLoaderViewModel) {
    viewModel.setFlowxScope(this@AnimatedLoader)

    val state by viewModel.loaderState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = state?.text ?: "")
        state?.image?.let { imageUrl ->
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        }
    }
}

private class AnimatedLoaderViewModel() : ViewModel() {
    private lateinit var flowxScope: CustomLoaderScope

    private val data: MutableStateFlow<Any?> = MutableStateFlow(
        JSONObject(
            """
            {
                "texts" : [ "text1", "text2", "text3" ],
                "images" : [ "img1", "img2", "img3" ]
            }
        """.trimIndent()
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val loaderState: StateFlow<State?> = data
        .mapNotNull {
            (it as? JSONObject)?.let { json ->
                State(
                    text = json.getJSONArray("texts")
                        .let { texts ->
                            // take a random text
                            (0 until texts.length()).randomOrNull()
                                ?.let { randomIndex -> texts.optString(randomIndex) }
                        }
                        ?.let { randomText ->
                            // try to query for the substitution tag
                            if (this@AnimatedLoaderViewModel::flowxScope.isInitialized) {
                                flowxScope.replaceSubstitutionTag(randomText)
                            } else {
                                randomText
                            }
                        },
                    image = json.getJSONArray("images")
                        .let { images ->
                            // take a random image
                            (0 until images.length()).randomOrNull()
                                ?.let { randomIndex -> images.optString(randomIndex) }
                        }
                        ?.let { randomImage ->
                            // try to query for the media resource url
                            if (this@AnimatedLoaderViewModel::flowxScope.isInitialized) {
                                flowxScope.getMediaResourceUrl(randomImage)
                            } else {
                                null
                            }
                        }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = State(),
            started = SharingStarted.WhileSubscribed(5000),
        )

    fun setFlowxScope(scope: CustomLoaderScope) {
        flowxScope = scope
    }

    companion object {
        fun provideViewModelFactory() = viewModelFactory {
            initializer { AnimatedLoaderViewModel() }
        }
    }
}

private data class State(
    val text: String? = null,
    val image: String? = null,
)