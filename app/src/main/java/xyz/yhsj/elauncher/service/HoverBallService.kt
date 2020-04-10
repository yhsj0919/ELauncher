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

        smallWindowParams.type = 2038
        smallWindowParams.format = 1
        smallWindowParams.flags = 40
        smallWindowParams.gravity = 51
        smallWindowParams.width = 60
        smallWindowParams.height = 60
        smallWindowParams.x = screenWidth
        smallWindowParams.y = (screenHeight / 2) - 50

        wm.addView(hoverBallView, smallWindowParams)
        showBall = true
//        hoverBallView.alpha=0.1f
    }
}
