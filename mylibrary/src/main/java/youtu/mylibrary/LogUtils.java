package youtu.mylibrary;

import android.content.Context;
import android.util.Log;

/**
 * Created by djf on 2017/7/7.
 */

public class LogUtils {

    private static Context context = ApplicationBase.getmContext();

    private static String getApplicationName() {
        return context.getClass().getSimpleName();
    }
    public static void d(String tag, String msg) {
        Log.d(getApplicationName() + "  " + tag, msg);
    }

    public static void d(String msg) {
        d("", msg);
    }

}
