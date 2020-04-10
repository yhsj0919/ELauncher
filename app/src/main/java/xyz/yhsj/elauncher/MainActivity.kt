package xyz.yhsj.elauncher

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import xyz.yhsj.elauncher.adapter.AppListAdapter
import xyz.yhsj.elauncher.bean.AppInfo
import xyz.yhsj.elauncher.service.HoverBallService
import xyz.yhsj.elauncher.setting.SettingActivity
import xyz.yhsj.elauncher.utils.ActionKey
import xyz.yhsj.elauncher.utils.SpUtil
import xyz.yhsj.elauncher.utils.getAllApp
import xyz.yhsj.elauncher.widget.AppDialog
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    lateinit var appListAdapter: AppListAdapter

    lateinit var appChangeReceiver: AppChangeReceiver

    lateinit var wifiManager: WifiManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //获取wifi管理服务
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

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
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED")
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED")
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED")
        intentFilter.addAction("xyz.yhsj.PACKAGE_HIDE")
        intentFilter.addDataScheme("package")

        appChangeReceiver = AppChangeReceiver()
        registerReceiver(appChangeReceiver, intentFilter)

        //初始化应用列表
        appListAdapter = AppListAdapter(appList)
        val layoutManager = GridLayoutManager(this, 5)
        layoutManager.reverseLayout = true
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
                        //获取wifi开关状态
                        val status = wifiManager.wifiState
                        if (status == WifiManager.WIFI_STATE_ENABLED) {
                            //wifi打开状态则关闭
                            wifiManager.isWifiEnabled = false;
                            Toast.makeText(this, "wifi已关闭", Toast.LENGTH_SHORT).show()
                        } else {
                            //关闭状态则打开
                            wifiManager.isWifiEnabled = true;
                            Toast.makeText(this, "wifi已打开", Toast.LENGTH_SHORT).show()

                        }
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

        refreshApp()
    }

    /**
     * 刷新应用列表
     */
    fun refreshApp() {
        thread {
            val appInfos = getAllApp(this)
            appInfos.add(AppInfo(name = "桌面设置", packageName = "setting", isApp = false))
            appInfos.add(AppInfo(name = "清理后台", packageName = "clear", isApp = false))
            appInfos.add(AppInfo(name = "wifi", packageName = "wifi", isApp = false))
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

            when {
                Intent.ACTION_PACKAGE_ADDED == action -> {
                    Log.e(TAG, packages + "应用程序安装了")
                    refreshApp()
                }
                Intent.ACTION_PACKAGE_REMOVED == action -> {
                    Log.e(TAG, packages + "应用程序卸载了")

                    SpUtil.remove(context, packages + "")

                    refreshApp()
                }
                Intent.ACTION_PACKAGE_REPLACED == action -> {
                    Log.e(TAG, packages + "应用程序覆盖了")
                    SpUtil.remove(context, packages + "")
                    refreshApp()
                }

                "xyz.yhsj.PACKAGE_HIDE" == action -> {
                    Log.e(TAG, packages + "应用程序隐藏")
                    refreshApp()
                }
            }

        }

    }


}