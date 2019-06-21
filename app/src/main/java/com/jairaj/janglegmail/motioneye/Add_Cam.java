package com.jairaj.janglegmail.motioneye;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

/*import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;*/

public class Add_Cam extends AppCompatActivity
{
    DataBase myDb;
    private ListView CameraList_ListView; //For displaying device list
    boolean checked = false; //Flag to store state of ListView CameraList_ListView' items: checked or not checked
    Toolbar toolbar; //Tool bar holding ToolBar title and edit, delete buttons and about option
    MenuItem dummy_delete; //for storing layout item of delete button in toolbar
    MenuItem dummy_edit; //for storing layout item of edit button in toolbar
    MenuItem dummy_about; //for storing layout item of about option in toolbar
    MenuItem dummy_help_faq;
    MenuItem dummy_settings;
    //private AdView mAdView; //for storing layout item of Ad view
    //AdRequest adRequest; //for storing ad request to adUnit id in linked layout file
    //AdListener adListener; //Listener for ads
    short isFirstTimeDrive_v = 0; //0 = never appeared before; 1 = First Time; 2 = not First Time
    int target_for_drive_icon = 0; //Resource target for tutorial

    FloatingActionButton fab; //object storing id of FAB in linked layout xml
    // Create a HashMap List from String Array elements
    List<HashMap<String, String>> listItems = new ArrayList<>(); //HashMap inside list
    // Create an ArrayAdapter from List
    SimpleAdapter adapter; //Adapter to link List with ListView
    HashMap<String, String> label_url_port = new HashMap<>(); //HashMap to store Label, Url + Port

    private static final int REQUEST_CODE = 1;

    boolean autoopen_pref = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (!checkWriteExternalPermission())
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

        myDb = new DataBase(this); // init DataBase object

