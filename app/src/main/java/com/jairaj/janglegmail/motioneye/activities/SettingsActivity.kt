package com.jairaj.janglegmail.motioneye.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.utils.AppUtils.askToRate
import com.jairaj.janglegmail.motioneye.utils.AppUtils.sendFeedback
import com.jairaj.janglegmail.motioneye.utils.Constants

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(
            ContextCompat.getColor(
                this,
                R.color.motioneye_dark_grey
            )
        )

        // Handle edge-to-edge insets for the root view
        setupEdgeToEdgeAppBar(findViewById(android.R.id.content))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setBackgroundDrawable(
            ContextCompat.getColor(
                this,
                R.color.motioneye_dark_grey
            ).toDrawable()
        )

        // load settings fragment
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, MainPreferenceFragment()).commit()
    }

    class MainPreferenceFragment : PreferenceFragmentCompat() {
        private val logTAG = MainPreferenceFragment::class.java.name

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.prefs_settings)

            //notification preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_fullscreen)))
            //notification preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_autoopen)))

            // feedback preference click listener
            val feedbackPref = findPreference<Preference>(getString(R.string.key_send_feedback))
            feedbackPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                sendFeedback(requireActivity())
                true
            }

            val ppPref = findPreference<Preference>(getString(R.string.key_pp))
            ppPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.KEY_LEGAL_DOC_TYPE,
                    Constants.LegalDocType.PRIVACY_POLICY
                )
                val i = Intent(activity, LegalDocShowActivity::class.java)
                i.putExtras(bundle)
                startActivity(i)
                true
            }

            val tncPref = findPreference<Preference>(getString(R.string.key_tnc))
            tncPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.KEY_LEGAL_DOC_TYPE,
                    Constants.LegalDocType.TNC
                )
                val i = Intent(activity, LegalDocShowActivity::class.java)
                i.putExtras(bundle)
                startActivity(i)
                true
            }

            // rate me click listener
            val rateMePref = findPreference<Preference>(getString(R.string.key_rate_me))
            rateMePref?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (activity != null)
                    askToRate(activity as Context)
                else
                    Log.e(logTAG, "Settings Activity context is null!")
                true
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private fun bindPreferenceSummaryToValue(preference: Preference?) {
            preference?.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            if (preference != null) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(
                    preference,
                    PreferenceManager
                        .getDefaultSharedPreferences(preference.context)
                        .getBoolean(preference.key, true)
                )
            }
        }

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value. Currently Blank, here for future scope
         */
        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { _, _ -> true }
    }
}