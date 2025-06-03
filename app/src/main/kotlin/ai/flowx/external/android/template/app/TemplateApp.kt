package ai.flowx.external.android.template.app

import ai.flowx.android.sdk.FlowxSdkApi
import ai.flowx.android.sdk.analytics.Event
import ai.flowx.android.sdk.process.model.SdkConfig
import ai.flowx.external.android.template.app.custom.CustomComponentsProviderImpl
import android.app.Application
import android.content.Intent
import android.util.Log
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
            analyticsCollector = { event ->
                // here: link your own implementation of `AnalyticsCollector` if needed
                when (event) {
                    is Event.Screen -> Log.i("Analytics", "Event.Screen(value = ${event.data.value})")
                    is Event.Action -> Log.i("Analytics", "Event.Action(value = ${event.data.value}, screen = ${event.data.screen}, component = ${event.data.component}, label = ${event.data.label})")
                }
            },
            onNewProcessStarted = { processInstanceUuid ->
                applicationContext.sendBroadcast(
                    Intent("ai.flowx.external.android.template.app.ProcessActivity.updateProcessData").apply {
                        putExtra("processInstanceUuid", processInstanceUuid)
                        setPackage("ai.flowx.external.android.template.app")
                    }
                )
            },
        )
    }
}
