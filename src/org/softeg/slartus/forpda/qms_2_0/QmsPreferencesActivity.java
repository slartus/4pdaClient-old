package org.softeg.slartus.forpda.qms_2_0;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.prefs.BasePreferencesActivity;


/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 29.05.13
 * Time: 15:50
 * To change this template use File | Settings | File Templates.
 */
public class QmsPreferencesActivity extends BasePreferencesActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.qms_prefs);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        MyApp.reStartQmsService();

    }
}

