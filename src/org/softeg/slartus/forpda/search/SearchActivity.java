package org.softeg.slartus.forpda.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.SearchTab;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 07.10.11
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public class SearchActivity extends BaseFragmentActivity {

    private EditText query_edit;
    private SearchSettingsView mSearchSettingsView;
    private ImageButton search_button, btnSettins;
    private SearchTab m_SearchTab;
    private SearchResultView m_SearchPostsTab;
    private ISearchResultView m_CurrentResultView;

    private Handler mHandler = new Handler();
    private SearchSettings m_SearchSettings;

    public static final String TOPICS_ONLY_KEY = "TopicsOnly";

    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("SearchUrl", url);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String forumId, String forumTitle, String topicId, String query) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("ForumId", forumId);
        intent.putExtra("ForumTitle", forumTitle);
        intent.putExtra("TopicId", topicId);
        intent.putExtra("Query", query);
        context.startActivity(intent);
    }

    public static void findUserTopicsActivity(Context context, String userName) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("UserName", userName);
        intent.putExtra("Result", "topics");
        intent.putExtra("Source", "top");
        context.startActivity(intent);
    }

    public static void findUserPostsActivity(Context context, String userName) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("UserName", userName);
        context.startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v,
                                    android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (m_CurrentResultView != null)
            m_CurrentResultView.onCreateContextMenu(menu, v, menuInfo, mHandler);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.search_activity);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        View customNav = LayoutInflater.from(this).inflate(R.layout.search_activity_panel, null);
        getSupportActionBar().setCustomView(customNav);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        query_edit = (EditText) customNav.findViewById(R.id.query_edit);
        query_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });
        search_button = (ImageButton) customNav.findViewById(R.id.btnSearch);
        search_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                search();
            }
        }
        );

        m_SearchSettings = new SearchSettings(this, "SearchThemes");
        Intent intent = getIntent();
        Boolean searchStartIntent = m_SearchSettings.tryFill(intent);
        Boolean topicsOnly = intent.getBooleanExtra(TOPICS_ONLY_KEY, false);
        if (!searchStartIntent)
            m_SearchSettings.loadSettings();


        mSearchSettingsView = (SearchSettingsView) findViewById(R.id.searchSettingsView);


        btnSettins = (ImageButton) findViewById(R.id.btnSettins);
        btnSettins.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mSearchSettingsView.setVisibility(mSearchSettingsView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        }

        );


        setSearchSettings();

        if (topicsOnly) {
            mSearchSettingsView.chkTopics.setChecked(true);
            mSearchSettingsView.chkTopics.setEnabled(false);
        }
        if (searchStartIntent)
            externalSearch();

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (m_CurrentResultView != null)
            if (m_CurrentResultView.dispatchKeyEvent(event))
                return true;

        return super.dispatchKeyEvent(event);

    }

    private void setSearchSettings() {
        mSearchSettingsView.setSearchSettings(m_SearchSettings);

        query_edit.setText(m_SearchSettings.getQuery());
        query_edit.selectAll();
        query_edit.setSelection(0, query_edit.getText().toString().length());

        query_edit.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void addSearchTab() {
        if (m_SearchTab != null) return;

        ((LinearLayout) findViewById(R.id.lnrThemes)).removeAllViews();
        m_SearchPostsTab = null;
        m_SearchTab = new SearchTab(this, "SearchThemes", null);
        m_CurrentResultView = m_SearchTab;
        m_SearchTab.getListView().setOnCreateContextMenuListener(this);
        ((LinearLayout) findViewById(R.id.lnrThemes)).addView(m_SearchTab, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }

    private void addSearchPosts() {
        if (m_SearchPostsTab != null) return;

        ((LinearLayout) findViewById(R.id.lnrThemes)).removeAllViews();
        m_SearchTab = null;
        m_SearchPostsTab = new SearchResultView(this);
        m_CurrentResultView = m_SearchPostsTab;
        registerForContextMenu(m_SearchPostsTab.getWebView());
        ((LinearLayout) findViewById(R.id.lnrThemes)).addView(m_SearchPostsTab, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }

    private void externalSearch() {
        hideKeybord(mSearchSettingsView.getUserNameEdit());
        hideKeybord(query_edit);

        if (!m_SearchSettings.getResultsInTopicView()) {
            addSearchPosts();
        } else {
            addSearchTab();
        }

        m_CurrentResultView.search(m_SearchSettings);
    }

    private void search() {
        search(mSearchSettingsView.chkTopics.isChecked() && !mSearchSettingsView.chkSearchInTopic.isChecked());
    }

    private void search(Boolean topicsResultView) {
        hideKeybord(mSearchSettingsView.getUserNameEdit());
        hideKeybord(query_edit);
        SearchActivity.this.mSearchSettingsView.setVisibility(View.GONE);

        mSearchSettingsView.fillSearchSettings(m_SearchSettings, query_edit.getText().toString(), topicsResultView);

        if (!topicsResultView) {
            addSearchPosts();

        } else {
            addSearchTab();
        }

        m_CurrentResultView.search(m_SearchSettings);
    }

    private void hideKeybord(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void share() {
        //   m_SearchSettings.getSearchQuery()
    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }

        private SearchActivity getProfileActivity() {

            return (SearchActivity) getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            com.actionbarsherlock.view.MenuItem item;


            item = menu.add("Поделиться").setIcon(R.drawable.ic_menu_share);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    //         IntentActivity.showInDefaultBrowser(getActivity(), "http://4pda.ru/forum/index.php?showuser=" + getProfileActivity().getUserId());
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }
}
