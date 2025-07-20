package com.jairaj.janglegmail.motioneye.utils

import androidx.annotation.IntDef

object Constants {
    const val SPLASH_SCREEN_TIME: Long = 300 // ms

    //Bundle Keys
    const val KEY_LABEL = "label"
    const val KEY_URL_PORT = "url_port"
    const val KEY_MODE = "mode"
    const val KEY_LEGAL_DOC_TYPE = "LEGAL_DOC"
    const val LABEL = "LABEL"
    const val EDIT = "EDIT"

    // Shared Prefs keys
    const val KEY_DRIVE_ADDED_BEFORE = "DRIVE_ADDED_BEFORE"
    const val KEY_DEVICE_ADDED_BEFORE = "DEVICE_ADDED_BEFORE"
    const val KEY_IS_APP_OPENED_BEFORE = "IS_APP_OPENED_BEFORE"

    const val DATA_IS_DRIVE_ADDED = "IS_DRIVE_ADDED"

    const val DEVICE_ADDITION_CANCELLED_RESULT_CODE = 2
    const val DEVICE_ADDITION_DONE_RESULT_CODE = 0

    const val KEYSTORE_ALIAS = "com.jairaj.janglegmail.motioneye"

    // Inside Download directory
    const val downloadFolderName = "motionEye"

    enum class DisplayTutorialMode {
        FirstTimeAppOpened,
        FirstTimeDeviceAdded,
        NotFirstTimeForDeviceAdditionButFirstTimeForDrive,
        FirstTimeForDeviceAdditionAsWellAsDrive
    }

    //Enum for selecting Legal document to show as only 1 activity is used for it
    internal enum class LegalDocType {
        PRIVACY_POLICY, TNC
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(MODE_CAMERA, MODE_DRIVE)
    internal annotation class ServerMode

    //CONNECTION MODES
    const val MODE_CAMERA = 1
    const val MODE_DRIVE = 2

    //EDIT MODES
    const val EDIT_MODE_NEW_DEV = 0
    const val EDIT_MODE_EXIST_DEV = 1
    const val EDIT_CANCELLED = 2

    //UI parameters
    const val PREVIEW_PADDING = 40

    const val RATE_CRITERIA_INSTALL_DAYS = 14
    const val RATE_CRITERIA_LAUNCH_TIMES = 20

    const val PREVIEW_ON = "1"
    const val PREVIEW_OFF = "0"
}