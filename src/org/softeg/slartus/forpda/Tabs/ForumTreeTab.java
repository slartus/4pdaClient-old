package org.softeg.slartus.forpda.Tabs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.ForumItem;
import org.softeg.slartus.forpda.classes.Forums;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.db.ForumsTable;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 20.09.11
 * Time: 10:56
 */
public class ForumTreeTab extends ThemesTab {
    public static final String KEY_FORUM_ID = "ForumId";

    public static final String KEY_TOPIC_ID = "TopicId";

    public static final int ID_FORUM_MAIN_NODE = -1; //- это главная ветка
    public static final int ID_FORUM_NULL = -2;


    private ArrayAdapter<Forum> m_ForumsAdapter;
    private int m_StartForumId = ID_FORUM_NULL;
    private int m_StartTopicId = -1;

    private Boolean m_StartForumThemes = false;
    private Forum m_PDAForum = null;
    private ArrayList<String> m_CheckedIds = new ArrayList<String>();
    private String m_Name = "Форумы";
    private Boolean m_Subforums = true;

    public String getTitle() {
        return m_Name;
    }

    public ForumTreeTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent);
        loadSettings();
        m_CurrentAdapter = "ForumsAdapter";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_StartForumId = prefs.getInt(getTabId() + ".startforum", ID_FORUM_NULL);
        m_StartForumThemes = prefs.getBoolean(getTabId() + ".startforum.themes", false);
        m_ForumsAdapter = new ArrayAdapter<Forum>(getContext(), R.layout.board_forum_name, new Forums());

        setHeaderText(getTitle());
        lstTree.getRefreshableView().setAdapter(m_ForumsAdapter);
    }

    private void loadSettings() {
        String tabTag = getTabId();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        m_Name = preferences.getString(tabTag + ".Template.Name", "");
        String template_forums = preferences.getString(tabTag + ".Template.Forums", "");
        m_CheckedIds = TabDataSettingsActivity.loadChecks(template_forums);
        m_Subforums = preferences.getBoolean(tabTag + ".Template.Subforums", true);
    }

    @Override
    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (m_CurrentItem != null && m_CurrentItem.hasChildForums()) {
            l = ListViewMethodsBridge.getItemId(getContext(), i, l);
            if (l < 0 || m_ForumsAdapter.getCount() <= l) return;
            showForum(m_ForumsAdapter.getItem((int) l));
        } else {
            super.listItemClick(adapterView, view, i, l);
        }
    }

    @Override
    protected Boolean needShowStarButton() {
        return true;
    }

    @Override
    protected void starButtonClick() {
        if (m_CurrentItem == null) {
            Toast.makeText(getContext(), "Форумы не загружены", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();

        m_StartForumId = Integer.parseInt(m_CurrentItem.getId());
        m_StartForumThemes = m_CurrentItem.getThemes().size() > 0;

        editor.putInt(getTabId() + ".startforum", m_StartForumId);
        editor.putBoolean(getTabId() + ".startforum.themes", m_StartForumThemes);
        editor.commit();
        Toast.makeText(getContext(), "Форум выбран стартовым", Toast.LENGTH_SHORT).show();
        setStarButtonState(true);
    }

    private void loadForums() {
        ShowForumsTask task = new ShowForumsTask(getContext());
        task.execute();
    }

    private void showForum(Forum forum) {

        if (!forum.hasChildForums()) {
            forum.LoadMore = false;
            showThemes(forum);
            return;
        }
        setStarButtonState(forum.getId().equals(Integer.toString(m_StartForumId)) && !m_StartForumThemes);
        m_CurrentAdapter = "ForumsAdapter";
        m_ForumsAdapter = new ArrayAdapter<Forum>(getContext(), R.layout.board_forum_name, forum.getForums());
        if (lstTree.getRefreshableView().getFooterViewsCount() > 0)
            lstTree.getRefreshableView().removeFooterView(m_Footer);
        lstTree.getRefreshableView().setAdapter(m_ForumsAdapter);

        setCurrentForumItem(forum);
    }

    private void showThemes(Forum forum) {

        m_ForumForLoadThemes = forum;
        m_Themes = m_ForumForLoadThemes.getThemes();
        loadLatest();

    }

    private void setCurrentForumItem(Forum forumItem) {
        m_CurrentItem = forumItem;
        if (m_CurrentItem != null)
            setHeaderText(m_CurrentItem.getTitle().toString());
    }

    Forum m_CurrentItem;

    @Override
    public Boolean onParentBackPressed() {

        if (m_CurrentItem == null || m_CurrentItem.getParent() == null) {
            return false;
        }
        m_CurrentItem.getThemes().clear();
        showForum(m_CurrentItem.getParent());
        return true;
    }

    @Override
    public String getTemplate() {
        return Tabs.TAB_FORUMS;
    }

    @Override
    public void refresh(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_FORUM_ID)) {
            m_StartForumThemes = true;
            m_StartForumId = ID_FORUM_NULL;
            String id = savedInstanceState.getString(KEY_FORUM_ID);
            if (!TextUtils.isEmpty(id)) {

                m_StartForumId = Integer.parseInt(id);
            }
            id = savedInstanceState.getString(KEY_TOPIC_ID);
            if (!TextUtils.isEmpty(id)) {
                m_StartTopicId = Integer.parseInt(id);
            }

        }
        refresh();
    }

    @Override
    public void refresh() {
        m_Refreshed = true;
        if (m_CurrentItem != null && !m_CurrentItem.hasChildForums()) {
            m_CurrentItem.clearChildren();
            showThemes(m_CurrentItem);
            return;
        }

        //String url=m_CurrentItem.getId();
        loadForums();
    }

    @Override
    protected void loadLatest() {
        if (m_CurrentItem != null)
            m_CurrentItem.LoadMore = true;
        super.loadLatest();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        if (m_CurrentItem != null && !m_CurrentItem.hasChildForums())
            super.onCreateContextMenu(menu, v, menuInfo, handler);
        else {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            if (info.id == -1) return;
            Forum forum = m_ForumsAdapter.getItem((int) info.id);
            if (TextUtils.isEmpty(forum.getId())) return;
            ExtUrl.addUrlMenu(getHandler(), getContext(), menu, "http://4pda.ru/forum/index.php?showforum=" + forum.getId(),
                    forum.getId(),
                    forum.getTitle());
        }

    }

    private class ShowForumsTask extends AsyncTask<ForumItem, String, Boolean> {


        private ProgressDialog dialog = null;

        public ShowForumsTask(Context context) {

            dialog = new AppProgressDialog(context);
            dialog.setCancelable(false);
        }

        private Forum m_StartForum;
        private Forum m_MainForum;

        @Override
        protected Boolean doInBackground(ForumItem... forums) {
            try {
                m_StartForum = null;
                Client.getInstance().loadTestPage();
                m_MainForum = ForumsTable.loadForumsTree(true);
                fillForums();
                if (m_StartForumId == ID_FORUM_NULL && m_StartTopicId != -1)
                    m_StartForumId = Integer.parseInt(Client.getInstance().getThemeForumId(Integer.toString(m_StartTopicId)));
                if (m_StartForumId > ID_FORUM_MAIN_NODE) {
                    m_StartForum = m_PDAForum.findById(Integer.toString(m_StartForumId), true, m_StartForumThemes);
                }

                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        private void fillForums() {
            if (m_CheckedIds.size() == 0 || (m_CheckedIds.contains("all") && m_Subforums))
                m_PDAForum = m_MainForum;
            else {
                m_PDAForum = new Forum(m_MainForum.getId(), m_MainForum.getTitle());
                fillForums(m_PDAForum, m_MainForum, false);
            }
        }

        private void fillForums(Forum toForum, Forum fromForum, Boolean parentAdded) {
            for (int i = 0; i < fromForum.getForums().size(); i++) {
                Forum childForum = fromForum.getForums().get(i);
                Forum f = toForum;
                Boolean added = parentAdded;
                if (!m_Subforums) {
                    added = m_CheckedIds.contains(childForum.getId());

                    if (added) {
                        f = new Forum(childForum.getId(), childForum.getTitle());
                        toForum.addForum(f);
                    }
                } else {

                    if (parentAdded || m_CheckedIds.contains(childForum.getId())) {
                        added = true;
                        f = new Forum(childForum.getId(), childForum.getTitle());
                        toForum.addForum(f);
                    }

                }
                fillForums(f, childForum, added);

            }

            if (toForum.getForums().size() == 1) {
                Forum aloneChild = toForum.getForums().get(0);
                if (aloneChild.getId().equals(toForum.getId()))
                    toForum.clearChildren();
//                else {
//                    toForum.setId(aloneChild.getId());
//                    toForum.setTitle(aloneChild.getTitle());
//                    toForum.clearChildren();
//
//                    for (int i = 0; i < aloneChild.getForums().size(); i++) {
//                        toForum.addForum(aloneChild.getForums().get(i));
//                    }
//                }
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setMessage(progress[0]);
        }

        // can use UI thread here
        protected void onPreExecute() {
            try {

                this.dialog.setMessage(getContext().getResources().getString(R.string.loading));
                this.dialog.show();
            } catch (Throwable ex) {
                Log.e(null, ex);
            }
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();

                }
            } catch (Throwable ex) {
                Log.e(null, ex);
            }
            if (success) {
                if (m_StartForum != null) {
                    if (m_StartForumThemes) {
                        setStarButtonState(true);
                        setCurrentForumItem(m_StartForum);
                        showThemes(m_StartForum);
                    } else
                        showForum(m_StartForum);
                } else
                    showForum(m_PDAForum);

                lstTree.onRefreshComplete();
                lstTree.getRefreshableView().setSelection(0);

            } else {
                Log.e(ForumTreeTab.this.getContext(), ex);
            }

            super.onPostExecute(success);
        }

    }

    private Forum m_ForumForLoadThemes;

    @Override
    protected void getThemes(OnProgressChangedListener progressChangedListener) throws Exception {

        if (m_ForumForLoadThemes.LoadMore || m_Themes.size() == 0) {


            loadThemes(m_ForumForLoadThemes, progressChangedListener);
        }
    }

    @Override
    protected void afterOnPostSuccessExecute() {
        //setStarButtonState(m_CurrentItem.getId().equals(Integer.toString(m_StartForumId)) && m_StartForumThemes);
        setCurrentForumItem(m_ForumForLoadThemes);
        setHeaderText(m_ForumForLoadThemes.getThemes().getThemesCount() + " тем @ " + m_ForumForLoadThemes.getTitle());
    }

    public static void loadFullVersionThemes(Forum forum, Matcher mainMatcher) {
        Pattern lastPageStartPattern = Pattern.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?showforum=\\d+.*?st=(\\d+)");
        Pattern themesPattern = Pattern.compile("<tr>(.*?)<a id=\"tid-link-\\d+\" href=\"/forum/index.php\\?showtopic=(\\d+)\".*?>(.*?)</a>.*?<div class=\"desc\"><span.*?>(.*?)</span>.*?<span class=\"lastaction\">(.*?)<br />.*?<b><a href=\"/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></b></span></td></tr>");

        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();

        Matcher m = themesPattern.matcher(mainMatcher.group(2));
        while (m.find()) {
            ExtTopic topic = new ExtTopic(m.group(2), m.group(3));
            topic.setDescription(m.group(4));
            //topic.setLastMessageAuthorId(m.group(6));
            topic.setLastMessageAuthor(m.group(7));
            topic.setIsNew(m.group(1).contains(";view=getnewpost"));
            topic.setLastMessageDate(Functions.parseForumDateTime(m.group(5), today, yesterday));
            forum.addTheme(topic);
        }
        m = lastPageStartPattern.matcher(mainMatcher.group(1));
        while (m.find()) {
            forum.getThemes().setThemesCountInt(Math.max(Integer.parseInt(m.group(2)), forum.getThemes().getThemesCount()));
        }
    }

    public static void loadThemes(Forum forum, OnProgressChangedListener progressChangedListener) throws IOException {


        int start = forum.getThemes().size();
        Pattern lastPageStartPattern = Pattern.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?showforum=\\d+&amp;.*?st=(\\d+)\">");
        String pageBody = Client.getInstance().loadPageAndCheckLogin("http://" + Client.SITE + "/forum/index.php?showforum="
                + forum.getId() + "&prune_day=100&sort_by=Z-A&sort_key=last_post&topicfilter=all&st=" + start, progressChangedListener);

        Pattern themesPattern = Pattern.compile("<div class=\"topic_title\">.*?<a href=\"/forum/index.php\\?showtopic=(\\d+)\">([^<]*)</a>.*?</div><div class=\"topic_body\"><span class=\"topic_desc\">([^<]*)<br /></span><span class=\"topic_desc\">автор: <a href=\"/forum/index.php\\?showuser=\\d+\">[^<]*</a></span><br />(<a href=\"/forum/index.php\\?showtopic=\\d+&amp;view=getnewpost\">Новые</a>)?\\s*<a href=\"/forum/index.php\\?showtopic=\\d+&amp;view=getlastpost\">Послед.:</a> <a href=\"/forum/index.php\\?showuser=(\\d+)\">([^<]*)</a>(.*?)<.*?/div>");

        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();

        Matcher m = themesPattern.matcher(pageBody);
        while (m.find()) {
            ExtTopic theme = new ExtTopic(m.group(1), m.group(2));
            theme.setDescription(m.group(3));
            // theme.setLastMessageAuthorId(m.group(5));
            theme.setLastMessageAuthor(m.group(6));
            theme.setIsNew(m.group(4) != null);
            theme.setLastMessageDate(Functions.parseForumDateTime(m.group(7), today, yesterday));
            theme.setForumId(forum.getId());
            forum.addTheme(theme);
        }
        m = lastPageStartPattern.matcher(pageBody);
        while (m.find()) {
            forum.getThemes().setThemesCountInt(Math.max(Integer.parseInt(m.group(2)), forum.getThemes().getThemesCount()));
        }





    }
}
