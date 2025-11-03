package com.example.newflutter

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.api.CheckoutComponents
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.RememberMeConfiguration
import com.checkout.components.interfaces.component.ComponentOption
import com.checkout.components.interfaces.error.CheckoutError
import com.checkout.components.interfaces.model.ComponentName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import kotlinx.coroutines.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType.Object
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.view.ViewCompat

class FlowPlatformView(
    context: Context,
    args: Any?,
    messenger: BinaryMessenger
) : PlatformView {

    private val activity = context as FlutterFragmentActivity // ✅ REQUIRED
    private val container = FrameLayout(activity)
    private val channel = MethodChannel(messenger, "checkout_bridge")
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var checkoutComponents: CheckoutComponents

    init {
        val params = args as? Map<*, *> ?: emptyMap<String, String>()
        val sessionId = params["paymentSessionID"] as? String ?: ""
        val sessionSecret = params["paymentSessionSecret"] as? String ?: ""
        val publicKey = params["publicKey"] as? String ?: ""

        if (sessionId.isEmpty() || sessionSecret.isEmpty() || publicKey.isEmpty()) {
            Log.e("FlowPlatformView", "Missing session parameters")
            //return
        }


        Log.d("ContextType gpay flow", "Activity class: ${activity::class.java.name}")

        val rememberMeConfiguration = RememberMeConfiguration(
            data = RememberMeConfiguration.Data(
                email = "jheng-hao.lin8@checkout.com"
            ),
            showPayButton = true
        )

        val componentOption = ComponentOption(
            showPayButton = true,
            rememberMeConfiguration = rememberMeConfiguration
        )

        val  paymentSessionG = PaymentSessionResponse(
            id = "ps_34xwjPDKnBmjJJM2TnWRrpBdElf",
            paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0eHdqUERLbkJtakpKTTJUbldScnBCZEVsZiIsImVudGl0eV9pZCI6ImVudF93dTcyNGV5emE0bnk1MmJkeG00NndtN2VuYSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfM3hocHY1ZHp1NXB1dGp4d3R4bHphNWthMnkiLCJhbW91bnQiOjEwMDAsImxvY2FsZSI6ImVuLUdCIiwiY3VycmVuY3kiOiJBRUQiLCJwYXltZW50X21ldGhvZHMiOlt7InR5cGUiOiJyZW1lbWJlcl9tZSIsImNhcmRfc2NoZW1lcyI6WyJWaXNhIiwiTWFzdGVyY2FyZCIsIkFtZXgiLCJNYWRhIl0sImVtYWlsIjoiamhlbmctaGFvLmxpbjhAY2hlY2tvdXQuY29tIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNvdW50cnkiOiJBRSJ9LCJkaXNwbGF5X21vZGUiOiJjaGVja2JveCJ9LHsidHlwZSI6ImNhcmQiLCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4IiwiTWFkYSJdLCJzY2hlbWVfY2hvaWNlX2VuYWJsZWQiOmZhbHNlLCJzdG9yZV9wYXltZW50X2RldGFpbHMiOiJkaXNhYmxlZCIsImJpbGxpbmdfYWRkcmVzcyI6eyJjb3VudHJ5IjoiQUUifX0seyJ0eXBlIjoiYXBwbGVwYXkiLCJkaXNwbGF5X25hbWUiOiJPbmxpbmUgc2hvcCIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiT25saW5lIHNob3AiLCJ0eXBlIjoiZmluYWwiLCJhbW91bnQiOiIxMCJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6Ik9ubGluZSBzaG9wIiwib3JpZ2luIjoiaHR0cHM6Ly9leGFtcGxlLmNvbSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxMCIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCJ9LCJjYXJkX3BhcmFtZXRlcnMiOnsiYWxsb3dlZF9hdXRoX21ldGhvZHMiOlsiUEFOX09OTFkiLCJDUllQVE9HUkFNXzNEUyJdLCJhbGxvd2VkX2NhcmRfbmV0d29ya3MiOlsiVklTQSIsIk1BU1RFUkNBUkQiLCJBTUVYIl19fV0sImZlYXR1cmVfZmxhZ3MiOlsiYW5hbHl0aWNzX29ic2VydmFiaWxpdHlfZW5hYmxlZCIsImNhcmRfZmllbGRzX2VuYWJsZWQiLCJnZXRfd2l0aF9wdWJsaWNfa2V5X2VuYWJsZWQiLCJsb2dzX29ic2VydmFiaWxpdHlfZW5hYmxlZCIsInJpc2tfanNfZW5hYmxlZCIsInVzZV9ub25fYmljX2lkZWFsX2ludGVncmF0aW9uIl0sInJpc2siOnsiZW5hYmxlZCI6ZmFsc2V9LCJtZXJjaGFudF9uYW1lIjoiT25saW5lIHNob3AiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzXzllYjg0YTZiLWUzODYtNGNhMy05MjQ0LTI2NjE5N2JhZWEyZiIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
            paymentSessionSecret = "pss_9eb84a6b-e386-4ca3-9244-266197baea2f"
        )

        val configuration = CheckoutComponentConfiguration(
            context = activity,
            paymentSession = paymentSessionG,
            publicKey = "pk_sbox_hwrpb642xwqpza52fhgbajaniam",
            environment = Environment.SANDBOX,
        )


        container.setViewTreeLifecycleOwner(activity)
        ViewCompat.setNestedScrollingEnabled(container, true)

        scope.launch {
            try {
                checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
                val flowComponent = checkoutComponents.create(ComponentName.Flow,componentOption)
                Log.d("Payment session", "session id ${paymentSessionG.id}")


                if (flowComponent.isAvailable()) {
                    withContext(Dispatchers.Main) {
                        val composeView = ComposeView(activity)


                        ViewCompat.setNestedScrollingEnabled(composeView, true)

                        composeView.setContent {
                            val scrollState = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            ) {
                                flowComponent.Render()
                            }
                        }
                        container.addView(composeView)
                    }
                } else {
                    Log.e("GooglePayPlatformView", "Google Pay component not available")
                }

            } catch (e: CheckoutError) {
                Log.e("GooglePayPlatformView", "Checkout AAAA error: ${e.message}")
                withContext(Dispatchers.Main) {
                    channel.invokeMethod("paymentError", e.message ?: "Unknown error")
                }
            }
        }
    }

    private fun handleActivityResult(resultCode: Int, data: String) {
        Log.d("handleactivityResult","ana hon")
        checkoutComponents?.handleActivityResult(resultCode, data)
    }

    override fun getView(): FrameLayout = container

    override fun dispose() {
        scope.cancel()
    }
}
