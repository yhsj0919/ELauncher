package xyz.yhsj.elauncher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.util.Log
import xyz.yhsj.elauncher.bean.AppInfo
import java.lang.Exception


fun getAllApp(context: Context): ArrayList<AppInfo> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null)
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    val activities = pm.queryIntentActivities(mainIntent, 0)

    val apps = ArrayList<AppInfo>()

    activities.forEach {
        val packName: String = it.activityInfo.packageName

        val appInfo = AppInfo()

        val showIcon = SpUtil.getBoolean(context, ActionKey.APP_ICON_SHOW, false)

        val userIcon = if (showIcon) FileUtils.getIcon(packName) else null
        appInfo.icon = if (userIcon == null) {
            it.activityInfo.applicationInfo.loadIcon(pm)
        } else {
            BitmapDrawable(userIcon)
        }
        appInfo.name = it.activityInfo.applicationInfo.loadLabel(pm).toString()
        appInfo.packageName = packName

        try {
            val info = pm.getPackageInfo(packName, 0);
            appInfo.version = info.versionName
            appInfo.updateTime = info.lastUpdateTime
        } catch (e: Exception) {
            appInfo.version = "1.0"
        }

        apps.add(appInfo)
    }

    activities.clear()

    return apps
}


/**
 * 判断服务是否开启
 * @param context
 * @param ServiceName
 * @return
 */
fun isServiceRunning(context: Context, ServiceName: String?): Boolean {
    if (TextUtils.isEmpty(ServiceName)) {
        return false
    }
    val myManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningService = myManager.getRunningServices(30)

    runningService.forEach {
        if (it.service.className == ServiceName) {
            return true
        }
    }
    return false
}


/**
 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
 */
fun dip2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density;
    return (dpValue * scale + 0.5f).toInt();
}