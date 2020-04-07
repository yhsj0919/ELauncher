package xyz.yhsj.elauncher.setting

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_hover_ball.*
import kotlinx.android.synthetic.main.activity_hover_ball.back
import kotlinx.android.synthetic.main.activity_setting.*
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.service.HoverBallService
import xyz.yhsj.elauncher.utils.SpUtil

class HoverBallActivity : AppCompatActivity() {
    private var totalTime: Long = 100
    var handler: Handler? = null
    private val TIME = 1 //message.what字段

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hover_ball)
        //选中状态
        cb_open.isChecked = SpUtil.getBoolean(this, "HoverBall", false)
        handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                if (totalTime > 0) {
                    totalTime -= 1

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(
                            this@HoverBallActivity
                        )
                    ) {
                        startService(Intent(this@HoverBallActivity, HoverBallService::class.java))
                        handler?.removeMessages(TIME)
                    } else {
                        sendEmptyMessageDelayed(TIME, 1000)
                    }
                } else {
                    handler?.removeMessages(TIME)

                }
            }
        }
        back.setOnClickListener { finish() }
        open.setOnClickListener {
            if (!SpUtil.getBoolean(this, "HoverBall", false)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
                    startService(Intent(this@HoverBallActivity, HoverBallService::class.java))
                } else {
                    Toast.makeText(
                        this@HoverBallActivity,
                        "开启权限后，系统将自动显示悬浮窗",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                    handler?.sendEmptyMessage(TIME)
                }
            } else {
                stopService(Intent(this, HoverBallService::class.java))
            }
            //更新存储的信息
            SpUtil.setValue(
                this,
                "HoverBall",
                !SpUtil.getBoolean(this, "HoverBall", false)
            )
            //改变选中状态
            cb_open.isChecked = SpUtil.getBoolean(this, "HoverBall", false)


        }
    }
}
