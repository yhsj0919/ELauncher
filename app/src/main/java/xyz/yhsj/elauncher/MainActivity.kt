package xyz.yhsj.elauncher

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import xyz.yhsj.elauncher.adapter.AppListAdapter
import xyz.yhsj.elauncher.bean.AppInfo
import xyz.yhsj.elauncher.event.MessageEvent
import xyz.yhsj.elauncher.permission.RxPermission
import xyz.yhsj.elauncher.service.HoverBallService
import xyz.yhsj.elauncher.setting.SettingActivity
import xyz.yhsj.elauncher.utils.*
import xyz.yhsj.elauncher.widget.AppDialog
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    lateinit var appListAdapter: AppListAdapter

    lateinit var appChangeReceiver: AppChangeReceiver
    lateinit var notificationReceiver: NotificationReceiver

    lateinit var wifiManager: WifiManager

    lateinit var mNM: NotificationManager

    lateinit var layoutManager: GridLayoutManager

    lateinit var listGesture: GestureDetector
    lateinit var bGgesture: GestureDetector

    val REQUEST_MEDIA_PROJECTION = 4656


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //获取wifi管理服务
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mNM = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notifications.createNotification(this, mNM)
        }

        //应用自启动
        if (SpUtil.getBoolean(this, ActionKey.AUTO_RUN, false)) {
            val packageName = SpUtil.getString(this, ActionKey.AUTO_RUN_PACKAGE, "")!!
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent);
            }
        }
        //开启悬浮球
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this))
            && SpUtil.getBoolean(this, ActionKey.HOVER_BALL, false)
        ) {
            startService(Intent(this, HoverBallService::class.java))
        }
        //监听app安装，卸载，更新
        val appFilter = IntentFilter()
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        appFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        appFilter.addAction(ActionKey.ACTION_PACKAGE_HIDE)
        appFilter.addAction(ActionKey.ACTION_APP_LIST_CHANGE)
        appFilter.addDataScheme("package")

        appChangeReceiver = AppChangeReceiver()
        registerReceiver(appChangeReceiver, appFilter)

        //监听通知事件
        val notificationFilter = IntentFilter()
        notificationFilter.addAction(ActionKey.ACTION_HOVER_BALL)
        //wifi开关
        notificationFilter.addAction(ActionKey.ACTION_WIFI_STATUS)
        notificationFilter.addAction(ActionKey.ACTION_SYSTEM_SCREENSHOT)
        notificationFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED)
        notificationReceiver = NotificationReceiver()
        registerReceiver(notificationReceiver, notificationFilter)

        //初始化应用列表
        appListAdapter = AppListAdapter(appList)
        layoutManager = GridLayoutManager(this, SpUtil.getInt(this, ActionKey.APP_LIST_COLUMN, 5))
        layoutManager.reverseLayout = SpUtil.getBoolean(this, ActionKey.APP_LIST_ARRANGE, true)
        appList.layoutManager = layoutManager

        appList.adapter = appListAdapter

        appListAdapter.setOnItemClickListener { _, _, position ->
            val appInfo = appListAdapter.data[position]
            if (appInfo.isApp) {
                val intent = packageManager.getLaunchIntentForPackage(
                    appInfo.packageName
                )
                if (intent != null) {
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "启动失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "未安装", Toast.LENGTH_SHORT).show()
                }
            } else {

                when (appInfo.packageName) {
                    "setting" -> {
                        startActivity(Intent(this, SettingActivity::class.java))
                    }
                    "clear" -> {
                        sendBroadcast(Intent("com.mogu.clear_mem"))
                    }

                    "wifi" -> {
                        sendBroadcast(Intent(ActionKey.ACTION_WIFI_STATUS))
                    }
                }
            }
        }
        appListAdapter.setOnItemLongClickListener { _, _, position ->
            val appInfo = appListAdapter.data[position]

            if (appInfo.isApp) {
                AppDialog(
                    this,
                    appName = appInfo.name,
                    appIcon = appInfo.icon!!,
                    appPackage = appInfo.packageName,
                    appVersion = appInfo.version,
                    appTime = appInfo.updateTime
                ).show()
            }
            return@setOnItemLongClickListener true
        }


        //手势处理
        listGesture = GestureDetector(this, object :
            GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                appList.visibility = View.GONE
                return true
            }
        })

        bGgesture = GestureDetector(this, object :
            GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                appList.visibility = View.VISIBLE
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (e1.y - e2.y > Math.abs(e1.x - e2.x) && e1.y - e2.y > 100) {
                    //上
                    appList.visibility = View.VISIBLE
                    return true
                }
                return true
            }
        })

        appList.setOnTouchListener { view, motionEvent ->
            return@setOnTouchListener listGesture.onTouchEvent(motionEvent)
        }
        mainBg.setOnTouchListener { view, motionEvent ->

            return@setOnTouchListener bGgesture.onTouchEvent(motionEvent)
        }

        refreshApp()

        getSysWallpaper()

        var mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION
        )


    }


    /**
     * 刷新应用列表
     */
    fun refreshApp() {
        layoutManager.spanCount = SpUtil.getInt(this, ActionKey.APP_LIST_COLUMN, 5)

        val arrange = SpUtil.getBoolean(this, ActionKey.APP_LIST_ARRANGE, true)

        layoutManager.reverseLayout = arrange

        thread {
            val appInfos = getAllApp(this)
            appInfos.add(
                AppInfo(
                    name = "桌面设置",
                    packageName = "setting",
                    isApp = false,
                    updateTime = if (arrange) 0 else Date().time
                )
            )
            appInfos.add(
                AppInfo(
                    name = "清理后台", packageName = "clear", isApp = false,
                    updateTime = if (arrange) 0 else Date().time
                )
            )
            appInfos.add(
                AppInfo(
                    name = "wifi", packageName = "wifi", isApp = false,
                    updateTime = if (arrange) 0 else Date().time
                )
            )
            // 如果需要更新UI 回到主线程中进行处理

            val apps = appInfos.filter {
                    !SpUtil.getBoolean(this, it.packageName, false)
                }
                .filter { it.packageName != this.packageName }
                .sortedBy { it.updateTime }
            runOnUiThread {
                appListAdapter.data = apps
            }
        }
    }

    /**
     * 销毁广播监听
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(appChangeReceiver)
        unregisterReceiver(notificationReceiver)
    }

    /**
     * 拦截返回键、Home键
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false
        }
        return if (keyCode == KeyEvent.KEYCODE_HOME) {
            false
        } else super.onKeyDown(keyCode, event)
    }

    /**
     * 内部类，监听APP变化
     */
    inner class AppChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /**
             * 获取（安装/替换/卸载）应用的 信息
             */
            val packages = intent.dataString?.replace("package:", "")

            val action = intent.action

            when (action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    Log.e(TAG, packages + "应用程序安装了")
                    refreshApp()
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    Log.e(TAG, packages + "应用程序卸载了")

                    SpUtil.remove(context, packages + "")

                    refreshApp()
                }
                Intent.ACTION_PACKAGE_REPLACED -> {
                    Log.e(TAG, packages + "应用程序覆盖了")
                    SpUtil.remove(context, packages + "")
                    refreshApp()
                }

                ActionKey.ACTION_PACKAGE_HIDE -> {
                    Log.e(TAG, packages + "应用程序隐藏")
                    refreshApp()
                }

                ActionKey.ACTION_APP_LIST_CHANGE -> {
                    Log.e(TAG, packages + "列表发生变化")
                    refreshApp()
                }
            }

        }
    }

    /**
     * 内部类，监听通知变化
     */
    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                //开关悬浮球
                ActionKey.ACTION_HOVER_BALL -> {
                    context.sendBroadcast(Intent("com.mogu.back_key"))

                    EventBus.getDefault().post(MessageEvent(ActionKey.ACTION_HOVER_BALL))
                }
                ActionKey.ACTION_WIFI_STATUS -> {
                    //获取wifi开关状态
                    val status = wifiManager.wifiState
                    if (status == WifiManager.WIFI_STATE_ENABLED) {
                        //wifi打开状态则关闭
                        wifiManager.isWifiEnabled = false;
                        Toast.makeText(this@MainActivity, "wifi已关闭", Toast.LENGTH_SHORT).show()
                    } else {
                        //关闭状态则打开
                        wifiManager.isWifiEnabled = true;
                        Toast.makeText(this@MainActivity, "wifi已打开", Toast.LENGTH_SHORT).show()
                    }
                }

                Intent.ACTION_WALLPAPER_CHANGED -> {
                    Log.e(TAG, "壁纸修改")
                    getSysWallpaper()
                }

                ActionKey.ACTION_SYSTEM_SCREENSHOT -> {
                    Log.e(TAG, "开启截图权限")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        var mediaProjectionManager =
                            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        startActivityForResult(
                            mediaProjectionManager.createScreenCaptureIntent(),
                            REQUEST_MEDIA_PROJECTION
                        )
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    HoverBallService.resultData = data
                } else {
                    Toast.makeText(this, "截图功能失效，使用时请开启相关服务", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun getSysWallpaper() {
        RxPermission(this)
            .request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe({
                // 获取壁纸管理器
                val wallpaperManager = WallpaperManager.getInstance(this)
                // 获取当前壁纸
                val wallpaperDrawable = wallpaperManager.drawable
                // 将Drawable,转成Bitmap
                val bm = (wallpaperDrawable as BitmapDrawable).bitmap
                // 设置 背景
                mainBg.background = (BitmapDrawable(bm))
            }, {
                Toast.makeText(this, "壁纸功能需要读写权限才能实现", Toast.LENGTH_LONG).show()
            })
    }
}