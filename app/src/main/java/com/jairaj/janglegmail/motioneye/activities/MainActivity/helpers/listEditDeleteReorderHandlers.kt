package com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers

import android.os.Handler
import android.os.Looper
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity
import com.jairaj.janglegmail.motioneye.views_and_adapters.CamDeviceRVAdapter

/**
 * Toggle list editing/deleting: on/of
 *
 * @param isEditDeleteEnabled true if edit/delete is enabled
 */
internal fun MainActivity.toggleEditDeleteMode(
    isEditDeleteEnabled: Boolean,
    position: Int? = null
) {
    buttonReorderList?.isVisible = !isEditDeleteEnabled
    buttonApplyListOrder?.isVisible = false
    buttonCancelListOrder?.isVisible = false

    setKebabMenuState(!isEditDeleteEnabled)

    setEditDeleteListOptionsState(isEditDeleteEnabled)

    updateToolbarAndFabVisibility(!isEditDeleteEnabled)

    isListViewCheckboxEnabled = !isListViewCheckboxEnabled

    val handler = Handler(Looper.getMainLooper())
    handler.post {
        val adapter = binding.deviceListRv.adapter
        if (adapter is CamDeviceRVAdapter) {
            val items = adapter.getItems()

            for ((index, item) in items.withIndex()) {
                item.reorderHandleVisibility = false
                item.expandCollapseButtonVisibility = true

                item.checkBoxVisibility = isEditDeleteEnabled

                if (index == position)
                    item.checkBoxIsChecked = isEditDeleteEnabled

                adapter.notifyItemChanged(index)
            }
            setLongTouchToReorder(false)
        }
    }
}

/**
 * Toggle list reordering: on/off
 *
 * @param isReorderingEnabled true if long hold to reorder list is enabled
 *                            false if reordering is disabled
 */
internal fun MainActivity.toggleListReorder(
    isReorderingEnabled: Boolean
) {
    this.isReorderingEnabled = isReorderingEnabled

    // Enable long touch to reorder listener if reordering is enabled
    setLongTouchToReorder(isReorderingEnabled)

    // Reorder list app bar icon is visible if reordering is disabled
    buttonReorderList?.isVisible = !isReorderingEnabled

    // Apply and cancel reorder button are visible if reordering is enabled
    buttonApplyListOrder?.isVisible = isReorderingEnabled
    buttonCancelListOrder?.isVisible = isReorderingEnabled

    // Kebab menu icon is visible if reordering is disabled
    setKebabMenuState(!isReorderingEnabled)

    // List edit and delete buttons are kept invisible when reordering is toggled: on/off
    setEditDeleteListOptionsState(false)
    // Also keep the list checkbox disabled when reordering is toggled on/off
    isListViewCheckboxEnabled = false

    // Toolbar title is invisible if reordering is enabled to make enough room for apply/cancel buttons
    updateToolbarAndFabVisibility(!isReorderingEnabled)

    // Run the code in a background thread using a Handler
    val handler = Handler(Looper.getMainLooper())
    handler.post {
        val adapter = binding.deviceListRv.adapter
        if (adapter is CamDeviceRVAdapter) {
            val items = adapter.getItems()

            for ((index, item) in items.withIndex()) {
                item.reorderHandleVisibility = isReorderingEnabled

                if (isReorderingEnabled) {
                    item.expandCollapseButtonVisibility = false
                    item.previewVisibility = false
                }

                adapter.notifyItemChanged(index)
            }
        }
    }
}

internal fun MainActivity.resetActionbarState() {
    // Run the code in a background thread using a Handler
    val handler = Handler(Looper.getMainLooper())
    handler.post {
        val adapter = binding.deviceListRv.adapter
        if (adapter is CamDeviceRVAdapter) {
            val items = adapter.getItems()

            for ((index, item) in items.withIndex()) {
                item.checkBoxVisibility = false
                item.checkBoxIsChecked = false
                item.reorderHandleVisibility = false

                adapter.notifyItemChanged(index)
            }
        }
        toggleEditDeleteMode(false)
        toggleListReorder(false)
    }
}

private fun MainActivity.updateToolbarAndFabVisibility(isVisible: Boolean) {
    if (!isVisible) {
        binding.toolbar.title = ""
        binding.fab.hide()
    } else {
        binding.toolbar.setTitle(R.string.motioneye_servers)
        binding.fab.show()
    }
}

private fun MainActivity.setEditDeleteListOptionsState(isVisible: Boolean) {
    buttonDelete?.isVisible = isVisible
    buttonEdit?.isVisible = isVisible
}

private fun MainActivity.setKebabMenuState(isVisible: Boolean) {
    val actions = listOf(actionAbout, actionHelpFaq, actionSettings)
    actions.forEach { it?.isVisible = isVisible }
}