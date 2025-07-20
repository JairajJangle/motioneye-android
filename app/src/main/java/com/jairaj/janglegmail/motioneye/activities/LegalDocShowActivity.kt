package com.jairaj.janglegmail.motioneye.activities

import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.databinding.ActivityLegalDocShowBinding
import com.jairaj.janglegmail.motioneye.utils.Constants

class LegalDocShowActivity : BaseActivity() {
    private lateinit var binding: ActivityLegalDocShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLegalDocShowBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupEdgeToEdgeAppBar(binding.appBarLayoutLegalDoc)

        val bundle = intent.extras
        var htmlLegalDoc = ""
        var title = ""

        //Extract the data…
        if (bundle != null) {
            if (bundle.getSerializable(Constants.KEY_LEGAL_DOC_TYPE) === Constants.LegalDocType.PRIVACY_POLICY) {
                htmlLegalDoc = getString(R.string.privacy_policy)
                title = getString(R.string.title_privacy_policy)
            } else if (bundle.getSerializable(Constants.KEY_LEGAL_DOC_TYPE) === Constants.LegalDocType.TNC) {
                htmlLegalDoc = getString(R.string.tnc)
                title = getString(R.string.title_tnc)
            }
        }

        setSupportActionBar(binding.toolbar)

        supportActionBar?.title = title

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val htmlAsSpanned = Html.fromHtml(htmlLegalDoc, Html.FROM_HTML_MODE_LEGACY)
        binding.textViewPrivacyPolicy.text = htmlAsSpanned
        binding.textViewPrivacyPolicy.movementMethod = ScrollingMovementMethod()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}