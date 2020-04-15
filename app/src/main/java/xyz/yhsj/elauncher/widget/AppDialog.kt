package xyz.yhsj.elauncher.widget

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.app_dialog_layout.*
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.utils.ActionKey
import xyz.yhsj.elauncher.utils.SpUtil
import java.text.SimpleDateFormat


class AppDialog(
    var mContext: Context,
    var appName: String,
    var appPackage: String,
    var appVersion: String,
    var appTime: Long,
    var appIcon: Drawable
) : Dialog(mContext, R.style.Dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_dialog_layout)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        tv_name.text = appName
        tv_package.text = "包名 $appPackage"
        tv_version.text = "版本 $appVersion"
        tv_time.text = "更新 ${SimpleDateFormat("yyyy-MM-dd HH:mm").format(appTime)}"
        icon.setImageDrawable(appIcon)
        close.setOnClickListener { dismiss() }
        unInstall.setOnClickListener {
            val uri = Uri.fromParts("package", appPackage, null)
            val intent = Intent(Intent.ACTION_DELETE, uri)
            mContext.startActivity(intent)

            dismiss()
        }
        hide.setOnClickListener {
            SpUtil.setValue(mContext, appPackage, true)
            //隐藏app
            val i = Intent(ActionKey.ACTION_PACKAGE_HIDE)
            i.data = Uri.parse("package:$appPackage")
            mContext.sendBroadcast(i)
            dismiss()
        }

        info.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.parse("package:$appPackage")
            mContext.startActivity(intent)

            dismiss()
        }
    }
}