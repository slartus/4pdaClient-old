package org.softeg.slartus.forpda.classes.common;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.view.Display;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 03.12.12
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class ExtDisplay {
    public static Point getDisplaySize(Display display){
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < 13) {
            return new Point(display.getWidth(),display.getHeight() );
        } else {
            Point screenSize = new Point();
            display.getSize(screenSize);
            return screenSize;
        }
    } 
}
