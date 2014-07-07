package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.softeg.slartus.forpda.MyApp;

import java.lang.reflect.Method;

/**
 * User: slinkin
 * Date: 25.01.12
 * Time: 10:00
 */
public class AdvWebView extends WebView {
    GestureDetector gd;

    public AdvWebView(Context context) {
        super(context);
        init(context);
    }

    public AdvWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }


    private void init(Context context) {
        // gd = new GestureDetector(context, sogl);
        getSettings().setJavaScriptEnabled(true);

        getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        if (Build.VERSION.SDK_INT >= 7)
            getSettings().setDomStorageEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Build.VERSION.SDK_INT >= 8 && Build.VERSION.SDK_INT < 18)
            getSettings().setPluginState(WebSettings.PluginState.ON);// для воспроизведения видео

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        if (prefs.getBoolean("system.WebViewScroll", true)) {
            setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            setScrollbarFadingEnabled(false);
        }

        setBackgroundColor(MyApp.getInstance().getThemeStyleWebViewBackground());
        loadData("<html><head></head><body bgcolor=" + MyApp.getInstance().getCurrentThemeName() + "></body></html>", "text/html", "UTF-8");
    }

    private Point m_LastMotionEvent = null;

    public Point getLastMotionEvent() {
        return m_LastMotionEvent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        m_LastMotionEvent = new Point((int) event.getX(), (int) event.getY());
        return super.onTouchEvent(event);
    }

    GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {
        public boolean onDown(MotionEvent event) {
            return true;
        }

        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event1.getRawX() > event2.getRawX()) {
                show_toast("swipe left");
            } else {
                show_toast("swipe right");
            }
            return true;
        }
    };

    void show_toast(final String text) {
        Toast t = Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT);
        t.show();
    }


    public void selectAndCopyText() {
        try {

            Method m = WebView.class.getMethod("emulateShiftHeld", null);
            m.invoke(this, null);
        } catch (Exception e) {
            e.printStackTrace();
            // fallback
            KeyEvent shiftPressEvent = new KeyEvent(0, 0,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
            shiftPressEvent.dispatch(this);
        }
    }

//    private boolean is_gone = false;
//
//    @Override
//    public void onWindowVisibilityChanged(int visibility) {
//        super.onWindowVisibilityChanged(visibility);
//        if (visibility == View.GONE) {
//            try {
//                WebView.class.getMethod("onPause").invoke(this);//stop flash
//            } catch (Exception e) {
//            }
//            this.pauseTimers();
//            this.is_gone = true;
//        } else if (visibility == View.VISIBLE) {
//            try {
//                WebView.class.getMethod("onResume").invoke(this);//resume flash
//            } catch (Exception e) {
//            }
//            this.resumeTimers();
//            this.is_gone = false;
//        }
//    }
//
//    public void onDetachedFromWindow() {//this will be trigger when back key pressed, not when home key pressed
//        if (this.is_gone) {
//            try {
//                this.destroy();
//            } catch (Exception e) {
//            }
//        }
//    }
}
