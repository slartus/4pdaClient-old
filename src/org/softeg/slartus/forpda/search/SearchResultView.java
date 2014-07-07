package org.softeg.slartus.forpda.search;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.classes.IWebViewContainer;
import org.softeg.slartus.forpda.classes.WebViewExternals;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.common.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 21.10.12
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
public class SearchResultView extends LinearLayout implements IWebViewContainer, ISearchResultView {
    private Handler mHandler = new Handler();
    private WebView mWvBody;
    private SearchSettings m_SearchSettings;
    private WebViewExternals m_WebViewExternals;

    public SearchResultView(Context context) {
        super(context);
        addView(inflate(context, R.layout.search_posts_result, null),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mWvBody = (WebView) findViewById(R.id.body_webview);
        m_WebViewExternals = new WebViewExternals(this);
        m_WebViewExternals.loadPreferences(PreferenceManager.getDefaultSharedPreferences(MyApp.getContext()));
        configWebView();
        m_WebViewExternals.setWebViewSettings();

    }

    public void search(SearchSettings searchSettings) {
        m_SearchSettings = searchSettings;
        search("0");

    }

    public void search(int startNum) {

        search(startNum + "");

    }


    public void search(String startNum) {

        LoadResultTask loadResultTask = new LoadResultTask(getContext());
        loadResultTask.execute(startNum);

    }

    private void configWebView() {

        mWvBody.getSettings().setJavaScriptEnabled(true);
        mWvBody.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        if (Build.VERSION.SDK_INT >= 7)
            mWvBody.getSettings().setDomStorageEnabled(true);
        mWvBody.getSettings().setAllowFileAccess(true);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        if (prefs.getBoolean("system.WebViewScroll", true)) {
            mWvBody.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            mWvBody.setScrollbarFadingEnabled(false);
        }


        m_WebViewExternals.setWebViewSettings();
        mWvBody.setWebViewClient(new MyWebViewClient());
        mWvBody.addJavascriptInterface(this, "HTMLOUT");

    }

    private Activity getActivity() {
        return ((Activity) getContext());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return m_WebViewExternals.dispatchKeyEvent(event);
    }

    private void showHtmlBody(String body) {
        try {

            mWvBody.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);


        } catch (Exception ex) {
            Log.e(getContext(), ex);
        }
    }

    @JavascriptInterface
    public void showUserMenu(final String userId, final String userNick) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ForumUser.showUserMenu(getContext(), getWebView(), userId, userNick);
            }
        });
    }

    @JavascriptInterface
    public void nextPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search(m_SearchResult.getCurrentPage() * m_SearchResult.getPostsPerPageCount(m_SearchSettings.getSearchQuery("posts")));
            }
        });
    }

    @JavascriptInterface
    public void prevPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search((m_SearchResult.getCurrentPage() - 2) * m_SearchResult.getPostsPerPageCount(m_SearchSettings.getSearchQuery("posts")));
            }
        });
    }

    @JavascriptInterface
    public void firstPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search("0");
            }
        });
    }

    @JavascriptInterface
    public void lastPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                search((m_SearchResult.getPagesCount() - 1) * m_SearchResult.getPostsPerPageCount(m_SearchSettings.getSearchQuery("posts")));
            }
        });
    }

    @JavascriptInterface
    public void jumpToPage() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence[] pages = new CharSequence[m_SearchResult.getPagesCount()];

                final int postsPerPage = m_SearchResult.getPostsPerPageCount(m_SearchSettings.getSearchQuery("posts"));

                for (int p = 0; p < m_SearchResult.getPagesCount(); p++) {
                    pages[p] = "Стр. " + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
                }

                new AlertDialogBuilder(getContext())
                        .setTitle("Перейти к странице")						
                        .setAdapterSingleChoiceItems(pages, m_SearchResult.getCurrentPage() - 1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                search(i * postsPerPage);
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    public String Prefix() {
        return "theme";
    }

    public WebView getWebView() {
        return mWvBody;
    }

    public ImageButton getFullScreenButton() {
        return (ImageButton) findViewById(R.id.btnFullScreen);
    }

    public Window getWindow() {
        return ((Activity) getContext()).getWindow();
    }

    public ActionBar getSupportActionBar() {
        return null;
    }

    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return false;
    }

    public void showLinkMenu(String link) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        ExtUrl.showSelectActionDialog(mHandler, getContext(), link);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler mHandler) {
        final WebView.HitTestResult hitTestResult = getWebView().getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {

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
                            Matcher m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(query);
                            ArrayList<String> objs = new ArrayList<String>();

                            while (m.find()) {
                                objs.add(m.group(2));
                            }
                            parameterValues = new String[objs.size()];
                            parameterTypes = new Class[objs.size()];
                            for (int i = 0; i < objs.size(); i++) {
                                parameterTypes[i] = String.class;
                                parameterValues[i] = objs.get(i);
                            }
                        }
                        Method method = SearchResultView.class.getMethod(function, parameterTypes);

                        method.invoke(SearchResultView.this, parameterValues);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }

            }

//            if (checkIsTheme(url))
//                return true;

            IntentActivity.tryShowUrl((Activity) getContext(), mHandler, url, true, false);

            return true;
        }
    }


    private SearchResult m_SearchResult;

    private class LoadResultTask extends AsyncTask<String, String, Boolean> {


        private final ProgressDialog dialog;

        public LoadResultTask(Context context) {

            dialog = new AppProgressDialog(context);
            dialog.setCancelable(false);
        }


        private String pageBody;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if (this.isCancelled()) return false;

                pageBody = Client.getInstance().loadPageAndCheckLogin(m_SearchSettings.getSearchQuery("posts") + "&st=" + params[0], null);
                SearchPostsParser searchPostsParser = new SearchPostsParser();
                pageBody = searchPostsParser.parse(pageBody);
                m_SearchResult = searchPostsParser.searchResult;
                return true;

            } catch (Throwable e) {
                //Log.e(getContext(), e);
                ex = e;
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setMessage(progress[0]);
        }


        // can use UI thread here
        protected void onPreExecute() {
            try {
                this.dialog.setMessage(getContext().getResources().getString(R.string.loading));
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        private Throwable ex;

        protected void onCancelled() {
            super.onCancelled();

        }


        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e(null, ex);
            }

            if (success) {
                showHtmlBody(pageBody);


            } else {
                if (ex != null)
                    Log.e(SearchResultView.this.getContext(), ex);
            }

            super.onPostExecute(success);
        }


    }

}
