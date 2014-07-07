package org.softeg.slartus.forpda.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.common.Log;

import java.lang.reflect.Method;

/**
 * Created by slinkin on 17.12.13.
 */
public class AppProgressDialog extends ProgressDialog {
    public AppProgressDialog(Context context) {
        super(new ContextThemeWrapper(context, MyApp.getInstance().getThemeStyleResID()));

    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 11) {
            if (Build.VERSION.SDK_INT > 7)
                setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        setColors(dialogInterface, MyApp.getInstance().getThemeTextColor(), MyApp.getInstance().getThemeBackgroundColor());
                    }
                });
        }

    }

    public static void setColors(DialogInterface alert, int textColor, int background) {
        try {

            Class c = alert.getClass();

            Method mAlert = c.getMethod("getWindow");
            Window window = (Window) mAlert.invoke(alert);

            ViewGroup root = (ViewGroup) window.getDecorView();

            setColors(root, textColor);


        } catch (Exception e) {
            Log.e(null, e);
        }
    }

    public static void setColors(ViewGroup viewGroup, int textColor) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);

            setColor(textColor, view);
        }
    }

    public static void setColor(int textColor, View view) {

        if (view instanceof TextView) {

            ((TextView) view).setTextColor(textColor);
        }

    }

}
