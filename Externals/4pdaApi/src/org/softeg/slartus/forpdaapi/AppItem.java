package org.softeg.slartus.forpdaapi;

import java.util.ArrayList;

/**
 * Created by slinkin on 17.01.14.
 */
public class AppItem implements IListItem {
    public static final int STATE_FINDED = 0;
    public static final int STATE_FINDED_AND_HAS_UPDATE = 1;
    public static final int STATE_UNFINDED = 2;

    private CharSequence m_Id;
    private CharSequence m_Title;
    private CharSequence description;
    private String versionName;
    private CharSequence packageName;
    private int state = STATE_UNFINDED;
    public ArrayList<CharSequence> Ids = new ArrayList<CharSequence>();
    private CharSequence type;

    public AppItem(CharSequence id, CharSequence title) {
        m_Id = id;
        m_Title = title;
    }

    public CharSequence getTitle() {
        return m_Title;
    }

    @Override
    public CharSequence getId() {
        return m_Id;
    }

    public void setId(CharSequence id) {
        m_Id = id;
    }

    @Override
    public CharSequence getTopLeft() {
        return "";
    }

    @Override
    public CharSequence getTopRight() {
        return type;
    }

    @Override
    public CharSequence getMain() {
        return getTitle();
    }

    @Override
    public CharSequence getSubMain() {
        return getDescription();
    }

    @Override
    public int getState() {
        switch (state) {
            case STATE_FINDED:
                return STATE_NORMAL;
            case STATE_FINDED_AND_HAS_UPDATE:
                return STATE_GREEN;
            case STATE_UNFINDED:
                return STATE_RED;
        }
        return STATE_NORMAL;
    }

    @Override
    public void setState(int state) {
        this.state = state;
    }

    public int getFindedState() {
        return state;
    }

    public void setFindedState(int state) {
        this.state = state;
    }

    public CharSequence getDescription() {
        return description;
    }

    public void setDescription(CharSequence description) {
        this.description = description;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public CharSequence getType() {
        return type;
    }

    public void setType(CharSequence type) {
        this.type = type;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }
}
