package org.softeg.slartus.forpda.Tabs;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.ForumsAdapter;
import org.softeg.slartus.forpda.classes.common.ExtSpinner;
import org.softeg.slartus.forpda.common.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * User: slinkin
 * Date: 25.10.11
 * Time: 16:10
 */
public class TabDataSettingsActivity extends BaseFragmentActivity {

    private EditText username_edit, template_name_edit, query_edit;

    private CheckBox chkSubforums;


    private Spinner spnrSource, spnrSort, spnrTemplates;
    private Button btnAddForum;
    private Handler mHandler = new Handler();

    private String m_TabId;
    private String m_Template;
    private Forum m_MainForum = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.tab_data_settings);

        spnrSource = (Spinner) findViewById(R.id.spnrSource);
        ExtSpinner.setResourceAdapter(this, spnrSource, R.array.SearchSourceArray);

        spnrTemplates = (Spinner) findViewById(R.id.spnrTemplates);
        ExtSpinner.setResourceAdapter(this, spnrTemplates, R.array.themesTemplatesArray);

        spnrTemplates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setEnablesByTemplate();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spnrSort = (Spinner) findViewById(R.id.spnrSort);
        ExtSpinner.setResourceAdapter(this, spnrSort, R.array.SearchSortArray);
        spnrSort.setSelection(1);

        username_edit = (EditText) findViewById(R.id.username_edit);
        query_edit = (EditText) findViewById(R.id.query_edit);
        template_name_edit = (EditText) findViewById(R.id.template_name_edit);


        btnAddForum = (Button) findViewById(R.id.btnAddForum);
        btnAddForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (m_MainForum == null || m_MainForum.getForums().size() == 0) {
                    loadForums();
                } else {
                    showForums();
                }
            }
        });
        chkSubforums = (CheckBox) findViewById(R.id.chkSubforums);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        m_TabId = extras.getString("tabId");
        m_Template = extras.getString("template");

        loadSettings();
        setEnablesByTemplate();
    }

    private String getTabId() {
        return m_TabId;
    }

    @Override
    public void onBackPressed() {

        saveSettings();
        Toast.makeText(this, "Изменения вступят в силу после перезапуска программы!", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    private void loadSettings() {
        String tabTag = getTabId();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String template_source = preferences.getString(tabTag + ".Template.Source", "all");
        String template = preferences.getString(tabTag + ".Template", m_Template);
        String template_name = preferences.getString(tabTag + ".Template.Name", "");
        String template_sort = preferences.getString(tabTag + ".Template.Sort", "dd");
        String template_username = preferences.getString(tabTag + ".Template.UserName", "");
        String template_query = preferences.getString(tabTag + ".Template.Query", "");
        String template_forums = preferences.getString(tabTag + ".Template.Forums", "");
        m_CheckedIds = loadChecks(template_forums);
        Boolean template_Subforums = preferences.getBoolean(tabTag + ".Template.Subforums", true);

        if (template.equals(Tabs.TAB_SEARCH) && TextUtils.isEmpty(template_name)) {
            if (template_source.equals("all") && template_sort.equals("dd") && TextUtils.isEmpty(template_username)
                    && (m_CheckedIds.size() == 0) && template_Subforums)
                template_name = "Последние";
            else
                template_name = "Поиск";
        }


        setSpinnerValue(spnrSource, R.array.SearchSourceValues, template_source);
        setSpinnerValue(spnrTemplates, R.array.themesTemplatesValues, template);
        template_name_edit.setText(template_name);
        setSpinnerValue(spnrSort, R.array.SearchSortValues, template_sort);
        username_edit.setText(template_username);
        query_edit.setText(template_query);
        setSelectedFroumsText();
        chkSubforums.setChecked(template_Subforums);
    }


    private void setEnablesByTemplate() {
        String selectedTemplate = getSpinnerValue(spnrTemplates, R.array.themesTemplatesValues);
        Boolean isSearchTemplate = selectedTemplate.equals(Tabs.TAB_SEARCH);
        Boolean isForumsTemplate = selectedTemplate.equals(Tabs.TAB_FORUMS);

        int visibility = isSearchTemplate ? View.VISIBLE : View.GONE;

        findViewById(R.id.username_row).setVisibility(visibility);
        findViewById(R.id.query_row).setVisibility(visibility);
        findViewById(R.id.source_row).setVisibility(visibility);
        findViewById(R.id.sort_row).setVisibility(visibility);

        visibility = (isSearchTemplate || isForumsTemplate) ? View.VISIBLE : View.GONE;
        findViewById(R.id.name_row).setVisibility(visibility);
        findViewById(R.id.forums_row).setVisibility(visibility);
        findViewById(R.id.subforums_row).setVisibility(visibility);

    }


    private void saveSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        String tabTag = getTabId();
        editor.putString(tabTag + ".Template.Source", getSpinnerValue(spnrSource, R.array.SearchSourceValues));
        editor.putString(tabTag + ".Template", getSpinnerValue(spnrTemplates, R.array.themesTemplatesValues));
        editor.putString(tabTag + ".Template.Name", template_name_edit.getText().toString());
        editor.putString(tabTag + ".Template.Sort", getSpinnerValue(spnrSort, R.array.SearchSortValues));
        editor.putString(tabTag + ".Template.UserName", username_edit.getText().toString());
        editor.putString(tabTag + ".Template.Query", query_edit.getText().toString());
        editor.putString(tabTag + ".Template.Forums", getCheckedIdsString(m_CheckedIds));
        editor.putBoolean(tabTag + ".Template.Subforums", chkSubforums.isChecked());
        editor.commit();
    }

    private String getSpinnerValue(Spinner spinner, int valuesResId) {
        return getResources().getStringArray(valuesResId)[spinner.getSelectedItemPosition()];
    }

    private void setSpinnerValue(Spinner spinner, int valuesResId, String value) {
        ArrayList<String> values = new ArrayList();
        Collections.addAll(values, getResources().getStringArray(valuesResId));
        int index = values.indexOf(value);
        if (index == -1)
            index = 0;
        spinner.setSelection(index);

    }


    public static ArrayList<String> loadChecks(String checksString) {
        ArrayList<String> res = new ArrayList<String>();
        if (TextUtils.isEmpty(checksString)) return res;
        try {
            String[] pairs = checksString.split(";");
            for (int i = 0; i < pairs.length; i++) {
                String val = pairs[i];
                if (TextUtils.isEmpty(val)) continue;

                res.add(val);
            }
        } catch (Exception ex) {
            Log.e(null, ex);
        }
        return res;
    }

    public static String getCheckedIdsString(ArrayList<String> checkedIds) {
        StringBuilder sb = new StringBuilder();

        for (String key : checkedIds) {
            sb.append(key + ";");
        }
        return sb.toString();
    }


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
        if (forumCaptions == null || forumCaptions.size() < 2) {
            forumCaptions = new ArrayList<CharSequence>();
            addForumCaptions(forumCaptions, m_MainForum, null, "");
        }
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View view = factory.inflate(R.layout.search_froums_list, null);

        ListView lstTree = (ListView) view.findViewById(R.id.lstTree);
        ForumsAdapter adapter = new ForumsAdapter(getContext(), R.layout.search_forum_item, forumCaptions, m_CheckedIds, m_VisibleIds);

        lstTree.setAdapter(adapter);


        new AlertDialogBuilder(new ContextThemeWrapper(getContext(), MyApp.getInstance().getThemeStyleResID()))
                .setTitle("Форумы")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setSelectedFroumsText();
                    }
                })
                .create().show();

    }

    private void setSelectedFroumsText() {
        StringBuilder sb = new StringBuilder();

        for (String key : m_CheckedIds) {
            if (key.equals("all")) {
                sb.append("Все форумы;");
            } else {
                Forum forum = m_MainForum.findById(key, true, false);
                if (forum != null)
                    sb.append(forum.getTitle());
            }
        }
        if (sb.toString().equals(""))
            sb.append("Все форумы");
        btnAddForum.setText(sb.toString());
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


}
