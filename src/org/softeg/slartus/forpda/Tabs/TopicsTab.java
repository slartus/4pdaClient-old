package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.QuickStartActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.common.HelpTask;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicApi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * Created by slinkin on 17.01.14.
 */
public abstract class TopicsTab extends ListTab {
    public TopicsTab(Context context, String tabId, ITabParent tabParent) {
        super(context, tabId, tabParent);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        try {
            l = ListViewMethodsBridge.getItemId(getContext(), i, l);
            if (l < 0 || getAdapter().getCount() <= l) return;
            final IListItem item = getAdapter().getItem((int) l);
            if (item == null) return;
            if (TextUtils.isEmpty(item.getId())) return;
            if (item.getId().equals("-1"))
                return;


            if (ThemesTab.getTopicNavigateAction(getTabId(), getTemplate()) == null) {
                ThemesTab.showNavigateDialog(getActivity(), getTabId(), getTemplate(),
                        item.getId(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        item.setState(IListItem.STATE_NORMAL);
                        notifyDataSetChanged();
                    }
                });
                return;
            }

            ExtTopic.showActivity(getContext(), item.getId(), ThemesTab.getOpenThemeParams(getTabId(), getTemplate()));
            item.setState(IListItem.STATE_NORMAL);
            notifyDataSetChanged();

        } catch (Throwable ex) {
            Log.e(this.getContext(), ex);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        try {
            final IListItem topic = getItem(menuInfo);
            if (topic == null) return;
            if (TextUtils.isEmpty(topic.getId())) return;

            menu.add(R.string.navigate_getfirstpost).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_FIRST_POST, "");

                    return true;
                }
            });
            menu.add(R.string.navigate_getlastpost).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_LAST_POST, "view=getlastpost");

                    return true;
                }
            });
            menu.add(R.string.navigate_getnewpost).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_NEW_POST, "view=getnewpost");

                    return true;
                }
            });

            ExtUrl.addUrlSubMenu(handler, getContext(), menu, ExtTopic.getShowBrowserUrl(topic.getId(), ""), topic.getId(),
                    topic.getMain().toString());
            addOptionsMenu(getContext(), handler, menu, topic, true, null);
        } catch (Throwable ex) {
            Log.e(this.getContext(), ex);
        }
    }

    private void showSaveNavigateActionDialog(final IListItem topic, final CharSequence selectedAction,
                                              final String params) {
        ThemeAdapter.showSaveNavigateActionDialog(getContext(), getTabId(), getTemplate(), selectedAction,
                new Runnable() {
                    @Override
                    public void run() {
                        showTopicActivity(topic, params);
                    }
                });
    }

    private void showTopicActivity(IListItem topic, String params) {
        ExtTopic.showActivity(getContext(), topic.getId(), params);
        topic.setState(IListItem.STATE_NORMAL);
        notifyDataSetChanged();
    }

    public static SubMenu addOptionsMenu(final Context context, final Handler mHandler, Menu menu, final IListItem topic,
                                         Boolean addFavorites, final String shareItUrl) {
        SubMenu optionsMenu = menu.addSubMenu("Опции..").setIcon(R.drawable.ic_menu_more);

        configureOptionsMenu(context, mHandler, optionsMenu, topic, addFavorites, shareItUrl);
        return optionsMenu;
    }

    public static void configureOptionsMenu(final Context context, final Handler mHandler, SubMenu optionsMenu, final IListItem topic,
                                            Boolean addFavorites, final String shareItUrl) {
        optionsMenu.clear();

        if (Client.getInstance().getLogined()) {
            optionsMenu.add(context.getString(R.string.AddToFavorites)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ExtTopic.startSubscribe(context, mHandler,topic.getId().toString());

                    return true;
                }
            });

            optionsMenu.add(context.getString(R.string.DeleteFromFavorites)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    final HelpTask helpTask = new HelpTask(context, context.getString(R.string.DeletingFromFavorites));
                    helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                        public Object onMethod(Object param) {
                            if (helpTask.Success)
                                Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                            else
                                Log.e(context, helpTask.ex);
                            return null;
                        }
                    });
                    helpTask.execute(new HelpTask.OnMethodListener() {
                                         public Object onMethod(Object param) throws IOException, ParseException, URISyntaxException {
                                              return TopicApi.deleteFromFavorites(Client.getInstance(), topic.getId().toString());
                                         }
                                     }
                    );

                    return true;
                }
            });
        }
        optionsMenu.add(context.getString(R.string.NotesByTopic)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {

                Intent intent = new Intent(context, QuickStartActivity.class);
                intent.putExtra("template", NotesTab.TEMPLATE);
                intent.putExtra(NotesTab.TOPIC_ID_KEY, topic.getId());
                context.startActivity(intent);
                return true;
            }
        });
        optionsMenu.add(context.getString(R.string.Share)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {

                try {
                    Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
                    sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, topic.getMain());
                    sendMailIntent.putExtra(Intent.EXTRA_TEXT, TextUtils.isEmpty(shareItUrl) ? ("http://4pda.ru/forum/index.php?showtopic=" + topic.getId()) : shareItUrl);
                    sendMailIntent.setType("text/plain");

                    context.startActivity(Intent.createChooser(sendMailIntent, context.getString(R.string.Share)));
                } catch (Exception ex) {
                    return false;
                }
                return true;
            }
        });
        //return optionsMenu;
    }

}
