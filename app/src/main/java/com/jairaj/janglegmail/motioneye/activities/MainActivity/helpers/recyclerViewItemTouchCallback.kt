package com.jairaj.janglegmail.motioneye.activities.MainActivity.helpers

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.jairaj.janglegmail.motioneye.databinding.ActivityMainBinding
import com.jairaj.janglegmail.motioneye.views_and_adapters.CamDeviceRVAdapter

class RecyclerViewItemTouchHelper(private val binding: ActivityMainBinding) :
    ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        (binding.deviceListRv.adapter as CamDeviceRVAdapter).onItemMove(
            viewHolder.bindingAdapterPosition,
            target.bindingAdapterPosition
        )
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not used in this example
    }
}