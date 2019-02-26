package com.jairaj.janglegmail.motioneye;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author sk8 on 25/02/19.
 */
class Constants {


    //Bundle Keys
    static final String KEY_URL_PORT = "url_port";
    static final String KEY_MODE = "mode";


    @Retention(SOURCE)
    @IntDef({MODE_CAMERA, MODE_DRIVE})
    @interface ServerMode {}
    //MODES
    static final int MODE_CAMERA = 1;
    static final int MODE_DRIVE = 2;

}
