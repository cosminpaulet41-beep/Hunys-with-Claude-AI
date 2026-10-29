/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * După repornire sau actualizare, pornește la loc stratul dacă era pornit.
 * After a reboot or an update, turns the layer back on if it was on.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Prefs.isActive(Prefs.get(context))) {
            Dimmer.apply(context);
        }
    }
}
