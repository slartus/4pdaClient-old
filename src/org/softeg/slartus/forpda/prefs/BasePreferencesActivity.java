package org.softeg.slartus.forpda.prefs;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;

/**
 * Created by slinkin on 27.12.13.
 */
public class BasePreferencesActivity extends SherlockPreferenceActivity {
    protected Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MyApp.getInstance().getThemeStyleResID());
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < 11) {
            setTheme(MyApp.getInstance().getThemeStyleResID());
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(getResources().getColor(MyApp.getInstance().isWhiteTheme() ? R.color.pda__background_light : R.color.pda__background_dark));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        if (Build.VERSION.SDK_INT < 11) {
            if (preference != null)
                if (preference instanceof PreferenceScreen) {
                    if (((PreferenceScreen) preference).getDialog() != null) {
                        ViewGroup root = (ViewGroup) ((PreferenceScreen) preference).getDialog().getWindow().getDecorView();
                        root.setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
                        AlertDialogBuilder.setColors(root, MyApp.getInstance().getThemeTextColor(), MyApp.getInstance().getThemeBackgroundColor(), 0);
                    }
                } else if (preference instanceof ListPreference) {
                    if (((ListPreference) preference).getDialog() != null) {
                        ViewGroup root = (ViewGroup) ((ListPreference) preference).getDialog().getWindow().getDecorView();
                        root.setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
                        AlertDialogBuilder.setColors(root, MyApp.getInstance().getThemeTextColor(), MyApp.getInstance().getThemeBackgroundColor(), 0);
                    }
                } else if (preference instanceof EditTextPreference) {
                    if (((EditTextPreference) preference).getDialog() != null) {
                        ViewGroup root = (ViewGroup) ((EditTextPreference) preference).getDialog().getWindow().getDecorView();
                        root.setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
                        AlertDialogBuilder.setColors(root, MyApp.getInstance().getThemeTextColor(), MyApp.getInstance().getThemeBackgroundColor(), 0);
                    }
                }
        }
        return false;
    }
}
