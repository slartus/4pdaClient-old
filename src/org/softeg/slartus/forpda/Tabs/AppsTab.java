package org.softeg.slartus.forpda.Tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.PdaApplication;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.db.ApplicationRelationsTable;
import org.softeg.slartus.forpda.db.ApplicationsDbHelper;
import org.softeg.slartus.forpda.db.DbHelper;
import org.softeg.slartus.forpdaapi.AppItem;
import org.softeg.slartus.forpdaapi.IListItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 11.11.11
 * Time: 8:18
 */
public class AppsTab extends TopicsTab {
    public static final String TITLE = "Приложения";

    public AppsTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent);
    }

    @Override
    public String getTemplate() {
        return Tabs.TAB_APPS;
    }


    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public ArrayList<? extends IListItem> loadItems(int startFrom) throws IOException {
        PackageManager packageManager = getContext().getPackageManager();
        List<PackageInfo> applications = packageManager.getInstalledPackages(0);

        ArrayList<String> appsName = new ArrayList<String>();
        ArrayList<AppItem> apps = new ArrayList<AppItem>();

        for (int n = 0; n < applications.size(); n++) {
            PackageInfo p = applications.get(n);
            if (p.applicationInfo == null) continue;
            if (!filterApp(p.applicationInfo)) continue;

            CharSequence title = p.applicationInfo.loadLabel(packageManager);
            AppItem topic = new AppItem("", title);

            topic.setDescription(p.packageName);
            topic.setVersionName(p.versionName);
            topic.setPackageName(p.packageName);
            apps.add(topic);
        }

        //if(!compareFromCache(apps))
        compareFromBases(appsName, apps);

        Boolean allFinded = apps.size() > 0;// если просто поставить тру, и нет apps, то неверно
        for (AppItem app : apps) {
            if (app.Ids.size() == 0) {
                allFinded = false;
                break;
            }
        }

        if (!allFinded)
            compareFromSite(appsName, apps);

        sort(apps);
        //saveCache(apps);
        return apps;
    }

    private void saveCache(ArrayList<AppItem> apps) {
        SQLiteDatabase db = null;
        try {
            DbHelper dbHelper = new DbHelper(MyApp.getInstance());
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (AppItem app : apps) {
                ApplicationRelationsTable.addCacheRelation(db, app.getPackageName(), app.getId());
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }

    }

    private void sort(ArrayList<AppItem> apps) {
        Collections.sort(apps, new Comparator<AppItem>() {
            public int compare(AppItem topic, AppItem topic1) {
                if (topic.getFindedState() != topic1.getFindedState()) {
                    if (topic1.getFindedState() == AppItem.STATE_FINDED_AND_HAS_UPDATE)
                        return 1;
                    if (topic.getFindedState() == AppItem.STATE_FINDED_AND_HAS_UPDATE)
                        return -1;
                    if (topic1.getFindedState() == AppItem.STATE_UNFINDED)
                        return -1;
                    if (topic.getFindedState() == AppItem.STATE_UNFINDED)
                        return 1;
                }
                return topic.getTitle().toString().toUpperCase().compareTo(topic1.getTitle().toString().toUpperCase());
            }
        });
    }

    private boolean compareFromCache(ArrayList<AppItem> apps) {
        SQLiteDatabase db = null;
        SQLiteDatabase appsDb = null;
        try {

            DbHelper dbHelper = new DbHelper(MyApp.getInstance());
            db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery("select 1 from ApplicationRelations where byuser='false'", null);
            if (c.getCount() == 0) {
                c.close();
                return false;
            }
            c.close();
            ApplicationsDbHelper applicationsDbHelper = new ApplicationsDbHelper(MyApp.getInstance());
            appsDb = applicationsDbHelper.getReadableDatabase();
            for (AppItem app : apps) {
                ArrayList<PdaApplication> pdaApps;
                try {
                    pdaApps = ApplicationRelationsTable.getApplications(db, app.getPackageName());
                    for (PdaApplication pdaApplication : pdaApps) {
                        String id = Integer.toString(pdaApplication.AppUrl);
                        app.Ids.add(id);
                        app.setFindedState(AppItem.STATE_NORMAL);
                        app.setId(id);
                    }
                } catch (Throwable ex) {
                    Log.e(null, ex);
                }
            }
            db.execSQL("delete from ApplicationRelations where byuser='false'", null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
            if (appsDb != null)
                appsDb.close();

        }
        return false;
    }

    private void compareFromBases(ArrayList<String> appsName, ArrayList<AppItem> apps) {
        SQLiteDatabase db = null;
        SQLiteDatabase appsDb = null;
        try {

            DbHelper dbHelper = new DbHelper(MyApp.getInstance());
            db = dbHelper.getReadableDatabase();
            ApplicationsDbHelper applicationsDbHelper = new ApplicationsDbHelper(MyApp.getInstance());
            appsDb = applicationsDbHelper.getReadableDatabase();
            for (AppItem app : apps) {
                ArrayList<PdaApplication> pdaApps;
                try {
                    pdaApps = ApplicationRelationsTable.getApplications(db, app.getPackageName());
                    for (PdaApplication pdaApplication : pdaApps) {
                        String id = Integer.toString(pdaApplication.AppUrl);
                        app.Ids.add(id);
                        app.setFindedState(AppItem.STATE_NORMAL);
                        app.setId(id);
                    }
                    if (app.Ids.size() != 0) {
                        appsName.add(null);
                        continue;
                    }
                } catch (Throwable ex) {
                    Log.e(null, ex);
                }
                try {
                    pdaApps = ApplicationRelationsTable.getApplications(appsDb, normalizePackName(app.getDescription().toString()));

                    for (PdaApplication pdaApplication : pdaApps) {
                        String id = Integer.toString(pdaApplication.AppUrl);
                        app.Ids.add(id);
                        app.setFindedState(AppItem.STATE_NORMAL);
                        app.setId(id);
                    }
                    //if (app.Ids.size() != 1)
                    appsName.add(app.Ids.size() != 1 ? normalizeTitle(app.getTitle()) : null);
                } catch (Throwable ex) {
                    Log.e(null, ex);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
            if (appsDb != null)
                appsDb.close();

        }

    }

    private void compareFromSite(ArrayList<String> appsName, ArrayList<AppItem> apps) throws IOException {
        final String appCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=112220";
        final String gameCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=117270";
        // Client.getInstance().doOnOnProgressChanged(progressChangedListener, "Получение данных...");

        String gamesBody = Client.getInstance().loadPageAndCheckLogin(gameCatalogUrl, null);
        String appsBody = Client.getInstance().loadPageAndCheckLogin(appCatalogUrl, null);
        //  Client.getInstance().doOnOnProgressChanged(progressChangedListener, "Обработка данных...");
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?showtopic=(\\d+)[^\"]*?. target=._blank.>(.*?)</a>(.*?)</li>").matcher(gamesBody);
        compareFromMatcher(appsName, apps, m);

        m = Pattern.compile("<a href=\"(?:http://4pda.ru)?/forum/index.php\\?showtopic=(\\d+)\" target=._blank.>(.*?)</a>.*?(?:</b>)? - (.*?)<").matcher(appsBody);
        compareFromMatcher(appsName, apps, m);
    }

    private void compareFromMatcher(ArrayList<String> appsName, ArrayList<AppItem> apps, Matcher m) {
        while (m.find()) {
            String id = m.group(1);
            AppItem app;
            while ((app = findById(apps, id)) != null) {
                app.Ids.clear();
                app.setId(id);
                app.setDescription(Html.fromHtml(m.group(3)));
                app.setFindedState(AppItem.STATE_NORMAL);
            }

            String normTitle = AppsTab.normalizeTitle(m.group(2));
            for (int i = 0; i < appsName.size(); i++) {

                if (normTitle.equals(appsName.get(i))) {
                    app = apps.get(i);
                    app.Ids.clear();
                    app.setId(id);
                    app.setDescription(Html.fromHtml(m.group(3)));
                    app.setFindedState(AppItem.STATE_NORMAL);

                    appsName.set(i, null);

                }
            }
        }
    }


    private Boolean filterApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true;
        }
        return false;
    }

    private static final CharSequence normalizePattern = "\\d+|alpha|beta|pro|trial|free|plus|premium|donate|demo|paid|special|next|hd|ultimate|pc|lite|classic";

    public static String normalizeTitle(CharSequence title) {
        return title.toString().toLowerCase().replaceAll("\\.?(" + normalizePattern + ")$|\\.?\\d+\\.?|\\s+", "");

    }

    private static String normalizePackName(String packName) {
        return packName.toLowerCase().replaceAll("\\.?(" + normalizePattern + ")$", "").replaceAll("\\.(" + normalizePattern + ")\\.", "%");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != Activity.RESULT_OK) return;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        try {
            final IListItem topic = getItem(menuInfo);
            if (topic == null) return;
            //if (TextUtils.isEmpty(topic.getId())) return;
            final AppItem appItem = (AppItem) topic;
            menu.add("Связать с темой на форуме").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    AlertDialog.Builder builder = new AlertDialogBuilder(getContext());
                    builder.setTitle("Введите урл темы");

// Set up the input
                    final EditText input = new EditText(getContext());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setText(topic.getId());
                    builder.setView(input);
//                    builder.setNeutralButton("Выбрать..", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            IntentActivity.selectTopicDialog((Activity) getContext());
//                        }
//                    });
// Set up the buttons
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String text = input.getText().toString();
                            if (TextUtils.isEmpty(text)) {
                                Toast.makeText(getContext(), "Пустой урл!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Matcher m = Pattern.compile("showtopic=(\\d+)").matcher(text.trim());
                            if (!m.find()) {
                                m = Pattern.compile("(\\d+)").matcher(text.trim());
                                if (m.find()) {
                                    if (m.group(1).length() != text.trim().length()) {
                                        Toast.makeText(getContext(), "Неверный урл!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Неверный урл!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            ApplicationRelationsTable.addRealtion(appItem.getPackageName(), m.group(1));
                            appItem.setFindedState(AppItem.STATE_FINDED);
                            appItem.setId(m.group(1));
                            notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    return true;
                }
            });
        } catch (Throwable ex) {
            Log.e(getContext(), ex);
        }

        super.onCreateContextMenu(menu, v, menuInfo, handler);
    }

    private AppItem findById(ArrayList<AppItem> apps, CharSequence id) {
        for (int i = 0; i < apps.size(); i++) {
            AppItem app = apps.get(i);
            if (app.Ids.contains(id))
                return app;
        }
        return null;
    }
}
