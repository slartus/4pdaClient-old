package org.softeg.slartus.forpda.profile;/*
 * Created by slinkin on 17.04.2014.
 */


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.ReputationActivity;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.qms_2_0.QmsNewThreadActivity;
import org.softeg.slartus.forpda.search.SearchActivity;

public class ProfileWebViewActivity extends BaseFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_fragment_activity);

        try {
            Bundle extras = getIntent().getExtras();
            ProfileWebViewFragment details = new ProfileWebViewFragment();
            details.setArguments(extras);

            MenuFragment menuFragment = new MenuFragment();
            menuFragment.setArguments(extras);


            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, details).add(menuFragment, "menufragment").commit();

        } catch (Throwable e) {
            Log.e(this, e);
        }
    }


    public static final class MenuFragment extends SherlockFragment {
        private String userId;
        private String userNick;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(ProfileActivity.USER_ID_KEY))
                    userId = savedInstanceState.getString(ProfileActivity.USER_ID_KEY);
                if (savedInstanceState.containsKey(ProfileActivity.USER_NAME_KEY))
                    userNick = savedInstanceState.getString(ProfileActivity.USER_NAME_KEY);
            }
            if (getArguments() != null) {
                if (getArguments().containsKey(ProfileActivity.USER_ID_KEY))
                    userId = getArguments().getString(ProfileActivity.USER_ID_KEY);
                if (getArguments().containsKey(ProfileActivity.USER_NAME_KEY))
                    userNick = getArguments().getString(ProfileActivity.USER_NAME_KEY);
            }
            setHasOptionsMenu(true);// важно после получения аргументов это сделать!!!
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putString(ProfileActivity.USER_ID_KEY, userId);
            outState.putString(ProfileActivity.USER_NAME_KEY, userNick);

            super.onSaveInstanceState(outState);
        }


        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            MenuItem item;

            if (Client.getInstance().getLogined() && userId != null && !userId.equals(Client.getInstance().UserId)) {
                item = menu.add(getString(R.string.MessagesQms)).setIcon(R.drawable.ic_menu_send);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        QmsNewThreadActivity.showUserNewThread(getActivity(), userId, userNick);

                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }


            item = menu.add(getString(R.string.Reputation)).setIcon(R.drawable.ic_menu_view);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    new AlertDialogBuilder(getActivity())
                            .setTitle("Репутация")
                            .setSingleChoiceItems(new CharSequence[]{"+1", "Посмотреть", "-1", "Действия с репутацией"}, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            ReputationActivity.plusRep(getActivity(), new Handler(), userId, userNick);
                                            break;
                                        case 1:
                                            ReputationActivity.showRep(getActivity(), userId);
                                            break;
                                        case 2:
                                            ReputationActivity.minusRep(getActivity(), new Handler(), userId, userNick);
                                            break;
                                        case 3:
                                            ReputationActivity.showSelfRep(getActivity(), userId);
                                            break;
                                    }
                                    dialogInterface.dismiss();
                                }
                            })
                            .create().show();

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(getString(R.string.FindUserTopics)).setIcon(R.drawable.ic_menu_view);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    SearchActivity.findUserTopicsActivity(getActivity(), userNick);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(getString(R.string.FindUserPosts)).setIcon(R.drawable.ic_menu_view);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    SearchActivity.findUserPostsActivity(getActivity(), userNick);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(getString(R.string.OpenInBrowser)).setIcon(R.drawable.ic_menu_view);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    IntentActivity.showInDefaultBrowser(getActivity(), "http://4pda.ru/forum/index.php?showuser=" + userId);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

}
