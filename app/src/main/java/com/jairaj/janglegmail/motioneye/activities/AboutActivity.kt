package com.jairaj.janglegmail.motioneye.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.databinding.ActivityAboutBinding
import com.jairaj.janglegmail.motioneye.utils.AppUtils.getVersionName
import com.jairaj.janglegmail.motioneye.utils.AppUtils.openInChrome
import com.jairaj.janglegmail.motioneye.utils.AppUtils.sendFeedback

class AboutActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupEdgeToEdgeAppBar(binding.appBarLayout)

        setSupportActionBar(binding.aboutToolbar)

        init()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun init() {
        window.navigationBarColor = getColor(R.color.motioneye_dark_grey)

        binding.buttonSendFeedback.setOnClickListener(this)
        binding.buttonInstallSteps.setOnClickListener(this)
        binding.buttonBeContributor.setOnClickListener(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val appVersionText = "App Version: ${getVersionName(this)}"
        binding.appVersionText.text = appVersionText

        binding.CreditShowCase.text = Html.fromHtml(
            "<a href= 'https://github.com/sjwall/MaterialTapTargetPrompt'> MaterialTapTargetPrompt</a>",
            Html.FROM_HTML_MODE_LEGACY
        )
        binding.CreditShowCase.movementMethod = LinkMovementMethod.getInstance()
        binding.Apache1.text = Html.fromHtml(
            "<a href= 'https://github.com/sjwall/MaterialTapTargetPrompt/blob/master/LICENSE'> Apache License</a>",
            Html.FROM_HTML_MODE_LEGACY
        )
        binding.Apache1.movementMethod = LinkMovementMethod.getInstance()
        binding.Apache2.text = Html.fromHtml(
            "<a href= 'http://www.apache.org/licenses/LICENSE-2.0.txt'> Apache License</a>",
            Html.FROM_HTML_MODE_LEGACY
        )
        binding.Apache2.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.buttonSendFeedback -> sendFeedback(this@AboutActivity)
            binding.buttonInstallSteps -> {
                val motionEyeSteps =
                    "https://github.com/motioneye-project/motioneye/wiki/Installation"
                openInChrome(motionEyeSteps, this@AboutActivity)
            }
            binding.buttonBeContributor -> {
                val motionEyeSteps =
                    "https://github.com/JairajJangle/motioneye-android"
                openInChrome(motionEyeSteps, this@AboutActivity)
            }
        }
    }
}