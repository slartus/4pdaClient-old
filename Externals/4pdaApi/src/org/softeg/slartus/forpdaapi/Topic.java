package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.softeg.slartus.forpdacommon.Functions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 12:06
 */
public class Topic implements IListItem {
    /**
     * Параметр для перехода в топике к первому непрочитанному сообщению
     */
    public static final CharSequence NAVIGATE_VIEW_NEW_POST = "getnewpost";

    /**
     * Параметр для перехода в топике к первому сообщению
     */
    public static final CharSequence NAVIGATE_VIEW_FIRST_POST = "getfirstpost";

    /**
     * Параметр для перехода в топике к последнему сообщению
     */
    public static final CharSequence NAVIGATE_VIEW_LAST_POST = "getlastpost";

    protected String m_Id;


    private String m_SpannedTitle;
    private Date lastMessageDate;
    private boolean isNew = false;


    public String getId() {
        return m_Id;
    }

    @Override
    public CharSequence getTopLeft() {
        return m_SpannedLastMessageAuthor;
    }

    @Override
    public CharSequence getTopRight() {
        return m_lastMessageDateStr;
    }

    @Override
    public CharSequence getMain() {
        return m_SpannedTitle;
    }

    @Override
    public CharSequence getSubMain() {
        return m_Description;
    }

    @Override
    public int getState() {
        if (getIsNew())
            return STATE_GREEN;
        return STATE_NORMAL;
    }

    @Override
    public void setState(int state) {
        switch (state) {
            case STATE_GREEN:
                setIsNew(true);
                break;
            default:
                setIsNew(false);
                break;
        }
    }

    public void setId(String value) {
        m_Id = value;
    }

    public Topic() {

    }

    public Topic(String id, String title) {
        this();
        m_Id = id;
        m_SpannedTitle = Html.fromHtml(title).toString();
    }

    public String getTitle() {

        return m_SpannedTitle;
    }

    public void setTitle(String title) {
        setTitle(title, true);
    }

    public void setTitle(String title, Boolean fromHtml) {
        if (fromHtml && title != null) {
            m_SpannedTitle = Html.fromHtml(title).toString();
        } else {
            m_SpannedTitle = title;
        }
    }

    private String m_SpannedLastMessageAuthor;

    public String getLastMessageAuthor() {
        return m_SpannedLastMessageAuthor;
    }

    public void setLastMessageAuthor(String lastMessageAuthor) {
        setLastMessageAuthor(lastMessageAuthor, true);
    }

    public void setLastMessageAuthor(String lastMessageAuthor, Boolean fromHtml) {
        if (fromHtml && lastMessageAuthor != null) {
            m_SpannedLastMessageAuthor = Html.fromHtml(lastMessageAuthor).toString();
        } else {
            m_SpannedLastMessageAuthor = lastMessageAuthor;
        }
    }

    private String m_lastMessageDateStr = null;

    public CharSequence getLastMessageDateStr() {
        return m_lastMessageDateStr;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        setLastMessageDate(lastMessageDate, null);
    }

    public void setLastMessageDate(Date lastMessageDate, SimpleDateFormat parseDateTimeFormat) {
        this.lastMessageDate = lastMessageDate;
        if (lastMessageDate == null) {
            lastMessageDate = new Date();
        }
        if (parseDateTimeFormat != null)
            m_lastMessageDateStr = parseDateTimeFormat.format(lastMessageDate);
        else
            m_lastMessageDateStr = Functions.getForumDateTime(lastMessageDate);
    }


    private String m_Description;

    public String getDescription() {
        return m_Description;
    }

    public void setDescription(String description) {
        setDescription(description, true);
    }

    public void setDescription(String description, Boolean fromHtml) {
        if (fromHtml && description != null) {

            m_Description = Html.fromHtml(description).toString().trim();
        } else {
            m_Description = description;
        }
    }

    public void setIsNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean getIsNew() {
        return isNew;
    }

    /**
     * Кто читает тему
     *
     * @param httpClient
     * @param topicId
     * @return
     * @throws IOException
     */
    public static TopicReadingUsers getReadingUsers(IHttpClient httpClient, String topicId) throws IOException {
        String body = httpClient.performGetFullVersion("http://4pda.ru/forum/index.php?showtopic=" + topicId);

        Matcher m = Pattern.compile("<a href=\".*?/forum/index.php\\?showuser=(\\d+)\" title=\"(.*?)\"><span style='color:(.*?)'>(.*?)</span></a>").matcher(body);
        TopicReadingUsers res = new TopicReadingUsers();
        while (m.find()) {
            User user = new User();
            user.setMid(m.group(1));
            user.setNick(m.group(4));
            user.setHtmlColor(m.group(3));
            res.add(user);
        }
        m = Pattern.compile("<div class=\"formsubtitle\" style=\"padding: 4px;\"><b>\\d+</b> чел. читают эту тему \\(гостей: (\\d+), скрытых пользователей: (\\d+)\\)</div>")
                .matcher(body);
        if (m.find()) {
            res.setGuestsCount(m.group(1));
            res.setHideCount(m.group(2));
        }
        return res;
    }

    public static Users getWriters(IHttpClient httpClient, String topicId) throws IOException {
        String body = httpClient.performGet("http://4pda.ru/forum/index.php?s=&act=Stats&CODE=who&t=" + topicId);

        Matcher m = Pattern.compile("showuser=(\\d+).*?>(.*?)</a>[\\s\\S]*?>(\\d+)</td>").matcher(body);
        Users res = new Users();
        while (m.find()) {
            User user = new User();
            user.setMid(m.group(1));
            user.setNick(m.group(2));
            user.MessagesCount = m.group(3);
            res.add(user);
        }
        m = Pattern.compile("<div class=\"maintitle\" align=\"center\">(.*?)</div>").matcher(body);
        if (m.find())
            res.setTag(m.group(1));
        return res;
    }


    public void setForumTitle(String s) {
        setDescription(s);
    }


}
