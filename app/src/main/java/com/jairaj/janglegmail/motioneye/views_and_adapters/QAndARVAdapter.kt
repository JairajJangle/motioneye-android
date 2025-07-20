package com.jairaj.janglegmail.motioneye.views_and_adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.dataclass.QandA
import com.jairaj.janglegmail.motioneye.views_and_adapters.QAndARVAdapter.MyViewHolder

class QAndARVAdapter internal constructor(private val QandAList: List<QandA>) :
    RecyclerView.Adapter<MyViewHolder>() {

    /**
     * View holder class
     */
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var questionText: TextView = view.findViewById(R.id.title_q)
        var answerText: TextView = view.findViewById(R.id.subtitle_ans)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        println("Bind [$holder] - Pos [$position]")
        val qa = QandAList[position]
        holder.questionText.text = qa.Question
        holder.answerText.text = qa.Answer
    }

    override fun getItemCount(): Int {
        Log.d("RV", "Item size [" + QandAList.size + "]")
        return QandAList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_list_helpandfaq, parent, false)
        return MyViewHolder(v)
    }

}