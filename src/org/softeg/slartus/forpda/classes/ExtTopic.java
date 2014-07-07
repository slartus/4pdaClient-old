package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicApi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 19.09.11
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
 */
public class ExtTopic extends Topic implements ForumItem, IListItem {
    private Messages m_Messages = new Messages();

    private String forumTitle;

    private String forumId;
    private String authKey;


    private boolean mIsOld = false;


    public ExtTopic(Topic topic) {

        setId(topic.getId());
        setTitle(topic.getTitle(), false);

        setIsNew(topic.getIsNew());
        setDescription(topic.getDescription(), false);
        setLastMessageDate(topic.getLastMessageDate());
        setLastMessageAuthor(topic.getLastMessageAuthor(), false);
    }

    public ExtTopic(String id, String title) {
        super(id, title);
    }

    private int m_PagesCount = 1;

    public int getPagesCount() {
        return m_PagesCount;
    }

    public void setPagesCount(String value) {
        m_PagesCount = Integer.parseInt(value) + 1;
    }

    public int getPostsPerPageCount(String lastUrl) {

        URI redirectUri = Client.getInstance().getRedirectUri();
        if (redirectUri != null)
            lastUrl = redirectUri.toString();
        Pattern p = Pattern.compile("st=(\\d+)");
        Matcher m = p.matcher(lastUrl);
        if (m.find())
            m_LastPageStartCount = Math.max(Integer.parseInt(m.group(1)), m_LastPageStartCount);

        return m_LastPageStartCount / (m_PagesCount - 1);
    }

    private int m_LastPageStartCount = 0;

    public int getLastPageStartCount() {
        return m_LastPageStartCount;
    }

    public void setLastPageStartCount(String value) {
        m_LastPageStartCount = Math.max(Integer.parseInt(value), m_LastPageStartCount);
    }

    public void addMessage(Post post) {
        m_Messages.add(post);
    }

    public Messages getMessages() {
        return m_Messages;
    }

    private int m_CurrentPage = 0;

    public void setCurrentPage(String value) {
        m_CurrentPage = Integer.parseInt(value);
    }

    public int getCurrentPage() {
        return m_CurrentPage;
    }


    public void setForumTitle(String forumTitle) {

        this.forumTitle = forumTitle;
        getForumTitle();
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public void showActivity(Context context) {
        Intent intent = new Intent(context, ThemeActivity.class);
        intent.putExtra("ThemeUrl", m_Id);

        context.startActivity(intent);
    }

    public void showActivity(Context context, String params) {
        showActivity(context, m_Id, params);
    }

    public static void showActivity(Context context, CharSequence themeId, CharSequence params) {
        Intent intent = new Intent(context, ThemeActivity.class);
        intent.putExtra("ThemeUrl", themeId);
        intent.putExtra("Params", params);
        context.startActivity(intent);
    }


    public String getShowBrowserUrl(String params) {
        return getShowBrowserUrl(m_Id, params);
    }

    public static String getShowBrowserUrl(CharSequence id, CharSequence params) {
        return "http://4pda.ru/forum/index.php?showtopic=" + id + (TextUtils.isEmpty(params) ? "" : ("&" + params));
    }

    public void showBrowser(Context context, String params) {
        showBrowser(context, m_Id, params);


    }


    public static void showBrowser(Context context, CharSequence topicId, String params) {
        IntentActivity.showInDefaultBrowser(context, getShowBrowserUrl(topicId, params));

    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getForumId() {
        return forumId;
    }

    public String getForumTitle() {
        return forumTitle;
    }

    public String removeFromFavorites() throws IOException, ParseException, URISyntaxException {
        return Client.getInstance().removeFromFavorites(this);
    }

    public boolean getIsOld() {
        return mIsOld;
    }

    public static void startSubscribe(final Context context, final android.os.Handler handler, final String topicId) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.subscribe_dialog, null);

        final RadioButton emailnone_radio = (RadioButton) layout.findViewById(R.id.emailnone_radio);
        final RadioButton emaildelayed_radio = (RadioButton) layout.findViewById(R.id.emaildelayed_radio);
        final RadioButton emailimmediate_radio = (RadioButton) layout.findViewById(R.id.emailimmediate_radio);
        final RadioButton emaildaily_radio = (RadioButton) layout.findViewById(R.id.emaildaily_radio);
        final RadioButton emailweekly_radio = (RadioButton) layout.findViewById(R.id.emailweekly_radio);
        final RadioGroup emailtype_radio = (RadioGroup) layout.findViewById(R.id.emailtype_radio);
        new AlertDialogBuilder(context)
                .setTitle("Добавление в избранное/подписки")
                .setView(layout)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        View v = emailtype_radio.findViewById(emailtype_radio.getCheckedRadioButtonId());
                        String emailtype = TopicApi.TRACK_TYPE_DELAYED;
                        if (v == emailnone_radio) {
                            emailtype = TopicApi.TRACK_TYPE_NONE;
                        } else if (v == emaildelayed_radio) {
                            emailtype = TopicApi.TRACK_TYPE_DELAYED;
                        } else if (v == emailimmediate_radio) {
                            emailtype = TopicApi.TRACK_TYPE_IMMEDIATE;
                        } else if (v == emaildaily_radio) {
                            emailtype = TopicApi.TRACK_TYPE_DAILY;
                        } else if (v == emailweekly_radio) {
                            emailtype = TopicApi.TRACK_TYPE_WEEKLY;
                        }

                        Toast.makeText(context, "Запрос на добавление отправлен", Toast.LENGTH_SHORT).show();

                        final String finalEmailtype = emailtype;
                        new Thread(new Runnable() {
                            public void run() {

                                Exception ex = null;

                                String res = null;
                                try {
                                    res =  Client.getInstance().themeSubscribe(topicId, finalEmailtype);
                                } catch (Exception e) {
                                    ex = e;
                                }

                                final Exception finalEx = ex;
                                final String finalRes = res;
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, "Ошибка добавления в избранное/подписки", Toast.LENGTH_SHORT).show();
                                                Log.e(context, finalEx);
                                            } else {
                                                Toast.makeText(context, finalRes, Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ex) {
                                            Log.e(context, ex);
                                        }

                                    }
                                });
                            }
                        }).start();

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }
    public void startSubscribe(final Context context, final android.os.Handler handler) {
        startSubscribe(context,handler,this.getId());
    }



    public void dispose() {
        if (m_Messages != null)
            m_Messages.clear();
    }
}
