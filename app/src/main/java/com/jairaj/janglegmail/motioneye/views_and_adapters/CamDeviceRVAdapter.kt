package com.jairaj.janglegmail.motioneye.views_and_adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.HttpAuthHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.toggleEditDeleteMode
import com.jairaj.janglegmail.motioneye.activities.MainActivity.utils.itemCheckedCountInDeviceList
import com.jairaj.janglegmail.motioneye.dataclass.CamDevice
import com.jairaj.janglegmail.motioneye.utils.AppUtils
import com.jairaj.janglegmail.motioneye.utils.Constants
import com.jairaj.janglegmail.motioneye.views_and_adapters.CamDeviceRVAdapter.MyViewHolder
import java.util.*

class CamDeviceRVAdapter internal constructor(
    private val camDeviceList: List<CamDevice>,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelText: TextView = view.findViewById(R.id.title_label_text)
        val urlPortText: TextView = view.findViewById(R.id.subtitle_url_port_text)
        val previewView: WebView = view.findViewById(R.id.preview_webview)
        val prevTouch: View = view.findViewById(R.id.prev_touch_overlay)
        val expandButton: ImageView = view.findViewById(R.id.expand_button)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val progressBar: ProgressBar = view.findViewById(R.id.preview_progressBar)
        val reorderHandle: ImageView = view.findViewById(R.id.reorderHandle)

        val driveButton: ImageButton = view.findViewById(R.id.button_drive)
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(camDeviceList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getItems(): List<CamDevice> {
        return camDeviceList
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        println("Bind [$holder] - Pos [$position]")
        val camDevice = camDeviceList[position]
        holder.labelText.text = camDevice.label
        holder.urlPortText.text = camDevice.urlPort

        if (camDevice.driveLink == "") {
            holder.driveButton.visibility = View.GONE
        } else {
            holder.driveButton.visibility = View.VISIBLE
            mainActivity.tutorialTargetDriveIcon = holder.driveButton
        }

        handlePreviewView(holder, camDevice, true)

        holder.itemView.setOnClickListener {
            mainActivity.camViewClickListener(camDevice, holder.itemView)
        }

        holder.itemView.setOnLongClickListener {
            mainActivity.camViewAddOnLongClickListener(position)
            true
        }

        holder.prevTouch.setOnClickListener {
            mainActivity.onPreviewClick(holder.itemView, camDevice)
        }

        holder.prevTouch.setOnLongClickListener {
            mainActivity.camViewAddOnLongClickListener(position)
            true
        }

        holder.expandButton.setOnClickListener {
            handlePreviewView(holder, camDevice, false)
            holder.expandButton.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }

        holder.driveButton.setOnClickListener {
            mainActivity.goToWebMotionEye(
                camDevice.label,
                camDevice.driveLink,
                Constants.MODE_DRIVE
            )
        }

        // Toggle visibilities of elements
        when (camDevice.reorderHandleVisibility) {
            true -> holder.reorderHandle.visibility = View.VISIBLE
            false -> holder.reorderHandle.visibility = View.GONE
            else -> {
                // Nothing
            }
        }

        when (camDevice.expandCollapseButtonVisibility) {
            true -> holder.expandButton.visibility = View.VISIBLE
            false -> holder.expandButton.visibility = View.GONE
            else -> {
                // Nothing
            }
        }

        when (camDevice.previewVisibility) {
            true -> {}
            false -> handlePreviewView(
                holder,
                camDevice,
                checkAll = false,
                forceCollapse = true,
                saveToDB = false
            )
            else -> {
                // Nothing
            }
        }

        when (camDevice.checkBoxVisibility) {
            true -> holder.checkBox.isVisible = true
            false -> holder.checkBox.isVisible = false
            else -> {
                // Nothing
            }
        }
        when (camDevice.checkBoxIsChecked) {
            true -> holder.checkBox.isChecked = true
            false -> holder.checkBox.isChecked = false
            else -> {
                // Nothing
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("RV", "Item size [" + camDeviceList.size + "]")
        return camDeviceList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.custom_list_item,
            parent, false
        )
        return MyViewHolder(v)
    }

    private fun MainActivity.camViewClickListener(camDevice: CamDevice, view: View) {
        if (!isListViewCheckboxEnabled) {
            goToWebMotionEye(camDevice.label, camDevice.urlPort, Constants.MODE_CAMERA)
            return
        }
        // else
        handleListCheckedState(view)
    }

    private fun MainActivity.camViewAddOnLongClickListener(position: Int) {
        if (isReorderingEnabled) {
            return
        }

        toggleEditDeleteMode(!isListViewCheckboxEnabled, position)
    }

    private fun MainActivity.onPreviewClick(view: View, camDevice: CamDevice) {
        Log.d(logTAG, "onPreviewClick called")

        if (!isListViewCheckboxEnabled) {
            val label = camDevice.label
            val urlPort = camDevice.urlPort
            goToWebMotionEye(label, urlPort, Constants.MODE_CAMERA)

            return
        }

        handleListCheckedState(view)
    }

    private fun MainActivity.handleListCheckedState(view: View) {
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        checkBox.isChecked = !checkBox.isChecked

        val numberOfCheckedItems = itemCheckedCountInDeviceList

        if (numberOfCheckedItems == 0) {
            val adapter = binding.deviceListRv.adapter
            if (adapter is CamDeviceRVAdapter) {
                val items = adapter.getItems()

                for ((index, item) in items.withIndex()) {
                    item.checkBoxVisibility = false

                    adapter.notifyItemChanged(index)
                }
            }
            toggleEditDeleteMode(false)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    internal fun handlePreviewView(
        holder: MyViewHolder,
        camDevice: CamDevice,
        checkAll: Boolean,
        forceCollapse: Boolean = false,
        saveToDB: Boolean = true
    ) {
        val label = camDevice.label

        var visibilityState = false
        if (checkAll && saveToDB)
            visibilityState =
                mainActivity.dataBaseHelper.prevStatFromLabel(label) != Constants.PREVIEW_OFF
        else {
            if (holder.previewView.visibility == View.GONE)
                visibilityState = true
        }

        if (forceCollapse) {
            Log.d(mainActivity.logTAG, "Force Collapse = True")
            visibilityState = false
        }

        if (visibilityState) {
            holder.expandButton.setImageResource(R.drawable.collapse_button)

            holder.previewView.visibility = View.VISIBLE
            (holder.previewView.parent as ConstraintLayout).setPadding(
                0,
                0,
                0,
                Constants.PREVIEW_PADDING
            )

            holder.previewView.settings.javaScriptEnabled = true
            holder.previewView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            holder.previewView.webViewClient = WebViewClient()
            holder.previewView.settings.javaScriptCanOpenWindowsAutomatically = true
            holder.previewView.settings.useWideViewPort = true
            holder.previewView.settings.loadWithOverviewMode = true
            holder.previewView.loadUrl(camDevice.urlPort)

            val liveStream = AppUtils.checkWhetherStream(camDevice.urlPort)
            holder.previewView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, progress: Int) {
                    if (view.url != "about:blank") {
                        holder.progressBar.progress = progress
                        if (progress == 100 || progress >= 30 && liveStream) {
                            holder.progressBar.visibility = View.GONE
                        } else {
                            holder.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            }

            var basicAuthTryCounter = 0

            holder.previewView.webViewClient = object : WebViewClient() {
                // TODO: Hide header and footer in streaming preview
//                override fun onLoadResource(view: WebView?, url: String?) {
//                    view?.loadUrl(
//                        "javascript:(function() { " +
//                                "document.getElementsByClassName('header')[0].style.position='absolute'; " +
//                                "document.getElementsByClassName('header')[0].style.top=-9999px; " +
//                                "document.getElementsByClassName('header')[0].style.left=-9999px; " +
//                                "})()"
//                    )
//                    super.onLoadResource(view, url)
//                }

                // Inject Javascript to load credentials and press Login Button
                override fun onPageFinished(view: WebView, url: String) {
                    AppUtils.handleMotionEyeUILogin(
                        mainActivity.dataBaseHelper,
                        label,
                        view
                    )
                }

                override fun onReceivedHttpAuthRequest(
                    view: WebView,
                    handler: HttpAuthHandler, host: String, realm: String
                ) {
                    if (basicAuthTryCounter > 2) {
                        Toast.makeText(
                            mainActivity,
                            "Incorrect Username/Password for $label",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    AppUtils.handleHttpBasicAuthentication(
                        mainActivity.dataBaseHelper,
                        label,
                        handler
                    )
                    basicAuthTryCounter++
                }
            }
            val isUpdate =
                mainActivity.dataBaseHelper.updatePrevStat(label, Constants.PREVIEW_ON)

            if (!isUpdate)
                Toast.makeText(
                    mainActivity,
                    R.string.error_try_delete,
                    Toast.LENGTH_LONG
                )
                    .show()
        } else {
            if (saveToDB) {
                val isUpdate =
                    mainActivity.dataBaseHelper.updatePrevStat(label, Constants.PREVIEW_OFF)

                if (!isUpdate)
                    Toast.makeText(
                        mainActivity,
                        R.string.error_try_delete,
                        Toast.LENGTH_LONG
                    )
                        .show()
            }

            (holder.previewView.parent as ConstraintLayout).setPadding(0, 0, 0, 0)
            holder.expandButton.setImageResource(R.drawable.expand_down)
            holder.previewView.loadUrl("about:blank")
            holder.previewView.visibility = View.GONE
            holder.progressBar.visibility = View.GONE
        }
    }
}