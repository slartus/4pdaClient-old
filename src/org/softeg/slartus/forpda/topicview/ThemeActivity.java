package org.softeg.slartus.forpda.topicview;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.londatiga.android3d.ActionItem;
import net.londatiga.android3d.QuickAction;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.EditPostPlusActivity;
import org.softeg.slartus.forpda.ImageViewActivity;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.MyImageView;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.ReputationActivity;
import org.softeg.slartus.forpda.Tabs.TopicReadingUsersTab;
import org.softeg.slartus.forpda.Tabs.TopicWritersTab;
import org.softeg.slartus.forpda.classes.AdvWebView;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.classes.BbCodesPanel;
import org.softeg.slartus.forpda.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.classes.TopicAttaches;
import org.softeg.slartus.forpda.classes.TopicBodyBuilder;
import org.softeg.slartus.forpda.classes.WebViewExternals;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.common.HtmlUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.db.TopicsHistoryTable;
import org.softeg.slartus.forpda.download.DownloadsService;
import org.softeg.slartus.forpda.emotic.SmilesBbCodePanel;
import org.softeg.slartus.forpda.notes.NoteDialog;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.NotReportException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 28.09.11
 * Time: 14:43
 */
public class ThemeActivity extends BrowserViewsFragmentActivity {
    private AdvWebView webView;
    private Handler mHandler = new Handler();
    private ImageButton btnPrevSearch, btnNextSearch, btnCloseSearch;
    private EditText txtSearch;
    private RelativeLayout pnlSearch;
    private String m_ThemeUrl;
    private String m_LastUrl;
    private String m_Params;
    private ExtTopic m_Topic;

    private Boolean m_SpoilFirstPost = true;


    // подтверждение отправки
    private Boolean m_ConfirmSend = true;

    private SessionHistoryList m_History = new SessionHistoryList();


    // флаг добавлять подпись к сообщению
    private Boolean m_HidePostForm = false;

    // текст редактирования сообщения при переходе по страницам
    private String m_PostBody = "";
    // id сообщения к которому скроллить
    private String m_ScrollElement = null;
    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private int m_ScrollX = 0;
    // пост, с которым совершают какие-то действия в текущий момент

    public static String s_ThemeBody = null;
    public static String s_ThemeId = null;
    public static String s_Params = null;
    TopicViewMenuFragment mFragment1;
    private Gallery glrBbCodes;
    private Gallery glrSmiles;
    private EditText txtPost;
    private ImageButton tglSmiles;
    private LinearLayout message_panel;

    private boolean m_EnableSig = true;
    private boolean m_EnableEmo = true;
    public static Boolean LoadsImagesAutomatically = null;


    @Override
    public String Prefix() {
        return "theme";
    }

    protected void afterCreate() {
        getWindow().requestFeature(android.view.Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.theme);
        LoadsImagesAutomatically = null;
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        createActionMenu();

        Client client = Client.getInstance();

        client.addOnUserChangedListener(new Client.OnUserChangedListener() {
            public void onUserChanged(String user, Boolean success) {
                userChanged();
            }
        });
        client.addOnMailListener(new Client.OnMailListener() {
            public void onMail(int count) {
                mailsChanged();
            }
        });

        //setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL); чтобы поиск начинался при вводе текста

        pnlSearch = (RelativeLayout) findViewById(R.id.pnlSearch);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearch(txtSearch.getText().toString());
            }

            public void afterTextChanged(Editable editable) {

            }
        });

        message_panel = (LinearLayout) findViewById(R.id.message_panel);
        glrBbCodes = (Gallery) findViewById(R.id.glrBbCodes);
        glrSmiles = (Gallery) findViewById(R.id.glrSmiles);
        txtPost = (EditText) findViewById(R.id.edMessage);
        new BbCodesPanel(this, glrBbCodes, txtPost);
        new SmilesBbCodePanel(this, glrSmiles, txtPost);

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post();
            }
        });

        findViewById(R.id.btnAdvForm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                advPost();
            }
        });

        ((ToggleButton) findViewById(R.id.tglEnableEmo)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_EnableEmo = b;
            }
        });

        ((ToggleButton) findViewById(R.id.tglEnableSig)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_EnableSig = b;
            }
        });

        findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean[] enableEmo = {m_EnableEmo};
                final boolean[] enableSig = {m_EnableSig};
                new AlertDialogBuilder(ThemeActivity.this)
                        .setTitle("Дополнительно")
                        .setMultiChoiceItems(new CharSequence[]{"Включить смайлики", "Добавить подпись"},
                                new boolean[]{enableEmo[0], enableSig[0]},
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                        switch (i) {
                                            case 0:
                                                enableEmo[0] = b;
                                                break;
                                            case 1:
                                                enableSig[0] = b;
                                                break;
                                        }
                                    }
                                })
                        .setPositiveButton("Применить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                m_EnableEmo = enableEmo[0];
                                m_EnableSig = enableSig[0];
                            }
                        })
//                        .setNeutralButton("Расширенная форма", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                                advPost();
//                            }
//                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
            }
        });
        tglSmiles = (ImageButton) findViewById(R.id.tglSmiles);
        tglSmiles.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                int mode = 0;// всё скрыто
                if (glrSmiles.getVisibility() == View.VISIBLE)
                    mode = 1;// смайлы видны
                if (glrBbCodes.getVisibility() == View.VISIBLE)
                    mode = 2;// bb-коды видны
                switch (mode) {
                    case 0:
                        glrSmiles.setVisibility(View.VISIBLE);
                        tglSmiles.setImageResource(R.drawable.ic_btn_bbcode);
                        break;
                    case 1:
                        glrSmiles.setVisibility(View.GONE);
                        glrBbCodes.setVisibility(View.VISIBLE);
                        tglSmiles.setImageResource(R.drawable.ic_btn_keyboard);
                        break;
                    case 2:
                        glrSmiles.setVisibility(View.GONE);
                        glrBbCodes.setVisibility(View.GONE);
                        tglSmiles.setImageResource(R.drawable.ic_btn_emotics);
                        break;
                }
            }
        });
