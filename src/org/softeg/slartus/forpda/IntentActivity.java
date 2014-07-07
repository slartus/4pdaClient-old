package org.softeg.slartus.forpda;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.softeg.slartus.forpda.Tabs.DevicesTab;
import org.softeg.slartus.forpda.Tabs.NewsTab;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.Tabs.TopicWritersTab;
import org.softeg.slartus.forpda.Tabs.TopicsHistoryTab;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.common.Email;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.download.DownloadsService;
import org.softeg.slartus.forpda.profile.ProfileActivity;
import org.softeg.slartus.forpda.qms_2_0.QmsChatActivity;
import org.softeg.slartus.forpda.qms_2_0.QmsContactThemesActivity;
import org.softeg.slartus.forpda.search.SearchActivity;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpda.video.PlayerActivity;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdacommon.UrlExtensions;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 17.01.12
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class IntentActivity extends Activity {
    public static final String ACTION_SELECT_TOPIC = "org.softeg.slartus.forpda.SELECT_TOPIC";
    public static final String RESULT_TOPIC_ID = "org.softeg.slartus.forpda.RESULT_TOPIC_ID";

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }


    public static boolean checkSendAction(final Activity activity, final Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            Bundle extras = intent.getExtras();
            try {
                if (extras != null && extras.containsKey(Intent.EXTRA_EMAIL)
                        && extras.get(Intent.EXTRA_EMAIL) != null && Email.EMAIL.equals(extras.getStringArray(Intent.EXTRA_EMAIL)[0])) {


                    Toast.makeText(activity, "Сообщение об ошибке отправлять только на email!", Toast.LENGTH_LONG).show();

                    return true;
                }
            } catch (Throwable ignored) {

            }
            if (extras != null && extras.containsKey(Intent.EXTRA_EMAIL)
                    && extras.get(Intent.EXTRA_EMAIL) != null && Log.EMAIL_SUBJECT.equals(extras.get(Intent.EXTRA_EMAIL).toString())) {
                EditPostPlusActivity.newPostWithAttach(activity,
                        null, "271502", Client.getInstance().getAuthKey(), extras);

                return true;
            }
            final CharSequence[] itemTemplates =
                    {Tabs.TAB_FAVORITES,  TopicsHistoryTab.TEMPLATE, Tabs.TAB_FORUMS,
                            Tabs.TAB_CATALOG, Tabs.TAB_APPS, "Поиск"};
            final CharSequence[] items =
                    {"Избранное", TopicsHistoryTab.TITLE, "Форумы",
                            "Каталог", "Приложения", "Поиск"};
            AlertDialog alertDialog = new AlertDialogBuilder(activity)
                    .setTitle("Ответить в теме из..")
                    .setAdapterSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                dialogInterface.dismiss();

                                Intent tabIntent;
                                new Intent(activity, QuickStartActivity.class);
                                switch (i) {
                                    case 6:
                                        tabIntent = new Intent(activity, SearchActivity.class);
                                        tabIntent.putExtra(SearchActivity.TOPICS_ONLY_KEY, true);
                                        break;
                                    default:
                                        tabIntent = new Intent(activity, QuickStartActivity.class);
                                        tabIntent.putExtra("template", itemTemplates[i]);
                                        break;
                                }
                                tabIntent.putExtras(intent.getExtras());

                                activity.startActivity(tabIntent);
                                activity.finish();
                            } catch (Throwable ex) {
                                Log.e(activity, ex);
                            }
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            activity.finish();
                        }
                    })
                    .create();
            alertDialog.show();
            return true;
        }
        return false;
    }

    public static Boolean isNews(String url) {
        final Pattern pattern = PatternExtensions.compile("4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+");
        final Pattern pattern1 = PatternExtensions.compile("4pda.ru/\\w+/(?:older|newer)/\\d+");

        return pattern.matcher(url).find() || pattern1.matcher(url).find();
    }

    public static Boolean isYoutube(String url) {
        return PlayerActivity.isYoutube(url);
    }

    public static Boolean tryShowYoutube(Activity context, String url, Boolean finish) {
        if (!isYoutube(url)) return false;
        PlayerActivity.showYoutubeChoiceDialog(context, url);
        if (finish)
            context.finish();
        return true;
    }


    public static Boolean isNewsList(String url) {
        final Pattern pattern = PatternExtensions.compile("4pda.ru/tag/.*");
        final Pattern pattern1 = PatternExtensions.compile("4pda.ru/page/(\\d+)/");

        return pattern.matcher(url).find() || pattern1.matcher(url).find() || "http://4pda.ru".equalsIgnoreCase(url)
                || "http://4pda.ru/".equalsIgnoreCase(url);
    }

    public static Boolean tryShowNewsList(Activity context, String url, Boolean finish) {

        if (isNewsList(url)) {
            NewsTab.showNewsList(context, url);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static Boolean tryShowNews(Activity context, String url, Boolean finish) {
        if (isNews(url)) {
            NewsActivity.shownews(context, url);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean isTheme(String url) {
        String[] patterns = {
                "4pda.ru.*?showtopic=.*",
                "4pda.ru.*?act=findpost&pid=\\d+(.*)?",
                "4pda.ru/forum/lofiversion/index.php\\?t\\d+(?:-\\d+)?.html"
        };
        for (String pattern : patterns) {
            if (PatternExtensions.compile(pattern).matcher(url).find())
                return true;
        }
        return false;
    }

    public static String normalizeThemeUrl(String url) {
        if (TextUtils.isEmpty(url))
            return url;

        Matcher m = PatternExtensions.compile("4pda.ru/forum/lofiversion/index.php\\?t(\\d+)(?:-(\\d+))?.html").matcher(url);
        if (m.find()) {
            return "http://4pda.ru/forum/index.php?showtopic=" + m.group(1) + (TextUtils.isEmpty(m.group(2)) ? "" : ("&st=" + m.group(2)));
        }
        m = PatternExtensions.compile("4pda.ru.*?act=boardrules").matcher(url);// переброс с правил форума на графический вариант
        if (m.find()) {
            return "http://4pda.ru/forum/index.php?showtopic=296875";
        }

        return url;
    }

    public static boolean tryShowSearch(Activity context, String url, Boolean finish) {

        Matcher m = PatternExtensions.compile("4pda.ru.*?act=search").matcher(url);
        if (m.find()) {
            SearchActivity.startActivity(context, url);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowReputation(Activity context, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru.*?act=rep&type=history&mid=(\\d+)").matcher(url);
        if (m.find()) {
            ReputationActivity.showRep(context, m.group(1));
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryPlusReputation(Activity context, Handler handler, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru.*?act=rep&type=win_add&mid=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            ForumUser.startChangeRep(context, handler, m.group(1), m.group(1), m.group(2), "add", "Поднять репутацию");
            if (finish)
                context.finish();
            return true;
        }
        m = PatternExtensions.compile("4pda.ru.*?act=rep&type=win_add&mid=(\\d+)").matcher(url);
        if (m.find()) {
            ReputationActivity.plusRep(context, handler, m.group(1), m.group(1));
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryMinusReputation(Activity context, Handler handler, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru.*?act=rep&type=win_minus&mid=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            ForumUser.startChangeRep(context, handler, m.group(1), m.group(1), m.group(2), "minus", "Опустить репутацию");
            if (finish)
                context.finish();
            return true;
        }
        m = PatternExtensions.compile("4pda.ru.*?act=rep&type=win_minus&mid=(\\d+)").matcher(url);
        if (m.find()) {
            ReputationActivity.minusRep(context, handler, m.group(1), m.group(1));
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowClaim(Activity context, Handler handler, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru/*forum/*index.php\\?act=report&t=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            org.softeg.slartus.forpda.classes.Post.claim(context, handler, m.group(1), m.group(2));

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryProfile(Activity context, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru/*forum/*index.php\\?.*?act=profile.*?id=(\\d+)").matcher(url);
        if (m.find()) {
            ProfileActivity.startActivity(context, m.group(1));

            if (finish)
                context.finish();
            return true;
        }

        m = PatternExtensions.compile("4pda.ru/*forum/*index.php\\?.*?showuser=(\\d+)").matcher(url);
        if (m.find()) {
            ProfileActivity.startActivity(context, m.group(1));

            if (finish)
                context.finish();
            return true;
        }

        return false;
    }

    public static boolean tryShowForum(Activity context, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru.*?showforum=(\\d+)$").matcher(url);

        String id = null;
        if (m.find()) {
            id = m.group(1);
        } else {
            m = PatternExtensions.compile("4pda.ru/forum/lofiversion/index.php\\?f(\\d+)\\.html").matcher(url);
            if (m.find())
                id = m.group(1);
        }
        if (!TextUtils.isEmpty(id)) {
            Intent intent = new Intent(context, QuickStartActivity.class);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("QuickTab.startforum", Integer.parseInt(id));
            editor.commit();


            intent.putExtra("template", Tabs.TAB_FORUMS);

            context.startActivity(intent);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity) {
        return tryShowUrl(context, handler, url, showInDefaultBrowser, finishActivity, null);
    }


    private static CharSequence getRedirect(CharSequence url) {
        Matcher m = PatternExtensions.compile("4pda\\.ru/pages/go/\\?u=(.*?)$").matcher(url);
        if (m.find()) {
            try {
                return UrlExtensions.decodeUrl(m.group(1));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity, String authKey) {
        url = getRedirect(url).toString();
        if (isTheme(url)) {
            showTopic(context, url);
            if (finishActivity)
                context.finish();
            return true;
        }

        if (tryShowNews(context, url, finishActivity)) {
            return true;
        }

        if (tryShowNewsList(context, url, finishActivity)) {
            return true;
        }

        if (tryShowFile(context, url, finishActivity)) {
            return true;
        }

        if (tryProfile(context, url, finishActivity)) {
            return true;
        }

        if (tryShowForum(context, url, finishActivity)) {
            return true;
        }

        if (tryShowReputation(context, url, finishActivity))
            return true;

        if (tryPlusReputation(context, handler, url, finishActivity))
            return true;

        if (tryMinusReputation(context, handler, url, finishActivity))
            return true;

        if (tryShowClaim(context, handler, url, finishActivity))
            return true;

        if (tryShowQms(context, url))
            return true;

        if (tryShowQms_2_0(context, url, finishActivity))
            return true;

        if (tryFavorites(context, url, finishActivity))
            return true;

        if (tryShowPm(context, url))
            return true;

        if (tryShowEditPost(context, url, authKey, finishActivity))
            return true;

        if (tryShowTopicWriters(context, url, finishActivity))
            return true;

        if (tryDevdb(context, url, finishActivity))
            return true;

        if (tryShowSearch(context, url, finishActivity))
            return true;

        if (tryShowYoutube(context, url, finishActivity))
            return true;

        if (showInDefaultBrowser)
            showInDefaultBrowser(context, url);


        if (finishActivity)
            context.finish();
        return false;
    }

    private static void showTopic(Activity context, String url) {
        Intent themeIntent = new Intent(context, ThemeActivity.class);
        themeIntent.setData(Uri.parse(url));
        context.startActivity(themeIntent);
    }

    private static boolean tryFavorites(Activity context, String url, Boolean finishActivity) {
        Matcher m = PatternExtensions.compile("4pda.ru.*?autocom=favtopics").matcher(url);
        if (m.find()) {
            Bundle extras = new Bundle();

            QuickStartActivity.showTab(context, Tabs.TAB_FAVORITES, extras);
            if (finishActivity)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryDevdb(Activity context, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("devdb.ru/(.*?)/([^\"]*?)$").matcher(url);
        if (m.find()) {
            Bundle extras = new Bundle();
            extras.putString(DevicesTab.BRAND_ID, m.group(2));
            extras.putString(DevicesTab.DEVICE_TYPE, m.group(1));
            QuickStartActivity.showTab(context, Tabs.TAB_DEVDB, extras);
            if (finish)
                context.finish();
            return true;
        }

        m = PatternExtensions.compile("devdb.ru/([^\"]*?)/").matcher(url);
        if (m.find()) {
            Bundle extras = new Bundle();
            extras.putString(DevicesTab.BRAND_ID, m.group(1));
            QuickStartActivity.showTab(context, Tabs.TAB_DEVDB, extras);
            if (finish)
                context.finish();
            return true;
        }

        m = PatternExtensions.compile("devdb.ru/([^\"]*?)$").matcher(url);
        if (m.find()) {
            if (!TextUtils.isEmpty(m.group(1)))
                DevDbDeviceActivity.showDevice(context, m.group(1));
            else
                QuickStartActivity.showTab(context, Tabs.TAB_DEVDB);
            if (finish)
                context.finish();
            return true;
        }

        m = PatternExtensions.compile("devdb.ru").matcher(url);
        if (m.find()) {
            QuickStartActivity.showTab(context, Tabs.TAB_DEVDB);
            if (finish)
                context.finish();
            return true;
        }

        return false;
    }

    public static boolean tryShowEditPost(Activity context, String url, String authKey, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=post&do=edit_post&f=(\\d+)&t=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            EditPostPlusActivity.editPost(context, m.group(1), m.group(2), m.group(3), authKey);

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowQms(Activity context, String url) {
        Matcher m = PatternExtensions.compile("4pda.ru/forum/index.php\\?autocom=qms&mid=(\\d+)").matcher(url);
        if (m.find()) {
            Toast.makeText(context, "Старая система QMS больше не поддерживается!", Toast.LENGTH_LONG).show();

            return true;
        }
        return false;
    }

    public static boolean tryShowQms_2_0(Activity context, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=qms&mid=(\\d+)&t=(\\d+)").matcher(url);
        if (m.find()) {
            QmsChatActivity.openChat(context, m.group(1), null, m.group(2), null);

            if (finish)
                context.finish();
            return true;
        }
        m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=qms&mid=(\\d+)").matcher(url);
        if (m.find()) {
            QmsContactThemesActivity.showThemes(context, m.group(1), "");

            if (finish)
                context.finish();
            return true;
        }

        m = PatternExtensions.compile("4pda.ru/forum/index.php\\?act=qms").matcher(url);
        if (m.find()) {
            org.softeg.slartus.forpda.qms_2_0.QmsContactsActivity.show(context);

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowPm(Activity context, String url) {
        Matcher m = PatternExtensions.compile("http://4pda.ru/forum/index.php\\?act=Msg&CODE=4&MID=(\\d+)").matcher(url);
        if (m.find()) {
            Toast.makeText(context, "Старая система ЛС больше не поддерживается!", Toast.LENGTH_LONG).show();
//            EditMailActivity.sendMessage(context, "CODE=04&act=Msg&MID=" + m.group(1), "", true);
//
//
//            if (finish)
//                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowTopicWriters(Activity context, String url, Boolean finish) {
        Matcher m = PatternExtensions.compile("4pda.ru/forum/index.php\\?.*?act=Stats.*?CODE=who.*?t=(\\d+)").matcher(url);
        if (m.find()) {
            TopicWritersTab.show(context, m.group(1));
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    private static boolean tryShowFile(final Activity activity, final String url, final Boolean finish) {
        Pattern filePattern = PatternExtensions.compile("http://4pda.ru/forum/dl/post/\\d+/[^\"]*");
        Pattern stFilePattern = PatternExtensions.compile("http://st.4pda.ru/wp-content/uploads/[^\"]*");
        final Pattern imagePattern = PatternExtensions.compile("http://.*?\\.(png|jpg|jpeg|gif)$");
        if (filePattern.matcher(url).find() || stFilePattern.matcher(url).find()) {
            if (!Client.getInstance().getLogined() && !Client.getInstance().hasLoginCookies()) {
                Client.getInstance().showLoginForm(activity, new Client.OnUserChangedListener() {
                    public void onUserChanged(String user, Boolean success) {
                        if (success) {
                            if (imagePattern.matcher(url).find()) {
                                showImage(activity, url);
                                if (finish)
                                    activity.finish();
                            } else
                                downloadFileStart(activity, url, finish);
                        } else if (finish)
                            activity.finish();

                    }
                });
            } else {
                if (imagePattern.matcher(url).find()) {
                    showImage(activity, url);
                    if (finish)
                        activity.finish();
                } else
                    downloadFileStart(activity, url, finish);
            }

            return true;
        }
        if (imagePattern.matcher(url).find()) {
            showImage(activity, url);
            if (finish)
                activity.finish();
            return true;
        }
        return false;
    }

    private static void showImage(Context context, String url) {
        ImageViewActivity.showImageUrl(context, url);
    }

    public static void downloadFileStart(final Activity activity, final String url, final Boolean finish) {

        if (PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getBoolean("files.ConfirmDownload", true)) {
            new AlertDialogBuilder(activity)
                    .setTitle("Уверены?")
                    .setMessage("Начать закачку файла?")
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            DownloadsService.download(activity, url);

                            if (finish)
                                activity.finish();
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (finish)
                                activity.finish();
                        }
                    })
                    .create().show();
        } else {
            DownloadsService.download(activity, url);
            if (finish)
                activity.finish();
        }

    }

    private static Boolean is4pdaUrl(String url) {
        return PatternExtensions.compile("4pda\\.ru").matcher(url).find();
    }

    public static void showInDefaultBrowser(Context context, String url) {
        try {

            Intent marketIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url));
            if (is4pdaUrl(url))
                context.startActivity(Intent.createChooser(marketIntent, "Открыть с помощью"));
            else
                context.startActivity(marketIntent);
        } catch (Exception ex) {
            Log.e(context, new NotReportException("Не найдено ни одно приложение для ссылки: " + url));
        }
    }
}