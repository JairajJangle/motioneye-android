package com.jairaj.janglegmail.motioneye;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class add_device_detail extends AppCompatActivity
{
    private static final String TAG = "GG";
    DataBase myDb;

    String edit_mode = "";
    String edit_label = "";
    String edit_url_port = "";
    String edit_port = "";
    String edit_url = "";
    String edit_drive_link = "";
    //private AdView mAdView;
    int should_proceed = 0; //0 = no, 1 = yes
    int flag = 0;
    //AdRequest adRequest;
    //AdListener adListener;

    Intent previousScreen;
    EditText url_input_j, port_input_j, label_input_j, drive_link_input_j;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        myDb = new DataBase(this);

        flag = 0;
        Bundle bundle = getIntent().getExtras();
        //Extract the data…
        if (bundle != null)
        {
            edit_mode = bundle.getString("EDIT");
            edit_label = bundle.getString("LABEL");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_detail);
        //MobileAds.initialize(this, "ca-app-pub-7081069887552324~4679468464");

        Toolbar toolbar = findViewById(R.id.toolbar);
        FloatingActionButton det = findViewById(R.id.det);
        FloatingActionButton can = findViewById(R.id.cancel);
        url_input_j = findViewById(R.id.url_input);
        port_input_j = findViewById(R.id.port_input);
        label_input_j = findViewById(R.id.label_input);
        drive_link_input_j = findViewById(R.id.drive_input);

        //TODO: Check working
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }

        final Intent previousScreen = new Intent(getApplicationContext(), Add_Cam.class);
        previousScreen.putExtra("Code",0);

        //display_ad();

        setSupportActionBar(toolbar);

        if(edit_mode.equals("1"))
        {
            edit_url = myDb.getUrl_from_Label(edit_label);
            edit_port = myDb.getPort_from_Label(edit_label);
            edit_drive_link = myDb.getDrive_from_Label(edit_label);

            url_input_j.setText(edit_url);
            port_input_j.setText(edit_port);
            label_input_j.setText(edit_label);
            drive_link_input_j.setText(edit_drive_link);
        }

        det.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                save_to_file();
                if(should_proceed == 1)
                {
                    setResult(0, previousScreen);
                    finish();
                }                //0 to add entries
                //1 to make changes on editing
                //2 to cancel edit
            }
        });

        can.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getBaseContext(), R.string.cancelled_toast,
                        Toast.LENGTH_SHORT).show();

                Intent previousScreen = new Intent(getApplicationContext(), Add_Cam.class);
                previousScreen.putExtra("Code",0);

                edit_mode = "2";
                save_to_file();

                setResult(2, previousScreen);
                finish();
            }
        });
    }

    private void save_to_file()
    {
        String url_input, port_input, label_input, drive_link_input;
        url_input = url_input_j.getText().toString();
        port_input = port_input_j.getText().toString();
        label_input = label_input_j.getText().toString();
        drive_link_input = drive_link_input_j.getText().toString();

        if (URLUtil.isValidUrl(url_input) && !label_input.equals("")
                && (URLUtil.isValidUrl(drive_link_input) || drive_link_input.equals("")))
        {
            switch (edit_mode)
            {
                case "0":
                    boolean isInserted = myDb.insertData(label_input, url_input, port_input, drive_link_input, "1");
                    if(isInserted)
                        Toast.makeText(getBaseContext(), R.string.toast_added,
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getBaseContext(), R.string.error_try_again,
                                Toast.LENGTH_SHORT).show();
                    break;

                case "1":
                    boolean isUpdate = myDb.updateData(edit_label, label_input, url_input, port_input, drive_link_input);
                    if(!isUpdate)
                        Toast.makeText(add_device_detail.this, R.string.error_try_delete,Toast.LENGTH_LONG).show();
                    break;
            }
            should_proceed = 1;
        }

        else if (!URLUtil.isValidUrl(url_input))
        {
            if(!edit_mode.equals("2"))
                Toast.makeText(getBaseContext(), R.string.warning_invalid_url, Toast.LENGTH_SHORT).show();
            should_proceed = 0;
        }

        else if (url_input.equals(""))
        {
            if(!edit_mode.equals("2"))
                Toast.makeText(getBaseContext(), R.string.warning_empty_url, Toast.LENGTH_SHORT).show();
            should_proceed = 0;
        }

        else if (label_input.equals(""))
        {
            if(!edit_mode.equals("2"))
                Toast.makeText(getBaseContext(), R.string.warning_empty_label, Toast.LENGTH_SHORT).show();
            should_proceed = 0;
        }

        else if(!URLUtil.isValidUrl(drive_link_input))
        {
            if(!edit_mode.equals("2"))
                Toast.makeText(getBaseContext(), R.string.invalid_drive_warning, Toast.LENGTH_SHORT).show();
            should_proceed = 0;
        }
    }

    @Override
    public void onBackPressed()
    {
        Toast.makeText(add_device_detail.this,"Cancelled",Toast.LENGTH_LONG).show();
        setResult(2, previousScreen);
        finish();
    }

    public void display_tutorial(int call_number)
    {
        /* call_number usage
         * 1 = First Time App Opened
         * 2 = First Time Device added
         * 3 = Not First Time for Device addition but First Time for Drive
         * 4 = First Time for device addition as well as drive
         */

        if(call_number == 1)
        {
            new MaterialTapTargetPrompt.Builder(add_device_detail.this)
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

        if(call_number == 2)
        {
            new MaterialTapTargetPrompt.Builder(add_device_detail.this)
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
    }

    /*public void display_ad()
    {
        mAdView = findViewById(R.id.adView);
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
}
