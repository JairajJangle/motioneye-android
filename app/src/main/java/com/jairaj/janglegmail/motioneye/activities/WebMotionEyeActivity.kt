package com.jairaj.janglegmail.motioneye.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.databinding.ActivityWebMotionEyeBinding
import com.jairaj.janglegmail.motioneye.utils.AppUtils
import com.jairaj.janglegmail.motioneye.utils.AppUtils.handleHttpBasicAuthentication
import com.jairaj.janglegmail.motioneye.utils.AppUtils.handleMotionEyeUILogin
import com.jairaj.janglegmail.motioneye.utils.Constants
import com.jairaj.janglegmail.motioneye.utils.Constants.downloadFolderName
import com.jairaj.janglegmail.motioneye.utils.CustomDialogClass
import com.jairaj.janglegmail.motioneye.utils.DataBaseHelper
import java.io.File

class WebMotionEyeActivity : AppCompatActivity() {
    private val logTAG = WebMotionEyeActivity::class.java.name
    private lateinit var binding: ActivityWebMotionEyeBinding

    private lateinit var databaseHelper: DataBaseHelper

    private var progressBar: ProgressDialog? = null
    private lateinit var takingTooLongWarningDialog: AlertDialog

    private var fullScreenPref = true
    private val mHideHandler = Handler(Looper.getMainLooper())
    private var mHandler = Handler(Looper.getMainLooper())
    private var label: String = ""
    private var urlPort: String = ""
    internal var mode = -1

