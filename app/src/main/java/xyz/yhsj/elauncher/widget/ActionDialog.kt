package xyz.yhsj.elauncher.widget

import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.get
import kotlinx.android.synthetic.main.action_dialog_layout.*
import xyz.yhsj.elauncher.R


class ActionDialog(
    var mContext: Context,
    var title: String,
    var defIndex: Int = 0,
    var listener: RadioGroup.OnCheckedChangeListener

) : Dialog(mContext, R.style.Dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.action_dialog_layout)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        tv_name.text = title

        cancel.setOnClickListener { dismiss() }

        val rb = actionGroup.get(defIndex)

        if (rb is RadioButton) {
            rb.isChecked = true
        }

        actionGroup.setOnCheckedChangeListener { radioGroup, i ->
            listener.onCheckedChanged(radioGroup, i)
            dismiss()
        }

    }
}