//
//        findViewById(R.id.tglbbCodes).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//
//                glrBbCodes.setVisibility(glrBbCodes.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//                //glrSmiles.setVisibility(glrSmiles.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//                //tglGallerySwitcher.setText(glrBbCodes.getVisibility() == View.VISIBLE ? ":)" : "Bb");
//            }
//        });


        btnPrevSearch = (ImageButton) findViewById(R.id.btnPrevSearch);
        btnPrevSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(false);
            }
        });
        btnNextSearch = (ImageButton) findViewById(R.id.btnNextSearch);
        btnNextSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(true);
            }
        });
        btnCloseSearch = (ImageButton) findViewById(R.id.btnCloseSearch);
        btnCloseSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeSearch();
            }
        });
        webView = (AdvWebView) findViewById(R.id.wvBody);
        registerForContextMenu(webView);
        setWebViewSettings(true);

        if (Build.VERSION.SDK_INT >= 7) {
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
            String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
            webView.getSettings().setAppCachePath(appCachePath);
            webView.getSettings().setAppCacheEnabled(true);
        }

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(this, "HTMLOUT");


        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            m_Data = intent.getData();


            return;
        }
        Bundle extras = intent.getExtras();

        m_ThemeUrl = IntentActivity.normalizeThemeUrl(extras.getString("ThemeUrl"));
        if (extras.containsKey("Params"))
            m_Params = extras.getString("Params");

        s_ThemeId = m_ThemeUrl;
        s_Params = m_Params;
