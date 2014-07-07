package org.softeg.slartus.forpda.Tabs;

import android.content.Context;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.db.TopicsHistoryTable;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.11.12
 * Time: 8:53
 * To change this template use File | Settings | File Templates.
 */
public class TopicsHistoryTab extends ThemesTab {


    public TopicsHistoryTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent);

    }


    public static final String TEMPLATE = Tabs.TAB_TOPICS_HISTORY;
    public static final String TITLE = "Посещенные темы";

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    protected Boolean isShowForumTitle() {
        return true;
    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws Exception {
        Client.getInstance().loadTestPage();
        TopicsHistoryTable.getTopicsHistory(m_Themes);
    }

}
