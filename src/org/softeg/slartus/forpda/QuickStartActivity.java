package org.softeg.slartus.forpda;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import org.softeg.slartus.forpda.Tabs.BaseTab;
import org.softeg.slartus.forpda.Tabs.ITabParent;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.Tabs.ThemesTab;

/**
 * User: slinkin
 * Date: 14.11.11
 * Time: 11:48
 */
public class QuickStartActivity extends BaseFragmentActivity implements ITabParent {
    private BaseTab themesTab;
    private Handler mHandler = new Handler();
    private MenuFragment mFragment1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createActionMenu();

        setContentView(R.layout.empty_activity);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String template = extras.getString("template");
        themesTab = Tabs.create(this, template, "QuickTab");
        setContentView(themesTab);
        registerForContextMenu(themesTab.getListView());
        setTitle(themesTab.getTitle());
        themesTab.refresh(extras);
        themesTab.setOnTabTitleChangedListener(new ThemesTab.OnTabTitleChangedListener() {
            public void onTabTitleChanged(String title) {
                setTitle(title);
            }
        });
    }

    public static void showTab(Activity activity, String tabTemplate) {
        showTab(activity, tabTemplate, null);
    }

    public static void showTab(Activity activity, String tabTemplate, Bundle extras) {
        Intent intent = new Intent(activity.getApplicationContext(), QuickStartActivity.class);
        if (extras != null)
            intent.putExtras(extras);
        intent.putExtra("template", tabTemplate);

        activity.startActivity(intent);
    }

    protected void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();

    }

    public BaseTab getTab() {
        return themesTab;
    }

    private Boolean m_ExitWarned = false;

    @Override
    public void onBackPressed() {

        if (!themesTab.onParentBackPressed()) {
            if (!m_ExitWarned) {
                Toast.makeText(getApplicationContext(), "Нажмите кнопку НАЗАД снова, чтобы закрыть", Toast.LENGTH_SHORT).show();
                m_ExitWarned = true;
            } else {
                finish();
            }

        } else {
            m_ExitWarned = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        themesTab.onActivityResult(requestCode, resultCode, data);
    }

    public void refresh() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Boolean refreshed() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ListView getListView() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {

    }

    @Override
    public void onResume() {
        super.onResume();
        m_ExitWarned = false;
    }


    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        private QuickStartActivity getInterface() {
            return (QuickStartActivity) getActivity();
        }

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            getInterface().getTab().onCreateOptionsMenu(menu, inflater);

            com.actionbarsherlock.view.MenuItem item = null;
            if (getInterface().getTab().refreshable()) {
                item = menu.add("Обновить").setIcon(R.drawable.ic_menu_refresh);
                item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        getInterface().themesTab.refresh();
                        return true;
                    }
                });
                item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

//            if(getInterface().getTab().cachable()){
//                item = menu.add("Кешировать список").setCheckable(true)
//                        .setChecked(PreferenceManager.getDefaultSharedPreferences(MyApp.getContext()).getBoolean(getInterface().getTab().getTemplate()+".themeslist.usechache",false));
//                item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
//                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
//                        menuItem.setChecked(!menuItem.isChecked());
//                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
//                        SharedPreferences.Editor editor = prefs.edit();
//                        editor.putBoolean(getInterface().getTab().getTemplate() + ".themeslist.usechache", menuItem.isChecked());
//                        editor.commit();
//
//                        return true;
//                    }
//                });
//                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//            }

            item = menu.add("Закрыть").setIcon(R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    getActivity().finish();

                    return true;
                }
            });
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        themesTab.onCreateContextMenu(menu, v, menuInfo, mHandler);

    }
}
