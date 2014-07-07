package org.softeg.slartus.forpda;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.common.ArrayUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.db.DbHelper;
import org.softeg.slartus.forpda.prefs.DonateActivity;
import org.softeg.slartus.forpda.prefs.PreferencesActivity;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdacommon.DateExtensions;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdanotifyservice.MainService;
import org.softeg.slartus.forpdanotifyservice.favorites.FavoritesNotifier;
import org.softeg.slartus.forpdanotifyservice.qms.QmsNotifier;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 05.08.11
 * Time: 8:03
 */
public class MyApp extends android.app.Application {
    public static final int THEME_WHITE = 0;
    public static final int THEME_BLACK = 1;

    public static final int THEME_WHITE_HD = 13;
    public static final int THEME_BLACK_HD = 14;

    public static final int THEME_PLASTICKBLACK_REMIE = 11;
    public static final int THEME_WHITE_REMIE = 2;
    public static final int THEME_BLACK_REMIE = 3;

    public static final int THEME_WHITE_TRABLONE = 4;
    public static final int THEME_WHITER_REMIE = 5;
    public static final int THEME_GRAY_REMIE = 6;
    public static final int THEME_WHITE_VETALORLOV = 7;

    public static final int THEME_GRAY_TRABLONE = 9;
    public static final int THEME_WHITE_OLD = 12;
    public static final int THEME_CUSTOM_CSS = 99;

    private final Integer[] WHITE_THEMES = {THEME_WHITE_TRABLONE, THEME_WHITE_VETALORLOV, THEME_WHITE_REMIE,
            THEME_WHITER_REMIE, THEME_WHITE, THEME_WHITE_OLD, THEME_WHITE_HD};

    private static boolean m_IsDebugModeLoaded = false;
    private static boolean m_IsDebugMode = false;

    public static boolean getIsDebugMode() {

        if (!m_IsDebugModeLoaded) {
            m_IsDebugMode = PreferenceManager
                    .getDefaultSharedPreferences(INSTANCE).getBoolean("DebugMode", false);
            m_IsDebugModeLoaded = true;
        }
        return m_IsDebugMode;
    }

    public static void showMainActivityWithoutBack(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public int getThemeStyleResID() {
        return isWhiteTheme() ? R.style.Theme_White : R.style.Theme_Black;
    }

    public int getThemeBackgroundColor() {
        return getResources().getColor(MyApp.getInstance().isWhiteTheme() ? R.color.pda__background_light : R.color.pda__background_dark);
    }

    public int getThemeTextColor() {
        return getResources().getColor(MyApp.getInstance().isWhiteTheme() ? R.color.pda__primary_text_light : R.color.pda__primary_text_dark);
    }

    public boolean isWhiteTheme() {
        String themeStr = MyApp.getInstance().getCurrentTheme();
        int theme = themeStr.length() < 3 ? Integer.parseInt(themeStr) : -1;

        return ArrayUtils.indexOf(theme, WHITE_THEMES) != -1 || themeStr.contains("/white/");
    }

    public int getThemeStyleWebViewBackground() {
        return isWhiteTheme() ? getResources().getColor(R.color.white_theme_webview_background) : Color.BLACK;
    }

    public String getCurrentTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString("appstyle", Integer.toString(THEME_WHITE_OLD));
    }

    public String getCurrentThemeName() {
        return isWhiteTheme() ? "white" : "black";
    }

    private String checkThemeFile(String themePath) {
        try {
            if (!new File(themePath).exists()) {
                // Toast.makeText(INSTANCE,"не найден файл темы: "+themePath,Toast.LENGTH_LONG).show();
                return defaultCssTheme();
            }
            return themePath;
        } catch (Throwable ex) {
            return defaultCssTheme();
        }
    }

    private final String defaultCssTheme() {
        return "/android_asset/forum/css/white.css";
    }

    public String getThemeCssFileName() {
        String themeStr = MyApp.getInstance().getCurrentTheme();
        return getThemeCssFileName(themeStr);
    }

