

package org.kde.kdeconnect.Plugins.ClibpoardPlugin

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.service.quicksettings.PendingIntentActivityWrapper
import androidx.core.service.quicksettings.TileServiceCompat
import org.kde.kdeconnect.KdeConnect

@RequiresApi(Build.VERSION_CODES.N)
class ClipboardTileService : TileService() {
    override fun onClick() {
        super.onClick()

        TileServiceCompat.startActivityAndCollapse(this, PendingIntentActivityWrapper(
            this, 0, Intent(this, ClipboardFloatingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                val ids = KdeConnect.getInstance().devices.values
                    .filter { it.isReachable && it.isPaired }
                    .map { it.deviceId }
                putExtra("connectedDeviceIds", ArrayList(ids))
            }, PendingIntent.FLAG_ONE_SHOT, true
        ))
    }
}
