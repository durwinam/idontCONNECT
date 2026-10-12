package ir.idontconnect.vpn

import android.content.Intent
import android.net.VpnService
import android.os.IBinder

/**
 * Android VPN service entry point. It intentionally does not establish a TUN until the
 * pinned native libXray integration and socket-protection lifecycle have been implemented.
 */
class IdontVpnService : VpnService() {
    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Fail closed: no VPN permission is bypassed and no fake connected state is published.
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
