package org.softeg.slartus.forpda;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.classes.BbCodesPanel;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.common.HtmlUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.emotic.SmilesBbCodePanel;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdaapi.Post;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.11.11
 * Time: 12:42
 */
public class EditPostPlusActivity extends BaseFragmentActivity {
    private Button tglGallerySwitcher;
    private Gallery glrSmiles, glrBbCodes;
    private EditText txtPost, txtpost_edit_reason;
    private ToggleButton tglEnableEmo, tglEnableSig;
    private Button btnAttachments;
    private ProgressBar progress_search;
    private String forumId;
    private String m_AttachFilePath;
    private String lastSelectDirPath = Environment.getExternalStorageDirectory().getPath();
    private String themeId;
    private String postId;

    private String authKey;
    private String attachPostKey;
    final Handler uiHandler = new Handler();
    // подтверждение отправки
    private Boolean m_ConfirmSend = true;
    // флаг добавлять подпись к сообщению
    private Boolean m_Enablesig = true;
    private Boolean m_EnableEmo = true;
    private final int REQUEST_SAVE = 0;
    private final int REQUEST_SAVE_IMAGE = 1;
    private MenuFragment mFragment1;
    private String postText;
    private View m_BottomPanel;

    public static void editPost(Context context, String forumId, String topicId, String postId, String authKey) {
        Intent intent = new Intent(context, EditPostPlusActivity.class);

        intent.putExtra("forumId", forumId);
        intent.putExtra("themeId", topicId);
        intent.putExtra("postId", postId);
        intent.putExtra("authKey", authKey);
        context.startActivity(intent);
    }

    public static void newPost(Context context, String forumId, String topicId, String authKey,
                               final String body) {
        Intent intent = new Intent(context, EditPostPlusActivity.class);

        intent.putExtra("forumId", forumId);
        intent.putExtra("themeId", topicId);
        intent.putExtra("postId", "-1");
        intent.putExtra("body", body);
        intent.putExtra("authKey", authKey);
        context.startActivity(intent);
    }

    public static void newPostWithAttach(Context context, String forumId, String topicId, String authKey,
                                         final Bundle extras) {
        Intent intent = new Intent(context, EditPostPlusActivity.class);

        intent.putExtra("forumId", forumId);
        intent.putExtra("themeId", topicId);
        intent.putExtra("postId", "-1");
        intent.putExtra("postId", "-1");
        intent.putExtras(extras);
        intent.putExtra("authKey", authKey);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);

//        setTheme(MyApp.getInstance().getThemeStyleResID());
        setContentView(R.layout.edit_post_plus);

//        if (getResources().getBoolean(R.bool.screen_small))
//            getSupportActionBar().hide();

        progress_search = (ProgressBar) findViewById(R.id.progress_search);
        lastSelectDirPath = prefs.getString("EditPost.AttachDirPath", lastSelectDirPath);
        m_ConfirmSend = prefs.getBoolean("theme.ConfirmSend", true);
        m_BottomPanel = findViewById(R.id.bottomPanel);
        glrBbCodes = (Gallery) findViewById(R.id.glrBbCodes);
        glrSmiles = (Gallery) findViewById(R.id.glrSmiles);
        txtPost = (EditText) findViewById(R.id.txtPost);
        txtpost_edit_reason = (EditText) findViewById(R.id.txtpost_edit_reason);
        new BbCodesPanel(this, glrBbCodes, txtPost);
        new SmilesBbCodePanel(this, glrSmiles, txtPost);

