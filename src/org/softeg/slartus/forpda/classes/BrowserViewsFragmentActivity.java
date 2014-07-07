package org.softeg.slartus.forpda.classes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

import org.softeg.slartus.forpda.BaseFragmentActivity;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 16.10.12
 * Time: 8:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class BrowserViewsFragmentActivity extends BaseFragmentActivity implements IWebViewContainer {
    public abstract String Prefix();

    public abstract WebView getWebView();


    WebViewExternals m_WebViewExternals;

    public WebViewExternals getWebViewExternals() {
        if (m_WebViewExternals == null)
            m_WebViewExternals = new WebViewExternals(this);
        return m_WebViewExternals;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected boolean getCurrentFullScreen() {
        return getWebViewExternals().getCurrentFullScreen();
    }

    protected void updateFullscreenStatus(Boolean fullScreen) {
        getWebViewExternals().updateFullscreenStatus(fullScreen);
    }

    public void setAndSaveUseZoom(Boolean useZoom) {
        getWebViewExternals().setAndSaveUseZoom(useZoom);
    }

    protected void setWebViewSettings(Boolean loadImagesAutomaticallyAlways) {
        getWebViewExternals().setWebViewSettings(loadImagesAutomaticallyAlways);
    }

    protected void setWebViewSettings() {
        setWebViewSettings(false);
    }

    public void onPrepareOptionsMenu() {
        getWebViewExternals().onPrepareOptionsMenu();
    }

    @Override
    public void setContentView(int id) {
        super.setContentView(id);
        if (getWebViewExternals().isUseFullScreen())
            getWebViewExternals().addFullScreenButton();
    }

    @Override
    protected void loadPreferences(SharedPreferences prefs) {
        super.loadPreferences(prefs);
        getWebViewExternals().loadPreferences(prefs);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return getWebViewExternals().dispatchKeyEvent(event);
    }

    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
}
