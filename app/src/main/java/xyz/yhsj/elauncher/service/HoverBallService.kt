package xyz.yhsj.elauncher.service

import android.app.Service
import android.content.Intent
import android.graphics.Point
import android.os.IBinder
import android.view.WindowManager
import androidx.core.content.getSystemService
import xyz.yhsj.elauncher.widget.HoverBallView

class HoverBallService : Service() {

    private val wm by lazy { getSystemService<WindowManager>()!! }

    private val hoverBallView: HoverBallView by lazy { HoverBallView(baseContext) }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createLayoutParam()
    }

    override fun onDestroy() {
        super.onDestroy()
        wm.removeView(hoverBallView)
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
        smallWindowParams.width = 100
        smallWindowParams.height = 100
        smallWindowParams.x = screenWidth
        smallWindowParams.y = (screenHeight / 2) - 50

        wm.addView(hoverBallView, smallWindowParams)
    }
}
