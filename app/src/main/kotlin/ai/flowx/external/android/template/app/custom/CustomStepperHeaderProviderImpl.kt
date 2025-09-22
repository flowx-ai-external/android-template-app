package ai.flowx.external.android.template.app.custom

import ai.flowx.android.sdk.api.custom.stepper.CustomStepperHeader
import ai.flowx.android.sdk.api.custom.stepper.CustomStepperHeaderProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class CustomStepperHeaderProviderImpl : CustomStepperHeaderProvider {
    private val CustomStepperHeader.Data.mainStepsProgress: Float
        get() = step.toFloat() / totalSteps

    private val CustomStepperHeader.Data.subStepsProgress: Float
        get() = substep?.let {
            val previousMainStepProgress: Float = runCatching { (step - 1).coerceAtLeast(0).toFloat() / totalSteps }.getOrElse { 0f } // percentage of total progress for the previous main step
            val subStepProgress: Float = totalSubsteps?.let { runCatching { (substep ?: 0).toFloat() / it }.getOrElse { 0f } } ?: 0f // percentage of allocated total for the current sub-step
            val progressRange = (mainStepsProgress - previousMainStepProgress).coerceAtLeast(0f) // range of progress allocated to the current main step
            previousMainStepProgress + subStepProgress * progressRange // normalize the sub-step progress to the range of the current main step and add it to its starting point
        } ?: mainStepsProgress

    override fun provideCustomStepperHeader(): CustomStepperHeader? {
        return object : CustomStepperHeader {
            override val composable: @Composable (data: CustomStepperHeader.Data) -> Unit
                get() = @Composable { data ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { data.mainStepsProgress },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = ProgressIndicatorDefaults.linearTrackColor,
                        )
                        LinearProgressIndicator(
                            progress = { data.subStepsProgress },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = Color.Blue,
                            trackColor = Color.Transparent
                        )
                    }
                }
        }
    }
}