    public String getThemeCssFileName(String themeStr) {
        if (themeStr.length() > 3)
            return checkThemeFile(themeStr);

        String path = "/android_asset/forum/css/";
        String cssFile = "white.css";
        int theme = Integer.parseInt(themeStr);
        if (theme == -1)
            return themeStr;
        switch (theme) {
            case THEME_WHITE:
                cssFile = "white.css";
                break;
            case THEME_BLACK:
                cssFile = "black.css";
                break;
            case THEME_WHITE_OLD:
                cssFile = "white_old.css";
                break;
//            case THEME_WHITE_REMIE:
//                cssFile = "white_Remie-l.css";
//                break;
//            case THEME_BLACK_REMIE:
//                cssFile = "black_Remie-l.css";
//                break;
//            case THEME_PLASTICKBLACK_REMIE:
//                cssFile = "plasticblack_Remie-l.css";
//                break;
//            case THEME_WHITE_TRABLONE:
//                cssFile = "white_trablone.css";
//                break;
//            case THEME_WHITER_REMIE:
//                cssFile = "whiter_Remie-l.css";
//                break;
//            case THEME_GRAY_REMIE:
//                cssFile = "gray_Remie-l.css";
//                break;
//
//            case THEME_WHITE_VETALORLOV:
//                cssFile = "white_vetalorlov.css";
//                break;
//
//            case THEME_GRAY_TRABLONE:
//                cssFile = "gray_trablone.css";
//                break;
            case THEME_WHITE_HD:
                cssFile = "white_hd.css";
                break;
            case THEME_BLACK_HD:
                cssFile = "black_hd.css";
                break;
            case THEME_CUSTOM_CSS:
                return "/mnt/sdcard/style.css";
        }
        return path + cssFile;
    }

    private static MyApp INSTANCE = null;

    public MyApp() {
        INSTANCE = this;


    }

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(getThemeStyleResID());
        try {
            DbHelper.prepareBases(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Boolean m_QmsStarted = false;
    private static Boolean m_FavoritesNotifierStarted = false;

    public static MyApp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MyApp();

        }
        if (!m_QmsStarted) {
            reStartQmsService();
        }
        if (!m_FavoritesNotifierStarted) {
            reStartFavoritesNotifierService();
        }
        return INSTANCE;
    }

    public static void resStartNotifierServices() {
        reStartQmsService();
        reStartFavoritesNotifierService();
    }

    private static void stopQmsService() {
        try {
            QmsNotifier.cancelAlarm(INSTANCE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void reStartQmsService() {
        stopQmsService();
        startQmsService();
    }

    private static void startQmsService() {
        m_QmsStarted = true;
        try {
            if (!QmsNotifier.isUse(getContext()))
                return;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());


            Intent intent = new Intent(INSTANCE, MainService.class);
            intent.putExtra("CookiesPath", PreferencesActivity.getCookieFilePath(INSTANCE));
            intent.putExtra(QmsNotifier.TIME_OUT_KEY, Math.max(ExtPreferences.parseFloat(sharedPreferences,
                    QmsNotifier.TIME_OUT_KEY, 5), 1));

            QmsNotifier.restartTask(INSTANCE, intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void stopFavoritesNotifierService() {
        try {
            FavoritesNotifier.cancelAlarm(INSTANCE);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void reStartFavoritesNotifierService() {
        stopFavoritesNotifierService();
        startFavoritesNotifierService();
    }

    private static void startFavoritesNotifierService() {
        m_FavoritesNotifierStarted = true;
        try {
            if (!FavoritesNotifier.isUse(getContext())) return;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            Intent intent = new Intent(INSTANCE, MainService.class);
            intent.putExtra("CookiesPath", PreferencesActivity.getCookieFilePath(INSTANCE));
            intent.putExtra(FavoritesNotifier.TIME_OUT_KEY, Math.max(ExtPreferences.parseFloat(sharedPreferences,
                    FavoritesNotifier.TIME_OUT_KEY, 5), 1));

            FavoritesNotifier.restartTask(INSTANCE, intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return getInstance();
    }


    public static boolean isBetaVersion(Context context) {
        String packageName = context.getPackageName();

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);
            return pInfo.versionName.contains("beta");
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(context, e1);
        }
        return false;
    }

    public static boolean isDonateVersion(Context context) {
        String packageName = context.getPackageName();
        return packageName.toLowerCase().equals(
                "org.softeg.slartus.forpda.forpda");

    }

    private static Boolean s_PromoChecked = false;

    public void showPromo(final SherlockFragmentActivity sherlockFragmentActivity) {
        try {
            if (s_PromoChecked) return;
            s_PromoChecked = true;

            //    if (isBetaVersion(sherlockFragmentActivity)) return;
            //    if (isDonateVersion(sherlockFragmentActivity)) return;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (prefs.getBoolean("donate.DontShow", false)) return;


            String appVersion = getAppVersion(sherlockFragmentActivity);
            if (prefs.getString("DonateShowVer", "").equals(appVersion)) {
                if (!isTimeForPromo(prefs)) return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("DonateShowVer", appVersion);
            ExtPreferences.putDateTime(editor, "donate.LastShowDate", new Date());
            editor.commit();

            SherlockDialogFragment dialogFragment = new SherlockDialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    return new AlertDialogBuilder(getActivity())
                            .setTitle("Неофициальный 4pda клиент")
                            .setMessage("Ваша поддержка - стимул к дальнейшей разработке и развитию программы\n" +
                                    "\n" +
                                    "Вы можете сделать это позже через меню>>настройки>>Помочь проекту")
                            .setPositiveButton("Помочь проекту..",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                            Intent settingsActivity = new Intent(
                                                    getActivity(), DonateActivity.class);
                                            startActivity(settingsActivity);

                                        }
                                    })
                            .setNegativeButton("Позже",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();

                                        }
                                    }).create();
                }
            };
            dialogFragment.show(sherlockFragmentActivity.getSupportFragmentManager(), "dialog");
        } catch (Throwable ex) {
            Log.e(sherlockFragmentActivity, ex);
        }
    }

    private boolean isTimeForPromo(SharedPreferences prefs) {
        GregorianCalendar lastShowpromoCalendar = new GregorianCalendar();
        lastShowpromoCalendar.setTime(ExtPreferences.getDateTime(prefs, "donate.LastShowDate", new Date()));

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        int days = DateExtensions.getDaysBetween(calendar.getTime(), lastShowpromoCalendar.getTime());
        return days > 31;
    }

    private static String getAppVersion(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);

            return pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(context, e1);
        }
        return "";
    }

