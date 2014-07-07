package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.classes.common.ExtColor;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.profile.ProfileActivity;
import org.softeg.slartus.forpdaapi.User;
import org.softeg.slartus.forpdaapi.Users;
import org.softeg.slartus.forpdacommon.ExtPreferences;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 27.03.13
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class UsersTab extends BaseTab implements AdapterView.OnItemClickListener,
        Loader.OnLoadCompleteListener<Users> {
    public static final String TEMPLATE = "UsersTab";
    public static final String TITLE = "Пользователи";
    PullToRefreshListView m_ListView;
    private android.os.Handler mHandler = new android.os.Handler();
    protected Users m_Users = new Users();
    private UsersAdapter mAdapter;


    public UsersTab(Context context, ITabParent tabParent) {
        super(context, tabParent);


        addView(inflate(context, R.layout.qms_contacts_list, null),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        m_ListView = (PullToRefreshListView) findViewById(R.id.pulltorefresh);
        setState(true);
        m_ListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mAdapter = new UsersAdapter(getContext(), R.layout.qms_contact_item, new Users());
        m_ListView.getRefreshableView().setAdapter(mAdapter);


    }


    protected Users loadUsers(Bundle extras) throws IOException {
        return new Users();
    }

    Bundle m_Extras;

    @Override
    public void refresh(Bundle extras) {
        m_Extras = extras;
        refresh();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Boolean onParentBackPressed() {
        return false;
    }

    @Override
    public void refresh() {

        refreshData();
    }

    @Override
    public Boolean cachable() {
        return false;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    private void refreshData() {
        m_Users.clear();

        setState(true);
        UsersLoader qmsUsersLoader = new UsersLoader(getContext(), new GetUsersInterface() {
            @Override
            public Users loadUsers() throws IOException {
                return UsersTab.this.loadUsers(m_Extras);
            }
        });
        qmsUsersLoader.registerListener(0, this);
        qmsUsersLoader.startLoading();

    }

    private void setState(boolean loading) {
        if (loading)
            m_ListView.setRefreshing(false);
        else
            m_ListView.onRefreshComplete();

    }

    @Override
    public Boolean refreshed() {
        return true;
    }

    @Override
    public ListView getListView() {
        return m_ListView.getRefreshableView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        User user = mAdapter.getItem((int) info.id);

        ForumUser.showUserMenu(getContext(), v, user.getMid(), user.getNick());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        l = ListViewMethodsBridge.getItemId(getContext(), i, l);
        if (l < 0 || mAdapter.getCount() <= l) return;
        User qmsUser = mAdapter.getItem((int) l);

        ProfileActivity.startActivity(getContext(), qmsUser.getMid());
    }


    public void onLoadComplete(Loader<Users> qmsUsersLoader, Users data) {
        if (data != null) {
            for (User item : data) {
                m_Users.add(item);
            }
            mAdapter.setData(m_Users);
        } else {
            m_Users = new Users();
            mAdapter.setData(m_Users);
        }


        setState(false);
        mAdapter.notifyDataSetChanged();
        if (getTabParent() != null)
            getTabParent().setTitle(getTitle());
    }

    public interface GetUsersInterface {
        Users loadUsers() throws IOException;
    }

    private static class UsersLoader extends AsyncTaskLoader<Users> {

        Users mApps;
        GetUsersInterface m_GetUsersInterface;
        Throwable ex;

        public UsersLoader(Context context, GetUsersInterface getUsersInterface) {
            super(context);
            m_GetUsersInterface = getUsersInterface;
        }

        @Override
        public Users loadInBackground() {
            try {
                return m_GetUsersInterface.loadUsers();
            } catch (Throwable e) {
                ex = e;

            }
            return null;
        }

        @Override
        public void deliverResult(Users apps) {
            if (ex != null)
                Log.e(getContext(), ex);
            if (isReset()) {
                if (apps != null) {
                    onReleaseResources();
                }
            }
            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
            }

            if (apps != null) {
                onReleaseResources();
            }
        }


        @Override
        protected void onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            if (takeContentChanged() || mApps == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }


        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(Users apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources();
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources();
                mApps = null;
            }


        }

        protected void onReleaseResources() {
            if (mApps != null)
                mApps.clear();

            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }

    public static class UsersAdapter extends ArrayAdapter<User> {
        private LayoutInflater m_Inflater;
        private int m_ThemeTitleSize = 13;
        private int m_TopTextSize = 10;
        private int m_BottomTextSize = 11;

        public void setData(Users data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (User item : data) {
                    add(item);
                }
            }
        }

        public UsersAdapter(Context context, int textViewResourceId, ArrayList<User> objects) {
            super(context, textViewResourceId, objects);

            m_Inflater = LayoutInflater.from(context);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
                    "interface.themeslist.title.font.size", 13);
            m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
            m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.qms_contact_item, parent, false);

                holder = new ViewHolder();
                holder.txtIsNew = (ImageView) convertView.findViewById(R.id.txtIsNew);
                holder.txtCount = (TextView) convertView.findViewById(R.id.txtMessagesCount);
                holder.txtCount.setTextSize(m_TopTextSize);

                holder.txtNick = (TextView) convertView.findViewById(R.id.txtNick);
                holder.txtNick.setTextSize(m_ThemeTitleSize);

                holder.txtDateTime = (TextView) convertView.findViewById(R.id.txtDateTime);
                holder.txtDateTime.setTextSize(m_TopTextSize);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            User user = this.getItem(position);

            holder.txtCount.setText(user.MessagesCount);
            holder.txtNick.setText(user.getNick());
            try {
                holder.txtNick.setTextColor(ExtColor.parseColor(user.getHtmlColor()));
            } catch (Exception ex) {
                Log.e(getContext(), new Exception("Не умею цвет: " + user.getHtmlColor()));
            }


//            holder.txtDateTime.setText(user.getLastMessageDateTime());
//
//            if (!TextUtils.isEmpty(user.getNewMessagesCount())) {
//                holder.txtIsNew.setImageResource(R.drawable.new_flag);
//            } else {
//                holder.txtIsNew.setImageBitmap(null);
//            }

            return convertView;
        }

        public class ViewHolder {
            ImageView txtIsNew;
            TextView txtNick;
            TextView txtDateTime;
            TextView txtCount;
        }
    }
}
