package org.softeg.slartus.forpdaapi;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 11:06
 */
public class QmsUsers extends ArrayList<QmsUser> {
    public Boolean hasUnreadMessage() {
        for (QmsUser qmsUser : this) {
            if (!TextUtils.isEmpty(qmsUser.getNewMessagesCount()))
                return true;
        }
        return false;
    }

    public String unreadMessageUsers() {
        String senders = "";

        for (QmsUser qmsUser : this) {
            if (!TextUtils.isEmpty(qmsUser.getNewMessagesCount()))
                senders += qmsUser.getNick() + ",";
        }
        return senders;
    }

    public int unreadMessageUsersCount() {
        int senders = 0;

        for (QmsUser qmsUser : this) {
            if (!TextUtils.isEmpty(qmsUser.getNewMessagesCount()))
                senders += 1;
        }
        return senders;
    }


}
