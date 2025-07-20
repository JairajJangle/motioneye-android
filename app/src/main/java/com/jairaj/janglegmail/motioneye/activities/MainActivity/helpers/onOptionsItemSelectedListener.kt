package com.jairaj.janglegmail.motioneye.helpers

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.AboutActivity
import com.jairaj.janglegmail.motioneye.activities.HelpFAQActivity
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.fetchDataAndDisplayList
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.toggleEditDeleteMode
import com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers.toggleListReorder
import com.jairaj.janglegmail.motioneye.activities.MainActivity.utils.itemCheckedCountInDeviceList
import com.jairaj.janglegmail.motioneye.activities.SettingsActivity
import com.jairaj.janglegmail.motioneye.utils.Constants
import com.jairaj.janglegmail.motioneye.utils.CustomDialogClass

internal fun MainActivity.onOptionsItemSelectedListener(item: MenuItem) {
    when (item.itemId) {
        R.id.delete -> handleDeleteAction()
        R.id.edit -> handleEditAction()
        R.id.action_about -> startActivity(this, AboutActivity::class.java)
        R.id.action_help -> startActivity(this, HelpFAQActivity::class.java)
        R.id.action_settings -> startActivity(this, SettingsActivity::class.java)
        R.id.reorder_list -> toggleListReorder(true)
        R.id.apply_list_order -> applyListOrder()
        R.id.cancel_list_order -> cancelListOrder()
    }
}

private fun MainActivity.applyListOrder() {
    binding.deviceListRv.children.forEachIndexed { index, view ->
        val label =
            (view.findViewById<View>(R.id.title_label_text) as TextView).text.toString()

        dataBaseHelper.updateSortIndexForLabel(label, index)
    }
    toggleListReorder(false)
    fetchDataAndDisplayList()
}

private fun MainActivity.cancelListOrder() {
    toggleListReorder(false)
    fetchDataAndDisplayList()
}

/**
 * Handles the action performed when the delete option is selected in the menu.
 */
private fun MainActivity.handleDeleteAction() {

    /**
     * Deletes the selected server entries.
     */
    fun deleteServerEntry() {
        // Perform haptic feedback on devices with API level >= 30
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.toolbar.performHapticFeedback(HapticFeedbackConstants.REJECT)
        }

        // Check if checkboxes are enabled and at least one checkbox is checked
        if (itemCheckedCountInDeviceList > 0 && isListViewCheckboxEnabled) {
            // Loop through all children (device entries) in the RecyclerView
            for (deviceView in binding.deviceListRv.children) {
                val checkbox: CheckBox = deviceView.findViewById(R.id.checkBox)
                // Check if the checkbox is checked
                if (checkbox.isChecked) {
                    // Get the label of the checked entry
                    val delLabel =
                        (deviceView.findViewById<View>(R.id.title_label_text) as TextView).text.toString()
                    // Delete the entry
                    deleteData(delLabel)
                    // Uncheck the checkbox
                    checkbox.isChecked = false
                }
            }

            // Re-fetch the data and disable the edit and delete actionbar elements
            fetchDataAndDisplayList(true)
            toggleEditDeleteMode(false)
        }
    }

    // Show a confirmation dialog before deleting the entries
    val cdd = CustomDialogClass(
        this,
        null,
        getString(R.string.delete_confirm_title),
        getString(R.string.delete_confirm_message),
        getString(R.string.yes),
        ::deleteServerEntry,
        getString(R.string.no),
        null,
        null,
        null
    )
    cdd.setCancelable(true)
    cdd.show()
}

/**
 * Starts an activity of the specified class.
 *
 * @param context The context to use for starting the activity.
 * @param clazz The class of the activity to start.
 */
private fun startActivity(context: Context, clazz: Class<*>) {
    val intent = Intent(context, clazz)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

/**
 * Handles the edit action for a device in the list.
 *
 * If the checkbox for the device is enabled, checks the number of items selected. If only one is selected,
 * navigates to the device details page in edit mode. If multiple items are selected, displays a Toast message
 * indicating that only one item can be selected for edit.
 */
private fun MainActivity.handleEditAction() {
    if (isListViewCheckboxEnabled) {
        val f = itemCheckedCountInDeviceList
        if (f > 1) {
            Toast.makeText(
                this, "Select only one entry to edit", Toast.LENGTH_SHORT
            ).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                binding.toolbar.performHapticFeedback(HapticFeedbackConstants.REJECT)
            }
        } else gotoAddDeviceDetail(Constants.EDIT_MODE_EXIST_DEV)
    }
}