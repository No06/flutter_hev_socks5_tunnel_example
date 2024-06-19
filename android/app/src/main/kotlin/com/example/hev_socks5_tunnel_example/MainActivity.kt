package com.example.hev_socks5_tunnel_example

import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import hev.htproxy.TProxyService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val channel = "vpn_manager"

    companion object {
        private const val TAG = "MainActivity"
        private const val ACTIVITY_REQUEST_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalConfig.CONFIG_PARENT_PATH = cacheDir
        Log.d(TAG, "init 'CONFIG_PARENT_PATH'")
    }

    override fun configureFlutterEngine( flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channel).setMethodCallHandler {
                call, result ->
            when (call.method) {
                "getConnectStatus" -> result.success(isVpnConnected())
                "setConfig" -> {
                    val configContent = call.arguments.toString()
                    // 将配置文件写入本地
                    saveConfigToFile(configContent)
//                    Log.d("configContent", configContent)
                    result.success(null);
                }
                "toggle" -> {
                    // 运行前检查授权状态
                    initVpnPermission()
                    if (GlobalConfig.CONFIG_PARENT_PATH == null) {
                        GlobalConfig.CONFIG_PARENT_PATH = cacheDir
                    }
                    if (isVpnConnected()) {
                        Log.d(TAG, "stop vpn")
                        stopVpn()
                    } else {
                        Log.d(TAG, "start vpn")
                        startVpn()
                    }
                    result.success(null)
                }
                "getConfig" -> {
                    val file = GlobalConfig.tproxyFile
                    val fileContent = file.readText()
                    result.success(fileContent)
                }
                "getTProxyStats" -> {
                    result.success(null)
                }
                else -> {
                    Log.e(TAG, "unimple")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // 请求码为 0 表示用户首次授权 VPN 并同意，启动 TProxyService
            startVpn()
        }
    }

    private fun initVpnPermission() {
        // 检查是否已经有 VPN 连接权限
        val intent = VpnService.prepare(this)
        if (intent != null) {
            // 用户还没有授权，需要请求权限
            startActivityForResult(intent, 0)
        } else {
            // 用户已经授权，发送请求码
            onActivityResult(1, Activity.RESULT_OK, null)
        }
    }

    // 将配置字符串保存到文件
    private fun saveConfigToFile(configContent: String) {
        // 获取内部存储的文件目录
        val file = GlobalConfig.tproxyFile
        // 将字符串内容写入文件
        file.writeText(configContent)
    }

    // 启动 VPN 服务
    private fun startVpn() {
        val serviceIntent = Intent(this, TProxyService::class.java)
        startService(serviceIntent.setAction(TProxyService.ACTION_CONNECT))
    }

    private fun stopVpn() {
        val serviceIntent = Intent(this, TProxyService::class.java)
        startService(serviceIntent.setAction(TProxyService.ACTION_DISCONNECT))
    }

    private fun isVpnConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        var hasvpn = false
        for (i in 0 until networks.size) {
            val caps: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(networks.get(i))
            hasvpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
        return hasvpn
    }
}
