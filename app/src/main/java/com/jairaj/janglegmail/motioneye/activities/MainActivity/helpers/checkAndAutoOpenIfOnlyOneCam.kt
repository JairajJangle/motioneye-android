package com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import androidx.preference.PreferenceManager
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity
import com.jairaj.janglegmail.motioneye.utils.Constants

fun MainActivity.checkAndAutoOpenIfOnlyOneCam() {
    // Check if Auto Open Camera Preference is checked
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val isAutoOpenCamEnabled = prefs.getBoolean(getString(R.string.key_autoopen), true)

    binding.deviceListRv.post {
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (deviceList.size == 1 && isAutoOpenCamEnabled) {
                    val label = deviceList.elementAt(0).label
                    val url = deviceList.elementAt(0).urlPort
                    val mode =
                        if (TextUtils.isEmpty(
                                // FIXME: Fix this blunder
                                dataBaseHelper.driveFromLabel(
                                    deviceList.elementAt(0).label
                                )
                            )
                        ) {
                            Constants.MODE_CAMERA
                        } else {
                            Constants.MODE_DRIVE
                        }
                    goToWebMotionEye(label, url, mode)
                }
            }
        }

        val threadToggleDrivePrev: Thread = object : Thread() {
            override fun run() {
                handler.sendEmptyMessage(0)
            }
        }
        threadToggleDrivePrev.start()
    }
}