    // Only required for Android 10 and below to temporarily store download file details
    // to initiate download only after checking WRITE permissions
    private var downloadUrl: String? = null
    private var downloadUserAgent: String? = null
    private var downloadContentDisposition: String? = null
    private var downloadMimeType: String? = null
    ///////////////////////////////////////////////////////////////////////////////

    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (fullScreenPref) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.fullscreenContent).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(
                window,
                binding.fullscreenContent
            ).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
    }

    private val mHideRunnable = Runnable { hide() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        binding = ActivityWebMotionEyeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        
        databaseHelper = DataBaseHelper(this)

        val bundle = intent.extras
        //Extract the data…
        if (bundle != null) {
            label = bundle.getString(Constants.KEY_LABEL) ?: ""
            urlPort = bundle.getString(Constants.KEY_URL_PORT) ?: "about:blank"
            mode = bundle.getInt(Constants.KEY_MODE)
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        fullScreenPref = prefs.getBoolean(getString(R.string.key_fullscreen), false)

        binding.fullscreenContent.settings.javaScriptEnabled = true
        binding.fullscreenContent.settings.javaScriptCanOpenWindowsAutomatically = true

        binding.fullscreenContent.settings.domStorageEnabled = true

        binding.fullscreenContent.settings.builtInZoomControls = true
        binding.fullscreenContent.settings.displayZoomControls = false

        binding.fullscreenContent.settings.loadWithOverviewMode = true
        binding.fullscreenContent.settings.useWideViewPort = true

        CookieManager.getInstance().setAcceptCookie(true)

        progressBar = when (mode) {
            Constants.MODE_CAMERA -> ProgressDialog.show(
                this@WebMotionEyeActivity,
                getString(R.string.connecting_mE), getString(R.string.loading)
            )

            Constants.MODE_DRIVE -> ProgressDialog.show(
                this@WebMotionEyeActivity,
                getString(R.string.connecting_cloud_storage), getString(R.string.loading)
            )

            else -> ProgressDialog.show(
                this@WebMotionEyeActivity,
                getString(R.string.connecting_uM), getString(R.string.loading)
            )
        }

        val progressbar = progressBar!!.findViewById<ProgressBar>(android.R.id.progress)
        progressbar.indeterminateDrawable
            .setColorFilter(
                resources.getColor(R.color.motioneye_blue),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

        progressBar!!.setCancelable(true)

        takingTooLongWarningDialog = AlertDialog.Builder(this).create()

        val window = takingTooLongWarningDialog.window
        window?.setGravity(Gravity.BOTTOM)

        takingTooLongWarningDialog.setMessage(":'( Taking too long to load?")

        takingTooLongWarningDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            "Click to Dismiss"
        ) { _, _ -> progressBar!!.dismiss() }

        progressBar!!.setOnCancelListener {
            if (progressBar != null)
                progressBar!!.dismiss()
            mHandler.removeMessages(0)
            finish()
        }

        binding.fullscreenContent.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            // Inject Javascript to load credentials and press Login Button
            override fun onPageFinished(view: WebView, url: String) {
                handleMotionEyeUILogin(databaseHelper, label, view)
                handleOnPageFinished()
            }

            // Inject Javascript to allow force zoom on motionEye UI
            override fun onLoadResource(view: WebView, url: String?) {
                view.loadUrl(
                    "javascript:document.getElementsByName(\"viewport\")[0]" +
                            "               .setAttribute(" +
                            "                   \"content\", " +
                            "                   \"width=device-width, " +
                            "                   initial-scale=1.0, " +
                            "                   maximum-scale=5.0, " +
                            "                   user-scalable=yes" +
                            "               \");"
                )
                super.onLoadResource(view, url)
            }

            // FIXME: On using new method signature for onReceivedError, playing video gives net::ERR_FAILED error
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showWebPageErrorDialog()
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView,
                handler: HttpAuthHandler, host: String, realm: String
            ) {
                handleHttpBasicAuthentication(databaseHelper, label, handler)
            }
        }

        val liveStream = AppUtils.checkWhetherStream(urlPort)
        binding.fullscreenContent.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (liveStream && progress >= 30)
                    handleOnPageFinished()
            }
        }

        binding.fullscreenContent.loadUrl(urlPort)

        binding.fullscreenContent.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            // Check if the permission is already granted
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(
                    this@WebMotionEyeActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Store the download details in temp vars
                downloadUrl = url
                downloadUserAgent = userAgent
                downloadContentDisposition = contentDisposition
                downloadMimeType = mimeType

                // Request the permission
                ActivityCompat.requestPermissions(
                    this@WebMotionEyeActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // Permission is already granted or not needed, proceed with download
                downloadFile(url, userAgent, contentDisposition, mimeType)
            }
        }
    }

    private fun downloadFile(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String
    ) {
        val request = DownloadManager.Request(Uri.parse(url))

        request.setMimeType(mimeType)
        //------------------------COOKIE!!------------------------
        val cookies = CookieManager.getInstance().getCookie(url)
        request.addRequestHeader("cookie", cookies)
        //------------------------COOKIE!!------------------------
        request.addRequestHeader("User-Agent", userAgent)
        request.setDescription("Downloading file...")

        var fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        fileName = "${label}_${fileName.replace(";+$".toRegex(), "")}"

        Log.d(logTAG, "Downloading filename = $fileName")

        request.setTitle(fileName)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            File.separator + downloadFolderName + File.separator + fileName
        )
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(baseContext, "Downloading to Downloads/motionEye", Toast.LENGTH_LONG)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with the download
                    downloadUrl?.let { url ->
                        downloadUserAgent?.let { userAgent ->
                            downloadContentDisposition?.let { contentDisposition ->
                                downloadMimeType?.let { mimeType ->
                                    downloadFile(url, userAgent, contentDisposition, mimeType)
                                }
                            }
                        }
                    }
                } else {
                    // Permission denied, show an error dialog
                    showErrorDialog()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showErrorDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_denied))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.go_to_settings), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Set the positive button color to blue
            positiveButton.setTextColor(resources.getColor(R.color.colorAccent))
            // Set the negative button color to red
            negativeButton.setTextColor(resources.getColor(R.color.error))

            // Set onClickListeners on the buttons to avoid automatic dismissing when clicked
            positiveButton.setOnClickListener {
                // Intent to open the app settings screen
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)

                // Dismiss the AlertDialog
                alertDialog.dismiss()
            }
            positiveButton.setTypeface(null, Typeface.BOLD)

            negativeButton.setOnClickListener {
                // Dismiss the AlertDialog
                alertDialog.dismiss()
            }

            // Set app font to the dialog message text
            val textView = alertDialog.window?.findViewById<View>(android.R.id.message) as TextView
            val typeface = ResourcesCompat.getFont(this, R.font.mavenpro_variable)
            textView.typeface = typeface
        }

        alertDialog.show()
    }


    //To prevent crashes on some devices WebView needs to be safely destroyed
    override fun onDestroy() {
        binding.fullscreenContent.visibility = View.GONE

        binding.fullscreenContent.loadUrl("about:blank")
        if (binding.fullscreenContent.parent != null) {
            (binding.fullscreenContent.parent as ViewGroup).removeView(binding.fullscreenContent)
            binding.fullscreenContent.destroy()
        }

        super.onDestroy()
    }

    internal fun handleOnPageFinished() {
        if (progressBar != null) {
            if (progressBar!!.isShowing)
                progressBar!!.dismiss()
        }

        if (takingTooLongWarningDialog.isShowing)
            takingTooLongWarningDialog.dismiss()
    }

    internal fun showWebPageErrorDialog() {
        fun onCheckHelpFAQPress() {
            val intent = Intent(this, HelpFAQActivity::class.java)
            this.finish()
            this.startActivity(intent)
        }

        fun onSubmitFeedback() {
            AppUtils.sendFeedback(this)
            this.finish()
        }

        val cdd =
            CustomDialogClass(
                this@WebMotionEyeActivity,

                android.R.drawable.ic_dialog_alert,
                getString(R.string.uh_oh),
                getString(R.string.page_error_dialog_message),

                getString(R.string.check_help_faq),
                ::onCheckHelpFAQPress,

                getString(R.string.cancel),
                null,

                getString(R.string.send_feedback),
                ::onSubmitFeedback
            )
        cdd.setCancelable(false)
        cdd.show()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        //        mControlsView.setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /*
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    @Suppress("SameParameterValue")
    @SuppressLint("InlinedApi")
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        private const val UI_ANIMATION_DELAY = 300
        const val PERMISSION_REQUEST_CODE = 1001
    }
}