        //Insert Preview status column in Data Base if the previous version of app didn't have it
        //TODO: Find if there is better way to handle SQL Table column addition over previous app version
        myDb.insertNewColumn();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__cam);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        autoopen_pref = prefs.getBoolean(getString(R.string.key_autoopen), true);

        //MobileAds.initialize(this, "ca-app-pub-7081069887552324~4679468464");
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);

        Utils.showRateDialog(this, false);

        adapter = new SimpleAdapter(this, listItems, R.layout.custom_list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.title_label_text, R.id.subtitle_url_port_text});

        //TODO: Check necessity
        if (CameraList_ListView == null)
            CameraList_ListView = findViewById(R.id.device_list);


        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.Camera_List);

        //If this is the first run of the app show tutorial
        if(isFirstTime())
        {
            display_tutorial(1);
        }

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                goto_add_device_detail(Constants.EDIT_MODE_NEW_DEV);
            }
        });

        //Handler to handle data fetching from SQL in BG
        final Handler handler_fetch_data =  new Handler() {
            @Override
            public void handleMessage(Message msg) {
                fetch_data();
            }
        };

        Thread t_fetch_data = new Thread() {
            @Override
            public void run() {
                handler_fetch_data.sendEmptyMessage(0);
            }
        };
        t_fetch_data.run();

        // Add this Runnable
        CameraList_ListView.post(new Runnable() {
            @Override
            public void run()
            {
                final Handler handler =  new  Handler()
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        toggle_visibility_of_drive_button();
                        toggle_visibility_of_prev();

                        if ((CameraList_ListView.getCount() == 1) && autoopen_pref)
                        {
                            String url = listItems.get(0).get("Second Line");
                            int mode = TextUtils.isEmpty(
                                    myDb.getDrive_from_Label(listItems.get(0).get("First Line"))) ?
                                    Constants.MODE_CAMERA : Constants.MODE_DRIVE;
                            goToWebMotionEye(url, mode);
                        }
                    }
                };

                Thread thread_toggle_drive_prev = new Thread() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(0);
                    }
                };
                thread_toggle_drive_prev.run();
            }
        });

        CameraList_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!checked)
                {
                    String selected_url_port = ((TextView) view.findViewById(R.id.subtitle_url_port_text)).getText().toString();
                    goToWebMotionEye(selected_url_port, Constants.MODE_CAMERA);
                }
                else
                {
                    CheckBox checkbox = view.findViewById(R.id.checkBox);
                    checkbox.setChecked(!checkbox.isChecked());

                    int no_of_checked_items = getItemCheckedCount_in_CameraList_ListView();

                    if (no_of_checked_items == 0)
                    {
                        for (int i = 0; i < CameraList_ListView.getChildCount(); i++)
                        {
                            view = CameraList_ListView.getChildAt(i);
                            checkbox = view.findViewById(R.id.checkBox);
                            checkbox.setVisibility(View.GONE);
                        }
                        toggle_ActionBar_elements();
                    }
                }
            }
        });

        CameraList_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(!checked)
                {
                    int i = 0;
                    while (i < CameraList_ListView.getChildCount())
                    {
                        view = CameraList_ListView.getChildAt(i);
                        CheckBox checkbox = view.findViewById(R.id.checkBox);
                        checkbox.setVisibility(View.VISIBLE);
                        if (i == position)
                            checkbox.setChecked(true);
                        i++;
                    }
                    toggle_ActionBar_elements();
                }
                else
                {
                    int i = 0;
                    while (i < CameraList_ListView.getChildCount()) {
                        view = CameraList_ListView.getChildAt(i);
                        CheckBox checkbox = view.findViewById(R.id.checkBox);
                        checkbox.setVisibility(View.GONE);
                        if (i == position)
                            checkbox.setChecked(false);
                        i++;
                    }
                    toggle_ActionBar_elements();
                }
                return true;
            }
        });
    }

    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void onPreviewClick(View v)
    {
        ConstraintLayout vwParentRow = (ConstraintLayout)v.getParent();
        TextView url_port_tv = vwParentRow.findViewById(R.id.subtitle_url_port_text);
        String url_port = url_port_tv.getText().toString();

        goToWebMotionEye(url_port, Constants.MODE_CAMERA);
    }

    private void goToWebMotionEye(String urlPort, @Constants.ServerMode int mode)
    {
        Log.v("In_goToWebMotionEye", "In_goToWebMotionEye");
        Bundle bundle = new Bundle();
        //Add your data from getFactualResults method to bundle
        bundle.putString(Constants.KEY_URL_PORT, urlPort);
        bundle.putInt(Constants.KEY_MODE, mode);
        Intent i = new Intent(Add_Cam.this, web_motion_eye.class);
        i.putExtras(bundle);
        startActivity(i);
    }

    private void fetch_data()
    {
        String url; //For storing url extracted from SQL
        String port; //For storing port extracted from SQL
        String label; //For storing label extracted from SQL
        String url_port; //For storing url:port merged

        label_url_port.clear();
        Cursor res = myDb.getAllData();
        if (res.getCount() != 0)
        {
            //isFirstTimeDevice();
            while (res.moveToNext())
            {
                label = res.getString(1);
                url = res.getString(2);
                port = res.getString(3);

                if (!port.equals(""))
                    url_port = url + ":" + port;
                else
                    url_port = url;

                label_url_port.put(label, url_port);
            }
        }

        final Handler handler =  new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                add_to_list();
            }
        };

        Thread thread_add_to_list = new Thread() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };

        thread_add_to_list.run();

        res.close();
    }

    private void add_to_list()
    {
        CameraList_ListView.setAdapter(null);
        listItems.clear();

        for (Object o : label_url_port.entrySet())
        {
            HashMap<String, String> resultsMap = new HashMap<>();
            Map.Entry pair = (Map.Entry) o;
            resultsMap.put("First Line", pair.getKey().toString());
            resultsMap.put("Second Line", pair.getValue().toString());
            listItems.add(resultsMap);
        }
        CameraList_ListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void goto_add_device_detail(int edit_mode)
    {
        String delete_label = "";
        Bundle bundle = new Bundle();

        if(edit_mode == Constants.EDIT_MODE_EXIST_DEV)
        {
            int i = 0;
            while (i < CameraList_ListView.getChildCount())
            {
                View view = CameraList_ListView.getChildAt(i);
                CheckBox checkbox = view.findViewById(R.id.checkBox);

                if (checkbox.isChecked())
                {
                    delete_label = ((TextView) view.findViewById(R.id.title_label_text)).getText().toString();
                    checkbox.setChecked(false);
                }
                checkbox.setVisibility(View.GONE);
                i++;
            }
            checked = false;

            toggle_ActionBar_elements();
            bundle.putString("LABEL", delete_label);
        }

        bundle.putInt("EDIT", edit_mode);

        Intent intent_for_add_device = new Intent(Add_Cam.this, add_device_detail.class);
        //Add the bundle to the intent
        intent_for_add_device.putExtras(bundle);
        startActivityForResult(intent_for_add_device, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data)
    {
        if(dummy_delete != null)
            dummy_delete.setVisible(false);
        if(dummy_edit != null)
            dummy_edit.setVisible(false);
        if(dummy_about != null)
            dummy_about.setVisible(true);
        if(dummy_help_faq != null)
            dummy_help_faq.setVisible(true);
        if(dummy_settings != null)
            dummy_settings.setVisible(true);

        toolbar.setTitle(R.string.Camera_List);

        fab.show();

        //display_ad();
        checked = false;
        fetch_data();

        CameraList_ListView.post(new Runnable() {
            @Override
            public void run()
            {
                final Handler handler =  new Handler()
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        toggle_visibility_of_drive_button();
                        toggle_visibility_of_prev();
                    }
                };

                Thread thread_toggle_drive_prev = new Thread() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(0);
                    }
                };

                thread_toggle_drive_prev.run();

                if(resultCode != 2)
                {
                    boolean flag_isFirstDevice;

                    flag_isFirstDevice = isFirstTimeDevice();

                    if(flag_isFirstDevice && (isFirstTimeDrive_v == 0))
                        display_tutorial(2);
                    else if(!flag_isFirstDevice && (isFirstTimeDrive_v == 1))
                        display_tutorial(3);
                    else if(flag_isFirstDevice && (isFirstTimeDrive_v == 1))
                        display_tutorial(4);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add__cam, menu);
        dummy_delete = menu.findItem(R.id.delete);
        dummy_edit = menu.findItem(R.id.edit);
        dummy_about = menu.findItem(R.id.action_about);
        dummy_help_faq = menu.findItem(R.id.action_help);
        dummy_settings = menu.findItem(R.id.action_settings);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.delete)
        {
            if(getItemCheckedCount_in_CameraList_ListView() > 0 && checked)
            {
                int i = 0;

                while (i < CameraList_ListView.getChildCount()) {
                    View view = CameraList_ListView.getChildAt(i);
                    CheckBox checkbox = view.findViewById(R.id.checkBox);

                    if (checkbox.isChecked()) {
                        String del_label = ((TextView) view.findViewById(R.id.title_label_text)).getText().toString();
                        delete_data(del_label);
                        checkbox.setChecked(false);
                    }
                    i++;
                }
                fetch_data();
                toggle_ActionBar_elements();
            }
        }


        if(id == R.id.edit)
        {
            if(checked)
            {
                int f = getItemCheckedCount_in_CameraList_ListView();

                if (f > 1)
                {
                    Toast.makeText(getBaseContext(), "Select only one entry to edit", Toast.LENGTH_SHORT).show();
                }
                else
                    goto_add_device_detail(Constants.EDIT_MODE_EXIST_DEV);
            }
        }

        if(id == R.id.action_about)
        {
            Intent intent_about_page = new Intent(Add_Cam.this, About_Page.class);
            intent_about_page.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent_about_page);
        }

        if(id == R.id.action_help)
        {
            Intent intent_help_faq = new Intent(Add_Cam.this, Help_FAQ.class);
            intent_help_faq.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent_help_faq);
        }

        if(id == R.id.action_settings)
        {
            Intent intent_settings = new Intent(Add_Cam.this, SettingsActivity.class);
            startActivity(intent_settings);
        }

        return super.onOptionsItemSelected(item);
    }

    private void delete_data(String del_label)
    {
        Integer deletedRows = myDb.deleteData(del_label);
        if (!(deletedRows > 0))
            Toast.makeText(Add_Cam.this, "Failed to delete", Toast.LENGTH_LONG).show();
    }

    public void onDriveIconClick(View v)
    {
        //get the row the clicked button is in
        ConstraintLayout vwParentRow = (ConstraintLayout)v.getParent();
        TextView LabelView_at_Drive_ic_click = vwParentRow.findViewById(R.id.title_label_text);
        String Label_text_at_drive_ic_click = LabelView_at_Drive_ic_click.getText().toString();
        String drive_link = myDb.getDrive_from_Label(Label_text_at_drive_ic_click);

        goToWebMotionEye(drive_link, Constants.MODE_DRIVE);
    }

    private void display_tutorial(int call_number)
    {
        /* call_number usage
         * 1 = First Time App Opened
         * 2 = First Time Device added
         * 3 = Not First Time for Device addition but First Time for Drive
         * 4 = First Time for device addition as well as drive
         */
        if(call_number == 1)
        {
            new MaterialTapTargetPrompt.Builder(Add_Cam.this)
                    .setTarget(R.id.fab)
                    .setPrimaryText(R.string.tut_title_add_button)
                    .setSecondaryText(R.string.tut_sub_add_button)
                    .setBackgroundColour(Color.argb(255, 30, 90, 136))
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                    {
                        @Override
                        public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state)
                        {
                            /*
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                            {
                                // User has pressed the prompt target
                            }
                            if(state == MaterialTapTargetPrompt.STATE_DISMISSED)
                            {
                                //display_ad();
                            }
                            */
                        }
                    })
                    .show();
        }

        else if(call_number == 2)
        {
            new MaterialTapTargetPrompt.Builder(Add_Cam.this)
                    .setTarget(R.id.dummy_show_case_button)
                    .setFocalColour(Color.argb(0, 0, 0, 0))
                    .setPrimaryText(R.string.tut_title_device_list)
                    .setSecondaryText(R.string.tut_sub_device_list)
                    .setBackgroundColour(Color.argb(255, 30, 90, 136))
                    .setPromptBackground(new RectanglePromptBackground())
                    .setPromptFocal(new RectanglePromptFocal())
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                    {
                        @Override
                        public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state)
                        {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                            {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        display_tutorial(3);
                                    }
                                }, 800); //delay
                                //display_ad();
                            }
                            if (state == MaterialTapTargetPrompt.STATE_DISMISSED)
                            {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        display_tutorial(3);
                                    }

                                }, 1000); //delay
                                //display_ad();
                            }
                        }
                    })
                    .show();
        }

        else if(call_number == 3)
        {
            new MaterialTapTargetPrompt.Builder(Add_Cam.this)
                    .setTarget(target_for_drive_icon)
                    .setPrimaryText(R.string.tut_title_drive_icon)
                    .setSecondaryText(R.string.tut_sub_drive_icon)
                    .setBackgroundColour(Color.argb(255, 30, 90, 136))
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                    {
                        @Override
                        public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state)
                        {
                            /*
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                            {
                                //display_ad();
                            }
                            if (state == MaterialTapTargetPrompt.STATE_DISMISSED)
                            {
                                //display_ad();
                            }
                            */
                        }
                    })
                    .show();
        }

        else if(call_number == 4)
        {
            display_tutorial(2);
        }
    }

    public void onExpandCamClick(final View v) {
        //get the row the clicked button is in
        ConstraintLayout vwParentRow = (ConstraintLayout) v.getParent();

        HandlePreviewView(vwParentRow, false);
    }

    public void toggle_visibility_of_prev()
    {
        View view;
        int i = 0;
        while (i < CameraList_ListView.getChildCount()) {
            view = CameraList_ListView.getChildAt(i);
            HandlePreviewView(view, true);

            i++;
        }
    }

    void HandlePreviewView(final View view, boolean checkAll) {
        TextView Each_Label = view.findViewById(R.id.title_label_text);
        String Each_label_text = Each_Label.getText().toString();

        WebView preview_view = view.findViewById(R.id.preview_webview);

        ImageView expand_button = view.findViewById(R.id.expand_button);

        final ProgressBar progressBar = view.findViewById(R.id.preview_progressBar);

        String visibilityState = "0";

        if (checkAll)
            visibilityState = myDb.getPrevStat_from_Label(Each_label_text);
        else {
            if (preview_view.getVisibility() == View.GONE)
                visibilityState = "1";
        }

        if (visibilityState.equals("1")) {
            String url_link = myDb.getUrl_from_Label(Each_label_text);
            String url_port;
            String port = myDb.getPort_from_Label(Each_label_text);

            if (!port.isEmpty())
                url_port = url_link + ":" + port;
            else
                url_port = url_link;

            expand_button.setImageResource(R.drawable.collapse_button);

            preview_view.setVisibility(View.VISIBLE);

            ((ConstraintLayout) preview_view.getParent()).setPadding(0, 0, 0, Constants.PREVIEW_PADDING);

            preview_view.getSettings().setJavaScriptEnabled(true);
            preview_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            preview_view.setWebViewClient(new WebViewClient());
            preview_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            preview_view.getSettings().setUseWideViewPort(true);
            preview_view.getSettings().setLoadWithOverviewMode(true);

            preview_view.loadUrl(url_port);

            final boolean LiveStream = Utils.checkWhetherStream(url_port);
            preview_view.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (!view.getUrl().equals("about:blank")) {
                        progressBar.setProgress(progress);
                        if (progress == 100 || (progress >= 30 && LiveStream)) {
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            boolean isUpdate = myDb.updatePrevStat(Each_label_text, "1");
            if (!isUpdate)
                Toast.makeText(Add_Cam.this, R.string.error_try_delete, Toast.LENGTH_LONG).show();
        } else {
            boolean isUpdate = myDb.updatePrevStat(Each_label_text, "0");

            if (!isUpdate)
                Toast.makeText(Add_Cam.this, R.string.error_try_delete, Toast.LENGTH_LONG).show();

            ((ConstraintLayout) preview_view.getParent()).setPadding(0, 0, 0, 0);

            expand_button.setImageResource(R.drawable.expand_down);
            preview_view.loadUrl("about:blank");
            preview_view.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        final View finalView = preview_view;
        preview_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    onPreviewClick(finalView);
                }
                return true;
            }
        });
    }

    private void toggle_visibility_of_drive_button()
    {
        View view;
        int i = 0;
        while (i < CameraList_ListView.getChildCount()) {
            view = CameraList_ListView.getChildAt(i);
            TextView Each_Label = view.findViewById(R.id.title_label_text);
            String Each_label_text = Each_Label.getText().toString();
            String drive_link = myDb.getDrive_from_Label(Each_label_text);
            ImageButton drive_button = view.findViewById(R.id.button_drive);

            if (drive_link.equals(""))
                drive_button.setVisibility(View.GONE);
            else {
                target_for_drive_icon = R.id.button_drive;
                drive_button.setVisibility(View.VISIBLE);
                if (isFirstTimeDrive())
                    isFirstTimeDrive_v = 1;
                else
                    isFirstTimeDrive_v = 2;
            }
            i++;
        }
    }

    private int getItemCheckedCount_in_CameraList_ListView()
    {
        View view;
        CheckBox checkbox;
        int f = 0;
        for (int i = 0; i < CameraList_ListView.getChildCount(); i++)
        {
            view = CameraList_ListView.getChildAt(i);
            checkbox = view.findViewById(R.id.checkBox);
            if (checkbox.isChecked())
                f++;
        }
        return f;
    }

    private void toggle_ActionBar_elements()
    {
        dummy_about.setVisible(!dummy_about.isVisible());
        dummy_help_faq.setVisible(!dummy_help_faq.isVisible());
        dummy_settings.setVisible(!dummy_settings.isVisible());
        dummy_delete.setVisible(!dummy_delete.isVisible());
        dummy_edit.setVisible(!dummy_edit.isVisible());

        if(toolbar.getTitle().equals(""))
            toolbar.setTitle(R.string.Camera_List);
        else
            toolbar.setTitle("");

        if(fab.getVisibility() == View.GONE)
            fab.show();
        else
            fab.hide();

        checked = !checked;
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.apply();
        }
        return !ranBefore;
    }

    private boolean isFirstTimeDevice()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("Device_added_before", false);
        if (!ranBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("Device_added_before", true);
            editor.apply();
        }
        return !ranBefore;
    }

    private boolean isFirstTimeDrive()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("Drive_RanBefore", false);
        if (!ranBefore)
        {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("Drive_RanBefore", true);
            editor.apply();
        }
        return !ranBefore;
    }

    /*public void display_ad()
    /*public void display_add()
    {
        //mAdView = findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        adListener = new AdListener();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                mAdView.setVisibility(View.GONE);
            }
        });
    }*/

    @Override
    public void onBackPressed()
    {
        int f = getItemCheckedCount_in_CameraList_ListView();
        if(f != 0)
        {
            View view;
            CheckBox checkbox;
            for (int i = 0; i < CameraList_ListView.getChildCount(); i++)
            {
                view = CameraList_ListView.getChildAt(i);
                checkbox = view.findViewById(R.id.checkBox);
                checkbox.setChecked(false);
                checkbox.setVisibility(View.GONE);
            }
            toggle_ActionBar_elements();
        }

        else
        {
/*            int[] fab_pos=new int[2];
            fab.getLocationOnScreen(fab_pos);

            int pos_fab_x = fab_pos[0];
            int pos_fab_y = fab_pos[1];

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("Fab_x", Integer.parseInt(Integer.toString(pos_fab_x)));
            editor.putInt("Fab_y", Integer.parseInt(Integer.toString(pos_fab_y)));
            editor.apply();
*/
            finish();
        }
    }
}