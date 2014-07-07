package org.softeg.slartus.forpda.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.softeg.slartus.forpda.Tabs.TabDataSettingsActivity;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdacommon.UrlExtensions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 23.10.12
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class SearchSettings {

    private Context m_Context;
    private String m_TabTag;
    private String m_TopicId;
    private Boolean m_IsSearchInTopic = false;

    public SearchSettings(Context context, String tabTag) {

        m_Context = context;
        m_TabTag = tabTag;
    }

    private ArrayList<String> m_CheckedIds = new ArrayList<String>();
    private String m_Query;

    public String getQuery() {
        return m_Query;
    }

    private String m_UserName;

    public String getUserName() {
        return m_UserName;
    }

    private String m_Source = "all";

    public String getSource() {
        return m_Source;
    }

    private String m_Name = "Поиск";

    public String getName() {
        return m_Name;
    }

    private String m_Sort = "dd";

    public String getSort() {
        return m_Sort;
    }

    private Boolean m_Subforums = false;
    private Boolean m_ResultsInTopicView = false;

    public Boolean Subforums() {
        return m_Subforums;
    }

    public ArrayList<String> getCheckedIds() {
        return m_CheckedIds;

    }

    public Boolean isSearchInTopic() {
        return m_IsSearchInTopic;
    }

    public String getSearchQuery(String results) {
        // http://4pda.ru/forum/index.php?forums%5B%5D=285&topics%5B%5D=271502&act=search&source=pst&query=remie
        String params = "";
        if (m_IsSearchInTopic) {
            params += "&source=pst";
            params += "&topics%5B%5D=" + m_TopicId;
        } else {
            params += "&source=" + m_Source;
            params += "&subforums=" + (m_Subforums ? "1" : "0");
        }

        params += "&sort=" + m_Sort;

        for (String key : m_CheckedIds) {
            params += "&forums%5B%5D=" + key;
        }
        if (m_CheckedIds.size() == 0) {
            params += "&forums%5B%5D=all";
        }
        if (!TextUtils.isEmpty(m_Query))
            params += "&query=" + tryUrlEncode(m_Query);

        if (!TextUtils.isEmpty(m_UserName))
            params += "&username=" + tryUrlEncode(m_UserName);


        return "http://4pda.ru/forum/index.php?act=search&query=" + "&result=" + results
                + params;

    }

    private String tryUrlEncode(String url) {
        try {
            return URLEncoder.encode(url, "windows-1251");
        } catch (UnsupportedEncodingException e) {
            Log.e(m_Context, e);
            return url;
        }
    }

    private String tryUrlDecode(String url) {
        try {
            if (UrlExtensions.isUrlUtf8Encoded(url))
                return URLDecoder.decode(url, "UTF-8");
            return URLDecoder.decode(url, "windows-1251");
        } catch (UnsupportedEncodingException e) {
            Log.e(m_Context, e);
            return url;
        }
    }

    public void fillAndSave(String query, String userName, String source, String sort, Boolean subforums,
                            ArrayList<String> checkedIds,
                            Boolean searchInTopic,
                            Boolean resultsInTopicsView) {
        m_IsSearchInTopic = searchInTopic;
        m_CheckedIds = checkedIds;
        m_Query = query;
        m_UserName = userName;
        m_Source = source;
        m_Sort = sort;
        m_Subforums = subforums;
        m_ResultsInTopicView = resultsInTopicsView;
        if (!m_IsSearchInTopic)
            saveSettings();
    }


    public void loadSettings() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_Context);
        m_Source = preferences.getString(m_TabTag + ".Template.Source", "all");
        m_Name = preferences.getString(m_TabTag + ".Template.Name", "Последние");

        m_Sort = preferences.getString(m_TabTag + ".Template.Sort", "dd");
        m_UserName = preferences.getString(m_TabTag + ".Template.UserName", "");
        m_Query = preferences.getString(m_TabTag + ".Template.Query", "");

        loadChecks(preferences.getString(m_TabTag + ".Template.Forums", "281"));
        m_Subforums = preferences.getBoolean(m_TabTag + ".Template.Subforums", true);
        m_ResultsInTopicView = preferences.getBoolean(m_TabTag + ".Template.ResultsInTopicView", false);

        if (TextUtils.isEmpty(m_Name)) {
            if (m_Source.equals("all") && m_Sort.equals("dd") && TextUtils.isEmpty(m_UserName)
                    && (m_CheckedIds.size() == 0) && m_Subforums)
                m_Name = "Последние";
            else
                m_Name = "Поиск";
        }
    }

    private void loadChecks(String checksString) {
        m_CheckedIds = new ArrayList<String>();
        if (TextUtils.isEmpty(checksString)) return;
        if (checksString.contains("¶") || checksString.contains("µ"))
            checksString = "281";
        try {
            String[] pairs = checksString.split(";");
            for (int i = 0; i < pairs.length; i++) {
                String id = pairs[i];
                if (TextUtils.isEmpty(id)) continue;
                if (m_CheckedIds.contains(id)) continue;

                m_CheckedIds.add(id);
            }
        } catch (Exception ex) {
            Log.e(m_Context, ex);
        }

    }

    public void saveSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_Context);
        if (!preferences.getBoolean("search.SaveSettings", false)) return;

        String tabTag = m_TabTag;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(tabTag + ".Template.Source", m_Source);
        editor.putString(tabTag + ".Template.Name", m_Name);

        editor.putString(tabTag + ".Template.Sort", m_Sort);
        editor.putString(tabTag + ".Template.UserName", m_UserName);
        editor.putString(tabTag + ".Template.Query", m_Query);
        editor.putString(tabTag + ".Template.Forums", TabDataSettingsActivity.getCheckedIdsString(m_CheckedIds));
