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

    if (method == "flow") {
      Future.delayed(const Duration(milliseconds: 100), showFlowBottomSheet);
    }
  }

  @override
  Widget build(BuildContext context) {

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
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () => onSelect("flow"),
                    child: const Text("Pay with Flow"),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color.fromARGB(255, 10, 89, 208),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                  ),
                ),
              ],
            ),
          ),
          const Spacer(),
        ],
      ),
    );
  }
}
class PlatformFlowView extends StatelessWidget {
  const PlatformFlowView({super.key});

  @override
  Widget build(BuildContext context) {
    const sessionParams = {
      'paymentSessionID': "ps_34qkFNt4uZBJRpTmA4gDtOQAHEU",
      'paymentSessionSecret': "pss_d142e75b-8892-4551-8d49-d748f47404ed",
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
