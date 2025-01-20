import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Floating Overlay',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Floating Overlay'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform =
      MethodChannel('com.example.floating_example_app/overlay');
  bool _isServiceRunning = false;

  Future<void> _startFloatingService() async {
    try {
      final bool result = await platform.invokeMethod('startFloatingService');
      setState(() {
        _isServiceRunning = result;
      });
    } on PlatformException catch (e) {
      print("Failed to start floating service: '${e.message}'.");
    }
  }

  Future<void> _stopFloatingService() async {
    try {
      final bool result = await platform.invokeMethod('stopFloatingService');
      setState(() {
        _isServiceRunning = !result;
      });
    } on PlatformException catch (e) {
      print("Failed to stop floating service: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Floating overlay service is:',
              style: TextStyle(fontSize: 18),
            ),
            Text(
              _isServiceRunning ? 'RUNNING' : 'STOPPED',
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _isServiceRunning
                  ? _stopFloatingService
                  : _startFloatingService,
              child: Text(_isServiceRunning ? 'Stop Service' : 'Start Service'),
            ),
          ],
        ),
      ),
    );
  }
}
