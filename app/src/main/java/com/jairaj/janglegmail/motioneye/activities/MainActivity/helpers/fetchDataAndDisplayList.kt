package com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity
import com.jairaj.janglegmail.motioneye.activities.WebMotionEyeActivity
import com.jairaj.janglegmail.motioneye.dataclass.CamDevice
import com.jairaj.janglegmail.motioneye.utils.Constants
import com.jairaj.janglegmail.motioneye.views_and_adapters.CamDeviceRVAdapter
import com.jairaj.janglegmail.motioneye.views_and_adapters.TextDrawable

fun MainActivity.fetchDataAndDisplayList(isDelete: Boolean = false) {
    //Handler to handle data fetching from SQL in BG
    val handlerFetchData = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            fetchData()

            if (isDelete)
                binding.deviceListRv.adapter?.notifyDataSetChanged()
        }
    }
    val tFetchData: Thread = object : Thread() {
        override fun run() {
            handlerFetchData.sendEmptyMessage(0)
        }
    }
    tFetchData.start()
}


private fun MainActivity.fetchData() {
    deviceList.clear()

    val data = dataBaseHelper.allData

    binding.noCamBackgroundLayout.root.visibility = if (data.count == 0) View.VISIBLE
    else View.GONE

    val dataList = ArrayList<CamDevice>()

    if (data.count != 0) {
        //isFirstTimeDevice();
        while (data.moveToNext()) {
            val label = data.getString(1)
            val url = data.getString(2)
            val port = data.getString(3)

            Log.e(logTAG, "Sort index = ${data.getString(7)}")

            val driveLink = dataBaseHelper.driveFromLabel(label)

            val urlPort = url + if (port.isBlank()) ""
            else ":$port"

            val dataObject = CamDevice(label, urlPort, driveLink)
            dataList.add(dataObject)
        }
    }
    data.close()

    dataList.forEach { camDevice -> deviceList.add(camDevice) }

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            addToList()
        }
    }
    val threadAddToList: Thread = object : Thread() {
        override fun run() {
            handler.sendEmptyMessage(0)
        }
    }
    threadAddToList.start()
}

private fun MainActivity.addToList() {
    Log.d(logTAG, "In addToList(...)")

    binding.deviceListRv.adapter = null

    val shortcut: MutableList<ShortcutInfo> = mutableListOf()

    for ((index, camDevice) in deviceList.withIndex()) {
        try {
            val bundle = Bundle()
            bundle.putString(Constants.KEY_URL_PORT, camDevice.urlPort)
            bundle.putInt(Constants.KEY_MODE, Constants.MODE_CAMERA)

            val webMotionEyeIntent = Intent(
                this, WebMotionEyeActivity::class.java
            )
            webMotionEyeIntent.putExtras(bundle)
            webMotionEyeIntent.action = Intent.ACTION_VIEW

            val shortcutText =
                (if (camDevice.label.length > 1) "${camDevice.label[0]}${camDevice.label[1]}"
                else camDevice.label[0])


            val labelShortcutIcon = TextDrawable(this, shortcutText as CharSequence)

            shortcut.add(
                ShortcutInfo.Builder(this, "$index").setShortLabel(camDevice.label)
                    .setLongLabel("${camDevice.label} - ${camDevice.urlPort}")
                    .setIcon(Icon.createWithBitmap(labelShortcutIcon.toBitmap()))
                    .setIntent(webMotionEyeIntent).build()
            )
        } catch (e: Exception) {
            Log.e(logTAG, "Error while creating shortcuts: $e")
        }
    }

    val camDevicesRvAdapter = CamDeviceRVAdapter(deviceList, this)
    binding.deviceListRv.adapter = camDevicesRvAdapter

    try {
        if (shortcut.isNotEmpty()) {
            shortcutManager?.dynamicShortcuts = shortcut
        }
    } catch (e: Exception) {
        Log.e(logTAG, "Error while adding shortcuts: $e")
    }
}