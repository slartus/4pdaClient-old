package org.softeg.slartus.forpda.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.common.Log;

/**
 * User: slinkin
 * Date: 10.10.12
 * Time: 9:26
 */
public class ProfileMainFullViewFragmentActivity extends BaseFragmentActivity {
    private ProfileMainFullViewFragment details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getResources().getConfiguration().orientation
//                == Configuration.ORIENTATION_LANDSCAPE) {
//            // If the screen is now in landscape mode, we can show the
//            // dialog in-line so we don't need this activity.
//            finish();
//            return;
//        }

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
             details = new ProfileMainFullViewFragment();
            details.setArguments(getIntent().getExtras());
            setTitle("Профиль: " + ProfileMainView.getGroupName(getIntent().getExtras().getInt("groupPosition")));
            getSupportFragmentManager().beginTransaction().add(
                    android.R.id.content, details).commit();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Boolean res=details.dispatchKeyEvent(event);
        if (!res)
            return super.dispatchKeyEvent(event);
        return res;
    }

    public static void showActivity(Activity context,int groupPosition, int childPosition, String data){
        try{
            Intent intent = new Intent();
            intent.setClass(context, ProfileMainFullViewFragmentActivity.class);
            intent.putExtra("groupPosition", groupPosition);
            intent.putExtra("childPosition", childPosition);
            intent.putExtra("data",data);
            context.startActivity(intent);
        }catch (Exception ex){
            Log.e(context, ex);
        }

    }
}
