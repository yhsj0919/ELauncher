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
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_hover_ball.*
import kotlinx.android.synthetic.main.activity_hover_ball.back
import kotlinx.android.synthetic.main.activity_setting.*
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.service.HoverBallService
import xyz.yhsj.elauncher.utils.ActionKey
import xyz.yhsj.elauncher.utils.SpUtil
import xyz.yhsj.elauncher.widget.ActionDialog

class HoverBallActivity : AppCompatActivity() {
    private var totalTime: Long = 100
    var handler: Handler? = null
    private val TIME = 1 //message.what字段


    val actionTitles = arrayListOf("单击", "双击", "长按", "上滑", "下滑", "左滑", "右滑")
    val spAction = arrayListOf(
        ActionKey.HOVER_BALL_CLICK,
        ActionKey.HOVER_BALL_DOUBLE_CLICK,
        ActionKey.HOVER_BALL_LONG_CLICK,
        ActionKey.HOVER_BALL_UP,
        ActionKey.HOVER_BALL_DOWN,
        ActionKey.HOVER_BALL_LEFT,
        ActionKey.HOVER_BALL_RIGHT
    )

    val actionName = arrayListOf(
        "无",
        "返回上一级",
        "返回主页",
        "全局刷新",
        "清理后台",
        "wifi开关",
        "悬浮球自杀"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hover_ball)
        //选中状态
        cb_open.isChecked = SpUtil.getBoolean(this, ActionKey.HOVER_BALL, false)
        //这里用来授权后自动启动悬浮球
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
            if (!SpUtil.getBoolean(this, ActionKey.HOVER_BALL, false)) {
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
                ActionKey.HOVER_BALL,
                !SpUtil.getBoolean(this, ActionKey.HOVER_BALL, false)
            )
            //改变选中状态
            cb_open.isChecked = SpUtil.getBoolean(this, ActionKey.HOVER_BALL, false)
        }

        lay_click.setOnClickListener {
            changeAction(0)
        }
        lay_double.setOnClickListener {
            changeAction(1)
        }
        lay_long.setOnClickListener {
            changeAction(2)
        }
        lay_up.setOnClickListener {
            changeAction(3)
        }
        lay_down.setOnClickListener {
            changeAction(4)
        }
        lay_left.setOnClickListener {
            changeAction(5)
        }
        lay_right.setOnClickListener {
            changeAction(6)
        }

        setActionName()
    }

    /**
     * 更改选中状态
     */
    fun changeAction(actionType: Int) {
        val defIndex = SpUtil.getInt(this, spAction[actionType], 0)
        ActionDialog(
            this,
            actionTitles[actionType],
            defIndex,
            RadioGroup.OnCheckedChangeListener { _, i ->
                val checkType = when (i) {
                    R.id.none -> 0
                    R.id.back -> 1
                    R.id.home -> 2
                    R.id.refresh -> 3
                    R.id.clear -> 4
                    R.id.kill -> 5
                    R.id.wifi -> 6
                    else -> 0
                }
                SpUtil.setValue(this, spAction[actionType], checkType)

                setActionName()
            }).show()
    }

    /**
     * 设置控件选中的名称
     */
    fun setActionName() {
        tv_click.text = actionName[SpUtil.getInt(this, spAction[0], 0)]
        tv_double.text = actionName[SpUtil.getInt(this, spAction[1], 0)]
        tv_long.text = actionName[SpUtil.getInt(this, spAction[2], 0)]
        tv_up.text = actionName[SpUtil.getInt(this, spAction[3], 0)]
        tv_down.text = actionName[SpUtil.getInt(this, spAction[4], 0)]
        tv_left.text = actionName[SpUtil.getInt(this, spAction[5], 0)]
        tv_right.text = actionName[SpUtil.getInt(this, spAction[6], 0)]
    }
}
