package com.jairaj.janglegmail.motioneye;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Objects;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class web_motion_eye extends AppCompatActivity //implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "";
    private ProgressDialog progressBar;
    private AlertDialog cancel_button;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    Handler mHandler = new Handler();
    private WebView mContentView;
    String url_port = "";
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
            mContentView.setSystemUiVisibility(
                    //View.SYSTEM_UI_FLAG_LOW_PROFILE
                    //| View.SYSTEM_UI_FLAG_FULLSCREEN
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    //| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
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
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run()
        {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    /*private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE)
            {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };*/

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

        mVisible = true;
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

        /*if (savedInstanceState != null)
        {
            (mContentView).restoreState(savedInstanceState);
        }*/
        /*else
        {
            mContentView.loadUrl(url_port);
        }*/

        CookieManager.getInstance().setAcceptCookie(true);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        if(mode == Constants.MODE_CAMERA)
            progressBar = ProgressDialog.show(web_motion_eye.this, getString(R.string.connecting_mE), getString(R.string.loading));

        else if(mode == Constants.MODE_DRIVE)
            progressBar = ProgressDialog.show(web_motion_eye.this, getString(R.string.connecting_gD), getString(R.string.loading));

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
                //view.loadUrl("javascript:document.getElementById('username').value = '"+"admin"+"';document.getElementById('password').value='"+"Kulswamini@41"+"';");
                /*view.loadUrl("javascript:(function(){"+
                        "l=document.getElementById('div.button.icon.logout-button.mouse-effect');"+
                        "e=document.createEvent('HTMLEvents');"+
                        "e.initEvent('click',true,true);"+
                        "l.dispatchEvent(e);"+
                        "})()");*/
                //Log.i(TAG, "Finished loading URL: " + url);

                if (progressBar != null)
                {
                    if(progressBar.isShowing())
                        progressBar.dismiss();
                }

                if(cancel_button != null)
                    cancel_button.dismiss();

//                swipe.setRefreshing(false);
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
                    Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
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

        //mContentView.loadUrl(url_port);
        // Set up the user interaction to manually show or hide the system UI.

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /*private void toggle()
    {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }*/

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")

/*    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }*/

    /**
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
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            Toast.makeText(getApplicationContext(), "Storage permission granted", Toast.LENGTH_SHORT).show();
            //resume tasks needing this permission
        }
        else
            Toast.makeText(getApplicationContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mContentView.saveState(outState);
    }*/
}
