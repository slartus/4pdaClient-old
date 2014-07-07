package org.softeg.slartus.forpda.profile;/*
 * Created by slinkin on 17.04.2014.
 */


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockFragment;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.HtmlBuilder;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.Profile;
import org.softeg.slartus.forpdaapi.ProfileApi;
import org.softeg.slartus.forpdacommon.PatternExtensions;

import java.util.regex.Matcher;

public class ProfileWebViewFragment extends SherlockFragment {

    private static final String TAG = "ProfileWebViewFragment";
    private WebView m_WebView;
    private Task mTask;
    private String userId;
    private String userNick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ProfileActivity.USER_ID_KEY))
                userId = savedInstanceState.getString(ProfileActivity.USER_ID_KEY);
            if (savedInstanceState.containsKey(ProfileActivity.USER_NAME_KEY))
                userNick = savedInstanceState.getString(ProfileActivity.USER_NAME_KEY);
        }
        if (getArguments() != null) {
            if (getArguments().containsKey(ProfileActivity.USER_ID_KEY))
                userId = getArguments().getString(ProfileActivity.USER_ID_KEY);
            if (getArguments().containsKey(ProfileActivity.USER_NAME_KEY))
                userNick = getArguments().getString(ProfileActivity.USER_NAME_KEY);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startLoadData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ProfileActivity.USER_ID_KEY, userId);
        outState.putString(ProfileActivity.USER_NAME_KEY, userNick);

        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_web_view_fragment, container, false);

        assert view != null;
        m_WebView = (WebView) view.findViewById(R.id.wvBody);
        if (Build.VERSION.SDK_INT >= 7)
            m_WebView.getSettings().setLoadWithOverviewMode(false);
        m_WebView.getSettings().setUseWideViewPort(true);
        m_WebView.getSettings().setDefaultFontSize(18);
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                m_WebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
            } catch (Throwable e) {
                android.util.Log.e(TAG, e.getMessage());
            }
        }
        m_WebView.setWebViewClient(new MyWebViewClient());
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mTask != null)
            mTask.cancel(null);
    }

    private void startLoadData() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                mTask = new Task(userId);
                mTask.execute();
            }
        };
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED)
            mTask.cancel(runnable);
        else {
            runnable.run();
        }
    }

    private void setLoading(Boolean loading) {
        try {
            if (getActivity() == null) return;

            if (getSherlockActivity() != null)
                getSherlockActivity().setSupportProgressBarIndeterminateVisibility(loading);

        } catch (Throwable ignore) {
            android.util.Log.e("TAG", ignore.toString());
        }
    }

    private void deliveryResult(Profile profile) {

        m_WebView.loadDataWithBaseURL("http://4pda.ru/forum/", profile.getHtmlBody(), "text/html", "UTF-8", null);
        if (getActivity() != null)
            getActivity().setTitle(profile.getNick());
    }


    private class ProfileHtmlBuilder extends HtmlBuilder {
        @Override
        protected String getStyle() {
            return "/android_asset/profile/css/" + (MyApp.getInstance().isWhiteTheme() ? "profile_white.css" : "profile_black.css");

        }
    }

    public class Task extends AsyncTask<Boolean, Void, Profile> {

        private Runnable onCancelAction;
        protected Throwable mEx;
        private CharSequence userId;

        public Task(CharSequence userId) {

            this.userId = userId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setLoading(true);
        }

        public void cancel(Runnable runnable) {
            onCancelAction = runnable;

            cancel(false);
        }

        @Override
        protected Profile doInBackground(Boolean[] p1) {
            try {
                Profile profile = ProfileApi.getProfile(Client.getInstance(), userId);
                ProfileHtmlBuilder builder = new ProfileHtmlBuilder();
                builder.beginHtml(profile.getNick().toString());
                builder.beginBody();
                builder.append(profile.getHtmlBody());
                builder.endBody();
                builder.endHtml();
                profile.setHtmlBody(builder.getHtml().toString());
                return profile;
            } catch (Throwable e) {
                mEx = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);
            if (result != null && !isCancelled()) {
                deliveryResult(result);
            }
            if (!isCancelled())
                setLoading(false);

            if (mEx != null)
                Log.e(getActivity(), mEx, new Runnable() {
                    @Override
                    public void run() {
                        startLoadData();
                    }
                });
        }

        @Override
        protected void onCancelled(Profile result) {
            if (onCancelAction != null)
                onCancelAction.run();
        }

        @Override
        protected void onCancelled() {
            if (onCancelAction != null)
                onCancelAction.run();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (getSherlockActivity() != null)
                getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (getSherlockActivity() != null)
                getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
//            if (tryProfile(url))
//                return true;
            IntentActivity.tryShowUrl(getActivity(), new Handler(), url, true, false,
                    Client.getInstance().getAuthKey());

            return true;
        }

        public boolean tryProfile(String url) {
            Matcher m = PatternExtensions.compile("4pda.ru/*forum/*index.php\\?.*?act=profile.*?id=(\\d+)").matcher(url);
            if (m.find()) {
                userId = m.group(1);
                userNick = null;
                startLoadData();
                return true;
            }

            m = PatternExtensions.compile("4pda.ru/*forum/*index.php\\?.*?showuser=(\\d+)").matcher(url);
            if (m.find()) {
                userId = m.group(1);
                userNick = null;
                startLoadData();
                return true;
            }

            return false;
        }
    }
}
