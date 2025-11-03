package com.example.newflutter

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.api.CheckoutComponents
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.ComponentOption
import com.checkout.components.interfaces.error.CheckoutError
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import kotlinx.coroutines.*

class CardPlatformView(
    private val activity: Activity, // ✅ Correct type passed from factory
    args: Any?,
    messenger: BinaryMessenger
) : PlatformView {

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
            Log.e("CardPlatformView", "Missing required session parameters")
//            return
        }

        val configuration = CheckoutComponentConfiguration(
            context = activity,
            paymentSession = PaymentSessionResponse(
                id = sessionId,
                paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzM0cGIwanZ4MEJWemM5RFVHbWRub3NzY2ZETyIsImVudGl0eV9pZCI6ImVudF93dTcyNGV5emE0bnk1MmJkeG00NndtN2VuYSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfM3hocHY1ZHp1NXB1dGp4d3R4bHphNWthMnkiLCJhbW91bnQiOjEwMDAsImxvY2FsZSI6ImVuLUdCIiwiY3VycmVuY3kiOiJBRUQiLCJwYXltZW50X21ldGhvZHMiOlt7InR5cGUiOiJyZW1lbWJlcl9tZSIsImNhcmRfc2NoZW1lcyI6WyJWaXNhIiwiTWFzdGVyY2FyZCIsIkFtZXgiLCJNYWRhIl0sImVtYWlsIjoiamlhLnRzYW5nQGV4YW1wbGUuY29tIiwiYmlsbGluZ19hZGRyZXNzIjp7ImNvdW50cnkiOiJBRSJ9LCJkaXNwbGF5X21vZGUiOiJjaGVja2JveCJ9LHsidHlwZSI6ImNhcmQiLCJjYXJkX3NjaGVtZXMiOlsiVmlzYSIsIk1hc3RlcmNhcmQiLCJBbWV4IiwiTWFkYSJdLCJzY2hlbWVfY2hvaWNlX2VuYWJsZWQiOmZhbHNlLCJzdG9yZV9wYXltZW50X2RldGFpbHMiOiJkaXNhYmxlZCIsImJpbGxpbmdfYWRkcmVzcyI6eyJjb3VudHJ5IjoiQUUifX0seyJ0eXBlIjoiYXBwbGVwYXkiLCJkaXNwbGF5X25hbWUiOiJPbmxpbmUgc2hvcCIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiLCJhbWV4Il0sInRvdGFsIjp7ImxhYmVsIjoiT25saW5lIHNob3AiLCJ0eXBlIjoiZmluYWwiLCJhbW91bnQiOiIxMCJ9fSx7InR5cGUiOiJnb29nbGVwYXkiLCJtZXJjaGFudCI6eyJpZCI6IjA4MTEzMDg5Mzg2MjY4ODQ5OTgyIiwibmFtZSI6Ik9ubGluZSBzaG9wIiwib3JpZ2luIjoiaHR0cHM6Ly9leGFtcGxlLmNvbSJ9LCJ0cmFuc2FjdGlvbl9pbmZvIjp7InRvdGFsX3ByaWNlX3N0YXR1cyI6IkZJTkFMIiwidG90YWxfcHJpY2UiOiIxMCIsImNvdW50cnlfY29kZSI6IkdCIiwiY3VycmVuY3lfY29kZSI6IkFFRCJ9LCJjYXJkX3BhcmFtZXRlcnMiOnsiYWxsb3dlZF9hdXRoX21ldGhvZHMiOlsiUEFOX09OTFkiLCJDUllQVE9HUkFNXzNEUyJdLCJhbGxvd2VkX2NhcmRfbmV0d29ya3MiOlsiVklTQSIsIk1BU1RFUkNBUkQiLCJBTUVYIl19fV0sImZlYXR1cmVfZmxhZ3MiOlsiYW5hbHl0aWNzX29ic2VydmFiaWxpdHlfZW5hYmxlZCIsImNhcmRfZmllbGRzX2VuYWJsZWQiLCJnZXRfd2l0aF9wdWJsaWNfa2V5X2VuYWJsZWQiLCJsb2dzX29ic2VydmFiaWxpdHlfZW5hYmxlZCIsInJpc2tfanNfZW5hYmxlZCIsInVzZV9ub25fYmljX2lkZWFsX2ludGVncmF0aW9uIl0sInJpc2siOnsiZW5hYmxlZCI6ZmFsc2V9LCJtZXJjaGFudF9uYW1lIjoiT25saW5lIHNob3AiLCJwYXltZW50X3Nlc3Npb25fc2VjcmV0IjoicHNzX2FiZTNhNjQwLWI0Y2QtNDcxYS05ZWE4LTA2MTE5Njk2NDczYSIsInBheW1lbnRfdHlwZSI6IlJlZ3VsYXIiLCJpbnRlZ3JhdGlvbl9kb21haW4iOiJkZXZpY2VzLmFwaS5zYW5kYm94LmNoZWNrb3V0LmNvbSJ9",
                paymentSessionSecret = sessionSecret
            ),
            publicKey = publicKey,
            environment = Environment.SANDBOX
        )

        scope.launch {
            try {
                checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
                val flow = checkoutComponents.create(
                    PaymentMethodName.Card,
                    ComponentOption(showPayButton = false)
                )

                if (flow.isAvailable()) {
                    withContext(Dispatchers.Main) {
                        val composeView = ComposeView(activity)
                        composeView.setContent {
                            flow.Render()
                        }
                        container.addView(composeView)
                    }
                } else {
                    Log.e("CardPlatformView", "Card component not available")
                }
            } catch (e: CheckoutError) {
                Log.e("CardPlatformView", "Checkout AAAA SSSSS error: ${e.message}")
                withContext(Dispatchers.Main) {
                    channel.invokeMethod("paymentError", e.message ?: "Unknown error")
                }
            }
        }
    }

    override fun getView(): FrameLayout = container

    override fun dispose() {
        scope.cancel()
    }
}
