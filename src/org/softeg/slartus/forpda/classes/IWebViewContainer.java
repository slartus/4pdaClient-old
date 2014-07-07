package org.softeg.slartus.forpda.classes;

import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 24.10.12
 * Time: 8:32
 * To change this template use File | Settings | File Templates.
 */
public interface IWebViewContainer {
    String Prefix();

    WebView getWebView();

    ImageButton getFullScreenButton();

    Window getWindow();

    com.actionbarsherlock.app.ActionBar getSupportActionBar();

    void nextPage();

    void prevPage();

    boolean dispatchSuperKeyEvent(KeyEvent event);
}
