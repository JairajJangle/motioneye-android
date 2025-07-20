package com.jairaj.janglegmail.motioneye.utils

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.jairaj.janglegmail.motioneye.R

internal class CustomDialogClass constructor(
    activity: Activity,

    private val dialogIconResId: Int?,
    private val dialogTitleText: String?,
    private val dialogMessageText: String?,

    private val btnPositiveText: String?,
    private val btnPositiveOnClick: (() -> Unit)?,

    private val btnNegativeText: String?,
    private val btnNegativeOnClick: (() -> Unit)?,

    private val btnNeutralText: String?,
    private val btnNeutralOnClick: (() -> Unit)?,
) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog)
        window?.setDimAmount(0.3f)
        val btnPositiveResponse = findViewById<Button>(R.id.button_positive)
        val btnNegativeResponse = findViewById<Button>(R.id.button_negative)
        val btnNeutralResponse = findViewById<Button>(R.id.button_neutral)
        val dialogTitle = findViewById<TextView>(R.id.alertTitle)
        val dialogMessage = findViewById<TextView>(R.id.message)
        val dialogIcon = findViewById<ImageView>(R.id.icon)
        val titlePanel = findViewById<LinearLayout>(R.id.topPanel)
        val messagePanel = findViewById<LinearLayout>(R.id.contentPanel)

        if (dialogIconResId != null) {
            dialogIcon.setImageResource(dialogIconResId)
        }

        if (!dialogTitleText.isNullOrEmpty()) {
            dialogTitle.text = dialogTitleText
        } else {
            titlePanel.visibility = View.GONE
        }

        if (!dialogMessageText.isNullOrEmpty()) {
            dialogMessage.text = dialogMessageText
        } else {
            messagePanel.visibility = View.GONE
        }

        setButtonParams(btnPositiveResponse, btnPositiveText, btnPositiveOnClick)
        setButtonParams(btnNegativeResponse, btnNegativeText, btnNegativeOnClick)
        setButtonParams(btnNeutralResponse, btnNeutralText, btnNeutralOnClick)
    }

    private fun setButtonParams(
        button: Button,
        text: String?,
        onClickAction: (() -> Unit)?
    ) {
        if (!text.isNullOrEmpty()) {
            button.text = text

            button.setOnClickListener {
                dismiss()
                if (onClickAction != null) {
                    onClickAction()
                }
            }
        } else {
            button.visibility = View.GONE
        }
    }

}