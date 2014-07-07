package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.NewsActivity;
import org.softeg.slartus.forpda.QuickStartActivity;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.News;
import org.softeg.slartus.forpdaapi.NewsApi;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.UrlExtensions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 06.12.11
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class NewsTab extends ThemesTab {

    @Override
    public Boolean cachable() {
        return false;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    public NewsTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent);

    }

    public static final String TEMPLATE = Tabs.TAB_NEWS;
    public static final String TITLE = "Новости";
    public static final String SEARCH_TAG_KEY = "SearchTag";
    private String m_SearchTag = "";

    public static void showNewsList(Context context, String url) {

        Intent intent = new Intent(context, QuickStartActivity.class);
        intent.putExtra("template", TEMPLATE);

        Matcher m = PatternExtensions.compile("4pda.ru/tag/(.*?)(/|$)").matcher(url);
        if (m.find()) {
            intent.putExtra(SEARCH_TAG_KEY, "tag/" + m.group(1) + "/");
        }
        context.startActivity(intent);
    }

    @Override
    public void refresh(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(SEARCH_TAG_KEY))
            m_SearchTag = savedInstanceState.getString(SEARCH_TAG_KEY);
        super.refresh(savedInstanceState);

    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final ExtTopic topic = m_ThemeAdapter.getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;

        ExtUrl.addUrlMenu(getHandler(), getContext(), menu, topic.getId(), topic.getId(),
                topic.getTitle());

    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws Exception {
        Client.getInstance().doOnOnProgressChanged(progressChangedListener, "Получение данных...");
        Client.getInstance().loadTestPage();
//        if (m_Themes.size() == 0){
//            getRssItems(progressChangedListener);
//            m_Themes.setThemesCountInt(m_Themes.size()+1);
//        }
//        else
        {
            getHttpItems();
        }
    }

    private void getHttpItems() throws Exception {
        ListInfo listInfo = new ListInfo();
        listInfo.setFrom(m_Themes.size());


        ArrayList<News> newsList = NewsApi.getNews(Client.getInstance(), "http://4pda.ru/" + m_SearchTag, listInfo);

        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        for (News news : newsList) {
            ExtTopic topic = new ExtTopic(UrlExtensions.removeDoubleSplitters("http://4pda.ru/" + news.getId().toString()), news.getTitle().toString());
            topic.setLastMessageDate(displayDateFormat.parse(news.getNewsDate()), displayDateFormat);
            topic.setLastMessageAuthor(news.getAuthor().toString(), false);
            topic.setDescription(news.getDescription().toString());
            m_Themes.add(topic);
        }
        m_Themes.setThemesCountInt(listInfo.getOutCount());
    }

    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getContext(), i, l);
        if (l < 0 || m_ThemeAdapter.getCount() <= l) return;
        if (m_ThemeAdapter == null) return;
        ExtTopic topic = m_ThemeAdapter.getItem((int) l);
        if (TextUtils.isEmpty(topic.getId())) return;
        topic.setIsNew(false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getString("tabs." + m_TabId + ".Action", "getlastpost").equals("browser"))
            showNewsBrowser(topic.getId());
        else
            showNewsActivity(topic.getId());

        m_ThemeAdapter.notifyDataSetChanged();
    }

    private void showNewsActivity(String url) {
        NewsActivity.shownews(getContext(), url);
    }

    private void showNewsBrowser(String url) {
        Intent marketIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url));
        getContext().startActivity(Intent.createChooser(marketIntent, "Выберите"));
    }

}



