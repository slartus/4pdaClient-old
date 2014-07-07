package org.softeg.slartus.forpda.prefs;

import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest;
import net.robotmedia.billing.helper.AbstractBillingActivity;
import net.robotmedia.billing.model.Transaction;

import org.softeg.slartus.forpda.R;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 28.10.12
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class DonateMarketActivity extends AbstractBillingActivity implements BillingController.IConfiguration {

    private DonateMarketActivity getContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.donate_market_prefs);

        findPreference("Billing.OneLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                getContext().requestPurchase("hcm8ljuyn47odel50kquup2oqx4p6wvj");
                return true;
            }
        });

        findPreference("Billing.ThreeLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getContext().requestPurchase("vlqddh090c8pwyorh5x1s03c3j0phewd");
                return true;
            }
        });

        findPreference("Billing.FiveLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getContext().requestPurchase("laf8wrotli3juv3uowdky310qdmkgrri");
                return true;
            }
        });

        findPreference("Billing.TenLevel").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getContext().requestPurchase("6e1aa31c5774442eae4989f735192913");
                return true;
            }
        });
    }

    @Override
    public void onPurchaseStateChanged(String itemId, Transaction.PurchaseState state) {

    }

    @Override
    public void onRequestPurchaseResponse(String itemId, BillingRequest.ResponseCode response) {
        if (response == BillingRequest.ResponseCode.RESULT_OK)
            Toast.makeText(this, getString(R.string.ThanksForDonate), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBillingChecked(boolean supported) {

    }


    public byte[] getObfuscationSalt() {
        return new byte[]{41, -90, -116, -41, 66, -53, 122, -110, -127, -96, -88, 77, 127, 115, 1, 73, 57, 110, 48, -116};
    }

    public String getPublicKey() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxFwr3tn79gN7vPEPD0kUfn9OQiFnBsQgIwC/fqN58Q9mJSomOqaGsTrVWqwyCW3UHGxc8Dw449+/T0dPFPgA7jhFonfNN1SCgJuCPG28almoXAGN6LaDLMcgrlN1SEcUUS4zzI9Wh9kheDo2Otm7/yzwa28zR77sstF1uS/3D3o6I27Ii8McWq7CmfYv1OpYfIWY0Wbv0wu+ZH1ADMHF+WtcLBcUdwC/Qfi7pCWWpnTi4MGRdh0Nk3ti77n/2FhiGM1hKZ7lKmdkGDW6zg+cUfhfNEthvEHIuM6UPbhIfFazpczHlnixjAyMfv3gOwTXoPk6BGRXp2S6b7vEdTAw6QIDAQAB";
    }
}