    public String getAppExternalFolderPath() throws IOException {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/4pdaClient/";
        if (!FileUtils.hasStorage(path, true))
            throw new NotReportException("Нет доступа к папке программы: " + path);
        return path;
    }

    private Date loadLastCheck4pdaVersionDate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lastDateStr = prefs.getString("LastCheck4pdaVersionDate", null);
        return null;
    }

    public static void check4pdaNewVersion(final Context context, final Handler handler) {
        try {
            String currentVersion = getAppVersion(context);
            if (currentVersion.contains("beta"))
                currentVersion = currentVersion.substring(0, currentVersion.indexOf("beta")).trim();
            final String finalCurrentVersion = currentVersion;
            new Thread(new Runnable() {
                public void run() {
                    Throwable threadEx = null;
                    String pageBody = null;
                    Boolean siteVersionsNewer = false;
                    String releaseVer = null;
                    try {
                        pageBody = Client.getInstance().performGet("http://4pda.ru/forum/lofiversion/index.php?t271502.html");
                        Matcher m = Pattern.compile("<b>версия:(.*?)</b>").matcher(pageBody);
                        if (m.find()) {
                            releaseVer = m.group(1).trim();
                            siteVersionsNewer = isSiteVersionsNewer(releaseVer, finalCurrentVersion);
                        }
                    } catch (Throwable e) {
                        threadEx = e;
                    }


                    final Throwable finalThreadEx = threadEx;


                    final Boolean finalSiteVersionsNewer = siteVersionsNewer;
                    final String finalReleaseVer = releaseVer;
                    handler.post(new Runnable() {
                        public void run() {
                            if (finalThreadEx != null) {
                                Log.e(context, new NotReportException("Ошибка проверки новой версии", finalThreadEx));
                            } else {
                                try {
                                    if (finalSiteVersionsNewer) {
                                        new AlertDialogBuilder(context)
                                                .setTitle("Новая версия!")
                                                .setMessage("На сайте 4pda выложена новая версия приложения: "
                                                        + finalReleaseVer)
                                                .setPositiveButton("Открыть тему", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent themeIntent = new Intent(context, ThemeActivity.class);
                                                        themeIntent.setData(Uri.parse("4pda.ru/forum/index.php?showtopic=271502&view=getnewpost"));
                                                        context.startActivity(themeIntent);
                                                    }
                                                })
                                                .setNegativeButton("Отмена", null)
                                                .create().show();
                                    }
                                } catch (Exception ex) {
                                    Log.e(context, new NotReportException("Ошибка проверки новой версии", ex));
                                }
                            }
                        }
                    });
                }
            }).start();
        } catch (Exception ex) {
            Log.e(context, new NotReportException("Ошибка проверки новой версии", ex));
        }

    }

    private static boolean isSiteVersionsNewer(String siteVersion, String programVersion) {
        String[] siteVersionVals = TextUtils.split(siteVersion, "\\.");
        String[] programVersionVals = TextUtils.split(programVersion, "\\.");

        for (int i = 0; i < siteVersionVals.length; i++) {
            int siteVersionVal = Integer.parseInt(siteVersionVals[i]);

            if (programVersionVals.length == i)// значит на сайте версия с доп. циферкой
                return true;

            int programVersionVal = Integer.parseInt(programVersionVals[i]);

            if (siteVersionVal == programVersionVal) continue;
            return siteVersionVal > programVersionVal;
        }
        return false;
    }
}