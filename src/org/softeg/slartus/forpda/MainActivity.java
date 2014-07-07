package org.softeg.slartus.forpda;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.softeg.slartus.forpda.Tabs.BaseTab;
import org.softeg.slartus.forpda.Tabs.ITab;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.Tabs.ThemesTab;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.ProfileMenuFragment;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.mainnotifiers.DonateNotifier;
import org.softeg.slartus.forpda.mainnotifiers.ForPdaVersionNotifier;
import org.softeg.slartus.forpda.mainnotifiers.TopicAttentionNotifier;
import org.softeg.slartus.forpda.prefs.PreferencesActivity;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdacommon.NotReportException;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends BaseFragmentActivity {
    private Handler mHandler = new Handler();
    TabHost mTabHost = null;
    private final int TAG_NAME = 0;
    private final int TAG_ID = 1;
    MenuFragment mFragment1;


    public TabHost getTabHost() {
        return mTabHost;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        try {
            getSupportActionBar().setHomeButtonEnabled(false);

            setContentView(R.layout.main);

            createMenu();


            org.softeg.slartus.forpda.Client client = org.softeg.slartus.forpda.Client.getInstance();

            client.addOnUserChangedListener(new Client.OnUserChangedListener() {
                public void onUserChanged(String user, Boolean success) {
                    userChanged();
                }
            });
            client.addOnMailListener(new Client.OnMailListener() {
                public void onMail(int count) {
                    mailsChanged();
                }
            });
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (!preferences.getBoolean("AutoLogin", false)) {
                client.clearCookies();
            }

            if (checkIntent())
                return;

            createTabHost(saveInstance);
            new DonateNotifier().start(this);
            //new MarketVersionNotifier(14).start(this);
            new TopicAttentionNotifier().start(this);
            new ForPdaVersionNotifier(1).start(this);
        } catch (Throwable ex) {
            Log.e(getApplicationContext(), ex);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mTabHost != null) {
            if (outState != null)
                outState.putString("tab", mTabHost.getCurrentTabTag());
            if (mTabHost.getCurrentView() != null)
                (((ITab) mTabHost.getCurrentView())).onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mTabHost.getCurrentView() != null)
            (((ITab) mTabHost.getCurrentView())).onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mTabHost.getCurrentView() == null) return;
        ((ITab) mTabHost.getCurrentView()).onActivityResult(requestCode, resultCode, data);

    }


    private void createMenu() {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
            if (mFragment1 == null) {
                mFragment1 = new MenuFragment();
                ft.add(mFragment1, "f1");
            }
            ft.commit();
        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    private boolean checkIntent() {
        return checkIntent(getIntent());
    }

    private boolean checkIntent(final Intent intent) {
        if (IntentActivity.checkSendAction(this, intent))
            return true;
        if (intent.getData() != null) {
            final String url = intent.getData().toString();
            if (IntentActivity.tryShowUrl(this, mHandler, url, false, true)) {
                return true;
            }


            Toast.makeText(this, "Не умею обрабатывать ссылки такого типа\n" + url, Toast.LENGTH_LONG).show();
            finish();
            return true;
        }
        return false;
    }

    private void startActivity(Uri data) {
        Intent themeIntent = new Intent(this, ThemeActivity.class);
        themeIntent.setData(data);
        startActivity(themeIntent);
    }

//    @Override
//    public void setTitle(java.lang.CharSequence title) {
//        txtTitle.setText(title);
//    }

    private Boolean m_TabHostCreating = false;
    private String m_Defaulttab = "Tab1";
    //private Boolean m_PreloadMenuShowed=false;

    private void createTabHost(Bundle saveInstance) throws NotReportException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        m_Defaulttab = prefs.getString("tabs.defaulttab", "Tab1");
        m_TabHostCreating = true;
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.clearAllTabs();
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String s) {
                if (m_TabHostCreating) return;

                ITab tab = (ITab) mTabHost.getCurrentView();
                if (tab != null && !tab.refreshed())
                    tab.refresh();
            }
        });
        // mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
        MainTabContentFactory mainTabContentFactory = new MainTabContentFactory();

        for (int i = 1; i <= getResources().getStringArray(R.array.tabsArray).length; i++) {
            if (Tabs.getTabVisible(prefs, "Tab" + i))
                addTab(mainTabContentFactory, "Tab" + i, prefs);
        }


        mTabHost.setCurrentTab(-1);
        if (saveInstance != null) {
            mTabHost.setCurrentTabByTag(saveInstance.getString("tab"));
        } else
            mTabHost.setCurrentTabByTag(m_Defaulttab);
        m_TabHostCreating = false;

