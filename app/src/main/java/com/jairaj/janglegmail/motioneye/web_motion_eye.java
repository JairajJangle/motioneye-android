package com.jairaj.janglegmail.motioneye;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Objects;

public class web_motion_eye extends AppCompatActivity //implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "";
    private ProgressDialog progressBar;
    private AlertDialog cancel_button;

    boolean FullScreenPref = true;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    Handler mHandler = new Handler();
    private WebView mContentView;
    String url_port = "";
    String page_error_title = "Error";
    int mode = -1;
    //private SwipeRefreshLayout swipe;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

            int UiVisibilityFlag = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            mContentView.setSystemUiVisibility(
                    //View.SYSTEM_UI_FLAG_LOW_PROFILE
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            //|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );

            if(FullScreenPref)
                mContentView.setSystemUiVisibility(mContentView.getSystemUiVisibility()
                        | UiVisibilityFlag);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run()
        {
            hide();
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Bundle bundle = getIntent().getExtras();
        //Extract the data…
        if (bundle != null)
        {
            url_port = bundle.getString(Constants.KEY_URL_PORT);
            mode = bundle.getInt(Constants.KEY_MODE);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_motion_eye);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        FullScreenPref = prefs.getBoolean(getString(R.string.key_fullscreen), true);

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.getSettings().setJavaScriptEnabled(true);
        mContentView.setWebViewClient(new WebViewClient());
        mContentView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        mContentView.getSettings().setBuiltInZoomControls(true);
        mContentView.getSettings().setSupportZoom(true);
        mContentView.getSettings().setDisplayZoomControls(true); // disable the default zoom controls on the page

//        swipe = findViewById(R.id.swipe);
//        swipe.setOnRefreshListener(this);

        CookieManager.getInstance().setAcceptCookie(true);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        if(mode == Constants.MODE_CAMERA) {
            progressBar = ProgressDialog.show(web_motion_eye.this, getString(R.string.connecting_mE), getString(R.string.loading));
            page_error_title = "Uh Oh! The camera stream could not be opened :( ";
        }
        else if(mode == Constants.MODE_DRIVE) {
            progressBar = ProgressDialog.show(web_motion_eye.this, getString(R.string.connecting_gD), getString(R.string.loading));
            page_error_title = "Uh Oh! The cloud link could not be opened :( ";
        }
        progressBar.setCancelable(true);

        cancel_button = new AlertDialog.Builder(this).create();

        Window window = cancel_button.getWindow();
        Objects.requireNonNull(window).setGravity(Gravity.BOTTOM);

        cancel_button.setMessage(":'( Taking too long to load?");

        cancel_button.setButton(DialogInterface.BUTTON_NEGATIVE, "Click to Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                progressBar.dismiss();
            }
        });

        progressBar.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            public void onCancel(DialogInterface arg0)
            {
                progressBar.dismiss();
                mHandler.removeMessages(0);
                finish();
            }
        });

        mContentView.setWebViewClient(new WebViewClient()
        {
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                //Log.i(TAG, "Processing webview url click...");
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url)
            {
                if (progressBar != null)
                {
                    if(progressBar.isShowing())
                        progressBar.dismiss();
                }

                if(cancel_button != null) {
                    if (cancel_button.isShowing())
                        cancel_button.dismiss();
                }
//                swipe.setRefreshing(false);
            }

            //TODO: Android < 5.0.0 Dialog Box inconsistent and Error on Send Feedback intent
            public void onReceivedError(WebView view, int errorCod,String description, String failingUrl)
            {
                Toast.makeText(getBaseContext(), page_error_title + "\nCheck FAQs or Send us Feedback" , Toast.LENGTH_LONG).show();
                //show_webpageErrorDialog();
            }

            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error)
            {
                show_webpageErrorDialog();
            }
        });

        mContentView.loadUrl(url_port);

        mContentView.setDownloadListener(new DownloadListener()
        {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength)
            {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                if(isStoragePermissionGranted())
                {
                    request.setMimeType(mimeType);
                    //------------------------COOKIE!!------------------------
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    //------------------------COOKIE!!------------------------
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getBaseContext(), "Downloading File", Toast.LENGTH_LONG).show();
                }
            }
        });

        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(progressBar.isShowing())
                    cancel_button.show();
            }
        }, 15000L);
    }

    void show_webpageErrorDialog()
    {
        Toast.makeText(getBaseContext(), "Error",
                Toast.LENGTH_LONG).show();

        new AlertDialog.Builder(web_motion_eye.this)
                .setTitle(page_error_title)
                .setMessage("Please help us fix the issue by letting us know by sending a feedback")

                .setPositiveButton("Send Feedback", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Utils.sendFeedback(getBaseContext());
                    }

                })

                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                })
                .setNegativeButton("Check Help and FAQ", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent i = new Intent(web_motion_eye.this, Help_FAQ.class);
                        finish();  //Kill the activity from which you will go to next activity
                        startActivity(i);
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")

    /*
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

//    @Override
//    public void onRefresh()
//    {
//        if(mContentView.getScrollY() == 0)
//        {
//            swipe.setRefreshing(true);
//            ReLoadWebView(url_port);
//        }
//        else
//        {
//            swipe.setRefreshing(false);
//        }
//    }

    private void ReLoadWebView(String currentURL)
    {
        mContentView.loadUrl(currentURL);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else
        {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            Toast.makeText(getBaseContext(), "Storage permission granted", Toast.LENGTH_SHORT).show();
            //resume tasks needing this permission
        }
        else
            Toast.makeText(getBaseContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
    }
}
