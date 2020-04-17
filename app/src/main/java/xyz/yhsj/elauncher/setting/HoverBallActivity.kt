package xyz.yhsj.elauncher.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.mohammedalaa.seekbar.OnRangeSeekBarChangeListener
import com.mohammedalaa.seekbar.RangeSeekBarView
import kotlinx.android.synthetic.main.activity_hover_ball.*
import org.greenrobot.eventbus.EventBus
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.event.MessageEvent
import xyz.yhsj.elauncher.service.HoverBallService
import xyz.yhsj.elauncher.utils.ActionKey
import xyz.yhsj.elauncher.utils.FileUtils
import xyz.yhsj.elauncher.utils.SpUtil
import xyz.yhsj.elauncher.widget.ActionDialog
import java.io.File

class HoverBallActivity : AppCompatActivity() {

    val IMAGE_PICK = 2654

    private var totalTime: Long = 100
    var handler: Handler? = null
    private val TIME = 1 //message.what字段

    //用于自定义选择图标
    private var selectIcon = 0


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
        "悬浮球自杀",
        "原生应用列表",
        "截图"
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
        val circleDrawable = resources.getDrawable(R.mipmap.icon_circle);
        circleDrawable.setBounds(0, 0, 40, 40);//必须设置图片的大小否则没有作用
        circleIcon.setCompoundDrawables(
            null,
            null,
            circleDrawable,
            null
        )
        val flowerDrawable = resources.getDrawable(R.mipmap.icon_flower);
        flowerDrawable.setBounds(0, 0, 40, 40);//必须设置图片的大小否则没有作用
        flowerIcon.setCompoundDrawables(
            null,
            null,
            flowerDrawable,
            null
        )

        //展示自定义图标
        val userIconPath = SpUtil.getString(this, ActionKey.HOVER_BALL_ICON_PATH, "")
        setUserDrawable(userIconPath)

        userIcon.setOnClickListener {
            if (userIcon.isChecked && selectIcon > 1) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_PICK)
            }
            selectIcon++
        }


        val ballIconIndex = SpUtil.getInt(this, ActionKey.HOVER_BALL_ICON_INDEX, 0)
        //默认选中第三个，就允许点击选择图片
        if (ballIconIndex == 2) {
            selectIcon = 2
        }

        val rb = iconGroup.get(ballIconIndex)

        if (rb is RadioButton) {
            rb.isChecked = true
        }

        iconGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.circleIcon -> {
                    SpUtil.setValue(this, ActionKey.HOVER_BALL_ICON_INDEX, 0)
                    selectIcon = 0
                    EventBus.getDefault().post(MessageEvent(ActionKey.HOVER_BALL_ICON_INDEX))
                }

                R.id.flowerIcon -> {
                    SpUtil.setValue(this, ActionKey.HOVER_BALL_ICON_INDEX, 1)
                    selectIcon = 0
                    EventBus.getDefault().post(MessageEvent(ActionKey.HOVER_BALL_ICON_INDEX))
                }

                R.id.userIcon -> {
                    SpUtil.setValue(this, ActionKey.HOVER_BALL_ICON_INDEX, 2)
                    selectIcon++
                    EventBus.getDefault().post(MessageEvent(ActionKey.HOVER_BALL_ICON_INDEX))
                }
            }
        }


        //透明度
        seekbar.setCurrentValue(SpUtil.getInt(this, ActionKey.HOVER_BALL_ALPHA, 100))
        seekbar.setOnRangeSeekBarViewChangeListener(object : OnRangeSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: RangeSeekBarView?,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: RangeSeekBarView?) {
            }

            override fun onStopTrackingTouch(seekBar: RangeSeekBarView?) {
                SpUtil.setValue(
                    this@HoverBallActivity,
                    ActionKey.HOVER_BALL_ALPHA,
                    seekBar?.getCurrentValue() ?: 100
                )
                EventBus.getDefault().post(MessageEvent(ActionKey.HOVER_BALL_ALPHA))
            }
        })

        //大小
        ballSize.setCurrentValue(SpUtil.getInt(this, ActionKey.HOVER_BALL_SIZE, 50))
        ballSize.setOnRangeSeekBarViewChangeListener(object : OnRangeSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: RangeSeekBarView?,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: RangeSeekBarView?) {

            }

            override fun onStopTrackingTouch(seekBar: RangeSeekBarView?) {
                SpUtil.setValue(
                    this@HoverBallActivity,
                    ActionKey.HOVER_BALL_SIZE,
                    seekBar?.getCurrentValue() ?: 50
                )
                EventBus.getDefault().post(MessageEvent(ActionKey.HOVER_BALL_SIZE))
            }
        })


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
                    R.id.wifi -> 5
                    R.id.kill -> 6
                    R.id.appList -> 7
                    R.id.screenshot -> 8
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

    /**
     * 设置自定义图标
     */
    private fun setUserDrawable(path: String?) {
        val file = File(path ?: "")
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(path)
            val userDrawable = BitmapDrawable(bitmap)
            userDrawable.setBounds(0, 0, 40, 40);//必须设置图片的大小否则没有作用
            userIcon.setCompoundDrawables(
                null,
                null,
                userDrawable,
                null
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val uri = data.data!!
                val imgPath = FileUtils.getPath(this, uri)
                setUserDrawable(imgPath)
                SpUtil.setValue(this, ActionKey.HOVER_BALL_ICON_PATH, imgPath ?: "")
                EventBus.getDefault().post(MessageEvent(ActionKey.HOVER_BALL_ICON_INDEX))
            }
        }
    }
}
