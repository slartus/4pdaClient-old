package org.softeg.slartus.forpda.qms_2_0;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import org.softeg.slartus.forpda.BaseActivity;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AdvWebView;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.classes.BbCodesPanel;
import org.softeg.slartus.forpda.classes.HtmlBuilder;
import org.softeg.slartus.forpda.classes.IWebViewContainer;
import org.softeg.slartus.forpda.classes.WebViewExternals;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.emotic.Smiles;
import org.softeg.slartus.forpda.emotic.SmilesBbCodePanel;
import org.softeg.slartus.forpda.prefs.HtmlPreferences;
import org.softeg.slartus.forpdaapi.Qms_2_0;
import org.softeg.slartus.forpdacommon.ExtPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 15:50
 */
public class QmsChatActivity extends BaseFragmentActivity implements IWebViewContainer {
    private Handler mHandler = new Handler();
    private AdvWebView wvChat;
    private String m_Id;
    private String m_TId;
    private String m_Nick = "";
    private String m_ThemeTitle = "";
    private String m_PageBody;
    private long m_LastBodyLength = 0;

    private EditText edMessage;

    private MenuFragment mFragment1;
    private long m_UpdateTimeout = 15000;
    private Timer m_UpdateTimer = new Timer();

    private static final String MID_KEY = "mid";
    private static final String TID_KEY = "tid";
    private static final String THEME_TITLE_KEY = "theme_title";
    private static final String NICK_KEY = "nick";
    private static final String PAGE_BODY_KEY = "page_body";
    private static final String POST_TEXT_KEY = "PostText";
    private HtmlPreferences m_HtmlPreferences;
    private WebViewExternals m_WebViewExternals;
    final Handler uiHandler = new Handler();
    private Button tglSmiles;

    private Gallery glrBbCodes;
    private Gallery glrSmiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.qms_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        m_HtmlPreferences = new HtmlPreferences();
        m_HtmlPreferences.load(this);


