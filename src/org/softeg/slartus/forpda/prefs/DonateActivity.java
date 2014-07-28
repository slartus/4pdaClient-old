package org.softeg.slartus.forpda.prefs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.common.StringUtils;


/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 18.10.12
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
public class DonateActivity extends SherlockPreferenceActivity {

    private Context getContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.donate_prefs);

        setDonateClickListeners(this);
    }

    public static void setDonateClickListeners(final SherlockPreferenceActivity activity) {
        activity.findPreference("Qiwi").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent marketIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.softeg.org/qiwi"));
                activity.startActivity(Intent.createChooser(marketIntent, activity.getString(R.string.Choice)));
                return true;
            }
        });

        activity.findPreference("Yandex.money").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringUtils.copyToClipboard(activity, "41001491859942");
                Toast.makeText(activity, activity.getString(R.string.DonateAccountNimberCopied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        activity.findPreference("WebMoney.moneyZ").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringUtils.copyToClipboard(activity, "Z188582160272");
                Toast.makeText(activity, activity.getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        activity.findPreference("WebMoney.moneyR").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringUtils.copyToClipboard(activity, "R391199896701");
                Toast.makeText(activity, activity.getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        activity.findPreference("WebMoney.moneyU").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringUtils.copyToClipboard(activity, "U177333629317");
                Toast.makeText(activity, activity.getString(R.string.DonatePurseCopied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        activity.findPreference("Paypal.money").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=slartus%40gmail%2ecom&lc=RU&item_name=slartus&no_note=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest";
                IntentActivity.showInDefaultBrowser(activity, url);
                return true;
            }
        });

    }
}
