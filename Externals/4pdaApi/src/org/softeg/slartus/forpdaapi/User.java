package org.softeg.slartus.forpdaapi;

import android.text.Html;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:56
 */
public class User {
    private String nick = null;
    private String mid = null;
    private String tag = null;
    public String MessagesCount = null;
    public String State = null;
    public String LastVisit = null;
    public String Group = null;

    public String getNick() {
        return nick;
    }

    private String htmlColor = "gray";

    public String getHtmlColor() {
        return htmlColor;
    }

    public void setHtmlColor(String htmlColor) {
        this.htmlColor = htmlColor;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }


    /**
     * Изменение репутации пользователя
     *
     * @param httpClient
     * @param postId     Идентификатор поста, за который поднимаем репутацию. 0 - "в профиле"
     * @param userId
     * @param type       "add" - поднять, "minus" - опустить
     * @param message
     * @return Текст ошибки или пустая строка в случае успеха
     * @throws IOException
     */
    public static Boolean changeReputation(IHttpClient httpClient, String postId, String userId, String type, String message,
                                           Map<String, String> outParams) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "rep");
        additionalHeaders.put("p", postId);
        additionalHeaders.put("mid", userId);
        additionalHeaders.put("type", type);
        additionalHeaders.put("message", message);

        String res = httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

        Pattern p = Pattern.compile("<title>(.*?)</title>");
        Matcher m = p.matcher(res);
        if (m.find()) {
            if (m.group(1) != null && m.group(1).contains("Ошибка")) {
                p = Pattern.compile("<div class='maintitle'>(.*?)</div>");
                m = p.matcher(res);
                if (m.find()) {
                    outParams.put("Result", "Ошибка изменения репутации: " + m.group(1));
                } else {
                    outParams.put("Result", "Ошибка изменения репутации: " + Html.fromHtml(res));
                }

                return false;
            }
            outParams.put("Result", "Репутация: " + m.group(1));
            return true;
        }
        outParams.put("Result", "Репутация изменена");
        return true;

    }

    /**
     * Загружает историю репутации пользователя
     *
     * @param httpClient
     * @param self          - действия пользователя с репутацией других пользователей
     * @param reputations   Массив уже загруженных изменений репутации. Догружает с того количества, которое уже в массиве
     * @param beforeGetPage
     * @param afterGetPage
     * @throws IOException
     */
    public static void loadReputation(IHttpClient httpClient, String userId, Boolean self, Reputations reputations,
                                      OnProgressChangedListener beforeGetPage, OnProgressChangedListener afterGetPage) throws IOException {

        String body = httpClient.performGetWithCheckLogin("http://4pda.ru/forum/index.php?act=rep&type=history&mid="
                + userId + "&st=" + reputations.size() + (self ? "&mode=from" : ""), beforeGetPage, afterGetPage);

        reputations.userId = userId;
        Pattern pattern;
        Matcher m = Pattern.compile("<div class='maintitle'>(История репутации участника (.*?) \\[\\+\\d+/-\\d+])<div").matcher(body);

        if (m.find()) {
            reputations.description = m.group(1);
            reputations.user = m.group(2);
        }

        if (reputations.fullListCount == 0) {
            pattern = Pattern.compile("parseInt\\((\\d+)/\\d+\\)");
            m = pattern.matcher(body);
            if (m.find())
                reputations.fullListCount = Integer.parseInt(m.group(1));
        }

        pattern = Pattern.compile("\\s*<td class='row2' align='left'><b><a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*)</a></b></td>\n" +
                "\\s*<td class='row2' align='left'>(<b>)?(<a href='(.*)'>)?(.*?)(</a>)?(</b>)?</td>\n" +
                "\\s*<td class='row2' align='left'>(.*?)</td>\n" +
                "\\s*<td class='row1' align='center'><img border='0' src='style_images/1/(.*?).gif' /></td>\n" +
                "\\s*<td class='row1' align='center'>(.*)</td>", Pattern.MULTILINE);
        m = pattern.matcher(body);

        while (m.find()) {
            Reputation rep = new Reputation();
            rep.userId = m.group(1);
            rep.user = m.group(2);
            rep.sourceUrl = m.group(5);
            rep.source = Html.fromHtml(m.group(6));
            rep.description = Html.fromHtml(m.group(9));
            rep.level = m.group(10);
            rep.date = m.group(11);
            reputations.add(rep);
        }


    }


    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
