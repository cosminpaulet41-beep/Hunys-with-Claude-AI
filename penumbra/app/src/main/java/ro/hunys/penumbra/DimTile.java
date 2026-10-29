/*
 * Penumbra — estompare ecran / screen dimmer
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.penumbra;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/** Butonul „Penumbra” din panoul rapid / The "Penumbra" Quick Settings tile. */
public class DimTile extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        update();
    }

    @Override
    public void onClick() {
        super.onClick();
        boolean on = !Prefs.isActive(Prefs.get(this));
        if (on && !Dimmer.canDim(this)) {
            // Lipsește permisiunea: o cerem din aplicație / Permission missing: ask for it in the app.
            openApp();
            return;
        }
        boolean ok = Dimmer.setActive(this, on);
        if (on && !ok && !Dimmer.fullCoverInUse(this)) {
            openApp();
            return;
        }
        update();
    }

    private void update() {
        Tile tile = getQsTile();
        if (tile == null) return;
        SharedPreferences sp = Prefs.get(this);
        boolean on = Prefs.isActive(sp);
        tile.setState(on ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel("Penumbra");
        if (Build.VERSION.SDK_INT >= 29) {
            int dim = Math.min(Prefs.getDim(sp), Look.maxDim(this, Dimmer.fullCoverInUse(this)));
            Txt txt = Txt.of(this);
            tile.setSubtitle(on ? dim + "%" : (txt.ro ? "Oprit" : "Off"));
        }
        tile.updateTile();
    }

    @SuppressWarnings("deprecation")
    private void openApp() {
        Intent i = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(PendingIntent.getActivity(this, 0, i, DimService.piFlags()));
            } else {
                startActivityAndCollapse(i);
            }
        } catch (RuntimeException ignored) {
        }
    }
}
