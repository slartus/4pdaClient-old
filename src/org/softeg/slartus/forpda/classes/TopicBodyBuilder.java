package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.common.HtmlUtils;
import org.softeg.slartus.forpda.emotic.Smiles;
import org.softeg.slartus.forpda.prefs.HtmlPreferences;

import java.util.Hashtable;

/**
 * User: slinkin
 * Date: 26.03.12
 * Time: 16:50
 */
public class TopicBodyBuilder extends HtmlBuilder {

    private Boolean m_Logined, m_IsWebviewAllowJavascriptInterface;
    private ExtTopic m_Topic;
    private String m_UrlParams, m_PostBody;
    private TopicAttaches m_TopicAttaches = new TopicAttaches();
    private HtmlPreferences m_HtmlPreferences;
    private Hashtable<String, String> m_EmoticsDict;

    public TopicBodyBuilder(Context context, Boolean logined, ExtTopic topic, String urlParams,
                            Boolean isWebviewAllowJavascriptInterface) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        m_HtmlPreferences = new HtmlPreferences();
        m_HtmlPreferences.load(context);
        m_EmoticsDict = Smiles.getSmilesDict();

        m_IsWebviewAllowJavascriptInterface = isWebviewAllowJavascriptInterface;
        m_Logined = logined;
        m_UrlParams = urlParams;
        m_Topic = topic;
    }

    public void beginTopic() {
        String desc = TextUtils.isEmpty(m_Topic.getDescription()) ? "" : (", " + m_Topic.getDescription());
        super.beginHtml(m_Topic.getTitle() + desc);
        super.beginBody();

        if (m_Topic.getPagesCount() > 1) {
            addButtons(m_Body, m_Topic.getCurrentPage(), m_Topic.getPagesCount(),
                    m_IsWebviewAllowJavascriptInterface, false, true);
        }

        m_Body.append(getTitleBlock());
    }

    public void endTopic() {
        m_Body.append("<div name=\"entryEnd\" id=\"entryEnd\"></div>\n");
        m_Body.append("<br/><br/>");
        if (m_Topic.getPagesCount() > 1) {
            addButtons(m_Body, m_Topic.getCurrentPage(), m_Topic.getPagesCount(),
                    m_IsWebviewAllowJavascriptInterface, false, false);
        }

        m_Body.append("<br/><br/>");
//        addPostForm(m_Body);

        m_Body.append("<div id=\"viewers\"><a " + getHtmlout(m_IsWebviewAllowJavascriptInterface, "showReadingUsers") + " class=\"href_button\">Кто читает тему..</a></div><br/>\n");
        m_Body.append("<div id=\"writers\"><a " + getHtmlout(m_IsWebviewAllowJavascriptInterface, "showWriters") + " class=\"href_button\">Кто писал сообщения..</a></div><br/><br/>\n");
        m_Body.append(getTitleBlock());


        m_Body.append("<br/><br/><br/><br/><br/><br/>\n");
        super.endBody();
        super.endHtml();
    }

    public void addPost(Post post, Boolean spoil, Boolean first) {

        m_Body.append("<div name=\"entry" + post.getId() + "\" id=\"entry" + post.getId() + "\"></div>\n");

        addPostHeader(m_Body, post, post.getId());

        m_Body.append("<div id=\"msg" + post.getId() + "\" name=\"msg" + post.getId() + "\">");

        if (spoil) {
            if (m_HtmlPreferences.isSpoilerByButton())
                m_Body.append("<div class='hidetop' style='cursor:pointer;' ><b>( &gt;&gt;&gt;ШАПКА ТЕМЫ&lt;&lt;&lt;)</b></div>" +
                        "<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility(this)\"/>" +
                        "<div class='hidemain' style=\"display:none\">");
            else
                m_Body.append("<div class='hidetop' style='cursor:pointer;' " +
                        "onclick=\"var _n=this.parentNode.getElementsByTagName('div')[1];" +
                        "if(_n.style.display=='none'){_n.style.display='';}else{_n.style.display='none';}\">" +
                        "Спойлер (+/-) <b>( &gt;&gt;&gt;ШАПКА ТЕМЫ&lt;&lt;&lt;)</b></div><div class='hidemain' style=\"display:none\">");
        }
        String postBody = post.getBody().trim();
        if (m_HtmlPreferences.isSpoilerByButton()) {

            postBody = HtmlPreferences.modifySpoiler(postBody);
        }
        //m_TopicAttaches.parseAttaches(post.getId(),post.getNumber(),postBody);
        m_Body.append(postBody);
        if (spoil)
            m_Body.append("</div>");
        m_Body.append("</div>\n\n");
        //m_Body.append("<div class=\"s_post_footer\"><table width=\"100%%\"><tr><td id=\""+post.getId()+"\"></td></tr></table></div>\n\n");

        addFooter(m_Body, post, "End", first);
        m_Body.append("<div class=\"between_messages\"></div>");
    }

    public String getBody() {
        String res;
        if (m_HtmlPreferences.isUseLocalEmoticons()) {
            res = HtmlPreferences.modifyStyleImagesBody(m_Body.toString());
            res = HtmlPreferences.modifyEmoticons(res, m_EmoticsDict, true);
        } else {
            res = HtmlPreferences.modifyEmoticons(m_Body.toString(), m_EmoticsDict, false);
        }
        if (!WebViewExternals.isLoadImages("theme"))
            res = HtmlPreferences.modifyAttachedImagesBody(m_IsWebviewAllowJavascriptInterface, res);
        return res;
    }

    public void addBody(String value) {
        m_Body.append(value);
    }

    public TopicAttaches getTopicAttaches() {
        return m_TopicAttaches;
    }

    public void clear() {
        m_Topic = null;
        m_Body = null;
    }

    private String getTitleBlock() {
        String desc = TextUtils.isEmpty(m_Topic.getDescription()) ? "" : (", " + m_Topic.getDescription());
        return "<div class=\"topic_title_post\"><a href=\"http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + (TextUtils.isEmpty(m_UrlParams) ? "" : ("&" + m_UrlParams)) + "\">" + m_Topic.getTitle() + desc + "</a></div>\n";
    }

    public static void addButtons(StringBuilder sb, int currentPage, int pagesCount, Boolean isUseJs,
                                  Boolean useSelectTextAsNumbers, Boolean top) {
        Boolean prevDisabled = currentPage == 1;
        Boolean nextDisabled = currentPage == pagesCount;
        sb.append("<div class=\"navi\" id=\"" + (top ? "top_navi" : "bottom_navi") + "\">\n");
        sb.append("<div class=\"first\"><a " + (prevDisabled ? "#" : getHtmlout(isUseJs, "firstPage")) + " class=\"href_button" + (prevDisabled ? "_disable" : "") + "\">&lt;&lt;</a></div>\n");
        sb.append("<div class=\"prev\"><a " + (prevDisabled ? "#" : getHtmlout(isUseJs, "prevPage")) + " class=\"href_button" + (prevDisabled ? "_disable" : "") + "\">  &lt;  </a></div>\n");
        String selectText = useSelectTextAsNumbers ? (currentPage + "/" + pagesCount) : "Выбор";
        sb.append("<div class=\"page\"><a " + getHtmlout(isUseJs, "jumpToPage") + " class=\"href_button\">" + selectText + "</a></div>\n");
        sb.append("<div class=\"next\"><a " + (nextDisabled ? "#" : getHtmlout(isUseJs, "nextPage")) + " class=\"href_button" + (nextDisabled ? "_disable" : "") + "\">  &gt;  </a></div>\n");
        sb.append("<div class=\"last\"><a " + (nextDisabled ? "#" : getHtmlout(isUseJs, "lastPage")) + " class=\"href_button" + (nextDisabled ? "_disable" : "") + "\">&gt;&gt;</a></div>\n");
        sb.append("</div>\n");

    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String val1, String val2) {
        return getHtmlout(webViewAllowJs, methodName, new String[]{val1, val2});
    }

    private static String getHtmlout(Boolean webViewAllowJs, String methodName, String val1) {
        return getHtmlout(webViewAllowJs, methodName, new String[]{val1});
    }

    private String getHtmlout(String methodName) {
        return getHtmlout(m_IsWebviewAllowJavascriptInterface, methodName, new String[0]);
    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName) {
        return getHtmlout(webViewAllowJs, methodName, new String[0]);
    }


    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String[] paramValues) {
        return getHtmlout(webViewAllowJs, methodName, paramValues, true);
    }

    public static String getHtmlout(Boolean webViewAllowJs, String methodName, String[] paramValues, Boolean modifyParams) {
        StringBuilder sb = new StringBuilder();
        if (!webViewAllowJs) {
            sb.append("href=\"http://www.HTMLOUT.ru/");
            sb.append(methodName + "?");
            int i = 0;

            for (String paramName : paramValues) {
                sb.append("val" + i + "=" + (modifyParams ? Uri.encode(paramName) : paramName) + "&");
                i++;
            }

            sb = sb.delete(sb.length() - 1, sb.length());
            sb.append("\"");
        } else {

            sb.append(" onclick=\"window.HTMLOUT." + methodName + "(");
            for (String paramName : paramValues) {
                sb.append("'"
                        + HtmlUtils.modifyHtmlQuote(paramName).replace("'", "\\'").replace("\"", "&quot;") + "',");
            }
            if (paramValues.length > 0)
                sb.delete(sb.length() - 1, sb.length());
            sb.append(")\"");
        }
        return sb.toString();
    }

    private void addPostHeader(StringBuilder sb, Post msg, String msgId) {
        String nick = msg.getNick();
//nick="\"~!@#$%^&*()<>'/{}[]\\\\`&#377;micier2\"";
        String nickLink = nick;
        if (!TextUtils.isEmpty(msg.getUserId())) {
            nickLink = "<a " +
                    getHtmlout(m_IsWebviewAllowJavascriptInterface,
                            "showUserMenu",
                            msg.getUserId(),
                            nick)
                    + " class=\"system_link\">" + nick + "</a>";
        }


        String userState = msg.getUserState() ? "post_nick_online_cli" : "post_nick_cli";

        sb.append("<div class=\"post_header\">\n");
        sb.append("\t<table width=\"100%\">\n");
        sb.append("\t\t<tr><td><span class=\"" + userState + "\">" + nickLink + "</span></td>\n");
        sb.append("\t\t\t<td><div align=\"right\"><span class=\"post_date_cli\">" + msg.getDate() + "|<a "
                + getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostLinkMenu", msg.getId()) + ">#" + msg.getNumber() + "</a></span></div></td>\n");
        sb.append("\t\t</tr>\n");
        String userGroup = msg.getUserGroup() == null ? "" : msg.getUserGroup();
        sb.append("<tr>\n" +
                "\t\t\t<td colspan=\"2\"><span  class=\"user_group\">" + userGroup + "</span></td></tr>");
        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>" + (TextUtils.isEmpty(msg.getUserId()) ? "" : getReputation(msg)) + "</td>\n");
        if (Client.getInstance().getLogined())
            sb.append("\t\t\t<td><div align=\"right\"><a " + getHtmlout(m_IsWebviewAllowJavascriptInterface, "showPostMenu", new String[]{msgId, msg.getCanEdit() ? "1" : "0", (msg.getCanDelete() ? "1" : "0")})
                    + " class=\"system_link\">меню</a></div></td>");
        sb.append("\t\t</tr>");
        sb.append("\t</table>\n");
        sb.append("</div>\n");
    }

    private String getReputation(Post msg) {
        String[] params = new String[]{msg.getId(), msg.getUserId(), msg.getNick(), msg.getCanPlusRep() ? "1" : "0", msg.getCanMinusRep() ? "1" : "0"};
        String rep = "<a " + getHtmlout(m_IsWebviewAllowJavascriptInterface, "showRepMenu", params) + "  class=\"system_link\" ><span class=\"post_date_cli\">Реп(" + msg.getUserReputation() + ")</span></a>";
//        if (!msg.getCanMinusRep() || !msg.getCanPlusRep())
        return rep;
    }

    private void addFooter(StringBuilder sb, Post post, String lastMessageId, Boolean first) {

        String style = MyApp.getInstance().getCurrentThemeName();
        sb.append("<div class=\"post_footer\"><table width=\"100%\"><tr>");

        sb.append("<td width=\"50\"><a href=\"javascript:scroll(0,0);\" class=\"system_link\"><span class=\"post_date_cli\">вверх</span></a></td>");

        sb.append("<td width=\"50\"><a href=\"javascript:scrollToElement('entry" + lastMessageId + "');\" class=\"system_link\"><span class=\"post_date_cli\">вниз</span></a></td>");
        if (m_Logined) {
            sb.append("<td></td>");
            String params = "'" + post.getId() + "','" + post.getDate() + "','" + post.getNick() + "'";

            sb.append("<td><div style=\"text-align:right\"><a class=\"system_link\" href=\"/forum/index.php?act=Post&amp;CODE=02&amp;f=" + m_Topic.getForumId()
                    + "&amp;t=" + m_Topic.getId() + "&amp;qpid=" + post.getId() + (first ? "&amp;first=1" : "&amp;first=0") + "\" >цитата</a></div></td>");
        }
        sb.append("</tr></table></div>\n\n");
    }

    public ExtTopic getTopic() {
        return m_Topic;
    }
}
