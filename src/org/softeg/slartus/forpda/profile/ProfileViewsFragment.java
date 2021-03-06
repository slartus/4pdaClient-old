package org.softeg.slartus.forpda.profile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.ListViewMethodsBridge;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpdaapi.User;
import org.softeg.slartus.forpdaapi.UserProfile;
import org.softeg.slartus.forpdacommon.ExtPreferences;

/**
 * User: slinkin
 * Date: 27.09.12
 * Time: 11:10
 */
public class ProfileViewsFragment extends ProfileMainView implements AdapterView.OnItemClickListener {
    private UserViewsAdapter mAdapter;
    private ProgressBar m_ProgressBar;
    private PullToRefreshListView m_ListView;
    private TextView txtLoadMoreThemes, txtPullToLoadMore;
    private ImageView imgPullToLoadMore;

    private View m_ListFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        View pframe = inflater.inflate(R.layout.mails_list, null);
        mAdapter = new UserViewsAdapter(getActivity());
        m_ListView = (PullToRefreshListView) pframe.findViewById(R.id.pulltorefresh);
        m_ListView.getRefreshableView().addFooterView(createListFooter(inflater));
        m_ListView.getRefreshableView().setAdapter(mAdapter);
        m_ListView.getRefreshableView().setOnItemClickListener(this);

        setState(true);
        m_ListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUserProfile(null);
                getLoaderManager().restartLoader(0, null, ProfileViewsFragment.this);
            }
        });

        return pframe;
    }

    public void refreshData() {
        if (getUserProfile() != null)
            getUserProfile().UserViews.clear();
        setState(true);
        if (getLoaderManager().getLoader(0) == null)
            getLoaderManager().initLoader(0, null, this);
        else
            getLoaderManager().restartLoader(0, null, this);
    }

    private View createListFooter(LayoutInflater inflater) {
        m_ListFooter = inflater.inflate(R.layout.list_footer, null);
        m_ListFooter.setVisibility(View.GONE);

        txtLoadMoreThemes = (TextView) m_ListFooter.findViewById(R.id.txtLoadMoreThemes1);
        txtPullToLoadMore = (TextView) m_ListFooter.findViewById(R.id.txtPullToLoadMore1);
        imgPullToLoadMore = (ImageView) m_ListFooter.findViewById(R.id.imgPullToLoadMore1);
        m_ProgressBar = (ProgressBar) m_ListFooter.findViewById(R.id.load_more_progress1);
        return m_ListFooter;
    }

    public void onItemClick(android.widget.AdapterView<?> adapterView, android.view.View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getActivity(), i, l);
        if (l < 0 || m_ListView.getRefreshableView().getAdapter().getCount() <= l) return;
        User user = getUserProfile().UserViews.get((int) l);
        ForumUser.showUserMenu(getActivity(), view, user.getMid(), user.getNick());
    }

    @Override
    protected void setState(Boolean loading) {
        if (m_ListView != null) {
            if (loading)
                m_ListView.setRefreshing(false);
            else
                m_ListView.onRefreshComplete();

        }

        if (imgPullToLoadMore == null) return;
        if (loading && imgPullToLoadMore != null && txtPullToLoadMore != null) {
            imgPullToLoadMore.setVisibility(View.GONE);
            txtPullToLoadMore.setVisibility(View.GONE);
        }
        if (m_ProgressBar != null)
            m_ProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (m_ListFooter != null)
            m_ListFooter.setEnabled(!loading);
    }

    @Override
    protected void fillData(UserProfile data) {

        setUserProfile(data);
        doOnProfileChanged(data);

        mAdapter.setData(getUserProfile());

        setState(false);

        mAdapter.notifyDataSetChanged();
        updateDataInfo();
    }

    private void updateDataInfo() {

        int loadMoreVisibility = (getUserProfile().UserViews.needLoadMore()) ? View.VISIBLE : View.GONE;
        txtPullToLoadMore.setVisibility(loadMoreVisibility);
        imgPullToLoadMore.setVisibility(loadMoreVisibility);
        txtLoadMoreThemes.setText("Всего: " + getUserProfile().UserViews.getFullLength());
        //  setHeaderText((getUserProfile().UserComments == null ? 0 : getUserProfile().UserComments.size()) + " сообщений");
        m_ListFooter.setVisibility(getUserProfile().UserViews.size() > 0 ? View.VISIBLE : View.GONE);
    }

    private static class UserViewsAdapter extends ArrayAdapter<User> {
        private LayoutInflater m_Inflater;
        private int m_ThemeTitleSize = 13;
        private int m_TopTextSize = 10;
        private int m_BottomTextSize = 11;

        static class ViewHolder {
            View usericon;
            ImageView txtIsNew;
            TextView txtAuthor;
            TextView txtLastMessageDate;
            TextView txtTitle;
            TextView txtDescription;
            TextView txtForumTitle;

        }

        public void setData(UserProfile data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (User item : data.UserViews) {
                    add(item);
                }
            }
        }

        public UserViewsAdapter(Activity context) {
            super(context, R.layout.simple_list_item_2);


            m_Inflater = LayoutInflater.from(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
                    "interface.themeslist.title.font.size", 13);
            m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
            m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {

                convertView = m_Inflater.inflate(R.layout.theme_item, parent, false);
                holder = new ViewHolder();
                holder.txtIsNew = (ImageView) convertView
                        .findViewById(R.id.txtIsNew);
                holder.usericon = convertView.findViewById(R.id.usericon);


                holder.txtAuthor = (TextView) convertView
                        .findViewById(R.id.txtAuthor);
                holder.txtAuthor.setTextSize(m_TopTextSize);

                holder.txtLastMessageDate = (TextView) convertView
                        .findViewById(R.id.txtLastMessageDate);
                holder.txtLastMessageDate.setTextSize(m_TopTextSize);

                holder.txtTitle = (TextView) convertView
                        .findViewById(R.id.txtTitle);
                holder.txtTitle.setTextSize(m_ThemeTitleSize);

                holder.txtDescription = (TextView) convertView
                        .findViewById(R.id.txtDescription);
                holder.txtDescription.setTextSize(m_BottomTextSize);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            User item = this.getItem(position);

            holder.txtAuthor.setText(item.LastVisit + " ");
            if (TextUtils.isEmpty(item.MessagesCount))
                holder.txtLastMessageDate.setText(" ");
            else
                holder.txtLastMessageDate.setText("Сообщений: " + item.MessagesCount);
            holder.txtTitle.setText(item.getNick() == null ? "" : item.getNick() + " ");
            holder.txtDescription.setText(item.Group == null ? "" : item.Group + " ");


            if ("Online".equals(item.State)) {
                holder.txtIsNew.setImageResource(R.drawable.new_flag);
            } else {
                holder.txtIsNew.setImageBitmap(null);
            }

            return convertView;
        }
    }

}
