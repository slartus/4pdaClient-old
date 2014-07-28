package org.softeg.slartus.forpda;

import android.annotation.TargetApi;
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
import android.os.Build;
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
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.emotic.SmilesBbCodePanel;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdaapi.post.EditAttach;
import org.softeg.slartus.forpdaapi.post.EditPost;
import org.softeg.slartus.forpdaapi.post.PostApi;
import org.softeg.slartus.forpdacommon.FileUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    private String m_AttachFilePath;
    private String lastSelectDirPath = Environment.getExternalStorageDirectory().getPath();

    private EditPost m_EditPost;

    final Handler uiHandler = new Handler();
    // подтверждение отправки
    private Boolean m_ConfirmSend = true;
    // флаг добавлять подпись к сообщению
    private Boolean m_Enablesig = true;
    private Boolean m_EnableEmo = true;
    private final int REQUEST_SAVE = 0;
    private final int REQUEST_SAVE_IMAGE = 1;

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

        setContentView(R.layout.edit_post_plus);

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


            String forumId = intent.getExtras().getString("forumId");
            String topicId = intent.getExtras().getString("themeId");
            String postId = intent.getExtras().getString("postId");
            String authKey = intent.getExtras().getString("authKey");
            m_EditPost = new EditPost();
            m_EditPost.setId(postId);
            m_EditPost.setForumId(forumId);
            m_EditPost.setTopicId(topicId);
            m_EditPost.setAuthKey(authKey);


            setDataFromExtras(intent.getExtras());

            startLoadPost(forumId, topicId, postId, authKey);
        } catch (Throwable ex) {
            Log.e(this, ex);
            finish();
        }
        createActionMenu();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (m_EditPost != null)
            outState.putSerializable("EditPost", m_EditPost);
        outState.putString("m_AttachFilePath", m_AttachFilePath);
        outState.putString("lastSelectDirPath", lastSelectDirPath);
        outState.putString("postText", txtPost.getText().toString());
        outState.putString("txtpost_edit_reason", txtpost_edit_reason.getText().toString());
        outState.putBoolean("Enablesig", tglEnableSig.isChecked());
        outState.putBoolean("EnableEmo", tglEnableEmo.isChecked());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        if (savedInstanceState.containsKey("EditPost"))
            m_EditPost = (EditPost) savedInstanceState.getSerializable("EditPost");
        m_AttachFilePath = savedInstanceState.getString("m_AttachFilePath");
        lastSelectDirPath = savedInstanceState.getString("lastSelectDirPath");
        txtPost.setText(savedInstanceState.getString("postText"));
        txtpost_edit_reason.setText(savedInstanceState.getString("txtpost_edit_reason"));
        tglEnableSig.setChecked(savedInstanceState.getBoolean("Enablesig"));
        tglEnableEmo.setChecked(savedInstanceState.getBoolean("EnableEmo"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setDataFromExtras(Bundle extras) {


        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            Uri uri = (Uri) extras.get(Intent.EXTRA_STREAM);
            m_AttachFilePath = getRealPathFromURI(uri);
        }

        if (extras.containsKey(Intent.EXTRA_TEXT))
            txtPost.setText(extras.get(Intent.EXTRA_TEXT).toString());
        if (Build.VERSION.SDK_INT >= 16 && extras.containsKey(Intent.EXTRA_HTML_TEXT))
            txtPost.setText(extras.get(Intent.EXTRA_HTML_TEXT).toString());

        if (isNewPost()) {
            if (extras.containsKey("body"))
                txtPost.setText(extras.get("body").toString());
        }

    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        MenuFragment mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    private Boolean isNewPost() {
        return PostApi.NEW_POST_ID.equals(m_EditPost.getId());
    }

    private Dialog mAttachesListDialog;

    private void showAttachesListDialog() {
        if (m_EditPost.getAttaches().size() == 0) {
            startAddAttachment();
            return;
        }
        AttachesAdapter adapter = new AttachesAdapter(m_EditPost.getAttaches(), this);
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
                if (requestCode == REQUEST_SAVE) {

                    m_AttachFilePath = getRealPathFromURI(data.getData());


                    saveAttachDirPath();

                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this, m_AttachFilePath).execute();
                } else if (requestCode == REQUEST_SAVE_IMAGE) {

                    Uri selectedImage = data.getData();

                    m_AttachFilePath = getRealPathFromURI(selectedImage);


                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this, m_AttachFilePath).execute();
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

    private void startLoadPost(String forumId, String topicId, String postId, String authKey) {
        new LoadTask(this, forumId, topicId, postId, authKey).execute();
    }

    private void sendPost(final String text, String editPostReason) {
        m_Enablesig = tglEnableSig.isChecked();
        m_EnableEmo = tglEnableEmo.isChecked();
        if (isNewPost()) {
            new PostTask(this, text, editPostReason,
                    m_EnableEmo, m_Enablesig)
                    .execute();
        } else {
            new AcceptEditTask(EditPostPlusActivity.this, text, editPostReason, m_EnableEmo, m_Enablesig).execute();
        }
    }



    public void toggleEditReasonDialog() {
        txtpost_edit_reason.setVisibility(txtpost_edit_reason.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public String getPostText() {
        return txtPost.getText().toString();
    }

    public String getEditReasonText() {
        return txtpost_edit_reason.getText().toString();
    }

    public boolean getConfirmSend() {
        return m_ConfirmSend;
    }

    private class UpdateTask extends AsyncTask<String, Integer, Boolean> {
        private final ProgressDialog dialog;
        private ProgressState m_ProgressState;
        private String newAttachFilePath;

        public UpdateTask(Context context) {
            dialog = new AppProgressDialog(context);
        }

        public UpdateTask(Context context, String newAttachFilePath) {
            this(context);
            this.newAttachFilePath = newAttachFilePath;
        }


        private EditAttach editAttach;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_ProgressState = new ProgressState() {
                    @Override
                    public void update(String message, int percents) {
                        publishProgress(percents);
                    }
                };
                editAttach = PostApi.attachFile(Client.getInstance(),
                        m_EditPost.getId(), newAttachFilePath, m_ProgressState);

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

            if (success || (isCancelled() && editAttach != null)) {
                m_EditPost.addAttach(editAttach);
                refreshAttachmentsInfo();
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Boolean success) {
            super.onCancelled(success);
            if (success || (isCancelled() && editAttach != null)) {
                m_EditPost.addAttach(editAttach);
                refreshAttachmentsInfo();
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

        private String attachId;

        public DeleteTask(Context context, String attachId) {

            this.attachId = attachId;

            dialog = new AppProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                PostApi.deleteAttachedFile(Client.getInstance(), m_EditPost.getId(), attachId);
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
                m_EditPost.deleteAttach(attachId);
                refreshAttachmentsInfo();
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
        private String postBody;
        private String postEditReason;
        private Boolean enableEmo;
        private Boolean enableSign;

        public AcceptEditTask(Context context,
                              String postBody, String postEditReason, Boolean enableEmo, Boolean enableSign) {
            this.postBody = postBody;
            this.postEditReason = postEditReason;
            this.enableEmo = enableEmo;
            this.enableSign = enableSign;
            dialog = new AppProgressDialog(context);
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                PostApi.sendPost(Client.getInstance(), m_EditPost.getParams(), postBody,
                        postEditReason, enableSign, enableEmo);
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
                ThemeActivity.s_ThemeId = m_EditPost.getTopicId();
                ThemeActivity.s_Params = "view=findpost&p=" + m_EditPost.getId();
                if (!ThemeActivity.class.toString().equals(getIntent().getExtras().get(BaseActivity.SENDER_ACTIVITY))) {
                    ExtTopic.showActivity(EditPostPlusActivity.this, m_EditPost.getTopicId(), ThemeActivity.s_Params);
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

    private void setEditPost(EditPost editPost) {
        m_EditPost = editPost;
        txtPost.setText(m_EditPost.getBody());
        txtpost_edit_reason.setText(m_EditPost.getPostEditReason());
        refreshAttachmentsInfo();
    }

    private void refreshAttachmentsInfo() {
        btnAttachments.setText(m_EditPost.getAttaches().size() + "");
    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;

        private String forumId;
        private String topicId;
        private String postId;
        private String authKey;

        public LoadTask(Context context, String forumId, String topicId, String postId, String authKey) {
            this.forumId = forumId;
            this.topicId = topicId;
            this.postId = postId;
            this.authKey = authKey;
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

        private EditPost editPost;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                editPost = PostApi.editPost(Client.getInstance(), forumId, topicId, postId, authKey);

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
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                setEditPost(editPost);
                if (!TextUtils.isEmpty(m_AttachFilePath)) {
                    saveAttachDirPath();

                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this, m_AttachFilePath).execute("");
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
        private String mPostResult = null;// при удачной отправке страница топика
        private String mError = null;
        private String postBody;
        private String postEditReason;
        private Boolean enableEmo;
        private Boolean enableSign;

        public PostTask(Context context,
                        String postBody, String postEditReason, Boolean enableEmo, Boolean enableSign) {
            this.postBody = postBody;
            this.postEditReason = postEditReason;
            this.enableEmo = enableEmo;
            this.enableSign = enableSign;
            dialog = new AppProgressDialog(context);
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = PostApi.sendPost(Client.getInstance(), m_EditPost.getParams(), postBody,
                        postEditReason, enableSign, enableEmo);

                mError = PostApi.checkPostErrors(mPostResult);
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
                    ThemeActivity.s_Params = "view=findpost&p=" + m_EditPost.getId();


                if (!ThemeActivity.class.toString().equals(getIntent().getExtras().get(BaseActivity.SENDER_ACTIVITY))) {
                    ExtTopic.showActivity(EditPostPlusActivity.this, m_EditPost.getTopicId(), ThemeActivity.s_Params);
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
        private final List<EditAttach> content;

        public AttachesAdapter(List<EditAttach> content, Activity activity) {
            super();
            this.content = content;
            this.activity = activity;
        }

        public int getCount() {
            return content.size();
        }

        public EditAttach getItem(int i) {
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


                assert convertView != null;
                holder.btnDelete = (ImageButton) convertView
                        .findViewById(R.id.btnDelete);
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();

                        EditAttach attach = (EditAttach) view.getTag();

                        new DeleteTask(EditPostPlusActivity.this,
                                attach.getId())
                                .execute();
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
                        EditAttach attach = (EditAttach) view.getTag();
                        if (txtPost.getText() != null)
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
                        EditAttach attach = (EditAttach) view.getTag();
                        if (txtPost.getText() != null)
                            txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getName() + "]");
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            EditAttach attach = this.getItem(position);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            m_BottomPanel.setVisibility(View.VISIBLE);
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