//        String url = "showtopic=" + m_ThemeUrl + (TextUtils.isEmpty(m_Params) ? "" : ("&" + m_Params));
//        showTheme(url);
    }


    @JavascriptInterface
    public void showImgPreview(final String title, final String previewUrl, final String fullUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showImgPreview(ThemeActivity.this, title, previewUrl, fullUrl);
            }
        });
    }

    public static void showImgPreview(final Context context, String title, String previewUrl, final String fullUrl) {

        AlertDialog alertDialog = new AlertDialogBuilder(context)
                .setTitle(title)

                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Полная версия", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String url = fullUrl;
                        try {
                            URI uri = new URI(fullUrl);
                            if (!uri.isAbsolute())
                                url = "http://4pda.ru" + url;
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        ImageViewActivity.showImageUrl(context, url);
                    }
                })
                .create();

        MyImageView view = new MyImageView(context, alertDialog.getWindow().getWindowManager());
        alertDialog.setView(view);
        alertDialog.show();
        view.setImageDrawable(previewUrl);
    }

    public ImageButton getFullScreenButton() {
        return (ImageButton) findViewById(R.id.btnFullScreen);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            if (getIntent().getData() == null)
//                onBackPressed();
//            else
            {
                MyApp.showMainActivityWithoutBack(this);
            }

            return true;
        }

        return true;
    }

    private void userChanged() {
        mHandler.post(new Runnable() {
            public void run() {

                mFragment1.setUserMenu();

            }
        });

    }

    private void mailsChanged() {
        mHandler.post(new Runnable() {
            public void run() {

                mFragment1.setUserMenu();

            }
        });

    }

    protected void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (TopicViewMenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new TopicViewMenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();

    }

    private Uri m_Data = null;

    @Override
    public void onResume() {
        super.onResume();
        webView.setWebViewClient(new MyWebViewClient());
        //webView.setWebChromeClient(new MyWebChromeClient());
        if (s_ThemeBody != null) {
            String url = "showtopic=" + s_ThemeId + (TextUtils.isEmpty(s_Params) ? "" : ("&" + s_Params));
            s_ThemeId = null;
            s_Params = null;
            clearPostBody();

            closeSearch();

            GetThemeTask getThemeTask = new GetThemeTask(this);
            String themeBody = s_ThemeBody;
            s_ThemeBody = null;
            getThemeTask.execute(url.toString().replace("|", ""), themeBody);
        }
        if (s_ThemeId != null) {

            String url = "showtopic=" + s_ThemeId + (TextUtils.isEmpty(s_Params) ? "" : ("&" + s_Params));
            s_ThemeId = null;
            s_Params = null;
            clearPostBody();
            showTheme(url);
        }

        if (m_Data != null) {
            String url = m_Data.toString();

            m_Data = null;
            if (checkIsTheme(url)) {
                return;
            }
            IntentActivity.tryShowUrl(this, mHandler, url, false, true);

        }
    }

    private void clearPostBody() {
        txtPost.getText().clear();
//        mHandler.post(new Runnable() {
//            public void run() {
//                webView.loadUrl("javascript:clearPostBody();");
//            }
//        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtPost.getWindowToken(), 0);
    }

    private int loadScreenOrientationPref(SharedPreferences prefs) {
        return ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1);
    }

    public Handler getHandler() {
        return mHandler;
    }

    public ExtTopic getTopic() {
        return m_Topic;
    }

    public String getLastUrl() {
        if (m_LastUrl == null)
            m_LastUrl = "showtopic=" + m_ThemeUrl + (TextUtils.isEmpty(m_Params) ? "" : ("&" + m_Params));

        return m_LastUrl;
    }

    public boolean onSearchRequested() {
        message_panel.setVisibility(View.GONE);
        pnlSearch.setVisibility(View.VISIBLE);

        return false;
    }

    @JavascriptInterface
    public void showTopicAttaches(final String postBody) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TopicAttaches topicAttaches = new TopicAttaches();
                topicAttaches.parseAttaches(postBody);
                if (topicAttaches == null || topicAttaches.size() == 0) {
                    Toast.makeText(ThemeActivity.this, "Страница не имеет вложений", Toast.LENGTH_SHORT).show();
                    return;
                }
                final boolean[] selection = new boolean[topicAttaches.size()];
                new AlertDialogBuilder(ThemeActivity.this)
                        .setTitle("Вложения")
                        .setMultiChoiceItems(topicAttaches.getList(), selection, new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                selection[i] = b;
                            }
                        })
                        .setPositiveButton("Скачать", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                if (!Client.getInstance().getLogined()) {
                                    new AlertDialogBuilder(ThemeActivity.this)
                                            .setTitle("Внимание!")
                                            .setMessage("Для скачивания файлов с сайта необходимо залогиниться!")
                                            .setPositiveButton("ОК", null)
                                            .create().show();
                                    return;
                                }
                                for (int j = 0; j < selection.length; j++) {
                                    if (!selection[j]) continue;
                                    DownloadsService.download(ThemeActivity.this, topicAttaches.get(j).getUri());
                                }
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    public void showTopicAttaches() {
        try {
            TopicAttaches res = new TopicAttaches();

            webView.loadUrl("javascript:window.HTMLOUT.showTopicAttaches('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        } catch (Throwable ex) {
            Log.e(this, ex);
        }


    }

    public void rememberScrollX() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);

        }
    }


    private void doSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        if (Build.VERSION.SDK_INT >= 16) {
            webView.findAllAsync(query);
        } else {
            webView.findAll(query);
        }
        try {
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(webView, true);
        } catch (Throwable ignored) {
        }
        onSearchRequested();
    }

    private void closeSearch() {
        mHandler.post(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= 16) {
                    webView.findAllAsync("");
                } else {
                    webView.findAll("");
                }

                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webView, false);
                } catch (Throwable ignored) {
                }

                pnlSearch.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
            }
        });

    }

    @Override
    protected void loadPreferences(SharedPreferences prefs) {
        super.loadPreferences(prefs);
        LoadsImagesAutomatically = WebViewExternals.isLoadImages("theme");
        m_SpoilFirstPost = prefs.getBoolean("theme.SpoilFirstPost", true);
        m_ConfirmSend = prefs.getBoolean("theme.ConfirmSend", true);
        m_HidePostForm = prefs.getBoolean("theme.HidePostForm", false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    @JavascriptInterface
    public void showPostLinkMenu(final String postId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLinkMenu(org.softeg.slartus.forpda.classes.Post.getLink(m_Topic.getId(), postId), postId);
            }
        })
        ;
    }

    public void showLinkMenu(final String link) {
        showLinkMenu(link, "");
    }

    public void showLinkMenu(final String link, String postId) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, ThemeActivity.this, m_Topic.getTitle().toString(), "", link, m_Topic.getId(),
                m_Topic.getTitle().toString(), postId, "", "");

    }

    @Override
    public void onBackPressed() {
        if (pnlSearch.getVisibility() == View.VISIBLE) {
            closeSearch();
            return;
        }
        if (getCurrentFullScreen()) {
            updateFullscreenStatus(false);

            return;
        }

        if (!m_History.isEmpty()) {
            m_FromHistory = true;
            SessionHistory history = m_History.get(m_History.size() - 1);
            m_History.remove(m_History.size() - 1);
            m_ScrollX = history.getX();
            m_ScrollY = history.getY();
            showTheme(history.getUrl());
        } else {
            getPostBody();
            if (!TextUtils.isEmpty(m_PostBody)) {
                new AlertDialogBuilder(ThemeActivity.this)
                        .setTitle("Подтвердите действие")
                        .setMessage("Имеется введенный текст сообщения! Закрыть тему?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                clear();
                                ThemeActivity.super.onBackPressed();
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                clear();
                super.onBackPressed();
            }
        }
    }

    public void clear() {
        clear(false);
    }

    public void clear(Boolean clearChache) {
        webView.setPictureListener(null);
        webView.setWebViewClient(null);
        webView.loadData("<html><head></head><body bgcolor=" + MyApp.getInstance().getCurrentThemeName() + "></body></html>", "text/html", "UTF-8");
        if (clearChache)
            webView.clearCache(true);
        if (m_Topic != null)
            m_Topic.dispose();
        m_Topic = null;
    }


    public String getPostBody() {
//        if (!Functions.isWebviewAllowJavascriptInterface(this))
//            return m_PostBody;
//
//        try {
//            webView.loadUrl("javascript:getPostBody();");
//            Thread.sleep(350);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return m_PostBody;
        m_PostBody = txtPost.getText().toString();
        return m_PostBody;
    }

    public void openActionMenu(final String postId, Boolean canEdit, Boolean canDelete) {
        try {
            final QuickAction mQuickAction = new QuickAction(this);

            ActionItem actionItem;


            int claimPosition = -1;
            if (Client.getInstance().getLogined()) {
                actionItem = new ActionItem();
                actionItem.setIcon(getResources().getDrawable(R.drawable.ic_action_claim));
                actionItem.setTitle("Жалоба");
                claimPosition = mQuickAction.addActionItem(actionItem);
            }

            int editPosition = -1;
            if (canEdit) {
                actionItem = new ActionItem();
                actionItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_edit));
                actionItem.setTitle("Редактировать");

                editPosition = mQuickAction.addActionItem(actionItem);
            }


            int deletePosition = -1;
            if (canDelete) {
                actionItem = new ActionItem();
                actionItem.setTitle("Удалить");
                actionItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_delete));
                deletePosition = mQuickAction.addActionItem(actionItem);
            }

            int plusOdinPosition = -1;
            int minusOdinPosition = -1;
            if (!canEdit && !canDelete && Client.getInstance().getLogined()) {

                actionItem = new ActionItem();
                actionItem.setTitle("+1 посту");
                actionItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_rating_good
                        //MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark)
                ));
                plusOdinPosition = mQuickAction.addActionItem(actionItem);

                actionItem = new ActionItem();
                actionItem.setTitle("-1 посту");
                actionItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_rating_bad
                        //MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark)
                ));
                minusOdinPosition = mQuickAction.addActionItem(actionItem);
            }

            int notePosition = -1;

            actionItem = new ActionItem();
            actionItem.setTitle("Заметка");
            actionItem.setIcon(getResources().getDrawable(R.drawable.ic_action_attach));
            notePosition = mQuickAction.addActionItem(actionItem);


            final int finalDeletePosition = deletePosition;
            final int finalEditPosition = editPosition;

            final int finalClaimPosition = claimPosition;
            final int finalPlusOdinPosition = plusOdinPosition;
            final int finalMinusOdinPosition = minusOdinPosition;
            final int finalNotePosition = notePosition;
            mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(QuickAction source, int pos, int actionId) {
                    if (pos == finalDeletePosition) {
                        prepareDeleteMessage(postId);

                    } else if (pos == finalEditPosition) {
                        EditPostPlusActivity.editPost(ThemeActivity.this, m_Topic.getForumId(), m_Topic.getId(), postId, m_Topic.getAuthKey());
                    } else if (pos == finalClaimPosition) {
                        org.softeg.slartus.forpda.classes.Post.claim(ThemeActivity.this, mHandler, m_Topic.getId(), postId);
                    } else if (pos == finalPlusOdinPosition) {
                        org.softeg.slartus.forpda.classes.Post.plusOne(ThemeActivity.this, mHandler, postId);
                    } else if (pos == finalMinusOdinPosition) {
                        org.softeg.slartus.forpda.classes.Post.minusOne(ThemeActivity.this, mHandler, postId);
                    } else if (pos == finalNotePosition) {
                        NoteDialog.showDialog(mHandler, ThemeActivity.this, m_Topic.getTitle().toString(), null,
                                "http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + "&view=findpost&p=" + postId,
                                m_Topic.getId(), m_Topic.getTitle().toString(), postId, null, null);
                    }
                }
            });

            mQuickAction.show(webView, webView.getLastMotionEvent());
        } catch (Throwable ex) {
            Log.e(this, ex);
        }
    }

    private void showThemeBody(String body) {
        try {
            setScrollElement();
            ThemeActivity.this.setTitle(m_Topic.getTitle());
            getSupportActionBar().setSubtitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount());
            webView.loadDataWithBaseURL("http://4pda.ru/forum/", body, "text/html", "UTF-8", null);

            TopicsHistoryTable.addHistory(m_Topic, m_Params);
        } catch (Exception ex) {
            Log.e(ThemeActivity.this, ex);
        }
    }

    public void showMessagePanel() {
        pnlSearch.setVisibility(View.GONE);
        message_panel.setVisibility(View.VISIBLE);
        message_panel.setEnabled(Client.getInstance().getLogined());
    }

    public void hideMessagePanel() {
        message_panel.setVisibility(View.GONE);
        hideKeyboard();
    }

    public void toggleMessagePanelVisibility() {
        if (message_panel.getVisibility() == View.GONE)
            showMessagePanel();
        else
            hideMessagePanel();

    }

    public boolean getLoadsImagesAutomatically() {
        return WebViewExternals.isLoadImages("theme");
    }

    public void setLoadsImagesAutomatically(boolean loadsImagesAutomatically) {
        LoadsImagesAutomatically = loadsImagesAutomatically;
        new AlertDialogBuilder(this)
                .setTitle("Выберите действие")
                .setMessage("Обновить страницу?")
                .setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        reloadTopic();
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

    private class MyPictureListener implements WebView.PictureListener {
        Thread m_ScrollThread;
        Boolean m_Wait = true;

        public void onNewPicture(WebView view, Picture arg1) {
            if (TextUtils.isEmpty(m_ScrollElement)) {
                //webView.setPictureListener(null);
                return;
            }

            if (m_ScrollThread != null) return;

            m_ScrollThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mHandler.post(new Runnable() {
                        public void run() {
                            tryScrollToElement();
                        }
                    });
                }
            });


            m_ScrollThread.start();
        }

        private void tryScrollToElement() {
            if (m_ScrollY != 0) {
                webView.scrollTo(0, m_ScrollY);
                m_ScrollY = 0;
            } else if (!TextUtils.isEmpty(m_ScrollElement)) {
                webView.scrollTo(0, 100);
                webView.scrollTo(0, 0);
                webView.loadUrl("javascript: scrollToElement('entry" + m_ScrollElement + "');");
            }
            m_ScrollElement = null;
            webView.setPictureListener(null);
        }
    }

    private final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialogBuilder(ThemeActivity.this)
                    .setTitle("Удаление сообщения")
                    .setMessage(message)
                    .setPositiveButton("Удалить",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .create()
                    .show();

            return true;
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            setSupportProgressBarIndeterminateVisibility(true);
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            setSupportProgressBarIndeterminateVisibility(false);
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            // if (ThemeActivity.this.webView.GetJavascriptInterfaceBroken())
            {
                if (url.contains("HTMLOUT.ru")) {
                    Uri uri = Uri.parse(url);
                    try {
                        String function = uri.getPathSegments().get(0);
                        String query = uri.getQuery();
                        Class[] parameterTypes = null;
                        String[] parameterValues = new String[0];
                        if (!TextUtils.isEmpty(query)) {
                            Matcher m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(url);
                            ArrayList<String> objs = new ArrayList<String>();

                            while (m.find()) {
                                objs.add(Uri.decode(m.group(2)));
                            }
                            parameterValues = new String[objs.size()];
                            parameterTypes = new Class[objs.size()];
                            for (int i = 0; i < objs.size(); i++) {
                                parameterTypes[i] = String.class;
                                parameterValues[i] = objs.get(i);
                            }
                        }
                        Method method = ThemeActivity.class.getMethod(function, parameterTypes);

                        method.invoke(ThemeActivity.this, parameterValues);
                    } catch (SecurityException e) {
                        Log.eToast(ThemeActivity.this, e);
                    } catch (NoSuchMethodException e) {
                        Log.eToast(ThemeActivity.this, e);
                    } catch (IllegalArgumentException e) {
                        Log.eToast(ThemeActivity.this, e);
                    } catch (IllegalAccessException e) {
                        Log.eToast(ThemeActivity.this, e);
                    } catch (InvocationTargetException e) {
                        Log.eToast(ThemeActivity.this, e);
                    } catch (Exception e) {
                        Log.eToast(ThemeActivity.this, e);
                    }
                    return true;
                }

            }
            m_ScrollY = 0;
            m_ScrollX = 0;
            if (checkIsTheme(url))
                return true;

            if (tryDeletePost(url))
                return true;

            if (tryQuote(url))
                return true;
            IntentActivity.tryShowUrl(ThemeActivity.this, mHandler, url, true, false, m_Topic.getAuthKey());

            return true;
        }

        private boolean tryDeletePost(String url) {

            Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Mod&CODE=04&f=(\\d+)&t=(\\d+)&p=(\\d+)&st=(\\d+)&auth_key=(.*?)").matcher(url);
            if (m.find()) {

                prepareDeleteMessage(m.group(3));
                return true;
            }
            return false;
        }

        private boolean tryQuote(final String url) {

            Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Post&CODE=02&f=\\d+&t=\\d+&qpid=\\d+").matcher(url);
            if (m.find()) {
                if (url.contains("first=1")) {
                    new AlertDialogBuilder(ThemeActivity.this)
                            .setTitle("Подтвердите действие")
                            .setMessage("Администрация портала не рекомендует цитировать полный текст шапки и может вынести предупреждение за такие действия!")
                            .setPositiveButton("Цитировать", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    new QuoteLoader(ThemeActivity.this).execute(url);
                                }
                            })
                            .setNegativeButton("Отмена", null)
                            .create().show();
                    return true;
                }
                new QuoteLoader(ThemeActivity.this).execute(url);
                return true;
            }
            return false;
        }
    }

    public void catchSelectedText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void getSelectedText() {
        webView.loadUrl("javascript:getSelectedText();");
    }


    private boolean checkIsTheme(String url) {
        url = IntentActivity.normalizeThemeUrl(url);

        String[] patterns = {
                Client.SITE + "/*forum/*index.php\\?((.*)?showtopic=[^\"]*)",
                Client.SITE + "/*forum/*index.php\\?((.*)?act=findpost&pid=\\d+([^\"]*)?)",
                Client.SITE + "/*index.php\\?((.*)?act=findpost&pid=\\d+([^\"]*)?)"
        };
        for (String pattern : patterns) {
            Matcher m = Pattern.compile(pattern).matcher(url);
            if (m.find()) {
                showTheme(m.group(1));
                return true;
            }
        }

        return false;
    }


    private void setThemeParams(String url) {
        Pattern pattern = Pattern.compile("showtopic=(\\d+)(&(.*))?");
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            m_ThemeUrl = m.group(1);

            m_Params = m.group(3);
        } else {
            m_ThemeUrl = null;

            m_Params = null;
        }
    }

    public void reloadTopic() {
        rememberScrollX();

        showTheme(getLastUrl());
    }

    public void showTheme(String url) {
        try {
            closeSearch();
            webView.clearCache(true);

            webView.setWebViewClient(new MyWebViewClient());
            if (m_ScrollY != 0)
                webView.setPictureListener(new MyPictureListener());

            webView.loadData("<html><head></head><body bgcolor=" + MyApp.getInstance().getCurrentThemeName() + "></body></html>", "text/html", "UTF-8");


            saveSessionHistory();

            GetThemeTask getThemeTask = new GetThemeTask(this);
            getThemeTask.execute(url.replace("|", ""));
        } catch (Throwable ex) {
            Log.e(this, ex);
        }
    }


    public AdvWebView getWebView() {
        return webView;
    }

    private void sendMessage(String body) {
        hideKeyboard();
        PostTask postTask = new PostTask(ThemeActivity.this);
        postTask.Post = body;
        postTask.execute();
    }

    private void prepareDeleteMessage(final String postId) {
        new AlertDialogBuilder(ThemeActivity.this)
                .setTitle("Подтвердите действие")
                .setMessage("Вы действительно хотите удалить это сообщение?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(postId);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                    }
                })
                .create()
                .show();
    }

    private void deleteMessage(final String postId) {
        final ProgressDialog dialog = new AppProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Удаление сообщения...");
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                Throwable ex = null;

                try {
                    org.softeg.slartus.forpda.classes.Post.delete(postId, m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
                } catch (Throwable e) {
                    ex = e;
                }

                final Throwable finalEx = ex;
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        } catch (Throwable ignoredEx) {

                        }
                        if (finalEx != null)
                            Log.e(ThemeActivity.this, finalEx);

                        m_ScrollY = 0;
                        m_ScrollX = 0;
                        showTheme(getLastUrl());
                    }
                });
            }
        }).start();

    }

    @JavascriptInterface
    public void showReadingUsers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TopicReadingUsersTab.show(ThemeActivity.this, getTopic().getId());
                } catch (ActivityNotFoundException e) {
                    Log.e(ThemeActivity.this, e);
                }
            }
        });

    }

    @JavascriptInterface
    public void showWriters() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TopicWritersTab.show(ThemeActivity.this, getTopic().getId());
            }
        });
    }

    @JavascriptInterface
    public void showUserMenu(final String userId, final String userNick) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ForumUser.showUserMenu(ThemeActivity.this, webView, userId, userNick, new ForumUser.InsertNickInterface() {
                    @Override
                    public void insert(String text) {
                        insertTextToPost(text);
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public void insertTextToPost(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        int selection = txtPost.getSelectionStart();
                        txtPost.getText().insert(selection == -1 ? 0 : selection, text);
                        showMessagePanel();
                    }
                });
            }
        });
    }

    // class JavaScriptInterface
    //{

    public void msgToClipboard(String msgId) {
        if (m_Topic != null)
            ExtUrl.copyLinkToClipboard(ThemeActivity.this, "http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + "&view=findpost&p=" + msgId);

    }

//        public void showUserMenu(final String userId, final String userNick) {
//            ThemeActivity.this.showUserMenu(userId,userNick);
//        }
//
//        public void showLinkMenu(String link) {
//            ThemeActivity.this.showLinkMenu(link);
//        }


    public void setPostBody(String postBody) {
        m_PostBody = postBody;
    }

    public void enableemo() {
        //m_EnableEmo = !m_EnableEmo;
    }

    public void enablesig() {
        //m_Enablesig = !m_Enablesig;
    }

    public String getPostText(String postId, String date, String userNick, String innerText) {

        return org.softeg.slartus.forpda.classes.Post.getQuote(postId, date, userNick, innerText);
    }

    @JavascriptInterface
    public void showPostMenu(final String postId, final String canEdit, final String canDelete) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ThemeActivity.this.openActionMenu(postId, "1".equals(canEdit), "1".equals(canDelete));
            }
        });
    }

    public void advPost() {
        EditPostPlusActivity.newPost(ThemeActivity.this, m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey(),
                getPostBody());

    }


    public void post() {

        post(getPostBody());

    }

    @JavascriptInterface
    public void post(final String body) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(body))
                    return;

                if (m_ConfirmSend) {
                    new AlertDialogBuilder(ThemeActivity.this)
                            .setTitle("Уверены?")
                            .setMessage("Подтвердите отправку")
                            .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sendMessage(body);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton("Отмена", null)
                            .create().show();
                } else {
                    sendMessage(body);
                }
            }
        });
    }

    @JavascriptInterface
    public void nextPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_ScrollY = 0;
                m_ScrollX = 0;


                showTheme("showtopic=" + m_ThemeUrl + "&st=" + m_Topic.getCurrentPage() * m_Topic.getPostsPerPageCount(getLastUrl()));
            }
        });
    }

    @JavascriptInterface
    public void prevPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_ScrollY = 0;
                m_ScrollX = 0;
                showTheme("showtopic=" + m_ThemeUrl + "&st=" + (m_Topic.getCurrentPage() - 2) * m_Topic.getPostsPerPageCount(getLastUrl()));
            }
        });

    }

    @JavascriptInterface
    public void firstPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_ScrollY = 0;
                m_ScrollX = 0;
                showTheme("showtopic=" + m_ThemeUrl);
            }
        });
    }

    @JavascriptInterface
    public void lastPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_ScrollY = 0;
                m_ScrollX = 0;
                showTheme("showtopic=" + m_ThemeUrl + "&st=" + (m_Topic.getPagesCount() - 1) * m_Topic.getPostsPerPageCount(getLastUrl()));
            }
        });
    }

    @JavascriptInterface
    public void jumpToPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    final CharSequence[] pages = new CharSequence[m_Topic.getPagesCount()];

                    final int postsPerPage = m_Topic.getPostsPerPageCount(getLastUrl());

                    for (int p = 0; p < m_Topic.getPagesCount(); p++) {
                        pages[p] = "Стр. " + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.select_page_layout, null);

                    final ListView listView = (ListView) view.findViewById(R.id.lstview);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(ThemeActivity.this,
                            android.R.layout.simple_list_item_single_choice, pages);
                    // присваиваем адаптер списку
                    listView.setAdapter(adapter);

                    final EditText txtNumberPage = (EditText) view.findViewById(R.id.txtNumberPage);
                    txtNumberPage.setText(Integer.toString(m_Topic.getCurrentPage()));
                    txtNumberPage.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                            if (txtNumberPage.getTag() != null && (Boolean) txtNumberPage.getTag() == false)
                                return;
                            if (TextUtils.isEmpty(charSequence)) return;
                            try {
                                int value = Integer.parseInt(charSequence.toString());
                                value = Math.min(pages.length - 1, value - 1);
                                listView.setTag(false);
                                listView.setItemChecked(value, true);
                                listView.setSelection(value);
                            } catch (Throwable ex) {
                                Log.e(ThemeActivity.this, ex);
                            } finally {
                                listView.setTag(true);
                            }

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (listView.getTag() != null && (Boolean) listView.getTag() == false)
                                return;
                            txtNumberPage.setTag(false);
                            try {
                                txtNumberPage.setText(Integer.toString((int) l + 1));
                            } catch (Throwable ex) {
                                Log.e(ThemeActivity.this, ex);
                            } finally {
                                txtNumberPage.setTag(true);
                            }
                        }
                    });

                    listView.setItemChecked(m_Topic.getCurrentPage() - 1, true);
                    listView.setSelection(m_Topic.getCurrentPage() - 1);

                    new AlertDialogBuilder(ThemeActivity.this)
                            .setTitle("Перейти к странице")
                            .setView(view)
