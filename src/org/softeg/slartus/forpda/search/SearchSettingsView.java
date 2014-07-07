package org.softeg.slartus.forpda.search;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.ForumsAdapter;
import org.softeg.slartus.forpda.classes.common.ArrayUtils;
import org.softeg.slartus.forpda.classes.common.ExtSpinner;
import org.softeg.slartus.forpda.common.Log;

import java.util.ArrayList;

/**
 * Created by slinkin on 20.12.13.
 */
public class SearchSettingsView extends TableLayout {
    private String mUserName;
    private int mSourceInt;
    private LayoutInflater inflater;


    private EditText username_edit;

    private CheckBox chkSubforums;
    public CheckBox chkTopics, chkSearchInTopic;

    private Spinner spnrSource, spnrSort;
    private Button btnAddForum;
    private Handler mHandler = new Handler();
    private SearchSettings m_SearchSettings;

    public SearchSettingsView(Context context) {
        super(context);

        initView(context);
    }


    public SearchSettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SearchSettingsView,
                0, 0);

        try {
            mUserName = a.getString(R.styleable.SearchSettingsView_user_name);
            mSourceInt = a.getInteger(R.styleable.SearchSettingsView_source, 0);
        } finally {
            a.recycle();
        }

        initView(context);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(l, t, r, b);
        }
    }

    private void initView(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(R.layout.search_settings, null));

        spnrSource = (Spinner) findViewById(R.id.spnrSource);
        ExtSpinner.setResourceAdapter(context, spnrSource, R.array.SearchSourceArray);
        spnrSort = (Spinner) findViewById(R.id.spnrSort);
        ExtSpinner.setResourceAdapter(context, spnrSort, R.array.SearchSortArray);

        username_edit = (EditText) findViewById(R.id.username_edit);

        btnAddForum = (Button) findViewById(R.id.btnAddForum);

        btnAddForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (m_MainForum == null) {
                    loadForums();
                } else {
                    showForums();
                }
            }
        }

        );

        chkTopics = (CheckBox) findViewById(R.id.chkTopics);

        chkSubforums = (CheckBox) findViewById(R.id.chkSubforums);

        chkSearchInTopic = (CheckBox) findViewById(R.id.chkSearchInTopic);
        chkSearchInTopic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                spnrSource.setEnabled(!b);
                // spnrSort.setEnabled(!b);
                btnAddForum.setEnabled(!b);
                chkSubforums.setEnabled(!b);
                chkTopics.setEnabled(!b);
            }
        });
    }

    private Forum m_MainForum = null;

    private void loadForums() {
        final ProgressDialog dialog = new AppProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    m_MainForum = Client.getInstance().loadForums();
                    mHandler.post(new Runnable() {
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showForums();
                        }
                    });

                } catch (Exception e) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.e(getContext(), e);
                }
            }
        }).start();
    }

    private ArrayList<String> m_CheckedIds = new ArrayList<String>();
    private ArrayList<String> m_VisibleIds = new ArrayList<String>();
    private ArrayList<CharSequence> forumCaptions;

    private void showForums() {
        if (forumCaptions == null) {
            forumCaptions = new ArrayList<CharSequence>();
            addForumCaptions(forumCaptions, m_MainForum, null, "");
        }
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View view = factory.inflate(R.layout.search_froums_list, null);

        ListView lstTree = (ListView) view.findViewById(R.id.lstTree);
        final ForumsAdapter adapter = new ForumsAdapter(getContext(), R.layout.search_forum_item, forumCaptions, m_CheckedIds, m_VisibleIds);

        lstTree.setAdapter(adapter);


        new AlertDialogBuilder(getContext())
                .setTitle("Форумы")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setForumButtonText();
                    }
                })
                .create().show();

    }

    private Forum getMainForum() {
        if (m_MainForum == null)
            try {
                m_MainForum = Client.getInstance().loadForums();
            } catch (Exception e) {
                m_MainForum = new Forum("all", "Все форумы");
                Log.e(getContext(), e);
            }
        return m_MainForum;

    }

    private void setForumButtonText() {
        try {
            StringBuilder sb = new StringBuilder();

            for (String key : m_CheckedIds) {
                if (key.equals("all")) {
                    sb.append("Все форумы;");
                } else {
                    Forum forum = getMainForum().findById(key, true, false);
                    if (forum != null)
                        sb.append(forum.getTitle() + ";");
//                Forum forum = Client.getInstance().MainForum.findById(key, true, false);
//                sb.append(forum.getTitle() + ";");
                }
            }
            if (sb.toString().equals(""))
                sb.append("Все форумы");
            btnAddForum.setText(sb.toString());
        } catch (Exception e) {
            Log.e(getContext(), e);
        }
    }

    private void addForumCaptions(ArrayList<CharSequence> forumCaptions, Forum forum, Forum parentForum, String node) {
        if (parentForum == null) {
            forumCaptions.add(">> Все форумы");
            m_VisibleIds.add("all");
        } else if (!parentForum.getId().equals(forum.getId())) {
            forumCaptions.add(node + forum.getTitle());
            m_VisibleIds.add(forum.getId());
        }
        if (parentForum == null)
            node = "  ";
        else if (node.trim().equals(""))
            node = "  |--";
        else
            node = node + "--";
        int childSize = forum.getForums().size();

        for (int i = 0; i < childSize; i++) {
            addForumCaptions(forumCaptions, forum.getForums().get(i), forum, node);
        }
    }

    public void setSearchSettings(SearchSettings searchSettings) {
        m_SearchSettings = searchSettings;

        spnrSort.setSelection(ArrayUtils.indexOf(m_SearchSettings.getSort(), getResources().getStringArray(R.array.SearchSortValues)));
        spnrSource.setSelection(ArrayUtils.indexOf(m_SearchSettings.getSource(), getResources().getStringArray(R.array.SearchSourceValues)));
        username_edit.setText(m_SearchSettings.getUserName());

        chkTopics.setChecked(m_SearchSettings.getResultsInTopicView());
        chkSubforums.setChecked(m_SearchSettings.Subforums());
        if (m_SearchSettings.isSearchInTopic()) {
            chkSearchInTopic.setVisibility(View.VISIBLE);
            chkSearchInTopic.setChecked(true);
        }

        m_CheckedIds = m_SearchSettings.getCheckedIds();
        setForumButtonText();
    }

    public void fillSearchSettings(SearchSettings searchSettings, String query, Boolean topicsResultView) {
        m_SearchSettings = searchSettings;

        m_SearchSettings.fillAndSave(query,
                username_edit.getText().toString(),
                getResources().getStringArray(R.array.SearchSourceValues)[spnrSource.getSelectedItemPosition()],
                getResources().getStringArray(R.array.SearchSortValues)[spnrSort.getSelectedItemPosition()],
                chkSubforums.isChecked(), m_CheckedIds, chkSearchInTopic.isChecked(), topicsResultView);

    }

    public View getUserNameEdit() {

        return username_edit;
    }
}
