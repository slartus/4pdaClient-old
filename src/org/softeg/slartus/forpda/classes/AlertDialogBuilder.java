package org.softeg.slartus.forpda.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.common.Log;

import java.lang.reflect.Method;

/**
 * Created by slinkin on 08.08.13.
 */
public class AlertDialogBuilder extends AlertDialog.Builder {
    private Context m_Context;

    public AlertDialogBuilder(Context context) {
        super(new ContextThemeWrapper(context, MyApp.getInstance().isWhiteTheme() ? R.style.Theme_White : R.style.Theme_Black));
        if (Build.VERSION.SDK_INT < 11) {
            m_Context = context;
        }
    }

    public Context getBridgeContext() {
        if (Build.VERSION.SDK_INT < 11) {
            return m_Context;
        } else {
            return getContext();
        }
    }

    @Override
    public android.app.AlertDialog create() {
        AlertDialog res = super.create();
        if (Build.VERSION.SDK_INT < 11) {
            if (Build.VERSION.SDK_INT > 7)
                res.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        setColors(dialogInterface, MyApp.getInstance().getThemeTextColor(), MyApp.getInstance().getThemeBackgroundColor());
                    }
                });
        }
        return res;
    }

    public static void setColors(DialogInterface alert, int textColor, int background) {
        try {

            Class c = alert.getClass();

            Method mAlert = c.getMethod("getWindow");
            Window window = (Window) mAlert.invoke(alert);

            ViewGroup root = (ViewGroup) window.getDecorView();
            root.setBackgroundColor(background);
            setColors(root, textColor, background, 0);


        } catch (Exception e) {
            Log.e(null, e);
        }
    }

    public static void setThemeColors(ViewGroup viewGroup) {
        viewGroup.setBackgroundColor(MyApp.getInstance().getThemeBackgroundColor());
        setColors(viewGroup, MyApp.getInstance().getThemeTextColor(), MyApp.getInstance().getThemeBackgroundColor(), 0);
    }

    public static void setColors(ViewGroup viewGroup, int textColor, int background, int level) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);

            setColor(textColor, background, level, view);
        }
    }

    public android.app.AlertDialog.Builder setAdapterSingleChoiceItems(java.lang.CharSequence[] items, int checkedItem,
                                                                       android.content.DialogInterface.OnClickListener listener) {
        if (Build.VERSION.SDK_INT < 11) {
            setAdapter(new ArrayAdapter<CharSequence>(getBridgeContext(), R.layout.simple_list_item_single_choice, items), null);
        }
        return super.setSingleChoiceItems(items, checkedItem, listener);
    }

    public static void setColor(int textColor, int background, int level, View view) {
        if (view instanceof CheckBox) {
            ((CheckBox) view).setTextColor(textColor);
        } else if (view instanceof ImageButton) {

        } else if (view instanceof Button) {
            if (view.getTag() == null || !view.getTag().toString().equals("colored")) {
                view.setBackgroundResource(MyApp.getInstance().isWhiteTheme() ? R.drawable.apptheme_btn_default_holo_light : R.drawable.apptheme_btn_default_holo_dark);
                ((Button) view).setTextColor(textColor);
            }
        }
        if (view instanceof TextView) {
            if (view.getBackground() == null)
                view.setBackgroundColor(background);
            ((TextView) view).setTextColor(textColor);
        }
        if (view instanceof EditText) {
            if (view.getBackground() == null)
                view.setBackgroundColor(background);
            ((EditText) view).setTextColor(textColor);
        }

        if (view instanceof ViewGroup) {
            setColors((ViewGroup) view, textColor, background, level + 1);
        } else {
            android.util.Log.i("", "");
        }
    }

    @Override
    public AlertDialogBuilder setTitle(java.lang.CharSequence title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public AlertDialogBuilder setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }
}
