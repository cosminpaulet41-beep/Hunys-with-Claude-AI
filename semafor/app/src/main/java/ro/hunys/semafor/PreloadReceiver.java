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
 * Alarma de dinaintea următoarei întrebări („o dată la … ore”): o pregătește, ca să apară imediat.
 * The alarm before the next question ("once every … hours"): prepares it so it appears at once.
 */
public class PreloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Jurnal.log("alarmă", "aproape de ora următoarei întrebări / near the next question's time");
        if (UnlockService.isRunning()) {
            UnlockService.preloadDue();
        } else if (Prefs.isActive(Prefs.get(context))) {
            Gate.apply(context);
        }
    }
}
