import 'package:flutter/services.dart';

class VpnManager {
  static const channel = MethodChannel("vpn_manager");

  static Future<bool?> get isConnected =>
      channel.invokeMethod("getConnectStatus");

  static Future<void> toggle() => channel.invokeMethod("toggle");

  static Future<String?> getConfig() => channel.invokeMethod("getConfig");
  static setConfig(String config) => channel.invokeMethod("setConfig", config);

  static Future<dynamic> getTProxyStats() =>
      channel.invokeMethod("getTProxyStats");
}
