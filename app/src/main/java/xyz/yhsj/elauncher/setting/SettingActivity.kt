package xyz.yhsj.elauncher.setting

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_setting.*
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.permission.RxPermission


class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        back.setOnClickListener { finish() }
        hoverBall.setOnClickListener { startActivity(Intent(this, HoverBallActivity::class.java)) }
        autoRun.setOnClickListener { startActivity(Intent(this, AutoRunActivity::class.java)) }
        appList.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AppListSettingActivity::class.java
                )
            )
        }
        appManager.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AppManagerActivity::class.java
                )
            )
        }

        wallpaperSetting.setOnClickListener {
            RxPermission(this)
                .request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .subscribe({
                    startActivity(
                        Intent(
                            this,
                            WallpaperActivity::class.java
                        )
                    )
                }, {
                    Toast.makeText(this, "壁纸功能需要读写权限才能使用", Toast.LENGTH_LONG).show()
                })

        }

        accessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }

        inputMethod.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        }

        locale.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS));
        }

        date.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DATE_SETTINGS));
        }

        deviceAdmin.setOnClickListener {
            val mIntent = Intent()
            val cm = ComponentName(
                "com.android.settings",
                "com.android.settings.DeviceAdminSettings"
            )
            mIntent.component = cm
            mIntent.action = "android.intent.action.VIEW"
            startActivity(mIntent)
        }

        donate.setOnClickListener {
            startActivity(Intent(this, DonateActivity::class.java));
        }
        about.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java));
        }

        unInstall.setOnClickListener {
            val uri = Uri.fromParts("package", this.packageName, null)
            val intent = Intent(Intent.ACTION_DELETE, uri)
            startActivity(intent)
        }

    }
}
