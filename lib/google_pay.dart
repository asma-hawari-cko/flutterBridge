import 'package:flutter/services.dart';

class GooglePayFlow {
  static const MethodChannel _channel =
  MethodChannel('checkout_google_pay');

  static Future<void> startPayment(int amount, String currency) async {
    try {
      final result = await _channel.invokeMethod('startGooglePayFlow', {
        'amount': amount,
        'currency': currency,
        'customer': {
        'email': "jheng-hao.lin8@checkout.com"}
      });
      print('Payment started: $result');
    } on PlatformException catch (e) {
      print('Payment failed: ${e.message}');
    }
  }

  static void setResultHandler(Function(Map<String, dynamic>) handler) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'onPaymentResult') {
        handler(Map<String, dynamic>.from(call.arguments));
      }
    });
  }
}
