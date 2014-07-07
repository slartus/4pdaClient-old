package org.softeg.slartus.forpda.Tabs;

import android.content.Context;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.appsgamescatalog.AppGameCatalog;
import org.softeg.slartus.forpdaapi.digest.DigestApi;
import org.softeg.slartus.forpdaapi.digest.DigestCatalog;

import java.io.IOException;
import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 27.10.11
 * Time: 8:31
 */
public class DigestTab extends TreeTab {


    @Override
    public String getTemplate() {
        return Tabs.TAB_DIGEST;
    }

    public String getTitle() {
        return "Дайджест";
    }

    public DigestTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent, Tabs.TAB_DIGEST);
    }

    @Override
    protected void loadForum(Forum forum, OnProgressChangedListener progressChangedListener) throws Exception {
        loadDigest(forum);
    }

    private DigestCatalog getDigestFrom(Forum forum) {
        DigestCatalog catalog = new DigestCatalog(forum.getId(), forum.getTitle());
        catalog.setHtmlTitle(forum.getHtmlTitle());
        catalog.setType("game".equals(forum.getTag()) ? AppGameCatalog.TYPE_GAMES : AppGameCatalog.TYPE_APPLICATIONS);
        catalog.setLevel(forum.level);
        return catalog;
    }

    @Override
    protected void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {

        DigestCatalog catalog = getDigestFrom(m_ForumForLoadThemes);

        if (m_ForumForLoadThemes.getParent() != null) {
            DigestCatalog parentCatalog = getDigestFrom(m_ForumForLoadThemes.getParent());
            if (m_ForumForLoadThemes.getParent().getParent() != null) {
                DigestCatalog parentParentCatalog = getDigestFrom(m_ForumForLoadThemes.getParent().getParent());
                parentCatalog.setParent(parentParentCatalog);
            }
            catalog.setParent(parentCatalog);
        }

        ArrayList<Topic> topics = DigestApi.loadTopics(Client.getInstance(), catalog);
        m_ForumForLoadThemes.getThemes().clear();
        for (Topic topic : topics) {
            m_ForumForLoadThemes.getThemes().add(new ExtTopic(topic));
        }
    }

    public void loadDigest(final Forum digest) throws Exception {
        digest.clearChildren();
        DigestCatalog appGameCatalog = new DigestCatalog("-1", getTitle());
        ArrayList<DigestCatalog> catalogs = DigestApi.getCatalog(Client.getInstance(), appGameCatalog);

        fillCatalog(digest, catalogs);

    }

    private void fillCatalog(Forum forum, ArrayList<DigestCatalog> catalogs) {
        for (DigestCatalog catalog : catalogs) {

            if (!catalog.getParent().getId().equals(forum.getId())) continue;

            Forum f = new Forum(catalog.getId().toString(), catalog.getTitle().toString());
            f.setHtmlTitle(catalog.getHtmlTitle());
            f.setTag(catalog.getType() == DigestCatalog.TYPE_GAMES ? "game" : "app");
            forum.addForum(f);

            if (catalog.getId().equals(catalog.getParent().getId())) continue;// каталог@темы
            fillCatalog(f, catalogs);
        }
    }



}