        edMessage = (EditText) findViewById(R.id.edMessage);
        glrBbCodes = (Gallery) findViewById(R.id.glrBbCodes);
        glrSmiles = (Gallery) findViewById(R.id.glrSmiles);
        new BbCodesPanel(this, glrBbCodes, edMessage);
        new SmilesBbCodePanel(this, glrSmiles, edMessage);
        tglSmiles = (Button) findViewById(R.id.tglSmiles);
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
                        tglSmiles.setText("Bb");
                        break;
                    case 1:
                        glrSmiles.setVisibility(View.GONE);
                        glrBbCodes.setVisibility(View.VISIBLE);
                        tglSmiles.setText(" v");
                        break;
                    case 2:
                        glrSmiles.setVisibility(View.GONE);
                        glrBbCodes.setVisibility(View.GONE);
                        tglSmiles.setText(":)");
                        break;
                }
            }
        });

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startSendMessage();
            }
        });


        wvChat = (AdvWebView) findViewById(R.id.wvChat);
        registerForContextMenu(wvChat);


        if (Build.VERSION.SDK_INT > 6) {
            wvChat.getSettings().setDomStorageEnabled(true);
            wvChat.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
            String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
            wvChat.getSettings().setAppCachePath(appCachePath);
            wvChat.getSettings().setAppCacheEnabled(true);
        }
        wvChat.getSettings().setAllowFileAccess(true);

        wvChat.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        wvChat.addJavascriptInterface(this, "HTMLOUT");

        m_WebViewExternals = new WebViewExternals(this);
        m_WebViewExternals.loadPreferences(PreferenceManager.getDefaultSharedPreferences(MyApp.getContext()));

        m_WebViewExternals.setWebViewSettings();

        wvChat.setWebViewClient(new MyWebViewClient());
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString(MID_KEY);
        m_Nick = extras.getString(NICK_KEY);
        m_TId = extras.getString(TID_KEY);
        m_ThemeTitle = extras.getString(THEME_TITLE_KEY);
        m_PageBody = extras.getString(PAGE_BODY_KEY);
        if (TextUtils.isEmpty(m_Nick))
            setTitle("QMS");
        else
            setTitle(m_Nick + ":QMS:" + m_ThemeTitle);

        if (!TextUtils.isEmpty(m_PageBody)) {
            m_LastBodyLength = m_PageBody.length();
            m_PageBody = transformChatBody(m_PageBody);
            wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", m_PageBody, "text/html", "UTF-8", null);
            m_PageBody = null;
        }
        hideKeyboard();
        //  hidePanels();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edMessage.getWindowToken(), 0);
    }

    @JavascriptInterface
    public void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(QmsChatActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @JavascriptInterface
    public void deleteMessages(final String[] checkBoxNames) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (checkBoxNames == null) {
                    Toast.makeText(QmsChatActivity.this, "Не выбраны сообщения для удаления!", Toast.LENGTH_LONG).show();
                    return;
                }

                final ArrayList<String> ids = new ArrayList<String>();
                Pattern p = Pattern.compile("message-id\\[(\\d+)\\]",Pattern.CASE_INSENSITIVE);
                for (String checkBoxName : checkBoxNames) {
                    Matcher m = p.matcher(checkBoxName);
                    if (m.find()) {
                        ids.add(m.group(1));
                    }
                }
                if (ids.size()==0) {
                    Toast.makeText(QmsChatActivity.this, "Не выбраны сообщения для удаления!", Toast.LENGTH_LONG).show();
                    return;
                }

                new AlertDialogBuilder(QmsChatActivity.this)
                        .setTitle("Подтвердите действие")
                        .setCancelable(true)
                        .setMessage(String.format("Вы действительно хотите удалить выбранные сообщения (%d)?", ids.size()))
                        .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                m_SendTask = new DeleteTask(QmsChatActivity.this);
                                m_SendTask.execute(ids);
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    public void deleteDialog() {

        new AlertDialogBuilder(this)
                .setTitle("Подтвердите действие")
                .setCancelable(true)
                .setMessage("Вы действительно хотите удалить диалог?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        ArrayList<String> ids = new ArrayList<String>();
                        ids.add(m_TId);
                        m_SendTask = new DeleteDialogTask(QmsChatActivity.this, ids);
                        m_SendTask.execute();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();

    }

    public static void openChat(Context activity, String userId, String userNick,
                                String tid, String themeTitle) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsChatActivity.class);
        intent.putExtra(MID_KEY, userId);
        intent.putExtra(NICK_KEY, userNick);

        intent.putExtra(TID_KEY, tid);
        intent.putExtra(THEME_TITLE_KEY, themeTitle);

        activity.startActivity(intent);
    }

    public static void openChat(Context activity, String userId, String userNick, String tid, String themeTitle,
                                String pageBody) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsChatActivity.class);
        intent.putExtra(MID_KEY, userId);
        intent.putExtra(NICK_KEY, userNick);

        intent.putExtra(TID_KEY, tid);
        intent.putExtra(THEME_TITLE_KEY, themeTitle);
        intent.putExtra(PAGE_BODY_KEY, pageBody);

        activity.startActivity(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        outState.putString(MID_KEY, m_Id);
        outState.putString(NICK_KEY, m_Nick);
        outState.putString(TID_KEY, m_TId);
        outState.putString(THEME_TITLE_KEY, m_ThemeTitle);
        outState.putString(POST_TEXT_KEY, edMessage.getText().toString());

    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);

        m_Id = outState.getString(MID_KEY);
        m_Nick = outState.getString(NICK_KEY);
        m_TId = outState.getString(TID_KEY);
        m_ThemeTitle = outState.getString(THEME_TITLE_KEY);
        setTitle(m_Nick + "-QMS-" + m_ThemeTitle);
        edMessage.setText(outState.getString(POST_TEXT_KEY));

    }


    @Override
    public void onResume() {
        super.onResume();
        loadPrefs();
        startUpdateTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }


    @Override
    public void onStop() {
        super.onStop();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    private void loadPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        m_UpdateTimeout = ExtPreferences.parseInt(preferences, "qms.chat.update_timer", 15) * 1000;
    }

    private String transformChatBody(String chatBody) {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.beginHtml("QMS");
        htmlBuilder.beginBody("onload=\"scrollToElement('bottom_element')\"");

        if (m_HtmlPreferences.isSpoilerByButton())
            chatBody = HtmlPreferences.modifySpoiler(chatBody);
        chatBody = HtmlPreferences.modifyBody(chatBody, Smiles.getSmilesDict(), m_HtmlPreferences.isUseLocalEmoticons());
        htmlBuilder.append(chatBody);
        htmlBuilder.append("<div id=\"bottom_element\" name=\"bottom_element\"></div>");
        htmlBuilder.endBody();
        htmlBuilder.endHtml();

        return htmlBuilder.getHtml().toString();
    }


    private void reLoadChatSafe() {
        uiHandler.post(new Runnable() {
            public void run() {
                setSupportProgressBarIndeterminateVisibility(true);
                //pbLoading.setVisibility(View.VISIBLE);
            }
        });
        String chatBody = null;
        Throwable ex = null;
        Boolean updateTitle = false;
        try {
            String body = null;

            if (TextUtils.isEmpty(m_Nick)) {
                updateTitle = true;
                Map<String, String> additionalHeaders = new HashMap<String, String>();
                body = Qms_2_0.getChat(Client.getInstance(), m_Id, m_TId, additionalHeaders);
                if (additionalHeaders.containsKey("Nick"))
                    m_Nick = additionalHeaders.get("Nick");
                if (additionalHeaders.containsKey("ThemeTitle"))
                    m_ThemeTitle = additionalHeaders.get("ThemeTitle");
            } else {
                body = Qms_2_0.getChat(Client.getInstance(), m_Id, m_TId);
            }
            if (body.length() == m_LastBodyLength) {
                uiHandler.post(new Runnable() {
                    public void run() {
                        setSupportProgressBarIndeterminateVisibility(false);
                    }
                });
                return;
            }
            m_LastBodyLength = body.length();
            chatBody = transformChatBody(body);
        } catch (Throwable e) {
            ex = e;
        }
        final Throwable finalEx = ex;
        final String finalChatBody = chatBody;
        final Boolean finalUpdateTitle = updateTitle;
        uiHandler.post(new Runnable() {
            public void run() {

                if (finalEx == null) {
                    if (finalUpdateTitle)
                        setTitle(m_Nick + "-QMS-" + m_ThemeTitle);
                    wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", finalChatBody, "text/html", "UTF-8", null);
                } else {
                    if (finalEx != null) {
                        if ("Такого диалога не существует.".equals(finalEx.getMessage())) {
                            new AlertDialogBuilder(QmsChatActivity.this)
                                    .setTitle("Ошибка")
                                    .setMessage(finalEx.getMessage())
                                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            showThread();
                                        }
                                    })
                                    .create().show();
                            m_UpdateTimer.cancel();
                            m_UpdateTimer.purge();

                        } else {
                            Toast.makeText(QmsChatActivity.this, Log.getLocalizedMessage(finalEx, finalEx.getLocalizedMessage()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
                                Toast.LENGTH_SHORT).show();

                }
                setSupportProgressBarIndeterminateVisibility(false);
            }
        });

    }

    private void onPostChat(String chatBody, Boolean success, Throwable ex) {
        if (success) {
            edMessage.getText().clear();

            wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", chatBody, "text/html", "UTF-8", null);
        } else {
            if (ex != null)
                Log.e(QmsChatActivity.this, ex, new Runnable() {
                    @Override
                    public void run() {
                        m_SendTask = new SendTask(QmsChatActivity.this);
                        m_SendTask.execute();
                    }
                });
            else
                Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
                        Toast.LENGTH_SHORT).show();
        }
    }

    private void startUpdateTimer() {
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
        m_UpdateTimer = new Timer();
        m_UpdateTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                try {
                    if (m_SendTask != null && m_SendTask.getStatus() != AsyncTask.Status.FINISHED)
                        return;
                    reLoadChatSafe();
                } catch (Throwable ex) {
                    Log.e(QmsChatActivity.this, ex);
                }

            }
        }, 0L, m_UpdateTimeout);

    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    private String m_MessageText = null;

    private void saveScale(float scale) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("qms.ZoomLevel", scale);
        editor.commit();
    }

    private float loadScale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return prefs.getFloat("qms.ZoomLevel", wvChat.getScrollY());

    }

    public static String getEncoding() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());

        return prefs.getString("qms.chat.encoding", "UTF-8");

    }

    private void startSendMessage() {
        m_MessageText = edMessage.getText().toString();
        if (TextUtils.isEmpty(m_MessageText)) {
            Toast.makeText(this, "Введите текст для отправки.", Toast.LENGTH_SHORT).show();
            return;
        }
        m_SendTask = new SendTask(QmsChatActivity.this);
        m_SendTask.execute();
    }

    private void zoomOut() {
        wvChat.zoomOut();
    }

    private void zoomIn() {
        wvChat.zoomIn();
    }

    @Override
    public String Prefix() {
        return "theme";
    }

    @Override
    public WebView getWebView() {
        return wvChat;
    }

    @Override
    public ImageButton getFullScreenButton() {
        return null;
    }

    @Override
    public void nextPage() {

    }

    @Override
    public void prevPage() {

    }

    @Override
    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return m_WebViewExternals.dispatchKeyEvent(event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v,
                                    android.view.ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = wvChat.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    public void showLinkMenu(final String link) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, this, m_ThemeTitle, "", link, "", "", "", m_Id, m_Nick);
    }

    private AsyncTask<ArrayList<String>, Void, Boolean> m_SendTask = null;

    private class SendTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final ProgressDialog dialog;
        public String m_ChatBody;


        public SendTask(Context context) {

            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(ArrayList<String>... params) {
            try {

                m_ChatBody = transformChatBody(Qms_2_0.sendMessage(Client.getInstance(), m_Id, m_TId, m_MessageText,
                        getEncoding()));

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

            onPostChat(m_ChatBody, success, ex);
        }


    }

    private class DeleteTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final ProgressDialog dialog;
        public String m_ChatBody;


        public DeleteTask(Context context) {

            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(ArrayList<String>... params) {
            try {

                m_ChatBody = transformChatBody(Qms_2_0.deleteMessages(Client.getInstance(),
                        m_Id, m_TId, params[0], getEncoding()));

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление сообщений...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            onPostChat(m_ChatBody, success, ex);
        }
    }

    private class DeleteDialogTask extends AsyncTask<ArrayList<String>, Void, Boolean> {


        private final ProgressDialog dialog;
        public String m_ChatBody;
        ArrayList<String> m_Ids;

        public DeleteDialogTask(Context context, ArrayList<String> ids) {
            m_Ids = ids;
            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(ArrayList<String>... params) {
            try {

                Qms_2_0.deleteDialogs(Client.getInstance(), m_Id, m_Ids);

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление диалогов...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (!success) {
                if (ex != null)
                    Log.e(QmsChatActivity.this, ex);
                else
                    Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }

            showThread();

        }
    }

    private void showThread() {
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(BaseActivity.SENDER_ACTIVITY)) {
            if ("class org.softeg.slartus.forpda.qms_2_0.QmsContactThemesActivity".equals(getIntent().getExtras().get(BaseActivity.SENDER_ACTIVITY))) {
                finish();
                return;
            }
        }

        QmsContactThemesActivity.showThemes(this, m_Id, m_Nick);
        finish();
    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }

        private QmsChatActivity getInterface() {
            if (getActivity() == null) return null;
            return (QmsChatActivity) getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            MenuItem item = menu.add("Обновить").setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    new Thread(new Runnable() {
                        public void run() {
                            ((QmsChatActivity) getActivity()).reLoadChatSafe();
                        }
                    }).start();

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Настройки").setIcon(R.drawable.ic_menu_preferences);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), QmsChatPreferencesActivity.class);
                    getActivity().startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


            item = menu.add("Удалить сообщения").setIcon(R.drawable.ic_menu_delete);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().getWebView().loadUrl("javascript:deleteMessages('thread_form');");
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            item = menu.add("Удалить диалог").setIcon(R.drawable.ic_menu_delete);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().deleteDialog();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

            item = menu.add("Увеличить масштаб").setIcon(R.drawable.ic_menu_zoom);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().zoomIn();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add("Уменьшить масштаб").setIcon(R.drawable.ic_menu_zoom_out);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().zoomOut();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }


    private class MyWebViewClient extends WebViewClient {

        public MyWebViewClient() {
            m_Scale = loadScale();
        }

        private float m_Scale;
        private int m_ScrollY;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            //  setSupportProgressBarIndeterminateVisibility(true);

            m_ScrollY = wvChat.getScrollY();
            wvChat.setInitialScale((int) (m_Scale * 100));
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            m_Scale = newScale;
            saveScale(m_Scale);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);


            try {

                wvChat.setInitialScale((int) (m_Scale * 100));

            } catch (Throwable ex) {
                Log.e(QmsChatActivity.this, ex);
            }


            setSupportProgressBarIndeterminateVisibility(false);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            IntentActivity.tryShowUrl(QmsChatActivity.this, mHandler, url, true, false, "");

            return true;
        }
    }
}
