package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.profile.ProfileActivity;
import org.softeg.slartus.forpda.qms_2_0.QmsContactThemesActivity;
import org.softeg.slartus.forpda.qms_2_0.QmsNewThreadActivity;
import org.softeg.slartus.forpda.search.SearchActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: slinkin
 * Date: 27.09.11
 * Time: 10:44
 */
public class ForumUser {
    private String m_Nick;
    private String m_Group;
    private String m_Id;
    private String m_Reputation;

    public interface InsertNickInterface {
        void insert(String text);
    }

    public static void showUserMenu(final Context context, View webView, final String userId,
                                    String userNick) {
        showUserMenu(context, webView, userId, userNick, null);
    }

    public static void showUserMenu(final Context context, View webView, final String userId,
                                    String userNick, final InsertNickInterface insertNickInterface) {
        try {
            userNick = Html.fromHtml(userNick.replace("<", "&lt;")).toString();


            // не забыть менять в ForumUser
            net.londatiga.android3d.QuickAction mQuickAction = new net.londatiga.android3d.QuickAction(context);
            int id = 0;
            Resources resourses = context.getResources();
            int insertNickPosition = id++;
            int sendQmsPosition = id++;
            if (Client.getInstance().getLogined()) {
                if (insertNickInterface != null)
                    mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(insertNickPosition,
                            context.getString(R.string.InsertNick), resourses.getDrawable(R.drawable.ic_action_paste)));
                mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(sendQmsPosition,
                        context.getString(R.string.MessagesQms), resourses.getDrawable(R.drawable.ic_action_qms)));
            }
            int showProfilePosition = id++;
            mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(showProfilePosition,
                    context.getString(R.string.Profile), resourses.getDrawable(R.drawable.ic_action_user_online)));
            int showUserTopicsPosition = id++;
            mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(showUserTopicsPosition,
                    context.getString(R.string.FindUserTopics), resourses.getDrawable(R.drawable.ic_action_search)));
            int showUserPostsPosition = id++;
            mQuickAction.addActionItem(new net.londatiga.android3d.ActionItem(showUserPostsPosition,
                    context.getString(R.string.FindUserPosts), resourses.getDrawable(R.drawable.ic_action_search)));

            if (mQuickAction.getItemsCount() == 0) return;

            final int finalInsertNickPosition = insertNickPosition;

            final int finalSendQmsPosition = sendQmsPosition;
            final int finalShowProfilePosition = showProfilePosition;
            final int finalShowUserTopicsPosition = showUserTopicsPosition;
            final int finalShowUserPostsPosition = showUserPostsPosition;
            final String finalUserNick = userNick;
            mQuickAction.setOnActionItemClickListener(new net.londatiga.android3d.QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(net.londatiga.android3d.QuickAction source, int pos, int actionId) {
                    try {
                        if (actionId == finalInsertNickPosition) {
                            insertNickInterface.insert("[b]" + finalUserNick + ",[/b] ");
                        } else if (actionId == finalSendQmsPosition) {
                            new AlertDialogBuilder(context)
                                    .setTitle(context.getString(R.string.SelectAnAction))
                                    .setMessage(context.getString(R.string.OpenWith) + " " + finalUserNick + "..")
                                    .setCancelable(true)
                                    .setPositiveButton(context.getString(R.string.NewDialog), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            QmsNewThreadActivity.showUserNewThread(context, userId, finalUserNick);
                                        }
                                    })
                                    .setNeutralButton(context.getString(R.string.AllDialogs), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            QmsContactThemesActivity.showThemes(context, userId, finalUserNick);
                                        }
                                    })
                                    .create()
                                    .show();

                        } else if (actionId == finalShowProfilePosition) {
                            ProfileActivity.startActivity(context, userId, finalUserNick);
                        } else if (actionId == finalShowUserTopicsPosition) {
                            SearchActivity.findUserTopicsActivity(context, finalUserNick);
                        } else if (actionId == finalShowUserPostsPosition) {
                            SearchActivity.findUserPostsActivity(context, finalUserNick);
                        }
                    } catch (Exception ex) {
                        Log.e(context, ex);
                    }
                }
            });

            if (webView.getClass() == AdvWebView.class)
                mQuickAction.show(webView, ((AdvWebView) webView).getLastMotionEvent());
            else
                mQuickAction.show(webView);
        } catch (Throwable ex) {
            Log.e(context, ex);
        }
    }


    public static void startChangeRep(final Context context, final android.os.Handler handler, final String userId,
                                      String userNick, final String postId, final String type, String title) {


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation, null);

        TextView username_view = (TextView) layout.findViewById(R.id.username_view);
        final EditText message_edit = (EditText) layout.findViewById(R.id.message_edit);
        username_view.setText(userNick);
        new AlertDialogBuilder(context)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton(context.getString(R.string.Change), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Toast.makeText(context, context.getString(R.string.ChangeReputationRequest), Toast.LENGTH_SHORT).show();

                        new Thread(new Runnable() {
                            public void run() {
                                Exception ex = null;
                                final Map<String, String> outParams = new HashMap<String, String>();
                                Boolean res = false;
                                try {
                                    res = Client.getInstance().changeReputation(postId, userId, type, message_edit.getText().toString()
                                            , outParams);
                                } catch (IOException e) {
                                    ex = e;
                                }

                                final Exception finalEx = ex;
                                final Boolean finalRes = res;
                                handler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            if (finalEx != null) {
                                                Toast.makeText(context, context.getString(R.string.ChangeReputationError), Toast.LENGTH_SHORT).show();
                                                Log.e(context, finalEx);
                                            } else if (!finalRes) {
                                                new AlertDialogBuilder(context)
                                                        .setTitle(context.getString(R.string.ChangeReputationError))
                                                        .setMessage(outParams.get("Result"))
                                                        .setCancelable(true)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        })
                                                        .create()
                                                        .show();
                                            } else {
                                                Toast.makeText(context, outParams.get("Result"), Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ex) {
                                            Log.e(context, ex);
                                        }

                                    }
                                });
                            }
                        }).start();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }
}
