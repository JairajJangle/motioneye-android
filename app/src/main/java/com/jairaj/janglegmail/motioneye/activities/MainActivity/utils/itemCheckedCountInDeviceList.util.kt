package com.jairaj.janglegmail.motioneye.activities.MainActivity.utils

import android.widget.CheckBox
import androidx.core.view.children
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity

internal val MainActivity.itemCheckedCountInDeviceList: Int
    get() = binding.deviceListRv.children
        .filter { (it.findViewById(R.id.checkBox) as CheckBox).isChecked }
        .count()