package xyz.yhsj.elauncher.setting

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_setting.*
import xyz.yhsj.elauncher.R


class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        back.setOnClickListener { finish() }
        hoverBall.setOnClickListener { startActivity(Intent(this, HoverBallActivity::class.java)) }
        autoRun.setOnClickListener { startActivity(Intent(this, AutoRunActivity::class.java)) }
        appList.setOnClickListener { Toast.makeText(this, "暂无功能", Toast.LENGTH_SHORT).show() }
        appManager.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AppManagerActivity::class.java
                )
            )
        }

        accessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
        deviceAdmin.setOnClickListener {
            val cm = ComponentName(
                "com.android.settings",
                "com.android.settings.DeviceAdminSettings"
            )
            intent.component = cm
            intent.action = "android.intent.action.VIEW"
            startActivity(intent)
        }

        about.setOnClickListener {
            Toast.makeText(this, "很简单的小桌面\n有什么问题的话\n可以在群里 @永恒瞬间", Toast.LENGTH_LONG).show()
        }

    }
}
