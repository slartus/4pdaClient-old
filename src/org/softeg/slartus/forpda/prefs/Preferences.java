package org.softeg.slartus.forpda.prefs;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.download.DownloadsService;
import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdacommon.Connectivity;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import java.io.File;

/*
 * Created by slartus on 23.02.14.
 */
public class Preferences {
    public static Boolean isLoadImagesFromWeb(String listName) {
        return isLoadImages(listName + ".LoadsImages");
    }

    public static Boolean isLoadImages(String prefsKey) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());

        int loadImagesType = ExtPreferences.parseInt(prefs, prefsKey, 1);
        if (loadImagesType == 2) {
            return Connectivity.isConnectedWifi(MyApp.getContext());
        }

        return loadImagesType == 1;
    }

    public static int getFontSize(String prefix) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        int res = ExtPreferences.parseInt(prefs, prefix + ".FontSize", 16);
        return Math.max(Math.min(res, 72), 1);
    }

    public static void setFontSize(String prefix, int value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        prefs.edit().putString(prefix + ".FontSize", Integer.toString(value)).commit();
    }

    public static class Lists {

        public static Boolean getScrollByButtons() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getBoolean("lists.scroll_by_buttons", true);
        }

        public static void setLastSelectedList(String listName) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            prefs.edit().putString("list.last_selected_list", listName).commit();
        }



        public static void addLastAction(String name) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            String[] lastActions = prefs.getString("lists.last_actions", "").split("\\|");

            String newValue = name + "|";
            int max = 5;
            for (String nm : lastActions) {
                if (TextUtils.isEmpty(nm)) continue;
                if (nm.equals(name)) continue;

                newValue += nm + "|";
                max--;
                if (max == 0) break;
            }
            prefs.edit().putString("lists.last_actions", newValue).commit();
        }

        public static String[] getLastActions() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getString("lists.last_actions", "").split("\\|");
        }


    }

    public static class List {
        public static class Favorites{
            public static Boolean isLoadFullPagesList() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                return prefs.getBoolean("lists.favorites.load_all", false);
            }
        }
        public static void setListSort(String listName, String value) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            prefs.edit().putString(listName + ".list.sort", value).commit();
        }

        public static String defaultListSort() {
            return "sortorder.asc";
        }

        public static String getListSort(String listName, String defaultValue) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getString(listName + ".list.sort", defaultValue);
        }


    }

    public static class Topic {
        public static Boolean getSpoilFirstPost() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getBoolean("theme.SpoilFirstPost", true);
        }

        public static Boolean getConfirmSend() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getBoolean("theme.ConfirmSend", true);
        }

        public static void setConfirmSend(Boolean value) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            prefs.edit().putBoolean("theme.ConfirmSend", value).commit();
        }


        public static int getFontSize() {
            return Preferences.getFontSize("theme");
        }

        public static void setFontSize(int value) {
            Preferences.setFontSize("theme", value);
        }

        public static class Post {


            public static void setEnableEmotics(Boolean value) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                prefs.edit().putBoolean("topic.post.enableemotics", value).commit();
            }

            public static boolean getEnableEmotics() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                return prefs.getBoolean("topic.post.enableemotics", true);
            }

            public static void setEnableSign(Boolean value) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                prefs.edit().putBoolean("topic.post.enablesign", value).commit();
            }

            public static boolean getEnableSign() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                return prefs.getBoolean("topic.post.enablesign", true);
            }

            public static void addEmoticToFavorites(String name) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                String[] favoritesEmotics = prefs.getString("topic.post.emotics_favorites", "").split("\\|");

                String newValue = name + "|";
                int max = 9;
                for (String nm : favoritesEmotics) {
                    if (TextUtils.isEmpty(nm)) continue;
                    if (nm.equals(name)) continue;

                    newValue += nm + "|";
                    max--;
                    if (max == 0) break;
                }
                prefs.edit().putString("topic.post.emotics_favorites", newValue).commit();
            }

            public static String[] getEmoticFavorites() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
                return prefs.getString("topic.post.emotics_favorites", "").split("\\|");
            }
        }
    }

    public static class System {
        /**
         * Показывать скролл в браузере
         * Снимите галочку, если программа падает при вызове темы. Например, для Nook Simple Touch
         */
        public static Boolean isShowWebViewScroll() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getBoolean("system.WebViewScroll", true);
        }

        public static boolean isScrollUpButton(int keyCode) {
            return isPrefsButton(keyCode, "keys.scrollUp", "24");
        }

        public static boolean isScrollDownButton(int keyCode) {
            return isPrefsButton(keyCode, "keys.scrollDown", "25");
        }

        public static boolean isPrefsButton(int keyCode, String prefKey, String defaultValue) {
            String scrollUpKeys = "," + PreferenceManager.getDefaultSharedPreferences(MyApp.getContext())
                    .getString(prefKey, defaultValue).replace(" ", "") + ",";
            return (scrollUpKeys.contains("," + Integer.toString(keyCode) + ","));
        }

        public static boolean isDeveloper() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getBoolean("system.developer", false);
        }

        public static String getDrawerMenuPosition() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getString("system.drawermenuposition", "left");
        }

        public static String getDownloadFilesDir() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            String res = prefs.getString("downloads.path", DownloadsService.getDownloadDir(MyApp.getContext()));
            if (TextUtils.isEmpty(res))
                return res;
            if (!res.endsWith(File.separator))
                res += File.separator;
            return res;
        }
    }

    public static class News {
        public static int getLastSelectedSection() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getInt("news.lastselectedsection", 0);
        }

        public static void setLastSelectedSection(int section) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            prefs.edit().putInt("news.lastselectedsection", section).commit();
        }

        public static int getFontSize() {
            return Preferences.getFontSize("news");
        }


    }

    public static class Menu {
        public static boolean getGroupExpanded(int groupIndex) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getBoolean("menu.GroupExpanded." + groupIndex, true);
        }

        public static void setGroupExpanded(int groupIndex, Boolean expanded) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            prefs.edit().putBoolean("menu.GroupExpanded." + groupIndex, expanded).commit();
        }
    }

    public static class Search {
    }


    public static class Attention{
        public static void setAttentionId(String value) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            prefs.edit().putString("attention.id", value).commit();
        }

        public static String getAttentionId() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
            return prefs.getString("attention.id", null);
        }
    }
}
