package xyz.yhsj.elauncher.widget

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import xyz.yhsj.elauncher.R

class HoverBallView(context: Context) : FrameLayout(context), View.OnTouchListener {

    init {
        LayoutInflater.from(context).inflate(R.layout.navigate_button, this)
        setOnTouchListener(this)
    }

    private var distance: Pair<Float, Float>? = null

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val param = layoutParams as WindowManager.LayoutParams
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                distance = (param.x - event.rawX) to (param.y - event.rawY)
            }
            MotionEvent.ACTION_MOVE -> {
                param.x = (event.rawX + distance!!.first).toInt()
                param.y = (event.rawY + distance!!.second).toInt()

                context.getSystemService<WindowManager>()?.updateViewLayout(this, param)
                invalidate()
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    private val gestureDetector = GestureDetector(context, object :
        GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            context.sendBroadcast(Intent("com.mogu.back_key"))
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addCategory(Intent.CATEGORY_HOME)
            context.startActivity(intent)
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            val i = Intent("android.eink.force.refresh")
//            val i = Intent("com.mogu.clear_mem")
            context.sendBroadcast(i)


//            val wifi = Intent("com.moan.closewifi")
//            com.moan.openwifi
//            context.sendBroadcast(wifi)

//            Log.e("测试", "应该所屏啥的")
//            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager;
//
//            //获取wifi开关状态
//            val status = wifiManager.wifiState;
//            if (status == WifiManager.WIFI_STATE_ENABLED) {
//                //wifi打开状态则关闭
//                wifiManager.isWifiEnabled = false;
//                Toast.makeText(context, "wifi已关闭", Toast.LENGTH_SHORT).show();
//            } else {
//                //关闭状态则打开
//                wifiManager.isWifiEnabled = true;
//                Toast.makeText(context, "wifi已打开", Toast.LENGTH_SHORT).show();
//
//            }

        }
    })
}