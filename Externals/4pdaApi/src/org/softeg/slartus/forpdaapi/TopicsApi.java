package org.softeg.slartus.forpdaapi;

import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdacommon.Functions;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by slartus on 02.06.2014.
 */
public class TopicsApi {
    public static ArrayList<FavTopic> getFavTopics(IHttpClient client,
                                                ListInfo listInfo,
                                                OnProgressChangedListener progressChangedListener) throws ParseException, IOException, URISyntaxException {
        return getFavTopics(client, listInfo, false, progressChangedListener);

    }

    public static ArrayList<FavTopic> getFavTopics(IHttpClient client,

                                                ListInfo listInfo, Boolean fullPagesList,
                                                OnProgressChangedListener progressChangedListener) throws ParseException, IOException, URISyntaxException {
        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("act", "fav"));
        qparams.add(new BasicNameValuePair("st", Integer.toString(listInfo.getFrom())));
        qparams.add(new BasicNameValuePair("type", "topics"));

        URI uri = URIUtils.createURI("http", "4pda.ru", -1, "/forum/index.php",
                URLEncodedUtils.format(qparams, "UTF-8"), null);
        String pageBody = client.performGetWithCheckLogin(uri.toString(), progressChangedListener, progressChangedListener);

        Document doc = Jsoup.parse(pageBody);

        Matcher m = PatternExtensions.compile("<a href=\"/forum/index.php\\?act=[^\"]*?st=(\\d+)\">&raquo;</a>").matcher(pageBody);
        if (m.find()) {
            listInfo.setOutCount(Integer.parseInt(m.group(1)) + 1);
        }

        Pattern lastPostPattern = Pattern.compile("<a href=\"[^\"]*view=getlastpost[^\"]*\">Послед.:</a>\\s*<a href=\"/forum/index.php\\?showuser=\\d+\">(.*?)</a>(.*?)$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Pattern trackTypePattern = Pattern.compile("wr_fav_subscribe\\(\\d+,\"(\\w+)\"\\);",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        ArrayList<FavTopic> res = new ArrayList<>();
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();

        for (Element topicElement : doc.select("div[data-item-fid]")) {
            Elements elements = topicElement.select("div.topic_title");
            if (elements.size() == 0) continue;
            Element topicTitleDivElement = elements.first();
            elements = topicTitleDivElement.select("a");
            if (elements.size() == 0) continue;
            Element element = elements.first();
            Uri ur = Uri.parse(element.attr("href"));
            String tId = topicElement.attr("data-item-fid");

            String trackType=null;
            elements = topicElement.select("div.topic_body");
            if (elements.size() > 0) {
                String html=elements.first().html();
                m=trackTypePattern.matcher(html);
                if(m.find())
                    trackType=m.group(1);
            }

            if (TextUtils.isEmpty(ur.getQueryParameter("showtopic"))) {
                FavTopic topic = new FavTopic(null, topicTitleDivElement.text());
                topic.setTid(tId);
                topic.setTrackType(trackType);
                topic.setDescription("Форум");
                //topic.setSortOrder(Integer.toString(sortOrder++));
                res.add(topic);
                continue;
            }


            String id = ur.getQueryParameter("showtopic");
            String title = element.text();
            FavTopic topic = new FavTopic(id, title);
            topic.setTid(tId);
            topic.setTrackType(trackType);
            elements = topicElement.select("div.topic_body");
            if (elements.size() > 0) {
                Element topicBodyDivElement = elements.first();
                elements = topicBodyDivElement.select("span.topic_desc");
                if (elements.size() > 0)
                    topic.setDescription(elements.first().text());
                String text = topicBodyDivElement.html();
                topic.setIsNew(text.contains("view=getnewpost"));

                m = lastPostPattern.matcher(text);
                if (m.find()) {
                    topic.setLastMessageDate(Functions.parseForumDateTime(m.group(2), today, yesterday));
                    topic.setLastMessageAuthor(m.group(1));
                }
                //topic.setSortOrder(Integer.toString(sortOrder++));
                res.add(topic);
            }
        }
        if (fullPagesList) {
            while (true) {
                if (listInfo.getOutCount() <= res.size())
                    break;
                listInfo.setFrom(res.size());
                ArrayList<FavTopic> nextPageTopics = getFavTopics(client, listInfo, true, progressChangedListener);
                if (nextPageTopics.size() == 0)
                    break;
                res.addAll(nextPageTopics);
            }
        }

        if (res.size() == 0) {
            m = PatternExtensions.compile("<div class=\"errorwrap\">([\\s\\S]*?)</div>")
                    .matcher(pageBody);
            if (m.find()) {
                throw new NotReportException(Html.fromHtml(m.group(1)).toString(), new Exception(Html.fromHtml(m.group(1)).toString()));
            }
        }
        return res;
    }
}
