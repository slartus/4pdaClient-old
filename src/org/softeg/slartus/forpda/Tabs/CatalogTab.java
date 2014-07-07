package org.softeg.slartus.forpda.Tabs;

import android.content.Context;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.appsgamescatalog.AppGameCatalog;
import org.softeg.slartus.forpdaapi.appsgamescatalog.AppsGamesCatalogApi;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 27.10.11
 * Time: 20:38
 */
public class CatalogTab extends TreeTab {
    public static final String TITLE = "Каталог";

    @Override
    public String getTemplate() {
        return Tabs.TAB_CATALOG;
    }

    public String getTitle() {
        return "Каталог";
    }

    public CatalogTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent, Tabs.TAB_CATALOG);
    }

    @Override
    protected void loadForum(Forum forum, OnProgressChangedListener progressChangedListener) throws Exception {

        loadCatalog(forum);

    }

    @Override
    protected Boolean isShowForumTitle() {
        return true;
    }

    private AppGameCatalog getCatalogFrom(Forum forum) {
        AppGameCatalog catalog = new AppGameCatalog(forum.getId(), forum.getTitle());
        catalog.setHtmlTitle(forum.getHtmlTitle());
        catalog.setType("games".equals(forum.getTag()) ? AppGameCatalog.TYPE_GAMES : AppGameCatalog.TYPE_APPLICATIONS);
        catalog.setLevel(forum.level);
        return catalog;
    }

    @Override
    protected void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {
        if (m_ForumForLoadThemes.getParent() == null)
            return;
        AppGameCatalog catalog = getCatalogFrom(m_ForumForLoadThemes);

        if (m_ForumForLoadThemes.getParent() != null) {
            AppGameCatalog parentCatalog = getCatalogFrom(m_ForumForLoadThemes.getParent());
            if (m_ForumForLoadThemes.getParent().getParent() != null) {
                AppGameCatalog parentParentCatalog = getCatalogFrom(m_ForumForLoadThemes.getParent().getParent());
                parentCatalog.setParent(parentParentCatalog);
            }
            catalog.setParent(parentCatalog);
        }

        ArrayList<Topic> topics = AppsGamesCatalogApi.loadTopics(Client.getInstance(), catalog);
        m_ForumForLoadThemes.getThemes().clear();
        for (Topic topic : topics) {
            m_ForumForLoadThemes.getThemes().add(new ExtTopic(topic));
        }

    }

    private void loadCatalog(Forum catalog) throws Exception {
        AppGameCatalog appGameCatalog = new AppGameCatalog("-1", getTitle());
        ArrayList<AppGameCatalog> catalogs = AppsGamesCatalogApi.getCatalog(Client.getInstance(), appGameCatalog);

        fillCatalog(catalog, catalogs);
    }

    private void fillCatalog(Forum forum, ArrayList<AppGameCatalog> catalogs) {
        for (AppGameCatalog catalog : catalogs) {

            if (!catalog.getParent().getId().equals(forum.getId())) continue;

            Forum f = new Forum(catalog.getId().toString(), catalog.getTitle().toString());
            f.setHtmlTitle(catalog.getHtmlTitle());
            f.setTag(catalog.getType() == AppGameCatalog.TYPE_GAMES ? "games" : "app");
            forum.addForum(f);

            if (catalog.getId().equals(catalog.getParent().getId())) continue;// каталог@темы
            fillCatalog(f, catalogs);
        }
    }

}
