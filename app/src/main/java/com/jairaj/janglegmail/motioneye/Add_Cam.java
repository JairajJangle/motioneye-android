//Resolve Call Numbers
package com.jairaj.janglegmail.motioneye;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/*import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class Add_Cam extends AppCompatActivity
{
    DataBase myDb;
    private ListView d_list; //For displaying device list
    boolean checked = false; //Flag to store state of ListView d_list' items: checked or not checked
    Toolbar toolbar; //Tool bar holding ToolBar title and edit, delete buttons and about option
    MenuItem dummy_delete; //for storing layout item of delete button in toolbar
    MenuItem dummy_edit; //for storing layout item of edit button in toolbar
    MenuItem dummy_about; //for storing layout item of about option in toolbar
    //private AdView mAdView; //for storing layout item of adview
    //AdRequest adRequest; //for storing ad request to adUnit id in linked layout file
    //AdListener adListener; //Listener for ads
    short isFirstTimeDrive_v = 0; //0 = never appeared before; 1 = First Time; 2 = not First Time
    int target_for_drive_icon = 0;
    volatile boolean display_tut_recursion = false;

    FloatingActionButton fab; //object storing id of FAB in linked layout xml
    // Create a HashMap List from String Array elements
    List<HashMap<String, String>> listItems = new ArrayList<>(); //HashMap inside list
    // Create an ArrayAdapter from List
    SimpleAdapter adapter; //Adapter to link List with ListView
    HashMap<String, String> label_url_port = new HashMap<>(); //HashMap to store Label, Url + Port

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

        myDb = new DataBase(this);

        myDb.insertNewColumn();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__cam);

        //MobileAds.initialize(this, "ca-app-pub-7081069887552324~4679468464");
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        adapter = new SimpleAdapter(this, listItems, R.layout.custom_list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.title_label_text, R.id.subtitle_url_port_text});

        if (d_list == null)
            d_list = findViewById(R.id.device_list);


        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.Camera_List);

        if(isFirstTime())
        {
            display_tutorial(1);
        }

        else
        {
 /*           SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            float fab_x = preferences.getInt("Fab_x", 0);
            float fab_y = preferences.getInt("Fab_y", 0);

            Toast.makeText(getBaseContext(), Float.toString(fab_y), Toast.LENGTH_SHORT).show();

            //if(fab_x != 0 && fab_y != 0)
            {
                fab.setX(fab_x);
                fab.setY(fab_y);
                fab.show();
            }
*/
            //display_ad();
        }

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                goto_add_device_detail("0");
            }
        });

        fetch_data();
        // Add this Runnable
        d_list.post(new Runnable() {
            @Override
            public void run()
            {
                final Handler handler =  new Handler()
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        show_hide_drive_button();
                        show_hide_prev();
                    }
                };

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(0);
                    }
                };

                t.run();
            }
        });

        d_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!checked)
                {
                    String selected_url_port = ((TextView) view.findViewById(R.id.subtitle_url_port_text)).getText().toString();
                    //Toast.makeText(getBaseContext(), selected_url_port, Toast.LENGTH_SHORT).show();

                    //Create the bundle
                    Bundle bundle = new Bundle();
                    //Add your data from getFactualResults method to bundle
                    bundle.putString("URL_PORT", selected_url_port);
                    bundle.putString("MODE", "CAMERA");
                    Intent i = new Intent(Add_Cam.this, web_motion_eye.class);
                    //Add the bundle to the intent
                    i.putExtras(bundle);
                    startActivity(i);
                }
                else
                {
                    CheckBox cb = view.findViewById(R.id.checkBox);
                    cb.setChecked(!cb.isChecked());

                    int f = getItemCheckedCount_in_d_list();

                    if (f == 0)
                    {
                        for (int i = 0; i < d_list.getChildCount(); i++)
                        {
                            view = d_list.getChildAt(i);
                            cb = view.findViewById(R.id.checkBox);
                            cb.setVisibility(View.GONE);
                        }
                        toggle_ActionBar_elements();
                    }
                }
            }
        });

        d_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(!checked)
                {
                    int i = 0;
                    while (i < d_list.getChildCount()) {
                        view = d_list.getChildAt(i);
                        CheckBox cb = view.findViewById(R.id.checkBox);
                        cb.setVisibility(View.VISIBLE);
                        if (i == position)
                            cb.setChecked(true);
                        i++;
                    }
                    toggle_ActionBar_elements();
                }
                else
                {
                    int i = 0;
                    while (i < d_list.getChildCount()) {
                        view = d_list.getChildAt(i);
                        CheckBox cb = view.findViewById(R.id.checkBox);
                        cb.setVisibility(View.GONE);
                        if (i == position)
                            cb.setChecked(false);
                        i++;
                    }
                    toggle_ActionBar_elements();
                }
                return true;
            }
        });
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

        Thread t = new Thread() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };

        t.run();

        res.close();
    }

    private void add_to_list()
    {
        d_list.setAdapter(null);
        listItems.clear();

        for (Object o : label_url_port.entrySet())
        {
            HashMap<String, String> resultsMap = new HashMap<>();
            Map.Entry pair = (Map.Entry) o;
            resultsMap.put("First Line", pair.getKey().toString());
            resultsMap.put("Second Line", pair.getValue().toString());
            listItems.add(resultsMap);
        }
        d_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void goto_add_device_detail(String edit_mode)
    {
        String delete_label = "";
        Bundle bundle = new Bundle();

        if(edit_mode.equals("1"))
        {
            int i = 0;
            while (i < d_list.getChildCount())
            {
                View view = d_list.getChildAt(i);
                CheckBox cb = view.findViewById(R.id.checkBox);

                if (cb.isChecked())
                {
                    delete_label = ((TextView) view.findViewById(R.id.title_label_text)).getText().toString();
                    cb.setChecked(false);
                }
                cb.setVisibility(View.GONE);
                i++;
            }
            checked = false;

            toggle_ActionBar_elements();
            bundle.putString("LABEL", delete_label);
        }

        bundle.putString("EDIT", edit_mode);

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

        toolbar.setTitle(R.string.Camera_List);

        fab.show();

        //display_ad();
        checked = false;
        fetch_data();

        d_list.post(new Runnable() {
            @Override
            public void run()
            {
                final Handler handler =  new Handler()
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        show_hide_drive_button();
                        show_hide_prev();
                    }
                };

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(0);
                    }
                };

                t.run();

                if(resultCode != 2)
                {
                    boolean shit_b;
                    String shit_s = "";
                    int shit_i;

                    shit_b = isFirstTimeDevice();
                    shit_i = isFirstTimeDrive_v;

                    shit_s = "Boolean is: " + Boolean.toString(shit_b) + " Integer is: " + Integer.toString(shit_i);
                    //Toast.makeText(getBaseContext(), shit_s, Toast.LENGTH_SHORT).show();

                    if(shit_b && (isFirstTimeDrive_v == 0))
                        display_tutorial(2);
                    else if(!shit_b && (isFirstTimeDrive_v == 1))
                        display_tutorial(3);
                    else if(shit_b && (isFirstTimeDrive_v == 1))
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
            if(getItemCheckedCount_in_d_list() > 0 && checked)
            {
                int i = 0;

                while (i < d_list.getChildCount()) {
                    View view = d_list.getChildAt(i);
                    CheckBox cb = view.findViewById(R.id.checkBox);

                    if (cb.isChecked()) {
                        String del_label = ((TextView) view.findViewById(R.id.title_label_text)).getText().toString();
                        delete_data(del_label);
                        cb.setChecked(false);
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
                int f = getItemCheckedCount_in_d_list();

                if (f > 1)
                {
                    Toast.makeText(getBaseContext(), "Select only one entry to edit", Toast.LENGTH_SHORT).show();
                }
                else
                    goto_add_device_detail("1");
            }
        }

        if(id == R.id.action_about)
        {
            Intent intent_about_page = new Intent(Add_Cam.this, About_Page.class);
            startActivity(intent_about_page);
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

        //Create the bundle
        Bundle bundle = new Bundle();
        bundle.putString("URL_PORT", drive_link);
        bundle.putString("MODE", "DRIVE");
        Intent i = new Intent(Add_Cam.this, web_motion_eye.class);
        //Add the bundle to the intent
        i.putExtras(bundle);
        startActivity(i);
        //Button btnChild = (Button)vwParentRow.getChildAt(1);
    }

    public void onExpandCamClick(View v)
    {
        //get the row the clicked button is in
        ConstraintLayout vwParentRow = (ConstraintLayout)v.getParent();
        WebView preview_view = vwParentRow.findViewById(R.id.preview_webview);

        TextView LabelView_at_Expand_ic_click = vwParentRow.findViewById(R.id.title_label_text);
        String Label_text_at_expand_ic_click = LabelView_at_Expand_ic_click.getText().toString();

        //TODO Test this onTouch code
        preview_view.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        ImageView expand_button = vwParentRow.findViewById(R.id.expand_button);

        if(preview_view.getVisibility() == View.GONE)
        {
            String url_link = myDb.getUrl_from_Label(Label_text_at_expand_ic_click);

            expand_button.setImageResource(R.drawable.collapse_button);

            preview_view.setVisibility(View.VISIBLE);

            preview_view.getSettings().setJavaScriptEnabled(true);
            preview_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            preview_view.setWebViewClient(new WebViewClient());
            preview_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            preview_view.loadUrl(url_link);
            preview_view.setInitialScale(100);

            boolean isUpdate = myDb.updatePrevStat(Label_text_at_expand_ic_click, "1");
            if(!isUpdate)
                Toast.makeText(Add_Cam.this, R.string.error_try_delete,Toast.LENGTH_LONG).show();
        }

        else
        {
            boolean isUpdate = myDb.updatePrevStat(Label_text_at_expand_ic_click, "0");
            if(!isUpdate)
                Toast.makeText(Add_Cam.this, R.string.error_try_delete,Toast.LENGTH_LONG).show();

            expand_button.setImageResource(R.drawable.expand_down);
            preview_view.loadUrl("about:blank");
            preview_view.setVisibility(View.GONE);
        }
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
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                        {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                            {
                                //display_ad();
                                // User has pressed the prompt target
                            }
                            if(state == MaterialTapTargetPrompt.STATE_DISMISSED)
                            {
                                //display_ad();
                            }
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
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
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
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                        {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED)
                            {
                                //display_ad();
                            }
                            if (state == MaterialTapTargetPrompt.STATE_DISMISSED)
                            {
                                //display_ad();
                            }
                        }
                    })
                    .show();
        }

        else if(call_number == 4)
        {
            display_tutorial(2);
        }
    }

    public void show_hide_prev()
    {
        View view;
        int i = 0;
        while (i < d_list.getChildCount())
        {
            view = d_list.getChildAt(i);
            TextView Each_Label = view.findViewById(R.id.title_label_text);
            String Each_label_text = Each_Label.getText().toString();
            String prev = myDb.getPrevStat_from_Label(Each_label_text);

            WebView preview_view = view.findViewById(R.id.preview_webview);

            TextView LabelView_at_Expand_ic_click = view.findViewById(R.id.title_label_text);
            String Label_text_at_expand_ic_click = LabelView_at_Expand_ic_click.getText().toString();

            //TODO Test this onTouch code
            preview_view.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    return true;
                }
            });

            ImageView expand_button = view.findViewById(R.id.expand_button);

            if(prev.equals("1"))
            {
                String url_link = myDb.getUrl_from_Label(Label_text_at_expand_ic_click);

                expand_button.setImageResource(R.drawable.collapse_button);

                preview_view.setVisibility(View.VISIBLE);

                preview_view.getSettings().setJavaScriptEnabled(true);
                preview_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                preview_view.setWebViewClient(new WebViewClient());
                preview_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                preview_view.loadUrl(url_link);
                preview_view.setInitialScale(100);

                boolean isUpdate = myDb.updatePrevStat(Label_text_at_expand_ic_click, "1");
                if(!isUpdate)
                    Toast.makeText(Add_Cam.this, R.string.error_try_delete,Toast.LENGTH_LONG).show();
            }

            else
            {
                boolean isUpdate = myDb.updatePrevStat(Label_text_at_expand_ic_click, "0");
                if(!isUpdate)
                    Toast.makeText(Add_Cam.this, R.string.error_try_delete,Toast.LENGTH_LONG).show();

                expand_button.setImageResource(R.drawable.expand_down);
                preview_view.loadUrl("about:blank");
                preview_view.setVisibility(View.GONE);
            }
            i++;
        }
    }

    private void show_hide_drive_button()
    {
        View view;
        int i = 0;
        while (i < d_list.getChildCount()) {
            view = d_list.getChildAt(i);
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

        //new LoadPreview(d_list, myDb).execute();
//
//        i = 0;
//        while (i < d_list.getChildCount())
//        {
//            view = d_list.getChildAt(i);
//            TextView Each_Label = view.findViewById(R.id.title_label_text);
//            String Each_label_text = Each_Label.getText().toString();
//
//            //TODO in Background Test Unstable
//            String url_link = myDb.getUrl_from_Label(Each_label_text);
//            WebView mContentView = view.findViewById(R.id.preview_wv);
//            mContentView.getSettings().setJavaScriptEnabled(true);
//            mContentView.setWebViewClient(new WebViewClient());
//            mContentView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//            mContentView.loadUrl(url_link);
//            mContentView.setInitialScale(50);
//            i++;
//        }
    }

//    private boolean mRunning;
//
//    static Handler mHandler = new Handler();
//
//    Runnable mUpdater = new Runnable()
//    {
//        @Override
//        public void run() {
//            // check if still in focus
//            if (!mRunning) return;
//
//            View view;
//            int i = 0;
//            while (i < d_list.getChildCount())
//            {
//                view = d_list.getChildAt(i);
//                TextView Each_Label = view.findViewById(R.id.title_label_text);
//                String Each_label_text = Each_Label.getText().toString();
//
//                //TODO in Background Test Unstable
//                String url_link = myDb.getUrl_from_Label(Each_label_text);
//                WebView mContentView = view.findViewById(R.id.preview_wv);
//                mContentView.getSettings().setJavaScriptEnabled(true);
//                mContentView.setWebViewClient(new WebViewClient());
//                mContentView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//
////                mContentView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
////                if (Build.VERSION.SDK_INT >= 19) {
////                    mContentView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
////                }
////                else {
////                    mContentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
////                }
//
//                mContentView.loadUrl(url_link);
//                mContentView.setInitialScale(50);
//                i++;
//            }
//            // upadte your list view
//
//            // schedule next run
//            //mHandler.postDelayed(this, 1000); // set time here to refresh views
//        }
//    };
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        mRunning = true;
//        // start first run by hand
//        mHandler.post(mUpdater);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mRunning= false;
//    }

    private int getItemCheckedCount_in_d_list()
    {
        View view;
        CheckBox cb;
        int f = 0;
        for (int i = 0; i < d_list.getChildCount(); i++)
        {
            view = d_list.getChildAt(i);
            cb = view.findViewById(R.id.checkBox);
            if (cb.isChecked())
                f++;
        }
        return f;
    }

    private void toggle_ActionBar_elements()
    {
        dummy_about.setVisible(!dummy_about.isVisible());
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
            editor.commit();
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
            editor.commit();
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
            editor.commit();
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
        int f = getItemCheckedCount_in_d_list();
        if(f != 0)
        {
            View view;
            CheckBox cb;
            for (int i = 0; i < d_list.getChildCount(); i++)
            {
                view = d_list.getChildAt(i);
                cb = view.findViewById(R.id.checkBox);
                cb.setChecked(false);
                cb.setVisibility(View.GONE);
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
            editor.commit();
*/
            finish();
        }
    }
}