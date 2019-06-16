package com.jairaj.janglegmail.motioneye;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

public class About_Page extends AppCompatActivity
{
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about__page);

        toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("About");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button send_fb = findViewById(R.id.send_feedb);
        Button join_dev = findViewById(R.id.join_dev);
        Button view_steps = findViewById(R.id.button_install_steps);

        TextView app_version = findViewById(R.id.app_version_text);

        TextView credits_showcase = findViewById(R.id.Credit_ShowCase);
        TextView apache_1 = findViewById(R.id.Apache_1);

        TextView apache_2 = findViewById(R.id.Apache_2);

        String appversion = BuildConfig.VERSION_NAME;

        String AppVersion_Text = "App Version: " + appversion;

        app_version.setText(AppVersion_Text);

        credits_showcase.setText(Html.fromHtml("<a href= 'https://github.com/sjwall/MaterialTapTargetPrompt'> MaterialTapTargetPrompt</a>"));
        credits_showcase.setMovementMethod(LinkMovementMethod.getInstance());

        apache_1.setText(Html.fromHtml("<a href= 'https://github.com/sjwall/MaterialTapTargetPrompt/blob/master/LICENSE'> Apache License</a>"));
        apache_1.setMovementMethod(LinkMovementMethod.getInstance());

        apache_2.setText(Html.fromHtml("<a href= 'http://www.apache.org/licenses/LICENSE-2.0.txt'> Apache License</a>"));
        apache_2.setMovementMethod(LinkMovementMethod.getInstance());

        join_dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String motion_eye_steps = "https://github.com/JairajJangle/motionEye_app_HomeSurveillanceSystem";
                Utils.open_in_chrome(motion_eye_steps, About_Page.this);
            }
        });

        view_steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String motion_eye_steps = "https://github.com/ccrisan/motioneye/wiki/Installation";
                Utils.open_in_chrome(motion_eye_steps, About_Page.this);
            }
        });

        send_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Utils.sendFeedback(About_Page.this);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
