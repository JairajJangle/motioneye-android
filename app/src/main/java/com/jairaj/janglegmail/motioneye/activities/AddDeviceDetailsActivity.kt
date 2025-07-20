package com.jairaj.janglegmail.motioneye.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import com.jairaj.janglegmail.motioneye.R
import com.jairaj.janglegmail.motioneye.activities.MainActivity.MainActivity
import com.jairaj.janglegmail.motioneye.databinding.ActivityAddDeviceDetailBinding
import com.jairaj.janglegmail.motioneye.utils.AppUtils.isValidURL
import com.jairaj.janglegmail.motioneye.utils.AppUtils.showKeyboard
import com.jairaj.janglegmail.motioneye.utils.Constants
import com.jairaj.janglegmail.motioneye.utils.Constants.DATA_IS_DRIVE_ADDED
import com.jairaj.janglegmail.motioneye.utils.Constants.DEVICE_ADDITION_CANCELLED_RESULT_CODE
import com.jairaj.janglegmail.motioneye.utils.Constants.DEVICE_ADDITION_DONE_RESULT_CODE
import com.jairaj.janglegmail.motioneye.utils.Constants.EDIT
import com.jairaj.janglegmail.motioneye.utils.Constants.LABEL
import com.jairaj.janglegmail.motioneye.utils.DataBaseHelper

//TODO: Check RTSP support

class AddDeviceDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityAddDeviceDetailBinding

    private lateinit var databaseHelper: DataBaseHelper
    private var editMode = Constants.EDIT_MODE_NEW_DEV
    private var previousLabel: String = ""

    private var canProceed = false
    private lateinit var previousScreen: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupEdgeToEdgeAppBar(binding.appBarLayout)

        databaseHelper = DataBaseHelper(this)

        val bundle = intent.extras
        //Extract the data…
        if (bundle != null) {
            editMode = bundle.getInt(EDIT)
            previousLabel = bundle.getString(LABEL) ?: ""
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        window.decorView.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        previousScreen = Intent(baseContext, MainActivity::class.java)

        if (editMode == Constants.EDIT_MODE_EXIST_DEV) {
            val editUrl = databaseHelper.urlFromLabel(previousLabel)
            val editPort = databaseHelper.portFromLabel(previousLabel)
            val editDriveLink = databaseHelper.driveFromLabel(previousLabel)
            val encryptedCredJSONStr = databaseHelper.credJSONFromLabel(previousLabel)

            binding.urlInput.setText(editUrl)
            binding.portInput.setText(editPort)
            binding.labelInput.setText(previousLabel)
            binding.driveInput.setText(editDriveLink)

            if (encryptedCredJSONStr.isEmpty()) {
                binding.usernameInput.setText("")
                binding.passwordInput.setText("")
            } else {
                val usernamePasswordPair = databaseHelper.getDecryptedCred(encryptedCredJSONStr)
                binding.usernameInput.setText(usernamePasswordPair.first)
                binding.passwordInput.setText(usernamePasswordPair.second)
            }
        }
        binding.buttonSave.setOnClickListener {
            saveToFile()
            if (canProceed) {
                setResult(DEVICE_ADDITION_DONE_RESULT_CODE, previousScreen)
                finish()
            } //0 to add entries
            //1 to make changes on editing
            //2 to cancel edit
        }

        // On pressing Keyboard Done button on password input field
        binding.passwordInput.setOnEditorActionListener(
            OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    binding.buttonSave.performClick()
                    return@OnEditorActionListener true
                }
                false
            })

        binding.passwordInput.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.addDetailsSv.post {
                    binding.addDetailsSv.scrollTo(0, binding.addDetailsSv.bottom)
                }
            }
        }

        binding.urlInput.showKeyboard()
    }

    private fun saveToFile() {
        val urlInputString: String = binding.urlInput.text.toString()
        val portInputString: String = binding.portInput.text.toString()
        val labelInputString: String = binding.labelInput.text.toString()
        val driveLinkInputString: String = binding.driveInput.text.toString()
        val usernameInputString: String = binding.usernameInput.text.toString()
        val passwordInputString: String = binding.passwordInput.text.toString()
        val sortIndex = databaseHelper.sortIndexFromLabel(previousLabel)

        val isValidDriveURL = isValidURL(driveLinkInputString, true)
        val isValidCameraServerURL = isValidURL(urlInputString)
        val isAllValidEntries =
            labelInputString.isNotBlank()
                    && isValidCameraServerURL
                    && isValidDriveURL

        // If all mandatory entries are valid
        if (isAllValidEntries) {
            val isLabelAlreadyPresent = databaseHelper.hasLabel(labelInputString)

            // For EDIT_MODE_NEW_DEV, this condition will anyway be true as prevLabel = ""
            val isLabelChanged = previousLabel != labelInputString

            // Prevent user from adding an already existing label
            if (isLabelAlreadyPresent && isLabelChanged) {
                binding.labelInput.error = getString(R.string.warning_duplicate_label)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    binding.buttonSave.performHapticFeedback(HapticFeedbackConstants.REJECT)
                }

                canProceed = false

                return
            }

            previousScreen.putExtra(DATA_IS_DRIVE_ADDED, isValidDriveURL)

            val encryptedCredJSONStr =
                databaseHelper.getEncryptedCredJSONStr(usernameInputString, passwordInputString)

            val highestSortIndex = databaseHelper.getHighestSortIndex()

            when (editMode) {
                Constants.EDIT_MODE_NEW_DEV -> {
                    val isInserted = databaseHelper.insertData(
                        labelInputString,
                        urlInputString,
                        portInputString,
                        driveLinkInputString,
                        Constants.PREVIEW_ON,
                        highestSortIndex,
                        encryptedCredJSONStr
                    )
                    if (isInserted) {
                        Toast.makeText(
                            baseContext, R.string.toast_added,
                            Toast.LENGTH_SHORT
                        ).show()


                    } else {
                        Toast.makeText(
                            baseContext, R.string.error_try_again,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Constants.EDIT_MODE_EXIST_DEV -> {
                    val isUpdate = databaseHelper.updateData(
                        previousLabel,
                        labelInputString,
                        urlInputString,
                        portInputString,
                        driveLinkInputString,
                        sortIndex,
                        encryptedCredJSONStr
                    )
                    if (!isUpdate) Toast.makeText(
                        this@AddDeviceDetailsActivity,
                        R.string.error_try_delete, Toast.LENGTH_LONG
                    ).show()
                }
            }
            canProceed = true
        }
        // Invalid Camera URL Error
        if (!isValidCameraServerURL) {
            if (editMode != Constants.EDIT_CANCELLED)
                binding.urlInput.error = getString(R.string.warning_invalid_url)

            canProceed = false
        }
        // Empty Camera URL Error
        if (urlInputString == "") {
            if (editMode != Constants.EDIT_CANCELLED)
                binding.urlInput.error = getString(R.string.warning_empty_url)

            canProceed = false
        }
        // Empty Label Error
        if (labelInputString.isBlank()) {
            if (editMode != Constants.EDIT_CANCELLED)
                binding.labelInput.error = getString(R.string.warning_empty_label)

            canProceed = false
        }
        // Invalid Cloud Storage URL Error
        if (!isValidDriveURL) {
            if (editMode != Constants.EDIT_CANCELLED)
                binding.driveInput.error = getString(R.string.invalid_cloud_storage_url_warning)

            canProceed = false
        }
        if (editMode != Constants.EDIT_CANCELLED) {
            if (!canProceed) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    binding.buttonSave.performHapticFeedback(HapticFeedbackConstants.REJECT)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Toast.makeText(
            baseContext, R.string.cancelled_toast,
            Toast.LENGTH_SHORT
        ).show()

        onBackPressed()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        setResult(DEVICE_ADDITION_CANCELLED_RESULT_CODE, previousScreen)
        finish()
    }
}