package org.softeg.slartus.forpdaapi;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by slartus on 02.06.2014.
 */
public class TopicApi {
    /**
     * не уведомлять
     */
    public static final String TRACK_TYPE_NONE = "none";
    /**
     * Первый раз
     */
    public static final String TRACK_TYPE_DELAYED = "delayed";
    /**
     * Каждый раз
     */
    public static final String TRACK_TYPE_IMMEDIATE = "immediate";
    /**
     * Каждый день
     */
    public static final String TRACK_TYPE_DAILY = "daily";
    /**
     * Каждую неделю
     */
    public static final String TRACK_TYPE_WEEKLY = "weekly";
    /**
     * Удалить
     */
    public static final String TRACK_TYPE_DELETE = "delete";
    /**
     * закрепить
     */
    public static final String TRACK_TYPE_PIN = "pin";
    /**
     * открепить
     */
    public static final String TRACK_TYPE_UNPIN = "unpin";
    public static String changeFavorite(IHttpClient httpClient, CharSequence topicId, String trackType) throws IOException, URISyntaxException, ParseException {
        FavTopic favTopic = findTopicInFav(httpClient, topicId);

        Boolean exists = favTopic != null;
        if (favTopic == null && TRACK_TYPE_DELETE.equals(trackType)) {
            return "Тема не найдена в избранном";
        }
        if (favTopic != null && trackType.equals(favTopic.getTrackType())) {
            return "Тема уже в избранном с этим типом подписки";
        }

        List<NameValuePair> qparams = new ArrayList<>();
        if (exists) {
            qparams.add(new BasicNameValuePair("act", "fav"));
            qparams.add(new BasicNameValuePair("selectedtids", favTopic.getTid()));
            qparams.add(new BasicNameValuePair("tact", trackType));
        }else{
            qparams.add(new BasicNameValuePair("act", "fav"));
            qparams.add(new BasicNameValuePair("type", "add"));
            qparams.add(new BasicNameValuePair("t", topicId.toString()));
            qparams.add(new BasicNameValuePair("track_type", trackType));
        }

        URI uri = URIUtils.createURI("http", "4pda.ru", -1, "/forum/index.php",
                URLEncodedUtils.format(qparams, "UTF-8"), null);
        httpClient.performGet(uri.toString());
        favTopic = findTopicInFav(httpClient, topicId);
        if (favTopic != null && trackType.equals(favTopic.getTrackType())) {
            if (exists) {
                if (TRACK_TYPE_NONE.equals(trackType))
                    return "Подписка на тему отменена";
                return "Подписка на тему изменена";
            }
            if (TRACK_TYPE_NONE.equals(trackType))
                return "Тема успешно добавлена в избранное";
            return "Подписка на тему оформлена";
        }

        if (favTopic != null && !trackType.equals(favTopic.getTrackType())) {
            if (exists)
                return "Что-то пошло не так. Подписка на тему не была изменена!";
            return "Что-то пошло не так. Подписка на тему не применена!";
        }
        if (favTopic == null && TRACK_TYPE_DELETE.equals(trackType)) {
            return "Тема удалена из избранного";
        }
        return "Ошибка добавления темы в избранное";
    }

    private static FavTopic findTopicInFav(IHttpClient httpClient, CharSequence topicId) throws ParseException, IOException, URISyntaxException {
        ListInfo listInfo = new ListInfo();
        listInfo.setFrom(0);
        int topicsCount = 0;
        while (true) {
            ArrayList<FavTopic> topics = TopicsApi.getFavTopics(httpClient, listInfo, null);

            for (FavTopic topic : topics) {
                if (!topicId.equals(topic.getId())) continue;
                return topic;
            }
            topicsCount += topics.size();
            if (listInfo.getOutCount() <= topicsCount)
                break;
            listInfo.setFrom(topicsCount);
        }
        return null;
    }

    public static String deleteFromFavorites(IHttpClient httpClient, String id) throws ParseException, IOException, URISyntaxException {
        return changeFavorite(httpClient,id,TRACK_TYPE_DELETE);
    }

    public static String pinFavorite(IHttpClient httpClient, String topicId, String trackType) throws ParseException, IOException, URISyntaxException {
        FavTopic favTopic = findTopicInFav(httpClient, topicId);

        Boolean exists = favTopic != null;
        if (favTopic == null) {
            return "Тема не найдена в избранном";
        }

        List<NameValuePair> qparams = new ArrayList<>();
        qparams.add(new BasicNameValuePair("act", "fav"));
        qparams.add(new BasicNameValuePair("selectedtids", favTopic.getTid()));
        qparams.add(new BasicNameValuePair("tact", trackType));
        URI uri = URIUtils.createURI("http", "4pda.ru", -1, "/forum/index.php",
                URLEncodedUtils.format(qparams, "UTF-8"), null);
        httpClient.performGet(uri.toString());
        return TRACK_TYPE_PIN.equals(trackType)?"Тема закреплена":"Тема откреплена";
    }
}
