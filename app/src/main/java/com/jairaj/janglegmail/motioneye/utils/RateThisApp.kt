package com.jairaj.janglegmail.motioneye.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import java.util.Calendar
import java.util.Date

/**
 * RateThisApp - App rating prompt manager
 *
 * Tracks app usage (install date, launch count) and shows rating dialogs based on configurable
 * criteria. Handles user responses (rate now, later, no thanks) and prevents repeated prompts.
 *
 * Usage:
 * 1. Initialize: RateThisApp.init(Config(installDays = 7, launchTimes = 10))
 * 2. Track launches: RateThisApp.onCreate(context)
 * 3. Show dialog: RateThisApp.showRateDialogIfNeeded(context, themeResId)
 */
object RateThisApp {

    private const val PREF_NAME = "RateThisApp"
    private const val KEY_INSTALL_DATE = "rate_install_date"
    private const val KEY_LAUNCH_TIMES = "rate_launch_times"
    private const val KEY_IS_NEVER_SHOWN = "rate_is_never_shown"
    private const val KEY_IS_OPTED_OUT = "rate_is_opted_out"
    private const val KEY_REMINDER_DATE = "rate_reminder_date"

    private const val TAG = "RateThisApp"

    private var config: Config? = null

    /**
     * Configuration class for RateThisApp
     */
    data class Config(
        val criteriaInstallDays: Int = 7,
        val criteriaLaunchTimes: Int = 10,
        val reminderInterval: Int = 1 // days to wait before showing again after "Later" is clicked
    )

    /**
     * Initialize the RateThisApp with configuration
     */
    fun init(config: Config) {
        this.config = config
    }

    /**
     * Call this method on app launch (typically in onCreate of main activity)
     */
    fun onCreate(context: Context) {
        val prefs = getPreferences(context)
        
        // Set install date if not set
        if (!prefs.contains(KEY_INSTALL_DATE)) {
            storeInstallDate(context, Date())
        }
        
        // Store current launch count BEFORE incrementing for criteria checking
        val currentLaunchTimes = prefs.getInt(KEY_LAUNCH_TIMES, 0)
        
        // Increment launch count for next time
        prefs.edit {
            putInt(KEY_LAUNCH_TIMES, currentLaunchTimes + 1)
        }
        
        Log.d(TAG, "Launch times: ${currentLaunchTimes + 1}")
    }

    /**
     * Show rate dialog if conditions are met
     */
    fun showRateDialogIfNeeded(context: Context, themeResId: Int = 0) {
        if (shouldShowRateDialog(context)) {
            showRateDialog(context, themeResId)
        }
    }

    /**
     * Force show rate dialog regardless of conditions
     */
    fun showRateDialog(context: Context, themeResId: Int = 0) {
        showCustomRateDialog(context, themeResId)
    }

    /**
     * Show custom rate dialog with proper motionEye styling
     */
    private fun showCustomRateDialog(context: Context, themeResId: Int = 0) {
        val appName = getApplicationName(context)

        // Create custom dialog
        val dialog = if (themeResId != 0) {
            AlertDialog.Builder(context, themeResId)
        } else {
            AlertDialog.Builder(context)
        }.create()

        // Create custom layout programmatically
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(
                dpToPx(context, 24f),
                dpToPx(context, 20f),
                dpToPx(context, 24f),
                dpToPx(context, 8f)
            )
        }

