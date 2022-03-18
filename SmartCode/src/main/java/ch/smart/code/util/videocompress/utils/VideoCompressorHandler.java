/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ch.smart.code.util.videocompress.utils;


import android.os.Handler;
import android.os.Looper;

public class VideoCompressorHandler {

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void runOnUIThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }

}