//        if(prefs.getBoolean("tabs.selectonstart",false))return;// выберем из диалога
//        m_PreloadMenuShowed=true;
        ITab tab = (ITab) mTabHost.getCurrentView();
        mTabHost.getCurrentView().invalidate();
        if (!tab.refreshed())
            tab.refresh();
    }

    private void createPreloadMenu() throws NotReportException {
        //m_PreloadMenuShowed=true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String[] tabsArray = getResources().getStringArray(R.array.tabsArray);
        ArrayList<String> tabs = new ArrayList<String>();
        final ArrayList<String> tabIds = new ArrayList<String>();
        for (int i = 1; i <= tabsArray.length; i++) {
            String tabId = "Tab" + i;
            if (!Tabs.getTabVisible(prefs, tabId)) continue;
            String tabName = Tabs.getTabName(prefs, tabId);
            tabs.add(tabName);
            tabIds.add(tabId);
        }

        new AlertDialogBuilder(this)
                .setTitle("Выберите вкладку")
                .setCancelable(false)
                .setAdapterSingleChoiceItems(tabs.toArray(new String[tabs.size()]), tabIds.indexOf(m_Defaulttab), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        m_TabHostCreating = true;
                        mTabHost.setCurrentTabByTag(tabIds.get(i));
                        m_TabHostCreating = false;
                        ITab tab = (ITab) mTabHost.getCurrentView();
                        mTabHost.getCurrentView().invalidate();
                        if (!tab.refreshed())
                            tab.refresh();
                    }
                })
                .create()
                .show();

    }

    private void addTab(MainTabContentFactory mainTabContentFactory, String tabId,
                        SharedPreferences prefs) throws NotReportException {
        String label = Tabs.getTabName(prefs, tabId);


        View tabView = createTabView(mTabHost.getContext(), label);
        tabView.setTag(new TagPair("Tab", tabId));

        registerForContextMenu(tabView);

        mTabHost.addTab(mTabHost.newTabSpec(tabId).setIndicator(tabView)
                .setContent(mainTabContentFactory));
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);

        //view.seton
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private Boolean m_ExitWarned = false;

    private void appExit() {
        finish();
        System.exit(0);
        // MyApp.stopQmsService();
    }

    @Override
    public void onBackPressed() {
        if (mTabHost == null || mTabHost.getCurrentView() == null) {
            appExit();
        }
        if (mTabHost != null && mTabHost.getCurrentView() != null && !((ITab) mTabHost.getCurrentView()).onParentBackPressed()) {
            if (!m_ExitWarned) {
                Toast.makeText(this, "Нажмите кнопку НАЗАД снова, чтобы выйти из программы", Toast.LENGTH_SHORT).show();
                m_ExitWarned = true;
            } else {
                appExit();
            }

        } else {
            m_ExitWarned = false;
        }
    }

    private void userChanged() {
        mHandler.post(new Runnable() {
            public void run() {
                mFragment1.setUserMenu();
            }
        });
    }

    private void mailsChanged() {
        mHandler.post(new Runnable() {
            public void run() {
                mFragment1.setUserMenu();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        m_ExitWarned = false;
        try {
//            if(!m_PreloadMenuShowed&&PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("tabs.selectonstart",false)){
//                createPreloadMenu();
//            }

        } catch (Throwable e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        try {
            if (v.getTag() != null) {
                Object o = v.getTag();
                if (TagPair.class.isInstance(o)) {
                    TagPair tagPair = TagPair.class.cast(o);
                    if (tagPair.first.equals("Tab")) {

                        final String tabId = tagPair.second;
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        String defaulttabId = prefs.getString("tabs.defaulttab", "Tab1");
                        try {
                            menu.setHeaderTitle(Tabs.getTabName(prefs, tabId));
                        } catch (NotReportException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        menu.add("По умолчанию").setCheckable(true).setChecked(tabId.equals(defaulttabId))
                                .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(android.view.MenuItem menuItem) {
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("tabs.defaulttab", tabId);
                                        editor.commit();
                                        menuItem.setChecked(true);
                                        return true;
                                    }
                                });
                        android.view.Menu defaultActionMenu = menu.addSubMenu("Действие по умолчанию..");
                        String[] actionsArray = getResources().getStringArray(R.array.ThemeActionsArray);
                        final String[] actionsValues = getResources().getStringArray(R.array.ThemeActionsValues);
                        final String actionPrefName = "tabs." + tabId + ".Action";
                        String defaultAction = prefs.getString(actionPrefName, "getfirstpost");
                        for (int i = 0; i < actionsValues.length; i++) {
                            final int finalI = i;
                            defaultActionMenu.add(actionsArray[i])
                                    .setCheckable(true).setChecked(defaultAction.equals(actionsValues[i]))
                                    .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(android.view.MenuItem menuItem) {
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(actionPrefName, actionsValues[finalI]);
                                            editor.commit();
                                            menuItem.setChecked(true);
                                            return true;
                                        }
                                    });
                        }

//                        menu.add("Кешировать список").setCheckable(true)
//                                .setChecked(PreferenceManager.getDefaultSharedPreferences(MyApp.getContext()).getBoolean(Tabs.getTemplate(prefs,tabId)+".themeslist.usechache",false))
//                                .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
//                                    public boolean onMenuItemClick(android.view.MenuItem menuItem) {
//                                        menuItem.setChecked(!menuItem.isChecked());
//                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
//                                        SharedPreferences.Editor editor = prefs.edit();
//                                        editor.putBoolean(Tabs.getTemplate(prefs, tabId) + ".themeslist.usechache", menuItem.isChecked());
//                                        editor.commit();
//
//                                        return true;
//            }
//                                });

                    }
                    return;
                }


            }
            ((ITab) mTabHost.getCurrentView()).onCreateContextMenu(menu, v, menuInfo, mHandler);
        } catch (Throwable ex) {
            Log.e(getContext(), ex);
        }
    }

    public Handler getHandler() {
        return mHandler;
    }


    private class MainTabContentFactory implements TabHost.TabContentFactory {

        public View createTabContent(String tabId) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String template = Tabs.getTemplate(prefs, tabId);
            BaseTab tabContent = Tabs.create(MainActivity.this, template, tabId);

            tabContent.setOnTabTitleChangedListener(new ThemesTab.OnTabTitleChangedListener() {
                public void onTabTitleChanged(String title) {
                    View tabView = mTabHost.getCurrentTabView();
                    if (tabView != null) {
                        ((TextView) tabView.findViewById(R.id.tabsText)).setText(title);

                    }
                }
            });
            if (tabContent != null) {
                ITab tab = (ITab) tabContent;
                ListView listView = tab.getListView();
                if (listView != null) {
                    registerForContextMenu(tab.getListView());
                    tab.getListView().setOnCreateContextMenuListener(MainActivity.this);
                }

//                if (!m_TabHostCreating || m_Defaulttab.equals(s))
//                    tab.refresh();

            }
            return tabContent;
        }
    }


    /**
     * A fragment that displays a menu.  This fragment happens to not
     * have a UI (it does not implement onCreateView), but it could also
     * have one if it wanted.
     */
    public static final class MenuFragment extends ProfileMenuFragment {

        private TabHost getTabHost() {
            if (getActivity() == null) return null;
            return ((MainActivity) getActivity()).getTabHost();
        }

        private Handler getHandler() {
            if (getActivity() == null) return null;
            return ((MainActivity) getActivity()).getHandler();
        }

        public MenuFragment() {
            super();
        }


        @Override
        public void onCreate(Bundle saveInstance) {
            super.onCreate(saveInstance);
            setHasOptionsMenu(true);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            if (m_miCheckAllAsRead != null)
                m_miCheckAllAsRead.setVisible(Client.getInstance().getLogined());
        }

        @Override
        public void setUserMenu() {
            super.setUserMenu();
            if (m_miCheckAllAsRead != null)
                m_miCheckAllAsRead.setVisible(Client.getInstance().getLogined());
        }

        private com.actionbarsherlock.view.Menu m_miOther;
        private com.actionbarsherlock.view.MenuItem m_miCheckAllAsRead;

        public void setOtherMenu() {
            m_miCheckAllAsRead = m_miOther.add("Отметить весь форум прочитанным");
            m_miCheckAllAsRead.setIcon(R.drawable.ic_menu_mark_all_as_read);
            m_miCheckAllAsRead.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    new AlertDialogBuilder(getActivity())
                            .setTitle("Подтвердите действие")
                            .setMessage("Отметить весь форум прочитанным?")
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    Toast.makeText(getActivity(), "Запрос отправлен", Toast.LENGTH_SHORT).show();

                                    new Thread(new Runnable() {
                                        public void run() {
                                            Throwable ex = null;
                                            try {
                                                Client.getInstance().markAllForumAsRead();
                                            } catch (Throwable e) {
                                                ex = e;
                                            }

                                            final Throwable finalEx = ex;

                                            getHandler().post(new Runnable() {
                                                public void run() {
                                                    try {
                                                        if (finalEx != null) {
                                                            Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                                                            Log.e(getActivity(), finalEx);
                                                        } else {
                                                            Toast.makeText(getActivity(), "Форум отмечен прочитанным", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception ex) {
                                                        Log.e(getActivity(), ex);
                                                    }

                                                }
                                            });
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();


                    return true;
                }
            });


            com.actionbarsherlock.view.MenuItem miQuickStart = m_miOther.add("Быстрый доступ..");
            miQuickStart.setIcon(R.drawable.ic_menu_quickrun);
            miQuickStart.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    new AlertDialogBuilder(getActivity())
                            .setTitle("Быстрый доступ")
                            .setItems(Tabs.getDefaultTemplateNames(), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    QuickStartActivity.showTab(getActivity(), Tabs.templates[i]);
                                }
                            })
                            .create().show();

                    return true;
                }
            });
