package com.jairaj.janglegmail.motioneye;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author sk8 on 25/02/19.
 */

class Constants
{
    //Bundle Keys for
    static final String KEY_URL_PORT = "url_port";
    static final String KEY_MODE = "mode";
    static final String KEY_LEGAL_DOC_TYPE = "LEGAL_DOC";

    //Enum for selecting Legal document to show as only 1 activity is used for it
    enum LEGAL_DOC_TYPE {
        PRIVPOL, TNC
    }

    //Enum for selecting Custom Dialog Box type
    enum DIALOG_TYPE {
        RATE_DIALOG, WEBPAGE_ERROR_DIALOG, WEBPAGE_LONG_LOADING
    }

    @Retention(SOURCE)
    @IntDef({MODE_CAMERA, MODE_DRIVE})
    @interface ServerMode {}
    //CONNECTION MODES
    static final int MODE_CAMERA = 1;
    static final int MODE_DRIVE = 2;
    //EDIT MODES
    static final int EDIT_MODE_NEW_DEV = 0;
    static final int EDIT_MODE_EXIST_DEV = 1;
    static final int EDIT_CANCELLED = 2;

    //UI parameters
    static final int PREVIEW_PADDING = 40;
}
