import 'package:flutter/material.dart';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key}); // ✅ Make the constructor const

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key}); // ✅ Make the constructor const

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String? selectedPaymentMethod;

  void showCardBottomSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) {
        return FractionallySizedBox(
          heightFactor: 0.5,
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
            ),
            child: ClipRRect(

              child: const PlatformCardView(), // ✅ const if constructor allows
            ),
          ),
        );
      },
    );
  }

  void showFlowBottomSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) {
        return FractionallySizedBox(
          heightFactor: 0.5,
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
            ),
            child: ClipRRect(
              child: const PlatformFlowView(), // ✅ const if constructor allows
            ),
          ),
        );
      },
    );
  }

  void onSelect(String method) {
    setState(() {
      selectedPaymentMethod = method;
    });

    if (method == "card") {
      Future.delayed(const Duration(milliseconds: 100), showCardBottomSheet);
    } else if (method == "flow") {
      Future.delayed(const Duration(milliseconds: 100), showFlowBottomSheet);
    }
  }

  @override
  Widget build(BuildContext context) {
    final showApplePay = selectedPaymentMethod == "applepay";

    return Scaffold(
      backgroundColor: Colors.grey.shade100,
      appBar: AppBar(
        title: const Text('Choose Payment Method'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0.5,
      ),
      body: Column(
        children: [
          const SizedBox(height: 16),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => onSelect("applepay"),
                    child: Center(
                      child: Text(
                        Platform.isIOS
                            ? "Pay with ApplePay"
                            : "Pay with GooglePay",
                        style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.black,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => onSelect("card"),
                    child: const Text("Pay with Card"),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.blueAccent,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => onSelect("flow"),
                    child: const Text("Pay with Flow"),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color.fromARGB(255, 255, 208, 68),
                      foregroundColor: Colors.black,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                  ),
                ),
              ],
            ),
          ),
          const Spacer(),
          if (showApplePay)
            Padding(
              padding: const EdgeInsets.all(16),
              child: Container(
                height: 160,
                decoration: BoxDecoration(
                  color: Colors.white,

                ),
                child: const ClipRRect(

                  child: PlatformApplePayView(),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

// ✅ Add const constructors where possible
class PlatformApplePayView extends StatelessWidget {
  const PlatformApplePayView({super.key});

  @override
  Widget build(BuildContext context) {
    const sessionParams = {
      'paymentSessionID': "ps_2vJHh6AfvMkxQ38KJ9W3cLBzsay",
      'paymentSessionSecret': "pss_0800f53c-ab38-4bcd-811b-ef32aa289c78",
      'publicKey': "pk_sbox_cwlkrqiyfrfceqz2ggxodhda2yh",
    };

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'flow_view_applepay',
        creationParams: sessionParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'flow_googlepay_view',
        creationParams: sessionParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return const Center(child: Text("Unsupported platform"));
    }
  }
}

class PlatformCardView extends StatelessWidget {
  const PlatformCardView({super.key});

  @override
  Widget build(BuildContext context) {
    const sessionParams = {
      'paymentSessionID': "ps_34pb0jvx0BVzc9DUGmdnosscfDO",
      'paymentSessionSecret': "pss_abe3a640-b4cd-471a-9ea8-06119696473a",
      'publicKey': "pk_sbox_hwrpb642xwqpza52fhgbajaniam",
    };

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'flow_view_card',
        creationParams: sessionParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'flow_card_view',
        creationParams: sessionParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return const Center(child: Text("Unsupported platform"));
    }
  }
}

class PlatformFlowView extends StatelessWidget {
  const PlatformFlowView({super.key});

  @override
  Widget build(BuildContext context) {
    const sessionParams = {
      'paymentSessionID': "ps_2vJHh6AfvMkxQ38KJ9W3cLBzsay",
      'paymentSessionSecret': "pss_0800f53c-ab38-4bcd-811b-ef32aa289c78",
      'publicKey': "pk_sbox_cwlkrqiyfrfceqz2ggxodhda2yh",
    };

    if (defaultTargetPlatform == TargetPlatform.iOS) {
      return UiKitView(
        viewType: 'flow_view_flow',
        creationParams: sessionParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'flow_flow_view',
        creationParams: sessionParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return const Center(child: Text("Unsupported platform"));
    }
  }
}