//            for (final String template : Tabs.templates) {
//                try {
//                    miQuickStart.add(Tabs.getDefaultTemplateName(template))
//                            .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
//                                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
//                                    Intent intent = new Intent(getActivity(), QuickStartActivity.class);
//
//                                    intent.putExtra("template", template);
//
//                                    getActivity().startActivity(intent);
//                                    return true;
//                                }
//                            });
//                } catch (NotReportException e) {
//                    Log.e(getActivity(), e);
//                }
//            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);

            boolean isLight = false;
            com.actionbarsherlock.view.MenuItem item = menu.add(R.string.Search)
                    .setIcon(R.drawable.ic_menu_search);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), org.softeg.slartus.forpda.search.SearchActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add(R.string.Settings).setIcon(R.drawable.ic_menu_preferences);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    Intent settingsActivity = new Intent(
                            getActivity(), PreferencesActivity.class);
                    startActivity(settingsActivity);


                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(R.string.Refresh).setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    try {

                        View currentView = getTabHost() == null ? null : getTabHost().getCurrentView();
                        if (currentView == null)
                            ((MainActivity) getActivity()).createTabHost(null);
                        else
                            ((ITab) getTabHost().getCurrentView()).refresh();
                    } catch (Exception ex) {
                        Log.e(getActivity(), ex);
                    }


                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(R.string.Downloads).setIcon(R.drawable.ic_menu_download);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    try {
                        QuickStartActivity.showTab(getActivity(), Tabs.TAB_DOWNLOADS);
                    } catch (Exception ex) {
                        Log.e(getActivity(), ex);
                    }

                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_NEVER);

            if (Build.VERSION.SDK_INT < 11)
                m_miOther = menu.addSubMenu(R.string.More).setIcon(R.drawable.ic_menu_more);
            else
                m_miOther = menu;

            setOtherMenu();


            item = menu.add(R.string.CloseApp)
                    .setIcon(R.drawable.ic_menu_close_clear_cancel)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {


                            getActivity().finish();
                            System.exit(0);
                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        }
    }

    public class TagPair extends Pair<String, String> {

        public TagPair(String first, String second) {
            super(first, second);
        }
    }
}
