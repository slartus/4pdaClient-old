package org.softeg.slartus.forpda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragment;

import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.download.DownloadsService;

/**
 * User: slinkin
 * Date: 28.11.11
 * Time: 14:04
 */
public class ImageViewActivity extends BaseFragmentActivity {
    private static final String URL_KEY = "url";

    private String mUrl;
    private MenuFragment mFragment1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setHomeButtonEnabled(false);

        MyImageView myImageView = new MyImageView(this, getWindowManager());
        setContentView(myImageView);


        createMenu();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mUrl = extras.getString(URL_KEY);

        myImageView.setImageDrawable(mUrl);
    }

    private void createMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    private String getUrl() {
        return mUrl;
    }

    public static void showImageUrl(Context activity, String imgUrl) {
        try {
            Intent intent = new Intent(activity, ImageViewActivity.class);
            intent.putExtra(ImageViewActivity.URL_KEY, imgUrl);
            activity.startActivity(intent);
        } catch (Exception ex) {
            Log.e(activity, ex);
        }
    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            com.actionbarsherlock.view.MenuItem item;

            item = menu.add("Скачать").setIcon(R.drawable.ic_menu_view);

            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    DownloadsService.download(getInterface(), getInterface().getUrl());
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Закрыть").setIcon(R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    getInterface().finish();
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);


        }

        public ImageViewActivity getInterface() {
            return (ImageViewActivity) getActivity();
        }
    }

}
