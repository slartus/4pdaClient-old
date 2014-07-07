package org.softeg.slartus.forpda.classes.common;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.softeg.slartus.forpda.R;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 25.02.13
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public class ExtSpinner extends Spinner {

    public ExtSpinner(Context context) {
        super(context);
    }

    public static Spinner setResourceAdapter(Context context, Spinner spinner, int textArrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                textArrayResId,
                R.layout.spinner_txt);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        if (Build.VERSION.SDK_INT < 11) {
//            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
//        }else{
//
//        }

        spinner.setAdapter(adapter);
        return spinner;
    }

    private class SpinnerAdapter extends ArrayAdapter<CharSequence> {
        public SpinnerAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }
    }
}
