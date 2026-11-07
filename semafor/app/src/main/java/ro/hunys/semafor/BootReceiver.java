/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.semafor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * După repornire sau actualizare, pornește la loc serviciul dacă era pornit.
 * After a reboot or an update, starts the service again if it was on.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Prefs.isActive(Prefs.get(context))) {
            Gate.apply(context);
        }
    }
}
