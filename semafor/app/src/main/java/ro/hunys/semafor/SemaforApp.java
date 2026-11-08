/*
 * Semafor — o întrebare la fiecare deblocare / a question on every unlock
 * Autor / Author: Hunys with Claude AI
 * Licență / License: MIT
 */
package ro.hunys.semafor;

import android.app.Application;
import android.os.Process;

/**
 * Pornește jurnalul și notează orice oprire bruscă (crash), cu traseul erorii.
 * Starts the log and records any crash, with its stack trace.
 */
public class SemaforApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Jurnal.init(this);
        Jurnal.log("proces", "pornit / started, pid " + Process.myPid());
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Jurnal.error("CRASH", "aplicația s-a oprit brusc / the app crashed (thread " + t.getName() + ")", e);
                if (previous != null) previous.uncaughtException(t, e);
            }
        });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_RUNNING_LOW) Jurnal.log("proces", "onTrimMemory " + level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Jurnal.log("proces", "onLowMemory");
    }
}
