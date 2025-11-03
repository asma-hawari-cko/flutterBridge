package com.example.newflutter
import android.util.Log
import com.example.newflutter.CardViewFactory
import com.example.newflutter.FlowViewFactory
import com.example.newflutter.GooglePayViewFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformViewRegistry

class MainActivity : FlutterFragmentActivity() {
    private val CHANNEL = "checkout_bridge"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val messenger = flutterEngine.dartExecutor.binaryMessenger
        val registry: PlatformViewRegistry = flutterEngine.platformViewsController.registry

        // ✅ Pass messenger to factories
        registry.registerViewFactory("flow_card_view", CardViewFactory(messenger, this))
        registry.registerViewFactory("flow_googlepay_view", GooglePayViewFactory(messenger,this))
        registry.registerViewFactory("flow_flow_view", FlowViewFactory(messenger, this))

        // ✅ Set up MethodChannel (for future callbacks or control)
        MethodChannel(messenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "initializeCheckout" -> {
                    val sessionId = call.argument<String>("paymentSessionID")
                    val sessionSecret = call.argument<String>("paymentSessionSecret")
                    val publicKey = call.argument<String>("publicKey")

                    Log.d("FlutterBridge", "Received: $sessionId, $sessionSecret, $publicKey")
                    result.success(null) // No-op since everything is in PlatformView
                }
                else -> result.notImplemented()
            }
        }
    }
}