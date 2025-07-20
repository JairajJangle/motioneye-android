package com.jairaj.janglegmail.motioneye.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity
import com.jairaj.janglegmail.motioneye.utils.Constants.KEY_DEVICE_ADDED_BEFORE
import com.jairaj.janglegmail.motioneye.utils.Constants.KEY_DRIVE_ADDED_BEFORE
import com.jairaj.janglegmail.motioneye.utils.Constants.KEY_IS_APP_OPENED_BEFORE
import com.jairaj.janglegmail.motioneye.utils.Constants.RATE_CRITERIA_INSTALL_DAYS
import com.jairaj.janglegmail.motioneye.utils.Constants.RATE_CRITERIA_LAUNCH_TIMES
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.net.InetAddress
import java.util.regex.Pattern

object AppUtils {
    private val logTAG = AppUtils::class.java.name

    fun sendFeedback(context: Context) {
        val body: String = try {
            val appInfo = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            "\n\n-----------------------------\n" +
                    "Please don't remove this information\n\n" +
                    "Device OS: Android \n" +
                    "Device OS version: ${Build.VERSION.RELEASE}\n" +
                    "App Version: $appInfo\n" +
                    "Device Brand: ${Build.BRAND}\n" +
                    "Device Model: ${Build.MODEL}\n" +
                    "Device Manufacturer: ${Build.MANUFACTURER}\n" +
                    "-----------------------------\n"

        } catch (e: Exception) {
            Log.e(logTAG, "Exception occurred while framing the feedback mail body: $e")
            ""
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("systems.sentinel@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "motionEye app Feedback")
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.choose_email_client)
            )
        )
    }

    fun openInChrome(url: String, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Chrome browser presumably not installed so allow user to choose instead
            Log.e(logTAG, "Exception while opening url in chrome: $e")

            intent.setPackage(null)
            context.startActivity(intent)
        }

    }

    fun checkWhetherStream(url_port: String): Boolean {
        return url_port.contains("8081")
    }

    fun askToRate(context: Context) {
        Log.d(logTAG, "askToRate called")

        fun requestAppRating() {
            showRateDialog(context, true)
        }

        fun requestFeedback() {
            sendFeedback(context)
        }

        val customDialogClass =
            CustomDialogClass(
                context as Activity,

                null,
                null,
                context.getString(R.string.are_you_enjoying),

                context.getString(R.string.yes),
                ::requestAppRating,

                context.getString(R.string.no),
                ::requestFeedback,

                null,
                null
            )
        customDialogClass.show()
    }

    fun showRateDialog(context: Context, showRightAway: Boolean) {
        // Custom condition: x days and y launches
        val config = RateThisApp.Config(RATE_CRITERIA_INSTALL_DAYS, RATE_CRITERIA_LAUNCH_TIMES)
        RateThisApp.init(config)

        // Monitor launch times and interval from installation
        RateThisApp.onCreate(context)
        // If the condition is satisfied, "Rate this app" dialog will be shown
        if (showRightAway)
            RateThisApp.showRateDialog(context, R.style.AlertDialogCustom)
        else
            RateThisApp.showRateDialogIfNeeded(context, R.style.AlertDialogCustom)
    }

    fun getVersionName(context: Context): String {
        val manager: PackageManager = context.packageManager
        val info: PackageInfo = manager.getPackageInfo(
            context.packageName, 0
        )

        return info.versionName ?: "N/A"
    }

    // return true if for the first time drive/cloud storage link is added
    fun isFirstTimeDrive(activity: Activity): Boolean {
        val preferences = activity.getPreferences(Context.MODE_PRIVATE)
        val isDriveAddedBefore = preferences.getBoolean(KEY_DRIVE_ADDED_BEFORE, false)

        if (!isDriveAddedBefore) {
            // first time
            val editor = preferences.edit()
            editor.putBoolean(KEY_DRIVE_ADDED_BEFORE, true)
            editor.apply()
        }
        return !isDriveAddedBefore
    }

    // returns true if for the first time any device is added
    fun isFirstTimeDevice(activity: Activity): Boolean {
        val preferences = activity.getPreferences(Context.MODE_PRIVATE)
        val isDeviceAddedBefore = preferences.getBoolean(KEY_DEVICE_ADDED_BEFORE, false)
        if (!isDeviceAddedBefore) {
            // first time
            val editor = preferences.edit()
            editor.putBoolean(KEY_DEVICE_ADDED_BEFORE, true)
            editor.apply()
        }
        return !isDeviceAddedBefore
    }

    // return true if ap is opened for the first time
    fun isFirstTimeAppOpened(activity: Activity): Boolean {
        val preferences = activity.getPreferences(Context.MODE_PRIVATE)
        val ranBefore = preferences.getBoolean(KEY_IS_APP_OPENED_BEFORE, false)
        if (!ranBefore) {
            // first time
            val editor = preferences.edit()
            editor.putBoolean(KEY_IS_APP_OPENED_BEFORE, true)
            editor.apply()
        }
        return !ranBefore
    }

    fun RecyclerView.runWhenReady(action: () -> Unit) {
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                action()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    fun handleMotionEyeUILogin(
        databaseHelper: DataBaseHelper,
        label: String,
        view: WebView
    ) {
        var username = ""
        var password = ""

        val encryptedCredJSONStr = databaseHelper.credJSONFromLabel(label)
        if (encryptedCredJSONStr.isNotEmpty()) {
            val usernamePasswordPair = databaseHelper.getDecryptedCred(encryptedCredJSONStr)
            username = usernamePasswordPair.first
            password = usernamePasswordPair.second
        }

        var jsToInject = "javascript: (function() {"

        if (username.isNotEmpty())
            jsToInject += "     document.getElementById('usernameEntry').value= '$username';"

        if (password.isNotEmpty())
            jsToInject += "     document.getElementById('passwordEntry').value= '$password';"
        jsToInject += "         document.getElementById('rememberCheck').click();"

        if (username.isNotEmpty() && password.isNotEmpty()) {
            jsToInject += "     document.querySelector(" +
                    "               'div.button.dialog.mouse-effect.default'" +
                    "           ).click();\n"
        }

        jsToInject += "   }) ();"

        view.loadUrl(jsToInject)
    }

    fun handleHttpBasicAuthentication(
        databaseHelper: DataBaseHelper,
        label: String,
        handler: HttpAuthHandler
    ) {
        val encryptedCredJSONStr = databaseHelper.credJSONFromLabel(label)

        var username = ""
        var password = ""
        if (encryptedCredJSONStr.isNotEmpty()) {
            val usernamePasswordPair = databaseHelper.getDecryptedCred(encryptedCredJSONStr)
            username = usernamePasswordPair.first
            password = usernamePasswordPair.second
        }

        if (username.isNotEmpty() && password.isNotEmpty()) {
            handler.proceed(username, password)
        }
    }

    fun displayMainActivityTutorial(
        mainActivity: MainActivity,
        mode: Constants.DisplayTutorialMode,
        isNextDriveTutorial: Boolean = false
    ) {
        val font = ResourcesCompat.getFont(mainActivity, R.font.mavenpro_variable)
        /* call_number usage
         * 1 = First Time App Opened
         * 2 = First Time Device added
         * 3 = Not First Time for Device addition but First Time for Drive
         * 4 = First Time for device addition as well as drive
         */
        when (mode) {
            Constants.DisplayTutorialMode.FirstTimeAppOpened -> {
                MaterialTapTargetPrompt.Builder(mainActivity)
                    .setTarget(R.id.fab)
                    .setPrimaryText(R.string.tut_title_add_button)
                    .setSecondaryText(R.string.tut_sub_add_button)
                    .setBackgroundColour(Color.argb(255, 30, 90, 136))
                    .setPromptStateChangeListener { _, _ -> /*
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
                    .setPrimaryTextTypeface(font, Typeface.NORMAL)
                    .setSecondaryTextTypeface(font, Typeface.NORMAL)
                    .show()
            }
            Constants.DisplayTutorialMode.FirstTimeDeviceAdded -> {
                MaterialTapTargetPrompt.Builder(mainActivity)
                    .setTarget(R.id.dummy_show_case_button)
                    .setFocalColour(Color.argb(0, 0, 0, 0))
                    .setPrimaryText(R.string.tut_title_device_list)
                    .setSecondaryText(R.string.tut_sub_device_list)
                    .setBackgroundColour(Color.argb(255, 30, 90, 136))
                    .setPromptBackground(RectanglePromptBackground())
                    .setPromptFocal(RectanglePromptFocal())
                    .setPromptStateChangeListener { _, state ->
                        if (isNextDriveTutorial) {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED
                                || state == MaterialTapTargetPrompt.STATE_DISMISSED
                            ) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    displayMainActivityTutorial(
                                        mainActivity,
                                        Constants.DisplayTutorialMode.NotFirstTimeForDeviceAdditionButFirstTimeForDrive
                                    )
                                }, 100)
                            }
                        }
                    }
                    .setPrimaryTextTypeface(font, Typeface.NORMAL)
                    .setSecondaryTextTypeface(font, Typeface.NORMAL)
                    .show()
            }
            Constants.DisplayTutorialMode.NotFirstTimeForDeviceAdditionButFirstTimeForDrive -> {

                if (mainActivity.tutorialTargetDriveIcon == null) return

                Log.i(logTAG, "Displaying Tutorial for Drive Button")
                val builder = MaterialTapTargetPrompt.Builder(mainActivity)
                    .setTarget(mainActivity.tutorialTargetDriveIcon)
                    .setPrimaryText(R.string.tut_title_cloud_storage_icon)
                    .setSecondaryText(R.string.tut_sub_cloud_storage_icon)
                    .setBackgroundColour(Color.argb(255, 30, 90, 136))
                    .setPromptStateChangeListener { _, _ ->
                    }
                    .setPrimaryTextTypeface(font, Typeface.NORMAL)
                    .setSecondaryTextTypeface(font, Typeface.NORMAL)

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        builder.show()
                    }, 1000
                )
            }
            Constants.DisplayTutorialMode.FirstTimeForDeviceAdditionAsWellAsDrive -> {
                displayMainActivityTutorial(
                    mainActivity,
                    Constants.DisplayTutorialMode.FirstTimeDeviceAdded,
                    true
                )
            }
        }
    }

    fun View.showKeyboard() {
        this.requestFocus()
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        Handler(Looper.getMainLooper()).postDelayed({
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }, 1000)
    }

    /**
     * Checks if the provided URL is valid. This function supports both standard URLs
     * (including those with IPv4 addresses) and URLs specifically containing IPv6 addresses.
     *
     * @param urlInputString The URL string to be validated.
     * @param allowEmpty Specifies whether an empty string should be considered as a valid URL.
     * @return True if the URL is valid or allowed to be empty, false otherwise.
     */
    fun isValidURL(urlInputString: String, allowEmpty: Boolean = false): Boolean {
        // First, check if the URL matches the standard URL pattern or if it's allowed to be empty.
        // The Patterns.WEB_URL.matcher provides a broad validation for URLs, including those with IPv4 addresses.
        val isValidStandardURL = Patterns.WEB_URL.matcher(urlInputString)
            .matches() || (allowEmpty && urlInputString.isEmpty())

        // Prepare a pattern to find IPv6 addresses within URLs. IPv6 addresses in URLs are enclosed in square brackets.
        val ipv6URLPattern = Pattern.compile("\\[([0-9a-fA-F:]+)\\]")
        val matcher = ipv6URLPattern.matcher(urlInputString)
        var isValidIPv6URL = false

        // Attempt to find an IPv6 address within the URL using the prepared regex pattern.
        if (matcher.find()) {
            // If an IPv6 address is found, extract it from the matcher.
            val ipv6Address = matcher.group(1)

            // Validate the extracted IPv6 address by trying to create an InetAddress object.
            // InetAddress.getByName will throw an UnknownHostException if the address is invalid.
            isValidIPv6URL = try {
                InetAddress.getByName(ipv6Address) is InetAddress
                true // The presence of an InetAddress object means the IPv6 address is valid.
            } catch (e: Exception) {
                false // Catch any exceptions, indicating the IPv6 address is not valid.
            }
        }

        // The URL is considered valid if it either matches the standard URL pattern or contains a valid IPv6 address.
        return isValidStandardURL || isValidIPv6URL
    }
}
