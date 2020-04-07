package xyz.yhsj.elauncher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * 开启无障碍服务帮助类
 */
object OpenAccessibilitySettingHelper {
    /**
     * 跳转到无障碍服务设置页面
     * @param context 设备上下文
     */
    fun jumpToSettingPage(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 判断是否有辅助功能权限
     * @return true 已开启
     * false 未开启
     */
    fun isAccessibilitySettingsOn(
        context: Context?,
        className: String
    ): Boolean {
        if (context == null) {
            return false
        }
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices =
            activityManager.getRunningServices(100) // 获取正在运行的服务列表
        if (runningServices.size < 0) {
            return false
        }
        for (i in runningServices.indices) {
            val service = runningServices[i].service
            if (service.className == className) {
                return true
            }
        }
        return false
    }
}