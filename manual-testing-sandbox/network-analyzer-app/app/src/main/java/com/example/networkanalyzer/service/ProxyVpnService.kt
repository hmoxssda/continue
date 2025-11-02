package com.example.networkanalyzer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.networkanalyzer.R
import com.example.networkanalyzer.model.NetworkRequest
import com.example.networkanalyzer.ui.main.MainActivity
import com.example.networkanalyzer.util.TrafficLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ProxyVpnService : VpnService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var localProxyServer: LocalProxyServer

    private val _capturedRequests = MutableSharedFlow<NetworkRequest>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val capturedRequests = _capturedRequests.asSharedFlow()

    override fun onCreate() {
        super.onCreate()
        TrafficLogger.init(applicationContext)
        localProxyServer = LocalProxyServer(port = 8080)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        configureVirtualInterface()
        localProxyServer.start()
        return START_STICKY
    }

    override fun onRevoke() {
        super.onRevoke()
        stopSelf()
    }

    override fun onDestroy() {
        localProxyServer.stop()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return if (intent?.action == SERVICE_INTERFACE) super.onBind(intent) else null
    }

    fun emitRequest(request: NetworkRequest) {
        scope.launch {
            _capturedRequests.emit(request)
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Network capture running"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Monitoring network traffic")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun configureVirtualInterface() {
        val builder = Builder()
            .addAddress("10.0.0.2", 32)
            .addDnsServer("1.1.1.1")
            .setSession("NetworkAnalyzerProxy")
        runCatching { builder.establish()?.close() }
    }

    companion object {
        private const val CHANNEL_ID = "proxy_vpn_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
