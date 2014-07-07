package org.softeg.slartus.forpda.classes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.LoginDialog;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.ReputationActivity;
import org.softeg.slartus.forpda.profile.ProfileActivity;


/**
 * User: slinkin
 * Date: 04.04.12
 * Time: 9:29
 */
public class ProfileMenuFragment extends SherlockFragment {
    private com.actionbarsherlock.view.SubMenu mUserMenuItem;
    private int m_MailItemId = 1234;
    private int m_QmsItemId = 4321;
    private com.actionbarsherlock.view.Menu m_Menu;

    public ProfileMenuFragment() {

    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setHasOptionsMenu(true);
    }


    private int getUserIconRes() {
        Boolean logged = Client.getInstance().getLogined();
        if (logged) {
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
//            Boolean showToast = preferences.getBoolean("ShowNewQmsLsToast", false);
//            String message = null;
            try {

                if (Client.getInstance().getQms_2_0_Count() > 0) {
//                    if (showToast)
//                        message = "QMS: " + Client.getInstance().getQms();
                    return R.drawable.ic_menu_user_qms;// MyApp.getInstance().isWhiteTheme() ? R.drawable.user_qms_white : R.drawable.user_qms_dark;
                }
                return R.drawable.ic_menu_user_online;//MyApp.getInstance().isWhiteTheme() ? R.drawable.user_online_white : R.drawable.user_online_dark;
            } finally {
//                if (!TextUtils.isEmpty(message))
//                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        } else {
            return R.drawable.ic_menu_user_offline;//MyApp.getInstance().isWhiteTheme() ? R.drawable.user_offline_white : R.drawable.user_offline_dark;
        }

    }

    public void setUserMenu() {
        if (mUserMenuItem == null) return;
        Boolean logged = Client.getInstance().getLogined();

        mUserMenuItem.getItem().setIcon(getUserIconRes());
        mUserMenuItem.getItem().setTitle(Client.getInstance().getUser());
        mUserMenuItem.clear();
        if (logged) {
            String text = Client.getInstance().getQms_2_0_Count() > 0 ? ("QMS (" + Client.getInstance().getQms_2_0_Count() + ")") : "QMS";
            mUserMenuItem.add(text)
                    .setIcon(R.drawable.ic_action_qms)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            org.softeg.slartus.forpda.qms_2_0.QmsContactsActivity.show(getActivity());

                            return true;
                        }
                    });

            mUserMenuItem.add(R.string.Profile)
                    .setIcon(R.drawable.ic_action_user_online)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            ProfileActivity.startActivity(getActivity(), Client.getInstance().UserId, Client.getInstance().getUser());
                            return true;
                        }
                    });


            mUserMenuItem.add(R.string.Reputation)
                    .setIcon(R.drawable.ic_action_user_online)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            ReputationActivity.showRep(getActivity(), Client.getInstance().UserId);
                            return true;
                        }
                    });

            mUserMenuItem.add(R.string.Logout).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    LoginDialog.logout(getActivity());
                    return true;
                }
            });
        } else {
            mUserMenuItem.add(R.string.Login).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    LoginDialog.showDialog(getActivity(), null);
                    return true;
                }
            });

            mUserMenuItem.add(R.string.Registration).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    Intent marketIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://4pda.ru/forum/index.php?act=Reg&CODE=00"));
                    getActivity().startActivity(marketIntent);
                    //
                    return true;
                }
            });
        }
    }

    private void createUserMenu(com.actionbarsherlock.view.Menu menu) {
        m_Menu = menu;
        mUserMenuItem = menu.addSubMenu(Client.getInstance().getUser());

        mUserMenuItem.getItem().setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        m_Menu = menu;
        createUserMenu(menu);

    }
}
