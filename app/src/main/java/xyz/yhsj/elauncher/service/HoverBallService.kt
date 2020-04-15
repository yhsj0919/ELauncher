package xyz.yhsj.elauncher.service

import android.app.Service
import android.content.Intent
import android.graphics.Point
import android.os.IBinder
import android.view.WindowManager
import androidx.core.content.getSystemService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import xyz.yhsj.elauncher.event.MessageEvent
import xyz.yhsj.elauncher.utils.ActionKey
import xyz.yhsj.elauncher.utils.SpUtil
import xyz.yhsj.elauncher.widget.HoverBallView


class HoverBallService : Service() {

    var showBall = false

    private val wm by lazy { this.getSystemService<WindowManager>()!! }

    private val hoverBallView: HoverBallView by lazy { HoverBallView(baseContext) }

    override fun onBind(intent: Intent): IBinder? = null


    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this);

        createLayoutParam()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        when (event?.action) {
            ActionKey.ACTION_HOVER_BALL -> {
                if (showBall) {
                    wm.removeView(hoverBallView)
                    showBall = false
                } else {
                    createLayoutParam()
                }
            }
            ActionKey.HOVER_BALL_ALPHA -> {
                val alpha = SpUtil.getInt(this, ActionKey.HOVER_BALL_ALPHA, 100) / 100.0f

                hoverBallView?.alpha = alpha
            }
            ActionKey.HOVER_BALL_SIZE -> {
                wm.removeView(hoverBallView)
                createLayoutParam()
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        wm.removeView(hoverBallView)

        EventBus.getDefault().unregister(this);
    }

    private fun createLayoutParam() {

        val point = Point()
        wm.defaultDisplay.getSize(point)
        val screenWidth = point.x
        val screenHeight = point.y
        val smallWindowParams = WindowManager.LayoutParams()

        val ballSize = SpUtil.getInt(this, ActionKey.HOVER_BALL_SIZE, 50)

        smallWindowParams.type = 2038
        smallWindowParams.format = 1
        smallWindowParams.flags = 40
        smallWindowParams.gravity = 51
        smallWindowParams.width = ballSize
        smallWindowParams.height = ballSize
        smallWindowParams.x = (screenWidth - ballSize-10)
        smallWindowParams.y = (screenHeight / 2) - 50

        wm.addView(hoverBallView, smallWindowParams)
        showBall = true

        val alpha = SpUtil.getInt(this, ActionKey.HOVER_BALL_ALPHA, 100) / 100.0f

        hoverBallView.alpha = alpha

//        hoverBallView.alpha=0.1f
    }
}
