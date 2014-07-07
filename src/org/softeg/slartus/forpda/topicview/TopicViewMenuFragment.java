package org.softeg.slartus.forpda.topicview;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.QuickStartActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.ForumTreeTab;
import org.softeg.slartus.forpda.Tabs.NotesTab;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.classes.AlertDialogBuilder;
import org.softeg.slartus.forpda.classes.ExtTopic;
import org.softeg.slartus.forpda.classes.ProfileMenuFragment;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.common.HelpTask;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.prefs.PreferencesActivity;
import org.softeg.slartus.forpda.search.SearchActivity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 01.11.12
 * Time: 7:18
 * To change this template use File | Settings | File Templates.
 */
public final class TopicViewMenuFragment extends ProfileMenuFragment {

    private ThemeActivity getInterface() {
        if (getActivity() == null) return null;
        return (ThemeActivity) getActivity();
    }

    public TopicViewMenuFragment() {
        super();

    }


    private Boolean m_FirstTime = true;

    @Override
    public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        if (!m_FirstTime)
            getInterface().onPrepareOptionsMenu();
        m_FirstTime = false;
        if (mTopicOptionsMenu != null)
            configureOptionsMenu(getActivity(), getInterface().getHandler(), mTopicOptionsMenu, getInterface(),
                    true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());
        else if (getInterface() != null && getInterface().getTopic() != null)
            mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface(),
                    true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());
    }

    private com.actionbarsherlock.view.SubMenu mTopicOptionsMenu;

    private static com.actionbarsherlock.view.SubMenu addOptionsMenu(final Context context, final Handler mHandler,
                                                                     com.actionbarsherlock.view.Menu menu, final ThemeActivity themeActivity,
                                                                     Boolean addFavorites, final String shareItUrl) {
        com.actionbarsherlock.view.SubMenu optionsMenu = menu.addSubMenu("Опции темы");

        optionsMenu.getItem().setIcon(R.drawable.ic_menu_more);
        configureOptionsMenu(context, mHandler, optionsMenu, themeActivity, addFavorites, shareItUrl);
        return optionsMenu;
    }

    private static Boolean checkTopicMenuItemEnabled(ExtTopic topic) {
        return Client.getInstance().getLogined() && topic != null;
    }

    private static void configureOptionsMenu(final Context context, final Handler mHandler, com.actionbarsherlock.view.SubMenu optionsMenu, final ThemeActivity themeActivity,
                                             Boolean addFavorites, final String shareItUrl) {

        optionsMenu.clear();


        if (addFavorites) {
            optionsMenu.add(R.string.AddingToFavorites).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    try {
                        themeActivity.getTopic().startSubscribe(context, mHandler);
                    } catch (Exception ex) {
                        Log.e(context, ex);
                    }

                    return true;
                }
            });

            optionsMenu.add(R.string.DeleteFromFavorites).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    try {
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
                                                 return themeActivity.getTopic().removeFromFavorites();  //To change body of implemented methods use File | Settings | File Templates.
                                             }
                                         }
                        );

                    } catch (Exception ex) {
                        Log.e(context, ex);
                    }
                    return true;
                }
            });

            optionsMenu.add(R.string.OpenTopicForum).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    try {
                        Intent intent = new Intent(context, QuickStartActivity.class);

                        intent.putExtra("template", Tabs.TAB_FORUMS);
                        intent.putExtra(ForumTreeTab.KEY_FORUM_ID, themeActivity.getTopic().getForumId());

                        intent.putExtra(ForumTreeTab.KEY_TOPIC_ID, themeActivity.getTopic().getId());
                        context.startActivity(intent);
                    } catch (Exception ex) {
                        Log.e(context, ex);
                    }
                    return true;
                }
            });
        }


        optionsMenu.add(R.string.NotesByTopic).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {

                Intent intent = new Intent(context, QuickStartActivity.class);
                intent.putExtra("template", NotesTab.TEMPLATE);
                intent.putExtra(NotesTab.TOPIC_ID_KEY, themeActivity.getTopic().getId());
                context.startActivity(intent);
                return true;
            }
        });

        optionsMenu.add(R.string.Share).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                try {
                    String url = TextUtils.isEmpty(shareItUrl) ? ("http://4pda.ru/forum/index.php?showtopic=" + themeActivity.getTopic().getId()) : shareItUrl;
                    ExtUrl.shareIt(context, url);
                } catch (Exception ex) {
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, final com.actionbarsherlock.view.MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {

            com.actionbarsherlock.view.MenuItem item = menu.add(R.string.Attaches)
                    .setIcon(R.drawable.ic_menu_download)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().showTopicAttaches();

                            return true;
                        }
                    });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            SubMenu subMenu = menu.addSubMenu(R.string.FindOnPage).setIcon(R.drawable.ic_menu_search);
            subMenu.getItem().setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            subMenu.add(R.string.FindOnPage)
                    .setIcon(R.drawable.ic_action_forum_search)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().onSearchRequested();

                            return true;
                        }
                    });
            subMenu.add(R.string.FindInTopic)
                    .setIcon(R.drawable.ic_action_post_search)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            try {
                                View searchView = LayoutInflater.from(getActivity()).inflate(R.layout.search_input, null);
                                final EditText editText = (EditText) searchView.findViewById(R.id.query_edit);
                                AlertDialog alertDialog = new AlertDialogBuilder(getActivity())
                                        .setTitle(R.string.FindInTopic)
                                        .setView(searchView)
                                        .setPositiveButton(R.string.Search, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                SearchActivity.startActivity(getActivity(),
                                                        getInterface().getTopic().getForumId(),
                                                        getInterface().getTopic().getForumTitle(),
                                                        getInterface().getTopic().getId(),
                                                        editText.getText().toString());
                                            }
                                        })
                                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .create();
                                if (Build.VERSION.SDK_INT >= 8)
                                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        public void onShow(DialogInterface dialogInterface) {
                                            editText.requestFocus();
                                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                                            imm.showSoftInput(editText, 0);
                                        }
                                    });
                                alertDialog.show();


                            } catch (Exception ex) {
                                Log.e(getInterface(), ex);
                            }


                            return true;
                        }
                    });

            MenuItem m_EditPost = menu.add(R.string.Write)
                    .setIcon(R.drawable.ic_menu_edit)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            getInterface().toggleMessagePanelVisibility();

                            return true;
                        }
                    });
            m_EditPost.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


            menu.add(R.string.Refresh)
                    .setIcon(R.drawable.ic_menu_refresh)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            getInterface().reloadTopic();
                            return true;
                        }
                    });
            menu.add(R.string.Browser)
                    .setIcon(R.drawable.ic_menu_goto)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                            try {
                                Intent marketIntent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://" + Client.SITE + "/forum/index.php?" + getInterface().getLastUrl()));
                                startActivity(Intent.createChooser(marketIntent, "Выберите"));


                            } catch (ActivityNotFoundException e) {
                                Log.e(getActivity(), e);
                            }


                            return true;
                        }
                    });

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            if (getInterface() != null)
                mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface(),
                        true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());

            if (Build.VERSION.SDK_INT < 11) {
                addCloseMenuItem(menu);
            }
            com.actionbarsherlock.view.SubMenu optionsMenu = menu.addSubMenu("Настройки отображения");
            optionsMenu.getItem().setIcon(R.drawable.ic_menu_preferences);
            optionsMenu.getItem().setTitle("Настройки отображения");
            optionsMenu.add("Масштабировать").setIcon(R.drawable.ic_menu_scale)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            try {
                                prefs.getBoolean("theme.ZoomUsing", true);
                                menuItem.setChecked(!menuItem.isChecked());
                                getInterface().setAndSaveUseZoom(menuItem.isChecked());

                            } catch (Exception ex) {
                                Log.e(getActivity(), ex);
                            }


                            return true;
                        }
                    }).setCheckable(true).setChecked(prefs.getBoolean("theme.ZoomUsing", true));


            optionsMenu.add("Запомнить масштаб")
                    .setIcon(R.drawable.ic_menu_save).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    try {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("theme.ZoomLevel", Integer.toString((int) (getInterface().getWebView().getScale() * 100)));
                        editor.commit();
                        getInterface().getWebView().setInitialScale((int) (getInterface().getWebView().getScale() * 100));
                        Toast.makeText(getActivity(), "Масштаб запомнен", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Log.e(getActivity(), ex);
                    }


                    return true;
                }
            });


            optionsMenu.add("Загружать изображения (для сессии)")
                    .setIcon(R.drawable.ic_menu_images).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    Boolean loadImagesAutomatically1 = getInterface().getLoadsImagesAutomatically();
                    getInterface().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                    menuItem.setChecked(!loadImagesAutomatically1);
                    return true;
                }
            }).setCheckable(true).setChecked(getInterface().getLoadsImagesAutomatically());


            optionsMenu.add("Стиль").setIcon(R.drawable.ic_menu_styles).setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    showStylesDialog(prefs);
                    return true;
                }
            });


            menu.add("Быстрый доступ..").setIcon(R.drawable.ic_menu_quickrun).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    new AlertDialogBuilder(getActivity())
                            .setTitle("Быстрый доступ")
                            .setItems(Tabs.getDefaultTemplateNames(), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    QuickStartActivity.showTab(getActivity(), Tabs.templates[i]);
                                }
                            })
                            .create().show();

                    return true;
                }
            });


            if (Build.VERSION.SDK_INT >= 11) {
                addCloseMenuItem(menu);
            }
        } catch (Exception ex) {
            Log.e(getActivity(), ex);
        }


    }

    private void addCloseMenuItem(Menu menu) {
        MenuItem item;
        item = menu.add("Закрыть")
                .setIcon(R.drawable.ic_menu_close_clear_cancel)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        getInterface().getPostBody();
                        if (!TextUtils.isEmpty(getInterface().getPostBody())) {
                            new AlertDialogBuilder(getActivity())
                                    .setTitle("Подтвердите действие")
                                    .setMessage("Имеется введенный текст сообщения! Закрыть тему?")
                                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            getInterface().clear();
                                            getInterface().finish();
                                        }
                                    })
                                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create()
                                    .show();
                        } else {
                            getInterface().clear(true);
                            getInterface().finish();
                        }

                        return true;
                    }
                });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    private void showStylesDialog(final SharedPreferences prefs) {
        try {
            final String currentValue = MyApp.getInstance().getCurrentTheme();

            ArrayList<CharSequence> newStyleNames = new ArrayList<CharSequence>();
            final ArrayList<CharSequence> newstyleValues = new ArrayList<CharSequence>();

            PreferencesActivity.getStylesList(getInterface(), newStyleNames, newstyleValues);
            final int selected = newstyleValues.indexOf(currentValue);


            LayoutInflater inflater = (LayoutInflater) getInterface()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_select_style, null);
            final ListView listView = (ListView) view.findViewById(R.id.listView);

            listView.setAdapter(new ArrayAdapter<CharSequence>(getInterface(),
                    android.R.layout.simple_list_item_single_choice, newStyleNames));
            listView.setItemChecked(selected, true);


            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            checkBox.setChecked(prefs.getBoolean("theme.BrowserStyle", false));

            AlertDialog alertDialog = new AlertDialogBuilder(getActivity())
                    .setTitle("Стиль")
                    .setCancelable(true)
                    .setView(view)
                    .setPositiveButton("Применить и перезагрузить страницу", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int selected = listView.getCheckedItemPosition();
                            if (selected == -1) {
                                Toast.makeText(getActivity(), "Выберите стиль", Toast.LENGTH_LONG).show();
                                return;
                            }
                            dialogInterface.dismiss();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("appstyle", newstyleValues.get(selected).toString());
                            editor.putBoolean("theme.BrowserStyle", checkBox.isChecked());
                            editor.commit();
                            getInterface().rememberScrollX();
//                            m_ScrollY = webView.getScrollY();
//                            m_ScrollX = webView.getScrollX();
                            getInterface().showTheme(getInterface().getLastUrl());
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();

            alertDialog.show();
        } catch (Exception ex) {
            Log.e(getInterface(), ex);
        }
    }
}
