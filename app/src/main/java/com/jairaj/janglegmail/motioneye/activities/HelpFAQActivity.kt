package com.jairaj.janglegmail.motioneye.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.databinding.ActivityHelpFaqBinding
import com.jairaj.janglegmail.motioneye.dataclass.QandA
import com.jairaj.janglegmail.motioneye.views_and_adapters.QAndARVAdapter

class HelpFAQActivity : BaseActivity() {
    private lateinit var binding: ActivityHelpFaqBinding

    private val qAndAList: MutableList<QandA> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpFaqBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupEdgeToEdgeAppBar(binding.appBarLayout)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.title = R.string.help_and_faq.toString()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.recyclerviewHFAQ.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerviewHFAQ.layoutManager = llm
        createList()

        val qAndARvAdapter = QAndARVAdapter(qAndAList)
        binding.recyclerviewHFAQ.adapter = qAndARvAdapter
    }

    private fun createList() {
        val res = resources
        val qAndAArray = res.getStringArray(R.array.QandAs)
        var i = 0
        while (i < qAndAArray.size) {
            qAndAList.add(QandA(qAndAArray[i], qAndAArray[i + 1]))
            i += 2
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}