//                .setSingleChoiceItems(pages, m_Topic.getCurrentPage() - 1, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                        showTheme("showtopic=" + m_ThemeUrl + "&st=" + i * postsPerPage);
//                    }
//                })
                            .setPositiveButton("Перейти", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    m_ScrollY = 0;
                                    m_ScrollX = 0;
                                    showTheme("showtopic=" + m_ThemeUrl + "&st=" + listView.getCheckedItemPosition() * postsPerPage);

                                }
                            })
                            .setNegativeButton("Отмена", null)
                            .setCancelable(true)
                            .create()
                            .show();
                } catch (Throwable ex) {
                    Log.e(ThemeActivity.this, ex);
                }
            }
        });

    }

    @JavascriptInterface
    public void plusRep(final String postId, final String userId, final String userNick) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showChangeRep(postId, userId, userNick, "add", "Поднять репутацию");
            }
        });
    }

    @JavascriptInterface
    public void minusRep(final String postId, final String userId, final String userNick) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showChangeRep(postId, userId, userNick, "minus", "Опустить репутацию");
            }
        });
    }

    @JavascriptInterface
    public void claim(final String postId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                org.softeg.slartus.forpda.classes.Post.claim(ThemeActivity.this, mHandler, m_Topic.getId(), postId);
            }
        });

    }

    public void showRep(final String userId) {
        ReputationActivity.showRep(this, userId);
    }

    @JavascriptInterface
    public void showRepMenu(final String postId, final String userId, final String userNick, final String canPlus, final String canMinus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final QuickAction mQuickAction = new QuickAction(ThemeActivity.this);
                ActionItem actionItem;


                int plusRepPosition = -1;
                if ("1".equals(canPlus)) {
                    actionItem = new ActionItem();

                    actionItem.setTitle("+1");
                    actionItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_rating_good
                            //MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark)
                    ));
                    plusRepPosition = mQuickAction.addActionItem(actionItem);
                }

                int showRepPosition = -1;

                actionItem = new ActionItem();
                actionItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_view));
                actionItem.setTitle("Посмотреть");

                showRepPosition = mQuickAction.addActionItem(actionItem);

                int minusRepPosition = -1;
                if ("1".equals(canMinus)) {
                    actionItem = new ActionItem();

                    actionItem.setTitle("-1");
                    actionItem.setIcon(getResources().getDrawable(R.drawable.ic_menu_rating_good
                            //MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark)
                    ));
                    minusRepPosition = mQuickAction.addActionItem(actionItem);
                }


                if (mQuickAction.getItemsCount() == 0) return;


                final int finalMinusRepPosition = minusRepPosition;
                final int finalShowRepPosition = showRepPosition;
                final int finalPlusRepPosition = plusRepPosition;
                mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(QuickAction source, int pos, int actionId) {
                        if (pos == finalMinusRepPosition) {
                            ReputationActivity.minusRep(ThemeActivity.this, mHandler, postId, userId, userNick);
                        } else if (pos == finalShowRepPosition) {
                            showRep(userId);
                        } else if (pos == finalPlusRepPosition) {
                            ReputationActivity.plusRep(ThemeActivity.this, mHandler, postId, userId, userNick);
                        }
                    }
                });

                mQuickAction.show(webView, webView.getLastMotionEvent());
            }
        });

    }

    private void showChangeRep(final String postId, String userId, String userNick, final String type, String title) {
        ForumUser.startChangeRep(ThemeActivity.this, mHandler, userId, userNick, postId, type, title);

    }
    // }

    private void saveSessionHistory() {
        if (m_FromHistory) {
            m_FromHistory = false;
            return;
        }

        m_History.addSessionHistory(m_ThemeUrl, m_Params, webView.getScrollX(), webView.getScrollY());

    }

    private void setScrollElement() {
        m_ScrollElement = null;
        URI redirectUri = Client.getInstance().getRedirectUri();
        String url = redirectUri != null ? redirectUri.toString() : getLastUrl();
        if (url != null) {
            Pattern p = Pattern.compile("#entry(\\d+)");
            Matcher m = p.matcher(url);
            if (m.find()) {
                m_ScrollElement = m.group(1);
            }
        }
        if (m_ScrollElement != null) {
            webView.setPictureListener(new MyPictureListener());
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });

        }
    }


    private class GetThemeTask extends AsyncTask<String, String, Boolean> {

        private final ProgressDialog dialog;

        public GetThemeTask(Context context) {
            dialog = new AppProgressDialog(context);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }


        protected void onCancelled() {
            Toast.makeText(ThemeActivity.this, "Отменено", Toast.LENGTH_SHORT).show();

        }

        private String m_ThemeBody;

        private CharSequence prepareTopicUrl(CharSequence url) {
            Matcher paramsMatcher = Pattern.compile("(\\w+=\\w+(?:&)?|#\\w+)").matcher(url);
            StringBuilder res = new StringBuilder();
            while (paramsMatcher.find()) {
                res.append(paramsMatcher.group(1));
            }
            return res;
        }

        @Override
        protected Boolean doInBackground(String... forums) {
            String pageBody = null;
            try {

                if (isCancelled()) return false;
                Client client = Client.getInstance();


                if (forums.length == 1) {

                    m_LastUrl = forums[0];

                    mHandler.post(new Runnable() {
                        public void run() {
                            getPostBody();
                        }
                    });

                    pageBody = client.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?" + prepareTopicUrl(m_LastUrl), null);

                } else
                    pageBody = forums[1];

                String lastUrl = client.getRedirectUri() == null ? m_LastUrl : client.getRedirectUri().toString();

                TopicBodyBuilder topicBodyBuilder = client.parseTopic(pageBody, MyApp.getInstance(), lastUrl,
                        m_SpoilFirstPost);


                setThemeParams(Client.getInstance().getRedirectUri() != null ? Client.getInstance().getRedirectUri().toString() : m_LastUrl);

                m_Topic = topicBodyBuilder.getTopic();

                m_ThemeBody = topicBodyBuilder.getBody();

                topicBodyBuilder.clear();
                return true;
            } catch (Throwable e) {
                m_ThemeBody = pageBody;
                // Log.e(ThemeActivity.this, e);
                ex = e;
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(final String... progress) {
            mHandler.post(new Runnable() {
                public void run() {
                    if (dialog != null)
                        dialog.setMessage(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Загрузка темы...");
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        private Throwable ex;

        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e(null, ex);
            }

            if (isCancelled()) return;

            if (success) {
                showThemeBody(m_ThemeBody);
            } else {
                if (ex.getClass() != NotReportException.class) {


                    ThemeActivity.this.setTitle(ex.getMessage());


                    webView.loadDataWithBaseURL("http://4pda.ru/forum/", m_ThemeBody, "text/html", "UTF-8", null);

                }
                Log.e(ThemeActivity.this, ex, new Runnable() {
                    @Override
                    public void run() {
                        showTheme(getLastUrl());
                    }
                });
            }
        }
    }

    private Boolean getEnableSig() {
        return m_EnableSig;
    }

    private Boolean getEnableEmo() {
        return m_EnableEmo;
    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;
        public String Post;
        private String mPostResult = null;
        private String mError = null;

        public PostTask(Context context) {

            dialog = new AppProgressDialog(context);
        }

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = Client.getInstance().reply(m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey(),
                        Post, getEnableSig(), getEnableEmo(), true, null);
                mError = org.softeg.slartus.forpdaapi.Post.checkPostErrors(mPostResult);

                String lastUrl = Client.getInstance().getRedirectUri() == null ? getLastUrl() : Client.getInstance().getRedirectUri().toString();
                TopicBodyBuilder topicBodyBuilder = Client.getInstance().parseTopic(mPostResult, MyApp.getInstance(), lastUrl,
                        m_SpoilFirstPost);

                setThemeParams(Client.getInstance().getRedirectUri() != null ? Client.getInstance().getRedirectUri().toString() : getLastUrl());
                m_Topic = topicBodyBuilder.getTopic();
                m_ThemeBody = topicBodyBuilder.getBody();

                topicBodyBuilder.clear();

                return true;
            } catch (Throwable e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if (!TextUtils.isEmpty(mError)) {
                    Toast.makeText(ThemeActivity.this, "Ошибка: " + mPostResult, Toast.LENGTH_LONG).show();
                    //return;
                }
                //webView.loadUrl("javascript:clearPostBody();");
                clearPostBody();
                hideMessagePanel();
                showThemeBody(m_ThemeBody);


            } else {
                if (ex != null)
                    Log.e(ThemeActivity.this, ex, new Runnable() {
                        @Override
                        public void run() {
                            sendMessage(Post);
                        }
                    });
                else
                    Toast.makeText(ThemeActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * A fragment that displays a menu.  This fragment happens to not
     * have a UI (it does not implement onCreateView), but it could also
     * have one if it wanted.
     */
    public class QuoteLoader extends AsyncTask<String, String, Boolean> {

        private final ProgressDialog dialog;

        public QuoteLoader(Context context) {
            dialog = new AppProgressDialog(context);
        }

        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Загрузка цитаты...");
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        private String m_Quote = "";
        private String m_Url;


        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                m_Url = strings[0];

                String page = Client.getInstance().performGet(m_Url);
                Matcher m = Pattern.compile("<textarea name=\"Post\".*?>([\\s\\S]*?)</textarea>").matcher(page);
                if (m.find()) {
                    String quote = m.group(1);
                    if (quote != null)
                        quote = quote.trim();
                    m_Quote = HtmlUtils.modifyHtmlQuote(quote)
//                            .replace("'", "\\'").replace("\n", "\\n")
                    ;
                }
                return true;
            } catch (Throwable e) {
                // Log.e(ThemeActivity.this, e);
                ex = e;
                return false;
            }

        }

        private Throwable ex;

        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e(null, ex);
            }

            if (isCancelled()) return;

            if (success) {
                insertTextToPost(m_Quote);

            } else {
                Log.e(ThemeActivity.this, ex, new Runnable() {
                    @Override
                    public void run() {
                        new QuoteLoader(ThemeActivity.this).execute(m_Url);
                    }
                });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString("LastUrl", getLastUrl());
            outState.putString("ScrollElement", m_ScrollElement);

            outState.putString("ThemeUrl", m_ThemeUrl);
            outState.putString("Params", m_Params);

            outState.putBoolean("Enablesig", getEnableSig());
            outState.putBoolean("EnableEmo", getEnableEmo());
            outState.putBoolean("FromHistory", m_FromHistory);

            outState.putParcelable("SessionHistory", m_History);
            outState.putString("LoadsImagesAutomatically", LoadsImagesAutomatically == null ? "null" : (LoadsImagesAutomatically ? "1" : "0"));
        } catch (Throwable ex) {
            Log.e(this, ex);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        try {
            m_LastUrl = outState.getString("LastUrl");
            m_ScrollElement = outState.getString("ScrollElement");

            m_ThemeUrl = outState.getString("ThemeUrl");
            m_Params = outState.getString("Params");

            if (m_ScrollElement != null && !TextUtils.isEmpty(m_ScrollElement))
                webView.setPictureListener(new MyPictureListener());
            m_EnableSig = outState.getBoolean("Enablesig");
            m_EnableEmo = outState.getBoolean("EnableEmo");
            m_FromHistory = outState.getBoolean("FromHistory");

            String sLoadsImagesAutomatically = outState.getString("LoadsImagesAutomatically");
            LoadsImagesAutomatically = "null".equals(sLoadsImagesAutomatically) ? null : Boolean.parseBoolean(sLoadsImagesAutomatically);

            m_History = outState.getParcelable("SessionHistory");
            loadPreferences(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        } catch (Throwable ex) {
            Log.e(this, ex);
        }
    }

    @Override
    public void onPause() {
        super.onPause();


        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }


    @Override
    public void onStop() {
        super.onStop();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }


}
