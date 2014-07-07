package org.softeg.slartus.forpda.Tabs;

import android.content.Context;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicsApi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;


public class FavoritesTab extends ThemesTab {
    public FavoritesTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent);
    }

    @Override
    public Boolean cachable() {
        return true;
    }

    @Override
    public String getTemplate() {
        return Tabs.TAB_FAVORITES;
    }

    @Override
    public String getTitle() {
        return "Избранное";
    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws IOException, ParseException, URISyntaxException {
        ListInfo listInfo = new ListInfo();
        listInfo.setFrom(m_Themes.size());
        ArrayList<FavTopic> topics = TopicsApi.getFavTopics(Client.getInstance(), listInfo, progressChangedListener);
        m_Themes.setThemesCountInt(listInfo.getOutCount());
        for (Topic topic : topics) {
            m_Themes.add(new ExtTopic(topic));
        }
    }

    @Override
    protected Comparator<ExtTopic> getSortComparator() {
        return new Comparator<ExtTopic>() {
            public int compare(ExtTopic theme, ExtTopic theme1) {
                if (theme1.getIsNew() == theme.getIsNew())
                    return theme1.getLastMessageDate().compareTo(theme.getLastMessageDate());
                return theme1.getIsNew() ? 1 : 0;
            }
        };
    }


    @Override
    public void refresh() {
        if (!Client.getInstance().getLogined() && !Client.getInstance().hasLoginCookies()) {
            Client.getInstance().showLoginForm(getContext(), new Client.OnUserChangedListener() {
                public void onUserChanged(String user, Boolean success) {
                    if (success)
                        FavoritesTab.super.refresh();
                }
            });
        } else
            super.refresh();
    }
}
