package org.softeg.slartus.forpda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import org.softeg.slartus.forpdacommon.ExtPreferences;

/**
 * User: slinkin
 * Date: 14.03.12
 * Time: 12:51
 */
public class BaseFragmentActivity extends SherlockFragmentActivity {

    protected void afterCreate() {

    }

    public Context getContext() {
        return this;
    }

    @Override
    public void startActivity(android.content.Intent intent) {
        intent.putExtra(BaseActivity.SENDER_ACTIVITY, getClass().toString());
        super.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle saveInstance) {
        setTheme(MyApp.getInstance().getThemeStyleResID());
        super.onCreate(saveInstance);
        afterCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences(prefs);


        if (Build.VERSION.SDK_INT < 11) {
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(MyApp.getInstance().getThemeBackgroundColor());
            // AlertDialogBuilder.setThemeColors((ViewGroup) view);
        }

    }

    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    protected void loadPreferences(SharedPreferences prefs) {
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
    }


}
