package org.softeg.slartus.forpda.qms_2_0;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AppProgressDialog;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.Qms_2_0;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 05.02.13
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public class QmsNewThreadActivity extends BaseFragmentActivity {
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NICK_KEY = "user_nick";
    private EditText username, title, message;
    private String m_Id;
    private String m_Nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.qms_new_thread);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        username = (EditText) findViewById(R.id.username);
        title = (EditText) findViewById(R.id.title);
        message = (EditText) findViewById(R.id.message);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        assert extras != null;
        m_Id = extras.getString(USER_ID_KEY);
        m_Nick = extras.getString(USER_NICK_KEY);
        if (!TextUtils.isEmpty(m_Nick)) {
            username.setText(m_Nick);
            username.setVisibility(View.GONE);
            setTitle(m_Nick + ":QMS:Новая тема");
        } else {
            setTitle("QMS:Новая тема");
        }
    }

    public static void showUserNewThread(Context activity, String userId, String userNick) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsNewThreadActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        intent.putExtra(USER_NICK_KEY, userNick);
        activity.startActivity(intent);
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

    private void send() {
        m_Nick = username.getText().toString();
        String theme = title.getText().toString();
        String post = message.getText().toString();

        if (TextUtils.isEmpty(m_Nick)) {
            Toast.makeText(this, "Укажите получателя", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(post)) {
            Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }
        new SendTask(this,m_Id,m_Nick, theme, post).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    private class SendTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;
        public String m_ChatBody;
        private String userId;
        private String userNick;
        private String title;
        private String body;


        public SendTask(Context context, String userId, String userNick, String title, String body) {
            this.userId = userId;
            this.userNick = userNick;
            this.title = title;
            this.body = body;

            dialog = new AppProgressDialog(context);
        }

        private Map<String, String> outParams;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                outParams = new HashMap<String, String>();
                m_ChatBody = Qms_2_0.createThread(Client.getInstance(),userId, userNick, title,body,
                        outParams, QmsChatActivity.getEncoding());

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
                finish();
                QmsChatActivity.openChat(QmsNewThreadActivity.this, outParams.get("mid"), outParams.get("user"),
                        outParams.get("t"), outParams.get("title"), m_ChatBody);
//                edMessage.getText().clear();
//
//                wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", m_ChatBody, "text/html", "UTF-8", null);
            } else {
                if (ex != null)
                    Log.e(QmsNewThreadActivity.this, ex);
                else
                    Toast.makeText(QmsNewThreadActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static final class MenuFragment extends SherlockFragment {

        private QmsNewThreadActivity getInterface() {
            if (getActivity() == null) return null;
            return (QmsNewThreadActivity) getActivity();
        }

        public MenuFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            MenuItem item = menu.add("Отправить").setIcon(R.drawable.ic_menu_edit);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().send();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }
}
