package org.softeg.slartus.forpda.Tabs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.db.NotesTable;
import org.softeg.slartus.forpda.notes.Note;
import org.softeg.slartus.forpda.notes.NoteActivity;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 21.02.13
 * Time: 8:20
 * To change this template use File | Settings | File Templates.
 */
public class NotesTab extends ThemesTab {
    public static final String TOPIC_ID_KEY = "TopicId";
    public static final String TEMPLATE = Tabs.TAB_NOTES;
    public static final String TITLE = "Заметки";
    private String m_TopicId = null;
    private Handler mHandler = new Handler();

    @Override
    public Boolean cachable() {
        return true;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    public NotesTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabTag, tabParent);

    }


    @Override
    public void refresh(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(TOPIC_ID_KEY))
            m_TopicId = savedInstanceState.getString(TOPIC_ID_KEY);
        super.refresh(savedInstanceState);

    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws Exception {

        m_Themes = NotesTable.getNoteThemes(m_TopicId);
    }

    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getContext(), i, l);
        if (l < 0 || m_ThemeAdapter.getCount() <= l) return;
        if (m_ThemeAdapter == null) return;
        ExtTopic topic = m_ThemeAdapter.getItem((int) l);
        if (TextUtils.isEmpty(topic.getId())) return;
        NoteActivity.showNote(getContext(), topic.getId());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final ExtTopic topic = m_ThemeAdapter.getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;

        AddLinksSubMenu(menu, topic);

        menu.add("Удалить..").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialogBuilder(getContext())
                        .setTitle("Подтвердите действие")
                        .setMessage("Удалить заметку?")
                        .setCancelable(true)
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    NotesTable.delete(topic.getId());
                                    m_Themes.remove(m_Themes.findById(topic.getId()));
                                    m_ThemeAdapter.notifyDataSetChanged();
                                } catch (Throwable ex) {
                                    Log.e(getContext(), ex);
                                }
                            }
                        })
                        .create().show();
                return true;
            }
        });

    }

    private void AddLinksSubMenu(ContextMenu menu, ExtTopic topic) {
        try {
            Note note = NotesTable.getNote(topic.getId());
            if (note != null) {
                ArrayList<Pair> links = note.getLinks();
                if (links.size() != 0) {
                    android.view.SubMenu linksMenu = menu.addSubMenu("Ссылки...");
                    for (final Pair pair : links) {
                        linksMenu.add(pair.first.toString())
                                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        IntentActivity.tryShowUrl((Activity) getContext(), mHandler, pair.second.toString(), true, false, null);
                                        return true;
                                    }
                                });
                    }
                }
            }
        } catch (Throwable e) {
            Log.e(getContext(), e);
        }
    }

}