        tglGallerySwitcher = (Button) findViewById(R.id.tglGallerySwitcher);
        tglGallerySwitcher.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                glrBbCodes.setVisibility(glrBbCodes.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                glrSmiles.setVisibility(glrSmiles.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                tglGallerySwitcher.setText(glrBbCodes.getVisibility() == View.VISIBLE ? ":)" : "Bb");
            }
        });
        tglEnableSig = (ToggleButton) findViewById(R.id.tglEnableSig);
        tglEnableEmo = (ToggleButton) findViewById(R.id.tglEnableEmo);

        btnAttachments = (Button) findViewById(R.id.btnAttachments);
        btnAttachments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showAttachesListDialog();
            }
        });

        try {
            Intent intent = getIntent();
            setDataFromExtras(intent.getExtras());
        } catch (Throwable ex) {
            Log.e(this, ex);
        }

        createActionMenu();

        startLoadPost();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("forumId", forumId);
        outState.putString("m_AttachFilePath", m_AttachFilePath);
        outState.putString("lastSelectDirPath", lastSelectDirPath);
        outState.putString("themeId", themeId);
        outState.putString("postId", postId);
        outState.putString("authKey", authKey);
        outState.putString("attachPostKey", attachPostKey);
        outState.putString("postText", txtPost.getText().toString());
        outState.putString("txtpost_edit_reason", txtpost_edit_reason.getText().toString());
        outState.putBoolean("Enablesig", tglEnableSig.isChecked());
        outState.putBoolean("EnableEmo", tglEnableEmo.isChecked());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        forumId = savedInstanceState.getString("forumId");
        m_AttachFilePath = savedInstanceState.getString("m_AttachFilePath");
        lastSelectDirPath = savedInstanceState.getString("lastSelectDirPath");
        themeId = savedInstanceState.getString("themeId");
        postId = savedInstanceState.getString("postId");
        authKey = savedInstanceState.getString("authKey");
        attachPostKey = savedInstanceState.getString("attachPostKey");
        txtPost.setText(savedInstanceState.getString("postText"));
        txtpost_edit_reason.setText(savedInstanceState.getString("txtpost_edit_reason"));
        tglEnableSig.setChecked(savedInstanceState.getBoolean("Enablesig"));
        tglEnableEmo.setChecked(savedInstanceState.getBoolean("EnableEmo"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setDataFromExtras(Bundle extras) {
        forumId = extras.getString("forumId");
        themeId = extras.getString("themeId");
        postId = extras.getString("postId");
        authKey = extras.getString("authKey");

        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            Uri uri = (Uri) extras.get(Intent.EXTRA_STREAM);
            m_AttachFilePath = getRealPathFromURI(uri);
        }

        if (extras.containsKey(Intent.EXTRA_TEXT))
            txtPost.setText(extras.get(Intent.EXTRA_TEXT).toString());
        if (extras.containsKey(Intent.EXTRA_HTML_TEXT))
            txtPost.setText(extras.get(Intent.EXTRA_HTML_TEXT).toString());

        if (isNewPost()) {
            if (extras.containsKey("body"))
                txtPost.setText(extras.get("body").toString());
        }

    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        menu.add("Отправить")
//                .setIcon(android.R.drawable.ic_menu_send)
//                .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
//
//                    public boolean onMenuItemClick(android.view.MenuItem item) {
//
//                        final String body = getPostText();
//                        if (TextUtils.isEmpty(body))
//                            return true;
//
//                        if (getConfirmSend()) {
//                            new AlertDialogBuilder(EditPostPlusActivity.this)
//                                    .setTitle("Уверены?")
//                                    .setMessage("Подтвердите отправку")
//                                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            dialogInterface.dismiss();
//                                            sendPost(body);
//
//                                        }
//                                    })
//                                    .setNegativeButton("Отмена", null)
//                                    .create().show();
//                        } else {
//                            sendPost(body);
//                        }
//
//                        return true;
//                    }
//                });
//
//
//        return super.onCreateOptionsMenu(menu);
//    }

    private Boolean isNewPost() {
        return postId.equals("-1");
    }

    private Dialog mAttachesListDialog;

    private void showAttachesListDialog() {
//        if (attaches.size() == 0) {
//            Toast.makeText(this, "Ни одного файла не загружено", Toast.LENGTH_SHORT).show();
//            return;
//        }
        String[] caps = new String[attaches.size()];
        int i = 0;
        for (Attach attach : attaches) {
            caps[i++] = attach.getName();
        }
        AttachesAdapter adapter = new AttachesAdapter(attaches, this);
        //  ListAdapter adapter = new ArrayAdapter<Attach>(this, R.layout.attachment_spinner_item, attaches);
        mAttachesListDialog = new AlertDialogBuilder(this)
                .setCancelable(true)
                .setTitle("Вложения")
                .setSingleChoiceItems(adapter, -1, null)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        startAddAttachment();
                    }
                })
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        mAttachesListDialog.show();
    }

    private void startAddAttachment() {
//        if (TextUtils.isEmpty(txtPost.getText().toString())) {
//            Toast.makeText(EditPostPlusActivity.this, "Вы должны ввести сообщение", Toast.LENGTH_SHORT).show();
//            return;
//        }


        CharSequence[] items = new CharSequence[]{"Файл", "Изображение"};
        new AlertDialogBuilder(EditPostPlusActivity.this)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        switch (i) {
                            case 0://файл
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("file/*");
                                    intent.setDataAndType(Uri.parse("file://" + lastSelectDirPath), "file/*");
                                    startActivityForResult(intent, REQUEST_SAVE);

                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(EditPostPlusActivity.this, "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Log.e(EditPostPlusActivity.this, ex);
                                }

//                                Intent intent = new Intent(EditPostPlusActivity.this.getBaseContext(),
//                                        FileDialog.class);
//                                intent.putExtra(FileDialog.START_PATH, lastSelectDirPath);
//                                EditPostPlusActivity.this.startActivityForResult(intent, REQUEST_SAVE);
                                break;
                            case 1:// Изображение
                                try {
                                    Intent imageintent = new Intent(
                                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                                    startActivityForResult(imageintent, REQUEST_SAVE_IMAGE);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(EditPostPlusActivity.this, "Ни одно приложение не установлено для выбора изображения!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Log.e(EditPostPlusActivity.this, ex);
                                }
                                break;
                        }
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                String postText = TextUtils.isEmpty(txtPost.getText()) ? TEMP_EMPTY_TEXT : txtPost.getText().toString();
                if (requestCode == REQUEST_SAVE) {
//                    m_AttachFilePath = data.getStringExtra(FileDialog.RESULT_PATH);

                    m_AttachFilePath = getRealPathFromURI(data.getData());


                    saveAttachDirPath();

                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this).execute(postText, txtpost_edit_reason.getText().toString());
                } else if (requestCode == REQUEST_SAVE_IMAGE) {

                    Uri selectedImage = data.getData();

                    m_AttachFilePath = getRealPathFromURI(selectedImage);


                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this).execute(postText, txtpost_edit_reason.getText().toString());
                }
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }

    }

    public String getRealPathFromURI(Uri contentUri) {
        if (!contentUri.toString().startsWith("content://"))
            return contentUri.getPath();

        // can post image
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,
                filePathColumn, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    private void saveAttachDirPath() {
        lastSelectDirPath = FileUtils.getDirPath(m_AttachFilePath);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EditPost.AttachDirPath", lastSelectDirPath);
        editor.commit();
    }

    private void startLoadPost() {
        new LoadTask(this).execute();
    }

    private void sendPost(final String text, String editPostReason) {
        m_Enablesig = tglEnableSig.isChecked();
        m_EnableEmo = tglEnableEmo.isChecked();
        if (isNewPost()) {
            new PostTask(EditPostPlusActivity.this).execute(text, editPostReason);
        } else {
            new AcceptEditTask(EditPostPlusActivity.this).execute(text, editPostReason);
        }
    }

    private void parsePody(String body) {
        String startFlag = "<textarea name=\"Post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
        int startIndex = body.indexOf(startFlag);
        startIndex += startFlag.length();
        int endIndex = body.indexOf("</textarea>", startIndex);

        if (TextUtils.isEmpty(txtPost.getText().toString()))
            txtPost.setText(HtmlUtils.modifyHtmlQuote(body.substring(startIndex, endIndex)));

        EditPostPlusActivity.this.attachPostKey = null;
        Matcher m = Pattern.compile("name='attach_post_key' value='(.*?)'").matcher(body);
        if (m.find()) {
            EditPostPlusActivity.this.attachPostKey = m.group(1);
        }

        txtpost_edit_reason.setText(null);
        m = Pattern.compile("name=('|\")post_edit_reason('|\") value=('|\")(.*?)('|\")").matcher(body);
        if (m.find()) {
            txtpost_edit_reason.setText(m.group(4));
            if (!TextUtils.isEmpty(m.group(4)))
                txtpost_edit_reason.setVisibility(View.VISIBLE);
        }
        parseAttaches(body);
    }

    private Attaches attaches = new Attaches();

    private void parseAttaches(String body) {
        Pattern pattern = Pattern.compile("onclick=\"insText\\('\\[attachment=(\\d+):(.*?)\\]'\\)");
        Pattern attachBodyPattern = Pattern.compile("<!-- ATTACH -->([\\s\\S]*?)</i>", Pattern.MULTILINE);
        Matcher m = attachBodyPattern.matcher(body);
        attaches = new Attaches();
        if (m.find()) {
            Matcher m1 = pattern.matcher(m.group(1));
            while (m1.find()) {
                attaches.add(new Attach(m1.group(1), m1.group(2)));
            }
        } else {
            Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                    "\n" +
                    "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
            m = checkPattern.matcher(body);
            if (m.find()) {
                Toast.makeText(this, m.group(1), Toast.LENGTH_LONG).show();
            }
        }
        btnAttachments.setText(attaches.size() + "");

    }

    public void toggleEditReasonDialog() {
        txtpost_edit_reason.setVisibility(txtpost_edit_reason.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
//    public void showEditReasonDialog(){
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View layout = inflater.inflate(R.layout.edit_text_dialog, null);
//
//        final AlertDialog dialog = new AlertDialogBuilder(this)
//                .setTitle("Причина редактирования")
//                .setView(layout)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        post_edit_reason= ((EditText) layout.findViewById(org.softeg.slartus.forpda.R.id.text)).getText().toString();
//                        dialogInterface.dismiss();
//                    }
//                })
//                .setNegativeButton("Отмена", null)
//                .setCancelable(true)
//                .create();
//
//        ((EditText)layout.findViewById(org.softeg.slartus.forpda.R.id.text)).setText(post_edit_reason);
//        dialog.show();
//    }

    public String getPostText() {
        return txtPost.getText().toString();
    }

    public String getEditReasonText() {
        return txtpost_edit_reason.getText().toString();
    }

    public boolean getConfirmSend() {
        return m_ConfirmSend;
    }


    private static final String TEMP_EMPTY_TEXT = "<temptext>";

    private class UpdateTask extends AsyncTask<String, Integer, Boolean> {


        private final ProgressDialog dialog;
        private ProgressState m_ProgressState;

        public UpdateTask(Context context) {

            dialog = new AppProgressDialog(context);

        }

        String body = null;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_ProgressState = new ProgressState() {
                    @Override
                    public void update(String message, int percents) {
                        publishProgress(percents);
                    }
                };
                String postBody = params[0];
                if (TextUtils.isEmpty(postBody))
                    postBody = TEMP_EMPTY_TEXT;
                String post_edit_reason = params.length > 1 ? params[1] : "";
                body = Client.getInstance().attachFilePost(forumId, themeId, authKey, attachPostKey,
                        postId, m_Enablesig, m_EnableEmo, postBody, m_AttachFilePath, attaches.getFileList(),
                        m_ProgressState, post_edit_reason);
                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            dialog.setProgress(values[0]);
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка файла...");
            this.dialog.setCancelable(true);
            this.dialog.setMax(100);
            this.dialog.setCanceledOnTouchOutside(false);
            this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    m_ProgressState.cancel();
                    cancel(false);
                }
            });
            this.dialog.setProgress(0);
            this.dialog.setIndeterminate(false);
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success || (isCancelled() && body != null)) {
                parseAttaches(body);
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onCancelled(Boolean success) {
            super.onCancelled(success);
            if (success || (isCancelled() && body != null)) {
                parseAttaches(body);
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class DeleteTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;

        public DeleteTask(Context context) {

            dialog = new AppProgressDialog(context);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String post_edit_reason = params.length > 1 ? params[1] : "";
                body = Client.getInstance().deleteAttachFilePost(forumId, themeId, authKey, postId, m_Enablesig, m_EnableEmo,
                        params[0],
                        m_AttachFilePath, attaches.getFileList(), post_edit_reason);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление файла...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parseAttaches(body);

            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class AcceptEditTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;

        public AcceptEditTask(Context context) {

            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String post_edit_reason = params.length > 1 ? params[1] : "";
                Client.getInstance().editPost(forumId, themeId, authKey, postId, m_Enablesig, m_EnableEmo,
                        params[0], attaches.getFileList(), post_edit_reason);
                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Редактирование сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                ThemeActivity.s_ThemeId = themeId;
                ThemeActivity.s_Params = "view=findpost&p=" + postId;
                if (!ThemeActivity.class.toString().equals(getIntent().getExtras().get(BaseActivity.SENDER_ACTIVITY))) {
                    ExtTopic.showActivity(EditPostPlusActivity.this, themeId, ThemeActivity.s_Params);
                }
                finish();

            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;

        public LoadTask(Context context) {
            dialog = new AppProgressDialog(context);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }

        String body = null;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Map<String, String> outParams = new HashMap<String, String>();
                body = Client.getInstance().getEditPostPlus(forumId, themeId, postId, authKey, outParams);
                if (outParams.size() > 0) {
                    forumId = outParams.get("forumId");
                    authKey = outParams.get("authKey");
                }
                return true;
            } catch (Throwable e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        protected void onCancelled() {
            Toast.makeText(EditPostPlusActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parsePody(body);
                if (!TextUtils.isEmpty(m_AttachFilePath)) {
                    saveAttachDirPath();

                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this).execute("");
                }

            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;
        private String mPostResult = null;
        private String mError = null;

        public PostTask(Context context) {

            dialog = new AppProgressDialog(context);
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = Client.getInstance().reply(forumId, themeId, authKey, attachPostKey,
                        params[0], m_Enablesig, m_EnableEmo, false, attaches.getFileList());

                mError = Post.checkPostErrors(mPostResult);
                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if (!TextUtils.isEmpty(mError)) {
                    Toast.makeText(EditPostPlusActivity.this, "Ошибка: " + mError, Toast.LENGTH_LONG).show();
                    return;
                }
                ThemeActivity.s_ThemeBody = mPostResult;
                if (isNewPost())
                    ThemeActivity.s_Params = "view=getlastpost";
                else
                    ThemeActivity.s_Params = "view=findpost&p=" + postId;


                if (!ThemeActivity.class.toString().equals(getIntent().getExtras().get(BaseActivity.SENDER_ACTIVITY))) {
                    ExtTopic.showActivity(EditPostPlusActivity.this, themeId, ThemeActivity.s_Params);
                }
                finish();
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public class AttachesAdapter extends BaseAdapter {
        private Activity activity;
        private final ArrayList<Attach> content;

        public AttachesAdapter(ArrayList<Attach> content, Activity activity) {
            super();
            this.content = content;
            this.activity = activity;
        }

        public int getCount() {
            return content.size();
        }

        public Attach getItem(int i) {
            return content.get(i);
        }

        public long getItemId(int i) {
            return i;
        }


        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                final LayoutInflater inflater = activity.getLayoutInflater();

                convertView = inflater.inflate(R.layout.attachment_spinner_item, parent, false);


                holder = new ViewHolder();


                holder.btnDelete = (ImageButton) convertView
                        .findViewById(R.id.btnDelete);
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();

                        Attach attach = (Attach) view.getTag();
                        m_AttachFilePath = attach.getId();
                        new DeleteTask(EditPostPlusActivity.this).execute(txtPost.getText().toString(), txtpost_edit_reason.getText().toString());
                    }
                });

                holder.btnSpoiler = (ImageButton) convertView
                        .findViewById(R.id.btnSpoiler);
                holder.btnSpoiler.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();

                        int selectionStart = txtPost.getSelectionStart();
                        if (selectionStart == -1)
                            selectionStart = 0;
                        Attach attach = (Attach) view.getTag();
                        txtPost.getText().insert(selectionStart, "[spoiler][attachment=" + attach.getId() + ":" + attach.getName() + "][/spoiler]");
                    }
                });

                holder.txtFile = (TextView) convertView
                        .findViewById(R.id.txtFile);
                holder.txtFile.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();
                        int selectionStart = txtPost.getSelectionStart();
                        if (selectionStart == -1)
                            selectionStart = 0;
                        Attach attach = (Attach) view.getTag();
                        txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getName() + "]");
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Attach attach = this.getItem(position);
            holder.btnDelete.setTag(attach);
            holder.btnSpoiler.setTag(attach);
            holder.txtFile.setText(attach.getName());
            holder.txtFile.setTag(attach);

            return convertView;
        }

        public class ViewHolder {

            ImageButton btnSpoiler;
            ImageButton btnDelete;
            TextView txtFile;
        }
    }

    private class Attach {
        private String mId;
        private String mName;

        public Attach(String id, String name) {
            mId = id;
            mName = name;
        }

        public String getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private class Attaches extends ArrayList<Attach> {
        public String getFileList() {
            String res = "0";
            for (Attach attach : this) {
                res += "," + attach.getId();
            }
            return res;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            m_BottomPanel.setVisibility(View.VISIBLE);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return true;
    }

    public void hidePanels() {
        getSupportActionBar().hide();
        m_BottomPanel.setVisibility(View.GONE);
    }

    private static final int SEARCH_RESULT_FOUND = 1;
    private static final int SEARCH_RESULT_NOTFOUND = 0;
    private static final int SEARCH_RESULT_EMPTYTEXT = -1;

    private Spannable clearPostHighlight() {
        int startSearchSelection = txtPost.getSelectionStart();
        Spannable raw = new SpannableString(txtPost.getText());
        BackgroundColorSpan[] spans = raw.getSpans(0,
                raw.length(),
                BackgroundColorSpan.class);

        for (BackgroundColorSpan span : spans) {
            raw.removeSpan(span);
        }
        txtPost.setSelection(startSearchSelection);
        txtPost.setCursorVisible(true);
        return raw;
    }

    private Boolean startSearch = false;

    private Boolean getStartSearch() {
        synchronized (startSearch) {
            return startSearch;
        }
    }

    private void setStartSearch(Boolean value) {
        synchronized (startSearch) {
            startSearch = value;
        }
    }

    private Timer m_SearchTimer = null;

    public void startSearch(final String searchText, final Boolean fromSelection) {

        if (m_SearchTimer != null) {
            m_SearchTimer.cancel();
            m_SearchTimer.purge();
        }
        m_SearchTimer = new Timer();
        m_SearchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (search(searchText, fromSelection) == SEARCH_RESULT_NOTFOUND)
                            searchEditText.setError("Совпадений не найдено");
                        else
                            searchEditText.setError(null);

                    }
                });
                m_SearchTimer.cancel();
                m_SearchTimer.purge();
            }
        }, 1000, 5000);


    }

    public int search(String searchText, Boolean fromSelection) {
        if (TextUtils.isEmpty(searchText)) return SEARCH_RESULT_EMPTYTEXT;
        try {
            progress_search.setVisibility(View.VISIBLE);

            searchText = searchText.toLowerCase();
            Spannable raw = clearPostHighlight();

            int startSearchSelection = 0;
            if (fromSelection)
                startSearchSelection = txtPost.getSelectionStart() + 1;
            String text = raw.toString().toLowerCase();


            int findedStartSelection = TextUtils.indexOf(text, searchText, startSearchSelection);
            if (findedStartSelection == -1 && startSearchSelection != 0)
                findedStartSelection = TextUtils.indexOf(text, searchText);

            if (findedStartSelection == -1)
                return SEARCH_RESULT_NOTFOUND;
            //   txtPost.setSelection(findedStartSelection, findedStartSelection + searchText.length());


            raw.setSpan(new BackgroundColorSpan(0xFF8B008B), findedStartSelection, findedStartSelection
                    + searchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


            txtPost.setText(raw);
            txtPost.setSelection(findedStartSelection);
            txtPost.setCursorVisible(true);
            return SEARCH_RESULT_FOUND;
        } catch (Throwable ex) {
            Log.e(this, ex);
        } finally {
            if (!fromSelection)
                searchEditText.requestFocus();
            progress_search.setVisibility(View.GONE);
        }
        return SEARCH_RESULT_EMPTYTEXT;
    }

    public EditText searchEditText;

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle saveInstance) {
            super.onCreate(saveInstance);
            setHasOptionsMenu(true);
//            if (getResources().getBoolean(R.bool.screen_small))
//                getInterface().getSupportActionBar().hide();
        }

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            com.actionbarsherlock.view.MenuItem item;


            if (!getInterface().isNewPost()) {
                item = menu.add("Причина редактирования").setIcon(R.drawable.ic_menu_edit);
                item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        getInterface().toggleEditReasonDialog();
                        return true;
                    }
                });
                item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }


            item = menu.add("Отправить").setIcon(R.drawable.ic_menu_send);
            //item.setVisible(Client.getInstance().getLogined());
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    return sendMail();
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            item = menu.add("Поиск по тексту").setIcon(R.drawable.ic_menu_search);
            item.setActionView(R.layout.action_collapsible_search);
            getInterface().searchEditText = (EditText) item.getActionView().findViewById(R.id.editText);
            getInterface().searchEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                            && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        String text = getInterface().searchEditText.getText().toString().trim();
                        getInterface().startSearch(text, true);
                        getInterface().searchEditText.requestFocus();
                        return true;
                    }

                    return false;
                }
            });
            getInterface().searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable mEdit) {
                    String text = mEdit.toString().trim();
                    getInterface().startSearch(text, false);
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    getInterface().searchEditText.requestFocus();
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    getInterface().txtPost.setText(getInterface().clearPostHighlight());
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);


            item = menu.add("Скрыть панели").setIcon(R.drawable.ic_media_fullscreen);
            //item.setVisible(Client.getInstance().getLogined());
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    getInterface().hidePanels();
//                    getInterface().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                    getInterface().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    return true;
                }
            });

            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        private boolean sendMail() {
            final String body = ((EditPostPlusActivity) getActivity()).getPostText();
            if (TextUtils.isEmpty(body))
                return true;

            if (((EditPostPlusActivity) getActivity()).getConfirmSend()) {
                new AlertDialogBuilder(getActivity())
                        .setTitle("Уверены?")
                        .setMessage("Подтвердите отправку")
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                ((EditPostPlusActivity) getActivity()).sendPost(body, getInterface().getEditReasonText());

                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .create().show();
            } else {
                ((EditPostPlusActivity) getActivity()).sendPost(body, getInterface().getEditReasonText());
            }

            return true;
        }

        public EditPostPlusActivity getInterface() {
            return (EditPostPlusActivity) getActivity();
        }
    }

}

