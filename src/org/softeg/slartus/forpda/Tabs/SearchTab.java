package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.os.Bundle;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.Themes;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpda.search.ISearchResultView;
import org.softeg.slartus.forpda.search.SearchSettings;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 15.10.11
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class SearchTab extends ThemesTab implements ISearchResultView {
    SearchSettings m_SearchSettings;

    private SearchSettings getSearchSettings() {
        if (m_SearchSettings == null) {
            m_SearchSettings = new SearchSettings(getContext(), m_TabId);
            m_SearchSettings.loadSettings();
        }
        return m_SearchSettings;
    }

    @Override
    public String getTitle() {
        return getSearchSettings().getName();
    }

    public SearchTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent);
    }


    @Override
    protected Boolean isShowForumTitle() {
        return true;
    }

    @Override
    public String getTemplate() {
        return Tabs.TAB_SEARCH;
    }

    @Override
    public void refresh(Bundle savedInstanceState) {

        super.refresh(savedInstanceState);
    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {
        getSearchThemes(super.m_Themes, getSearchSettings().getSearchQuery("topics"), progressChangedListener);
    }

    public void search(SearchSettings searchSettings) {
        m_SearchSettings = searchSettings;

        refresh();
    }

    private void getSearchThemes(Themes themes, String query, OnProgressChangedListener progressChangedListener) throws IOException {


        String pageBody = Client.getInstance().loadPageAndCheckLogin(query + "&st=" + themes.size()
                , progressChangedListener);

        Pattern pattern = Pattern.compile("<tr>(.*?)<a href=\"/forum/index.php\\?showtopic=(\\d+)\">(.*?)</a><br /><span class=\"desc\">(.*?)</span></td>" +
                "<td class=\"row2\" width=\"15%\"><span class=\"forumdesc\"><a href=\"/forum/index.php\\?showforum=(\\d+)\" title=\".*?\">(.*?)</a></span></td>" +
                "<td align=\"center\" class=\"row1\" width=\"10%\"><a href=\"/forum/index.php\\?showuser=\\d+\">.*?</a></td>" +
                "<td align=\"center\" class=\"row2\"><a href=\"javascript:who_posted\\(\\d+\\);\">(\\d+)</a></td>" +
                "<td align=\"center\" class=\"row1\">\\d+</td><td class=\"row1\"><span class=\"desc\">(.*?)<br /><a href=\"/forum/index.php\\?showtopic=\\d+&amp;view=getlastpost\">Послед.:</a> <b><a href=\"/forum/index.php\\?showuser=(\\d+)\">(.*?)</a>");
        Pattern pagesCountPattern = Pattern.compile("<a href=\"/forum/index.php.*?st=(\\d+)\">");

        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();

        Matcher m = pattern.matcher(pageBody);

        while (m.find()) {

            ExtTopic topic = new ExtTopic(m.group(2), m.group(3));
            topic.setIsNew(m.group(1).contains("view=getnewpost"));
            topic.setDescription(m.group(4));
            topic.setForumId(m.group(5));
            topic.setForumTitle(m.group(6));


            topic.setLastMessageDate(Functions.parseForumDateTime(m.group(8), today, yesterday));

            //topic.setLastMessageAuthorId(m.group(9));
            topic.setLastMessageAuthor(m.group(10));
            themes.add(topic);
        }

        m = pagesCountPattern.matcher(pageBody);
        int themesCount = 0;
        while (m.find()) {
            themesCount = Math.max(Integer.parseInt(m.group(1)) + 1, themesCount);
        }
        themes.setThemesCountInt(themesCount);

    }

}
