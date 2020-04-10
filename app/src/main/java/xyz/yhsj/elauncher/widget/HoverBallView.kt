package xyz.yhsj.elauncher.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import xyz.yhsj.elauncher.R
import xyz.yhsj.elauncher.utils.ActionKey
import xyz.yhsj.elauncher.utils.SpUtil

class HoverBallView(context: Context) : FrameLayout(context), View.OnTouchListener {

    init {
        LayoutInflater.from(context).inflate(R.layout.navigate_button, this)
        setOnTouchListener(this)
    }

    private var distance: Pair<Float, Float>? = null

    //用于屏蔽滑动多次触发，提高准确率
    private var actionLock: Boolean = false
    private var actionMove: Boolean = false

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val param = layoutParams as WindowManager.LayoutParams
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                distance = (param.x - event.rawX) to (param.y - event.rawY)
            }
            MotionEvent.ACTION_MOVE -> {
                if (actionMove) {
                    param.x = (event.rawX + distance!!.first).toInt()
                    param.y = (event.rawY + distance!!.second).toInt()
                    context.getSystemService<WindowManager>()?.updateViewLayout(this, param)
                }
                invalidate()
            }
        }

        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                actionLock = false
                actionMove = false
            }
        }


        return gestureDetector.onTouchEvent(event)
    }

    private val gestureDetector = GestureDetector(context, object :
        GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            execAction(ActionKey.HOVER_BALL_CLICK)
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            execAction(ActionKey.HOVER_BALL_DOUBLE_CLICK)
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            actionMove = true

            execAction(ActionKey.HOVER_BALL_LONG_CLICK)
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (actionLock) {
                return true
            }
            actionLock = true

            if (e1.y - e2.y > Math.abs(e1.x - e2.x)) {
                //上
                execAction(ActionKey.HOVER_BALL_UP)
                return true
            }

            if (e2.y - e1.y > Math.abs(e1.x - e2.x)) {
                //下
                execAction(ActionKey.HOVER_BALL_DOWN)
                return true
            }
            if (e1.x - e2.x > Math.abs(e2.y - e1.y)) {
                //左
                execAction(ActionKey.HOVER_BALL_LEFT)
                return true
            }
            if (e2.x - e1.x > Math.abs(e2.y - e1.y)) {
                //右
                execAction(ActionKey.HOVER_BALL_RIGHT)
                return true
            }

            return true
        }
    })

    /**
     * 执行操作
     */
    fun execAction(actionName: String) {
        val actionType = SpUtil.getInt(context, actionName, 0)

        when (actionType) {
            0 -> {

            }
            1 -> {
                context.sendBroadcast(Intent("com.mogu.back_key"))
            }
            2 -> {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addCategory(Intent.CATEGORY_HOME)
                context.startActivity(intent)
            }
            3 -> {
                context.sendBroadcast(Intent("android.eink.force.refresh"))
            }
            4 -> {
                context.sendBroadcast(Intent("com.mogu.clear_mem"))
            }


        }

    }


}