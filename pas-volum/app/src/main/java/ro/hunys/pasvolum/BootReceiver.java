/*
 * Pas Volum / Volume Step
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 *
 * Pornește serviciul după repornirea proiectorului sau după o actualizare a aplicației.
 * Starts the service after the projector boots or after the app is updated.
 */
package ro.hunys.pasvolum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            if (Prefs.isEnabled(Prefs.get(context))) {
                VolumeWatchService.start(context);
            }
        }
    }
}
