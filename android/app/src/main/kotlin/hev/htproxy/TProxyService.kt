package hev.htproxy

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.hev_socks5_tunnel_example.GlobalConfig
import java.io.IOException

class TProxyService : VpnService() {
    companion object {
        @JvmStatic
        private external fun TProxyStartService(config_path: String, fd: Int)
        @JvmStatic
        private external fun TProxyStopService()
        @JvmStatic
        private external fun TProxyGetStats(): LongArray?

        const val ACTION_CONNECT: String = "hev.sockstun.CONNECT"
        const val ACTION_DISCONNECT: String = "hev.sockstun.DISCONNECT"
        const val STARTUP_NOTIFICATION_ID = 1

        init {
            System.loadLibrary("hev-socks5-tunnel")
        }
    }

    private var tunFd: ParcelFileDescriptor? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISCONNECT) {
            stopService()
            return START_NOT_STICKY
        }
        startService()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRevoke() {
        stopService()
        super.onRevoke()
    }

    private fun startService() {
        if (tunFd != null) return

        val prefs = Preferences()

        /* VPN */
        var session = ""
        val builder = Builder()
        builder.setMtu(prefs.tunnelMtu)
        if (prefs.ipv4) {
            val addr = prefs.tunnelIpv4Address
            val prefix = prefs.tunnelIpv4Prefix
            val dns = prefs.dnsIpv4
            builder.addAddress(addr, prefix)
            builder.addRoute("0.0.0.0", 0)
            if (dns.isNotEmpty()) builder.addDnsServer(dns)
            session += "IPv4"
        }
        if (prefs.ipv6) {
            val addr = prefs.tunnelIpv6Address
            val prefix = prefs.tunnelIpv6Prefix
            val dns = prefs.dnsIpv6
            builder.addAddress(addr, prefix)
            builder.addRoute("::", 0)
            if (dns.isNotEmpty()) builder.addDnsServer(dns)
            if (session.isNotEmpty()) session += " + "
            session += "IPv6"
        }
        var disallowSelf = true
        if (prefs.global) {
            session += "/Global"
        } else {
            prefs.apps.forEach { appName ->
                try {
                    builder.addAllowedApplication(appName)
                    disallowSelf = false
                } catch (e: PackageManager.NameNotFoundException) {
                }
            }
            session += "/per-App"
        }
        if (disallowSelf) {
            val selfName: String = applicationContext.packageName
            try {
                builder.addDisallowedApplication(selfName)
            } catch (e: PackageManager.NameNotFoundException) {
            }
        }
        builder.setSession(session)
        tunFd = builder.establish()
        if (tunFd == null) {
            stopSelf()
            return
        }

        /* TProxy */
        val tproxyFile = GlobalConfig.tproxyFile
        TProxyStartService(tproxyFile.absolutePath, tunFd!!.fd)

        val channelName = "socks5"
        initNotificationChannel(channelName)
        createNotification(channelName)
        Log.d(null, "startService")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun stopService() {
        if (tunFd == null) return

        stopForeground(STOP_FOREGROUND_REMOVE)

        /* TProxy */
        TProxyStopService()

        /* VPN */
        try {
            tunFd?.close()
        } catch (e: IOException) {
        }
        tunFd = null
        Log.d(null, "stop service")
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification(channelName: String) {
        val i = Intent(this, TProxyService::class.java)
        val pi: PendingIntent = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE)
        val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, channelName)
        val notify: Notification = notification
            .setContentTitle("AppName")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentIntent(pi)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(STARTUP_NOTIFICATION_ID, notify, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(STARTUP_NOTIFICATION_ID, notify);
        }
    }

    // create NotificationChannel
    private fun initNotificationChannel(channelName: String) {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "AppName"
            val channel =
                NotificationChannel(channelName, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
