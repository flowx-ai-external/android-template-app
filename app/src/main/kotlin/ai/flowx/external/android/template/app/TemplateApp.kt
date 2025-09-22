package ai.flowx.external.android.template.app

import ai.flowx.android.sdk.api.Config
import ai.flowx.android.sdk.api.analytics.Event
import ai.flowx.android.sdk.main.Flowx
import ai.flowx.android.sdk.main.FlowxOwner
import ai.flowx.external.android.template.app.custom.CustomComponentsProviderImpl
import ai.flowx.external.android.template.app.custom.CustomLoadersProviderImpl
import ai.flowx.external.android.template.app.custom.CustomStepperHeaderProviderImpl
import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.util.Log
import java.util.Locale

class TemplateApp : Application(), FlowxOwner {
    override val flowx: Lazy<Flowx> = lazy { Flowx.getInstance() }

    override fun onCreate() {
        super.onCreate()
        initFlowXSdk()
    }

    private fun initFlowXSdk() {
        Flowx.getInstance().init(
            context = applicationContext,
            config = object : Config {
                override val baseUrl = Env.baseUrl
                override val imageBaseUrl = Env.imageBaseUrl
                override val enginePath = Env.enginePath
                override val language = "en"
                override val locale = Locale.getDefault()
                override val validators: Map<String, (String) -> Boolean>? = mapOf("cnp" to { it.length == 13 }) // a simplified example for custom validator, named "cnp", which checks only the length of the given data
                override val logEnabled: Boolean get() = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            },
            customComponentsProvider = CustomComponentsProviderImpl(),
//            customStepperHeaderProvider = CustomStepperHeaderProviderImpl(), // here: link your own implementation of `CustomStepperHeaderProvider` if needed
//            customLoaderProvider = CustomLoadersProviderImpl(), // here: link your own implementation of `CustomLoaderProvider` if needed
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
