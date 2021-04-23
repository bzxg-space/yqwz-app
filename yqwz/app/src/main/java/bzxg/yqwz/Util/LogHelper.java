package bzxg.yqwz.Util;

import android.util.Log;

import java.io.IOException;

/**
 * Created by bzxg on 2021/3/7.
 */
public class LogHelper {

    public static void ShowLog(String s) {
        Log.d("message",s);
    }

    public static void ShowException(IOException ex) {
        Log.e("exception",ex.getMessage(),ex);
    }
}
