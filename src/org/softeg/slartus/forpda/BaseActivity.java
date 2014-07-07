package org.softeg.slartus.forpda;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import org.softeg.slartus.forpdacommon.ExtPreferences;

/**
 * User: slinkin
 * Date: 07.12.11
 * Time: 13:24
 */
public class BaseActivity extends Activity {
    public static final String SENDER_ACTIVITY = "sender_activity";

    @Override
    public void startActivity(android.content.Intent intent) {
        intent.putExtra(BaseActivity.SENDER_ACTIVITY, getClass().toString());
        super.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
        setTheme(MyApp.getInstance().getThemeStyleResID());
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(Color.WHITE);
    }


}
