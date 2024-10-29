package ai.flowx.external.android.template.app

import ai.flowx.android.sdk.FlowxSdkApi
import ai.flowx.android.sdk.process.model.SdkConfig
import ai.flowx.external.android.template.app.custom.CustomComponentsProviderImpl
import android.app.Application
import java.util.Locale

class TemplateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initFlowXSdk()
    }

    private fun initFlowXSdk() {
        FlowxSdkApi.getInstance().init(
            context = applicationContext,
            config = SdkConfig(
                baseUrl = Env.baseUrl,
                imageBaseUrl = Env.imageBaseUrl,
                enginePath = Env.enginePath,
                language = "en",
                locale = Locale.getDefault(),
                validators = mapOf("cnp" to { it.length == 13 }), // a simplified example for custom validator, named "cnp", which checks only the length of the given data
            ),
//            accessTokenProvider = null, // null for now, we will set it later through the `FlowxSdkApi.getInstance().setAccessTokenProvider(...)` call
            customComponentsProvider = CustomComponentsProviderImpl(),
            customStepperHeaderProvider = null, // here: link your own implementation of `CustomStepperHeaderProvider` if needed
        )
    }
}