        // Get Maven Pro font
        val mavenProFont = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, com.jairaj.janglegmail.motioneye.R.font.mavenpro_variable)
        } catch (e: Exception) {
            Log.w(TAG, "Could not load Maven Pro font, using default", e)
            android.graphics.Typeface.DEFAULT
        }

        // Title
        val titleView = android.widget.TextView(context).apply {
            text = context.getString(com.jairaj.janglegmail.motioneye.R.string.rate_dialog_title, appName)
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, com.jairaj.janglegmail.motioneye.R.color.white))
            typeface = mavenProFont
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, dpToPx(context, 16f))
        }

        // Message
        val messageView = android.widget.TextView(context).apply {
            text = context.getString(com.jairaj.janglegmail.motioneye.R.string.rate_dialog_message, appName)
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, com.jairaj.janglegmail.motioneye.R.color.white))
            typeface = mavenProFont
            setPadding(0, 0, 0, dpToPx(context, 24f))
        }

        // Button container
        val buttonContainer = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.END
            setPadding(0, dpToPx(context, 8f), 0, dpToPx(context, 8f))
        }

        val motionEyeBlue = ContextCompat.getColor(context, com.jairaj.janglegmail.motioneye.R.color.motioneye_blue)

        // No Thanks button
        val noThanksButton = android.widget.Button(context).apply {
            text = context.getString(com.jairaj.janglegmail.motioneye.R.string.rate_dialog_no_thanks)
            setTextColor(motionEyeBlue)
            background = null
            isAllCaps = true
            textSize = 14f
            typeface = mavenProFont
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dpToPx(context, 8f), dpToPx(context, 8f), dpToPx(context, 8f), dpToPx(context, 8f))
            setOnClickListener {
                getPreferences(context).edit {
                    putBoolean(KEY_IS_OPTED_OUT, true)
                }
                dialog.dismiss()
            }
        }

        // Later button
        val laterButton = android.widget.Button(context).apply {
            text = context.getString(com.jairaj.janglegmail.motioneye.R.string.rate_dialog_later)
            setTextColor(motionEyeBlue)
            background = null
            isAllCaps = true
            textSize = 14f
            typeface = mavenProFont
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dpToPx(context, 8f), dpToPx(context, 8f), dpToPx(context, 8f), dpToPx(context, 8f))
            setOnClickListener {
                getPreferences(context).edit {
                    val reminderDate = Date()
                    val cal = Calendar.getInstance()
                    cal.time = reminderDate
                    cal.add(Calendar.DAY_OF_YEAR, config?.reminderInterval ?: 1)
                    putLong(KEY_REMINDER_DATE, cal.timeInMillis)
                }
                dialog.dismiss()
            }
        }

        // Rate Now button
        val rateNowButton = android.widget.Button(context).apply {
            text = context.getString(com.jairaj.janglegmail.motioneye.R.string.rate_dialog_rate_now)
            setTextColor(motionEyeBlue)
            background = null
            isAllCaps = true
            textSize = 14f
            typeface = mavenProFont
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dpToPx(context, 8f), dpToPx(context, 8f), dpToPx(context, 8f), dpToPx(context, 8f))
            setOnClickListener {
                getPreferences(context).edit {
                    putBoolean(KEY_IS_NEVER_SHOWN, true)
                }

                try {
                    context.startActivity(createRateIntent(context))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open Play Store", e)
                }
                dialog.dismiss()
            }
        }

        // Add buttons to container
        buttonContainer.addView(noThanksButton)
        buttonContainer.addView(laterButton)
        buttonContainer.addView(rateNowButton)

        // Add views to layout
        layout.addView(titleView)
        layout.addView(messageView)
        layout.addView(buttonContainer)

        dialog.setView(layout)
        dialog.setCancelable(false)
        dialog.show()
    }

    /**
     * Convert dp to pixels
     */
    private fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

/**
 * Check if rate dialog should be shown based on criteria
 */
private fun shouldShowRateDialog(context: Context): Boolean {
    val prefs = getPreferences(context)
    val currentConfig = config ?: Config()
    
    // Don't show if user opted out
    if (prefs.getBoolean(KEY_IS_OPTED_OUT, false)) {
        Log.d(TAG, "User opted out")
        return false
    }
    
    // Don't show if already shown and user rated
    if (prefs.getBoolean(KEY_IS_NEVER_SHOWN, false)) {
        Log.d(TAG, "Already shown and rated")
        return false
    }
    
    // Check if reminder interval has passed
    if (prefs.contains(KEY_REMINDER_DATE)) {
        val reminderDate = Date(prefs.getLong(KEY_REMINDER_DATE, 0))
        val currentDate = Date()
        if (currentDate.before(reminderDate)) {
            Log.d(TAG, "Reminder interval not yet passed")
            return false
        }
    }
    
    // Check install date criteria
    val installDate = getInstallDate(context)
    val daysSinceInstall = getDaysBetween(installDate, Date())
    if (daysSinceInstall < currentConfig.criteriaInstallDays) {
        Log.d(TAG, "Install days criteria not met: $daysSinceInstall < ${currentConfig.criteriaInstallDays}")
        return false
    }
    
    // Check launch times criteria - use count BEFORE this session's increment
    val launchTimes = prefs.getInt(KEY_LAUNCH_TIMES, 0) - 1 // Subtract 1 for current session
    if (launchTimes < currentConfig.criteriaLaunchTimes) {
        Log.d(TAG, "Launch times criteria not met: $launchTimes < ${currentConfig.criteriaLaunchTimes}")
        return false
    }
    
    Log.d(TAG, "All criteria met - should show dialog")
    return true
}

    /**
     * Get install date from preferences
     */
    private fun getInstallDate(context: Context): Date {
        val prefs = getPreferences(context)
        val installDateMillis = prefs.getLong(KEY_INSTALL_DATE, 0)
        return Date(installDateMillis)
    }

    /**
     * Store install date in preferences
     */
    private fun storeInstallDate(context: Context, date: Date) {
        getPreferences(context).edit {
            putLong(KEY_INSTALL_DATE, date.time)
        }
        Log.d(TAG, "Install date stored: $date")
    }

    /**
     * Calculate days between two dates
     */
    private fun getDaysBetween(start: Date, end: Date): Int {
        val diffInMillis = end.time - start.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Get SharedPreferences for RateThisApp
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get application name
     */
    private fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) {
            applicationInfo.nonLocalizedLabel.toString()
        } else {
            context.getString(stringId)
        }
    }

    /**
     * Create intent to open app in Play Store
     */
    private fun createRateIntent(context: Context): Intent {
        val packageName = context.packageName
        return try {
            // Try to open in Play Store app
            Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            // Fallback to web browser
            Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}