//        editor.putString(tabTag + ".Template.Forums",m_Source);
        editor.putBoolean(tabTag + ".Template.Subforums", m_Subforums);
        editor.putBoolean(tabTag + ".Template.ResultsInTopicView", m_ResultsInTopicView);


        editor.commit();
    }

    public Boolean tryFill(String url) {
        url = tryUrlDecode(url);
        Matcher m = Pattern.compile("(?:([\\w\\[\\]]+)=(.*?))(?:\\&|$)").matcher(url);
        m_CheckedIds = new ArrayList<String>();
        boolean res = false;
        while (m.find()) {
            String key = m.group(1).toLowerCase();
            String val = m.group(2);
            if (key.equals("query")) {
                m_Query = val;
                res = true;
            } else if (key.equals("username")) {
                m_UserName = val;
                res = true;
            } else if (key.equals("forums[]")) {
                m_CheckedIds.add(val);
                res = true;
            } else if (key.equals("subforums")) {
                m_Subforums = "1".equals(val);
                res = true;
            } else if (key.equals("source")) {
                m_Source = val;
                res = true;
            } else if (key.equals("sort")) {
                m_Sort = val;
                res = true;
            } else if (key.equals("result")) {
                m_ResultsInTopicView = "topics".equals(val);
                res = true;
            }
        }
        return res;
    }

    public Boolean tryFill(Intent intent) {
        Boolean res = false;
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey("SearchUrl")) {
                    return tryFill(extras.getString("SearchUrl"));
                }
                if (extras.containsKey("ForumId")) {
                    m_CheckedIds = new ArrayList<String>();
                    String forumTitle = extras.containsKey("ForumTitle") ? extras.getString("ForumTitle") : extras.getString("ForumId");
                    m_CheckedIds.add(extras.getString("ForumId"));
                    res = true;
                }
                if (extras.containsKey("TopicId")) {
                    m_TopicId = extras.getString("TopicId");
                    m_Sort = "rel";
                    m_IsSearchInTopic = true;
                    res = true;
                }
                if (extras.containsKey("Query")) {
                    m_Query = extras.getString("Query");
                    res = true;
                }

                if (extras.containsKey("UserName")) {
                    m_UserName = extras.getString("UserName");
                    res = true;
                }

                if (extras.containsKey("Result")) {
                    m_ResultsInTopicView = "topics".equals(extras.getString("Result"));
                    res = true;
                }

                if (extras.containsKey("Source")) {
                    m_Source = extras.getString("Source");
                    res = true;
                }
            }
        }

        return res;
    }

    public boolean getResultsInTopicView() {
        return m_ResultsInTopicView;
    }
}
