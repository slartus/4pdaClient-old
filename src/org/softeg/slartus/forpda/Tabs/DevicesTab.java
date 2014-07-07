package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.DevDbDeviceActivity;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 24.11.11
 * Time: 9:56
 */
public class DevicesTab extends TreeTab {
    public DevicesTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent, TEMPLATE);
        // m_CurrentItem = new Forum("-1", "DevDB.ru");
    }

    public static final String BRAND_ID = "BrandId";
    public static final String DEVICE_TYPE = "DeviceType";
    public static final String TEMPLATE = Tabs.TAB_DEVDB;
    public static final String TITLE = "DevDB.ru";

    @Override
    public Boolean cachable() {
        return true;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    private String m_BrandId = null;
    private String m_DeviceType = null;

    @Override
    public void refresh(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(BRAND_ID))
            m_BrandId = savedInstanceState.getString(BRAND_ID);
        if (savedInstanceState != null && savedInstanceState.containsKey(DEVICE_TYPE))
            m_DeviceType = savedInstanceState.getString(DEVICE_TYPE);

        if (!TextUtils.isEmpty(m_BrandId) && !TextUtils.isEmpty(m_DeviceType)) {
            Forum forum = new Forum(m_DeviceType + "/" + m_BrandId, m_BrandId);
            Forum deviceForum = new Forum(m_DeviceType, m_DeviceType);
            deviceForum.setParent(m_Digest);
            m_Digest.addForum(deviceForum);

            forum.setParent(deviceForum);
            deviceForum.addForum(forum);

            m_CurrentItem = forum;


            m_DeviceType = null;
            m_BrandId = null;


        } else if (!TextUtils.isEmpty(m_DeviceType)) {
            Forum forum = new Forum(m_DeviceType, m_DeviceType);

            forum.setParent(m_Digest);
            m_Digest.addForum(forum);
            m_CurrentItem = forum;
            m_CurrentItem.addForum(new Forum("", ""));

            m_DeviceType = null;
            m_BrandId = null;

        }

        super.refresh(savedInstanceState);

    }

    @Override
    public Boolean onParentBackPressed() {
        if (m_CurrentItem == null || m_CurrentItem.getParent() == null) {
            return false;
        }
        m_CurrentItem.clearChildren();
        if (m_CurrentItem.getParent() != null && m_CurrentItem.getParent().getForums().size() == 1) {
            loadForums(m_CurrentItem.getParent());
            return true;
        }
        showForum(m_CurrentItem.getParent());
        return true;
    }

    @Override
    protected void loadForum(Forum forum, OnProgressChangedListener progressChangedListener) throws Throwable {
        switch (forum.level) {
            case 0:
                parseDevicesTypes(forum, progressChangedListener);
                break;
            case 1:
                parseDevicesBrands(forum, progressChangedListener);
                break;
            case 2:
                //parseModels(forum, progressChangedListener);
                break;

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final ExtTopic topic = m_ThemeAdapter.getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;
        ExtUrl.addUrlMenu(getHandler(), getContext(), menu, "http://devdb.ru/" + topic.getId(), topic.getId(),
                topic.getTitle());
    }

    @Override
    protected void getThemes(OnProgressChangedListener progressChangedListener) throws Throwable {
        parseModels(m_ForumForLoadThemes, progressChangedListener);
    }

    @Override
    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getContext(), i, l);

        if ("ForumsAdapter".equals(m_CurrentAdapter)) {
            if (l < 0 || m_ForumsAdapter.getCount() <= l) return;
            Forum f = m_ForumsAdapter.getItem((int) l);
            if (f.hasChildForums())
                showForum(f);
            else
                loadForums(f);
        } else {
            if (l < 0 || m_ThemeAdapter.getCount() <= l) return;
            ExtTopic topic = m_ThemeAdapter.getItem((int) l);

            DevDbDeviceActivity.showDevice(getContext(), topic.getId());
        }
    }


    public void parseDevicesTypes(Forum forum, OnProgressChangedListener progressChangedListener) throws Throwable {
        String pageBody = performGet("http://devdb.ru", progressChangedListener);

        Pattern pattern = Pattern.compile("<a href=\"http://devdb.ru/(.*?)/\">.*?<br /><br />(.*?)</a></p>");
        Matcher m = pattern.matcher(pageBody);
        while (m.find()) {
            Forum f = new Forum(m.group(1), Html.fromHtml(m.group(2)).toString());
            forum.addForum(f);
        }
    }

    public void parseDevicesBrands(Forum forum, OnProgressChangedListener progressChangedListener) throws Throwable {
        String pageBody = performGet("http://devdb.ru/" + forum.getId(), progressChangedListener);

        Pattern pattern = Pattern.compile("<li><a href=\"http://devdb.ru/(.*?)\">(.*?)</a></li>");
        Matcher m = pattern.matcher(pageBody);
        while (m.find()) {
            Forum f = new Forum(m.group(1), m.group(2));
            forum.addForum(f);
        }
    }

    public void parseModels(Forum forum, OnProgressChangedListener progressChangedListener) throws Throwable {

        String pageBody = performGet("http://devdb.ru/" + forum.getId(), progressChangedListener);

        Pattern pattern = Pattern.compile("<li><a href=\"http://devdb.ru/(.*?)\">(.*?)</a></li>");
        Matcher m = pattern.matcher(pageBody);
        while (m.find()) {
            ExtTopic f = new ExtTopic(m.group(1), m.group(2));
            forum.addTheme(f);
        }
    }

    public static String performGet(String url, OnProgressChangedListener progressChangedListener) throws Throwable {
        progressChangedListener.onProgressChanged("Получение данных...");
        String pageBody = Client.getInstance().performGet(url);
        progressChangedListener.onProgressChanged("Обработка данных...");

        return pageBody;
    }
}
