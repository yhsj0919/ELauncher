package xyz.yhsj.elauncher.setting

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import xyz.yhsj.elauncher.R


class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        back.setOnClickListener { finish() }

        versionName.text = "版本：${getAppVersionName(this)}"
    }


    /**
     * 获取当前app version name
     */
    fun getAppVersionName(context: Context): String? {
        var appVersionName = ""
        try {
            val packageInfo: PackageInfo = context.getApplicationContext()
                .getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
            appVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("", e.message)
        }
        return appVersionName
    }
}
