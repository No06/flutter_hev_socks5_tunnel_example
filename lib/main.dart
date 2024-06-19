import 'package:flutter/material.dart';
import 'package:hev_socks5_tunnel_example/config_generator.dart';
import 'package:hev_socks5_tunnel_example/vpn_manager.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  var isConnected = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [Text("isConnected: $isConnected")],
        ),
      ),
      floatingActionButton: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          FloatingActionButton(
            onPressed: () async {
              // await VpnManager.setConfig(config);
              await VpnManager.toggle();
            },
            child: const Icon(Icons.play_arrow),
          ),
          FloatingActionButton(
            onPressed: () => VpnManager.isConnected.then((value) {
              setState(() {
                if (value != null) isConnected = value;
              });
            }),
            child: const Icon(Icons.add),
          ),
        ],
      ),
    );
  }
}
