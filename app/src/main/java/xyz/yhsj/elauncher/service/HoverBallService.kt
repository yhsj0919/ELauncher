package xyz.yhsj.elauncher.service

import android.annotation.TargetApi
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.getSystemService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import xyz.yhsj.elauncher.event.MessageEvent
import xyz.yhsj.elauncher.utils.ActionKey
import xyz.yhsj.elauncher.utils.FileUtils
import xyz.yhsj.elauncher.utils.SpUtil
import xyz.yhsj.elauncher.widget.HoverBallView
import kotlin.concurrent.thread

/**
 *
 * 启动悬浮窗界面
 */
class HoverBallService : Service() {
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    lateinit var mImageReader: ImageReader
    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var mScreenDensity = 0

    private val wm by lazy { this.getSystemService<WindowManager>()!! }

    private val hoverBallView: HoverBallView by lazy { HoverBallView(baseContext) }

    var showBall = false

    lateinit var mediaProjectionManager: MediaProjectionManager


    companion object {
        var resultData: Intent? = null
    }


    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)

        createFloatView()

        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        createImageReader()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        when (event?.action) {
            ActionKey.ACTION_HOVER_BALL -> {
                if (showBall) {
                    wm.removeView(hoverBallView)
                    showBall = false
                } else {
                    createFloatView()
                }
            }
            ActionKey.HOVER_BALL_ALPHA -> {
                val alpha = SpUtil.getInt(this, ActionKey.HOVER_BALL_ALPHA, 100) / 100.0f

                hoverBallView.alpha = alpha
            }
            ActionKey.HOVER_BALL_SIZE -> {
                wm.removeView(hoverBallView)
                createFloatView()
            }
            ActionKey.ACTION_SYSTEM_SCREENSHOT -> {
                startScreenShot()
            }
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createFloatView() {
        val point = Point()
        wm.defaultDisplay.getSize(point)
        val screenWidth = point.x
        val screenHeight = point.y

        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mScreenWidth = metrics.widthPixels
        mScreenHeight = metrics.heightPixels


        val smallWindowParams = WindowManager.LayoutParams()

        val ballSize = SpUtil.getInt(this, ActionKey.HOVER_BALL_SIZE, 50)

        smallWindowParams.type = 2038
        smallWindowParams.format = 1
        smallWindowParams.flags = 40
        smallWindowParams.gravity = 51
        smallWindowParams.width = ballSize
        smallWindowParams.height = ballSize
        smallWindowParams.x = (screenWidth - ballSize - 10)
        smallWindowParams.y = (screenHeight / 2) - 50

        wm.addView(hoverBallView, smallWindowParams)

        val alpha = SpUtil.getInt(this, ActionKey.HOVER_BALL_ALPHA, 100) / 100.0f
        showBall = true
        hoverBallView.alpha = alpha

    }


    private fun startScreenShot() {
        val mHandler = Handler()
        mHandler.postDelayed({ //start virtual
            startVirtual()
        }, 5)
        mHandler.postDelayed({ //capture the screen
            startCapture()
        }, 30)
    }

    private fun createImageReader() {
        mImageReader = ImageReader.newInstance(
            mScreenWidth,
            mScreenHeight,
            PixelFormat.RGBA_8888,
            1
        )
    }

    fun startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay()
        } else {
            setUpMediaProjection()
            virtualDisplay()
        }
    }

    fun setUpMediaProjection() {
        if (resultData == null) {
            sendBroadcast(Intent(ActionKey.ACTION_SYSTEM_SCREENSHOT))
        } else {
            mMediaProjection = mediaProjectionManager.getMediaProjection(
                Activity.RESULT_OK,
                resultData!!
            )
        }
    }


    private fun virtualDisplay() {
        mVirtualDisplay = mMediaProjection?.createVirtualDisplay(
            "screen-mirror",
            mScreenWidth,
            mScreenHeight,
            mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader.surface,
            null,
            null
        )
    }

    private fun startCapture() {
        val image = mImageReader.acquireLatestImage()
        if (image == null) {
            startScreenShot()
        } else {
            thread {

                val width = image.width
                val height = image.height
                val planes = image.planes
                val buffer = planes[0].buffer
                //每个像素的间距
                val pixelStride = planes[0].pixelStride
                //总的间距
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width
                var bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap!!.copyPixelsFromBuffer(buffer)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
                image.close()

                val path = FileUtils.saveImage(bitmap)
                Log.e(">>>>>>>>", "截图地址" + path)
            }
            Toast.makeText(baseContext, "截图成功,请在ELauncher目录查看", Toast.LENGTH_SHORT).show()

        }
    }

    private fun tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    private fun stopVirtual() {
        mVirtualDisplay?.release()
        mVirtualDisplay = null
    }

    override fun onDestroy() {
        // to remove mFloatLayout from windowManager
        super.onDestroy()
        stopVirtual()
        tearDownMediaProjection()
        wm.removeView(hoverBallView)

        EventBus.getDefault().unregister(this);
    }


}