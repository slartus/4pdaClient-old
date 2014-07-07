package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.IListItem;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by slinkin on 17.01.14.
 */
public abstract class ListTab extends BaseTab implements AdapterView.OnItemClickListener,
        Loader.OnLoadCompleteListener<ListTab.LoaderResult> {
    protected Boolean m_Refreshed = false;
    PullToRefreshListView m_ListView;
    private android.os.Handler mHandler = new android.os.Handler();
    protected ArrayList<IListItem> m_Items = new ArrayList<IListItem>();
    private ListItemsAdapter mAdapter;
    private String tabId;

    @Override
    public abstract String getTemplate();

    @Override
    public Boolean refreshed() {
        return m_Refreshed;
    }


    public String getTabId() {
        return tabId;
    }

    public abstract ArrayList<? extends IListItem> loadItems(int startFrom) throws IOException;

    public ListTab(Context context, String tabId, ITabParent tabParent) {
        super(context, tabParent);
        this.tabId = tabId;


        addView(inflate(context, R.layout.forum_tree, null),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        m_ListView = (PullToRefreshListView) findViewById(R.id.lstTree);

        setState(true);
        m_ListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListTab.this.onItemClick(adapterView, view, i, l);
            }
        });
        m_ListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mAdapter = new ListItemsAdapter(getContext(), new ArrayList<IListItem>());
        m_ListView.getRefreshableView().setAdapter(mAdapter);
    }


    protected ArrayList<IListItem> loadItems(Bundle extras) throws IOException {
        return new ArrayList<IListItem>();
    }

    Bundle m_Extras;

    @Override
    public void refresh(Bundle extras) {
        m_Extras = extras;
        refresh();
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

    private void refreshData() {
        m_Items.clear();
        getAdapter().clear();
        m_Refreshed = true;
        setState(true);
        ListLoader qmsUsersLoader = new ListLoader(getContext(), new GetItemsInterface() {
            @Override
            public int getStartCount() {
                return m_Items.size();
            }

            @Override
            public ArrayList<? extends IListItem> loadItems() throws IOException {
                return ListTab.this.loadItems(getStartCount());
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

    public IListItem getItem(ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return null;
        return mAdapter.getItem((int) info.id);
    }

    public ListItemsAdapter getAdapter() {
        return mAdapter;
    }

    protected void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public ListView getListView() {
        return m_ListView.getRefreshableView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        IListItem item = mAdapter.getItem((int) info.id);
    }


    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        l = ListViewMethodsBridge.getItemId(getContext(), i, l);
        if (l < 0 || mAdapter.getCount() <= l) return;
        IListItem item = mAdapter.getItem((int) l);
    }

    @Override
    public void onLoadComplete(Loader<ListTab.LoaderResult> qmsUsersLoader, ListTab.LoaderResult data) {
        if (data.Ex != null) {
            setState(false);
            Log.e(getContext(), data.Ex);
            return;
        }
        if (data != null && data.Items != null) {
            for (IListItem item : data.Items) {
                m_Items.add(item);
            }
            mAdapter.setData(m_Items);
        } else {
            m_Items = new ArrayList<IListItem>();
            mAdapter.setData(m_Items);

        }
        setState(false);
        mAdapter.notifyDataSetChanged();
        if (getTabParent() != null)
            getTabParent().setTitle(getTitle());
    }

    public interface GetItemsInterface {
        int getStartCount();

        ArrayList<? extends IListItem> loadItems() throws IOException;
    }

    public static class LoaderResult {
        public ArrayList<? extends IListItem> Items = null;
        public Throwable Ex = null;
    }

    private static class ListLoader extends AsyncTaskLoader<LoaderResult> {

        LoaderResult m_Data;
        GetItemsInterface m_GetUsersInterface;


        public ListLoader(Context context, GetItemsInterface getUsersInterface) {
            super(context);
            m_GetUsersInterface = getUsersInterface;
        }

        @Override
        public LoaderResult loadInBackground() {
            LoaderResult res = new LoaderResult();
            try {
                res.Items = m_GetUsersInterface.loadItems();

            } catch (Throwable e) {
                res.Ex = e;

            }
            return res;
        }

        @Override
        public void deliverResult(LoaderResult data) {

            if (isReset()) {
                if (data != null && data.Items != null) {
                    onReleaseResources();
                }
            }
            m_Data = data;

            if (isStarted()) {
                super.deliverResult(data);
            }

            if (data != null && data.Items != null) {
                onReleaseResources();
            }
        }


        @Override
        protected void onStartLoading() {
            if (m_Data != null && m_Data.Items != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(m_Data);
            }

            if (takeContentChanged() || m_Data == null || m_Data.Items == null) {
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
        public void onCanceled(LoaderResult items) {
            super.onCanceled(items);

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
            if (m_Data != null && m_Data.Items != null) {
                onReleaseResources();
                m_Data = null;
            }


        }

        protected void onReleaseResources() {
            if (m_Data != null && m_Data.Items != null)
                m_Data.Items.clear();

            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }

    public static class ListItemsAdapter extends ArrayAdapter<IListItem> {
        private LayoutInflater m_Inflater;

        public void setData(ArrayList<IListItem> data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (IListItem item : data) {
                    add(item);
                }
            }
        }

        public ListItemsAdapter(Context context, ArrayList<IListItem> objects) {
            super(context, R.layout.list_item, objects);

            m_Inflater = LayoutInflater.from(context);

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//        m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
//                "interface.themeslist.title.font.size", 13);
//        m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
//        m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.list_item, parent, false);

                holder = new ViewHolder();
                holder.imgFlag = (ImageView) convertView.findViewById(R.id.imgFlag);
                holder.txtTopLeft = (TextView) convertView.findViewById(R.id.txtTopLeft);
                holder.txtTopRight = (TextView) convertView.findViewById(R.id.txtTopRight);
                holder.txtMain = (TextView) convertView.findViewById(R.id.txtMain);
                holder.txtSubMain = (TextView) convertView.findViewById(R.id.txtSubMain);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            IListItem item = this.getItem(position);

            switch (item.getState()) {
                case IListItem.STATE_GREEN:
                    holder.imgFlag.setImageResource(R.drawable.new_flag);
                    break;
                case IListItem.STATE_RED:
                    holder.imgFlag.setImageResource(R.drawable.old_flag);
                    break;
                default:
                    holder.imgFlag.setImageBitmap(null);
            }

            holder.txtTopLeft.setText(item.getTopLeft());
            holder.txtTopRight.setText(item.getTopRight());
            holder.txtMain.setText(item.getMain());
            holder.txtSubMain.setText(item.getSubMain());

            return convertView;
        }

        public class ViewHolder {
            ImageView imgFlag;
            TextView txtTopLeft;
            TextView txtTopRight;
            TextView txtMain;
            TextView txtSubMain;
        }

    }
}
